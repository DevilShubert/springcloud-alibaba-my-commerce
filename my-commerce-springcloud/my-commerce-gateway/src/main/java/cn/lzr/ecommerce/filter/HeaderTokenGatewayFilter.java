package cn.lzr.ecommerce.filter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <h1>HTTP 请求头部携带 Token 验证过滤器（仅仅作为验证）</h1>
 * 因为需要指定order所以需要单独实现order接口
 * */
@Slf4j
public class HeaderTokenGatewayFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("being through HeaderTokenGatewayFilter");
        String name = exchange.getRequest().getHeaders().getFirst("token");
        if ("imooc".equals(name)){
            return chain.filter(exchange);
        }
        // 如果header中没有包含token，则标记此次请求没有权限, 并结束这次请求
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // 标识结束此次请求，一般用于指示完成或错误的Mono
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // 过滤器顺数第2个
        return HIGHEST_PRECEDENCE+3;
    }
}
