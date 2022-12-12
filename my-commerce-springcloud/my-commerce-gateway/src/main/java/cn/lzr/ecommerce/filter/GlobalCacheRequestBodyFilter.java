package cn.lzr.ecommerce.filter;

import cn.lzr.ecommerce.constant.GatewayConstant;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Constants;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings("all")
public class GlobalCacheRequestBodyFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("being through GlobalCacheRequestBodyFilter");

        boolean isLoginOrRegister = exchange.getRequest().getURI().getPath().contains(GatewayConstant.LOGIN_URI)
                    || exchange.getRequest().getURI().getPath().contains(GatewayConstant.REGISTER_URI);

        // 在http协议中，请求提交的数据必须放在消息主体body中，但是http协议并没有规定body中的数据是哪一种格式（json、multipart等）
        // ContentType的作用就是用于标识哪一钟格式
        if (Objects.isNull(exchange.getRequest().getHeaders().getContentType()) || !isLoginOrRegister) {
            // 如果不是用于登录或注册的请求，则往下继续走其它的过滤器（访问其他微服务）
            return chain.filter(exchange);
        }

        // 因为exchange中的请求是ReactiveServerHttpRequest，其中的body是一个Flux（Publisher），他是惰性求值的
        // 如果不在全局过滤器中手动保存下来则后续的过滤器则无法拿到（或者只能拿去一次）
        Mono<Void> returnMono = DataBufferUtils.join(exchange.getRequest().getBody()).map(dataBuffer -> {
            // dataBuffer表示服务器的网络缓冲区
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            // 这里需要读到线程的缓冲区bytes中
            dataBuffer.read(bytes);
            // 释放服务器的网络缓冲区dataBuffer
            DataBufferUtils.release(dataBuffer);
            // 进行下面的流操作
            return bytes;
        }).flatMap(bodyBytes -> {
            String msg = new String(bodyBytes, StandardCharsets.UTF_8);
            log.info("全局缓存过滤器获取到的body: \n {}", msg);
            exchange.getAttributes().put("CACHE", msg);
            // 生成新的Request用于传递给下一个过滤器
            ServerHttpRequest newRequest = generateNewRequest(exchange.getRequest(), bodyBytes);
            return chain.filter(exchange.mutate().request(newRequest).build());
        });
        // 返回的mono代表从后面的过滤器回来的mono
        return returnMono;
    }


    // DataBufferUtils.join 拿到请求中的数据 --> DataBuffer，接下来的代码都是webflux，目前阶段直接跳过
    // 下面代码的含义就是，如果是用于登录或注册的请求，则手动从请求中拿到body，然后包装为新的request，进入之后的过滤器链
    // 因为普通请求的body在经过过滤器时读取不到，所以要主动保存，而这里需要主动保存的仅用于login和register
//        Flux<DataBuffer> body = exchange.getRequest().getBody();
//
//        Mono<Void> monoResult = DataBufferUtils.
//                join(body).
//                flatMap(dataBuffer -> {
//
//                    // 确保数据缓冲区不被释放, 必须要 DataBufferUtils.retain
//                    DataBufferUtils.retain(dataBuffer);
//
//                    // defer、just 都是去创建数据源, 得到当前数据的副本
//                    Flux<DataBuffer> cachedFlux = Flux.defer(() ->
//                            Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
//
//                    // 重新包装 ServerHttpRequest, 重写 getBody 方法, 能够返回请求数据
//                    ServerHttpRequest mutatedRequest =
//                            new ServerHttpRequestDecorator(exchange.getRequest()) {
//                                @Override
//                                public Flux<DataBuffer> getBody() {
//                                    return cachedFlux;
//                                }
//                            };
//
//                    // 将包装之后的 ServerHttpRequest 向下继续传递
//                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
//                });
//
//        // 返回的mono代表从Filter回来
//        return monoResult;
//   }

    /**
     * 用于生产新的ReactiveServerHttpRequest传递给接下里的过滤器链
     * @param request
     * @param bytes
     * @return
     */
    private ServerHttpRequest generateNewRequest(ServerHttpRequest request, byte[] bytes) {
        URI ex = UriComponentsBuilder.fromUri(request.getURI()).build(true).toUri();
        ServerHttpRequest newRequest = request.mutate().uri(ex).build();
        // 将数据包装
        DataBuffer dataBuffer = stringBuffer(bytes);
        // Publisher生产数据
        Flux<DataBuffer> flux = Flux.just(dataBuffer);
        newRequest = new ServerHttpRequestDecorator(newRequest) {
            @Override
            public Flux<DataBuffer> getBody() {
                return flux;
            }
        };
        return newRequest;
    }

    /**
     * 将数据通过NettyDataBufferFactory工厂包装
     * @param bytes
     * @return
     */
    private DataBuffer stringBuffer(byte[] bytes) {
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        return nettyDataBufferFactory.wrap(bytes);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE+1;
    }
}
