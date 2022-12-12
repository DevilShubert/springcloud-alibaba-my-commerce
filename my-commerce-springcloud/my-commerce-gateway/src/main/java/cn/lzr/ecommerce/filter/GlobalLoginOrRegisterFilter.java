package cn.lzr.ecommerce.filter;


import cn.lzr.ecommerce.constant.CommonConstant;
import cn.lzr.ecommerce.constant.GatewayConstant;
import cn.lzr.ecommerce.util.TokenParseUtil;
import cn.lzr.ecommerce.vo.JwtToken;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class GlobalLoginOrRegisterFilter implements GlobalFilter, Ordered {
    /** 注册中心客户端, 可以从注册中心中获取服务实例信息 **/
    private final LoadBalancerClient loadBalancerClient;

    /** 用于访问Rest访问其他微服务的工具**/
    private final RestTemplate restTemplate;

    public GlobalLoginOrRegisterFilter(LoadBalancerClient loadBalancerClient, RestTemplate restTemplate) {
        this.loadBalancerClient = loadBalancerClient;
        this.restTemplate = restTemplate;
    }

    /**
     * <h2>登录、注册、鉴权</h2>
     * 1. 如果是登录或注册, 则去授权中心拿到 Token 并返回给客户端；
     * 2. 如果是访问其他的服务, 则鉴权, 没有权限返回 401
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        log.info("being through GlobalLoginOrRegisterFilter");

        String cacheBody = (String) exchange.getAttribute("CACHE");
        log.info("第二个过滤器获取到的body:[{}]",cacheBody);

        // 1、登录情况
        if (request.getURI().getPath().contains(GatewayConstant.LOGIN_URI)){
            // 去授权中心拿 token
            String token = getTokenFromAuthorityCenter(request, GatewayConstant.AUTHORITY_CENTER_LOGIN_URL_FORMAT, cacheBody);
            // 设置header
            response.getHeaders().add(
                    CommonConstant.JWT_USER_INFO_KEY,
                    null == token ? "null" : token
            );
            // 设置响应码
            response.setStatusCode(HttpStatus.OK);
            // 返回响应
            return response.setComplete();
        }

        // 2、注册情况
        if(request.getURI().getPath().contains(GatewayConstant.REGISTER_URI)){
            // 去授权中心拿 token: 先创建用户, 再返回 Token
            String token = getTokenFromAuthorityCenter(
                    request,
                    GatewayConstant.AUTHORITY_CENTER_REGISTER_URL_FORMAT,
                    cacheBody
            );
            // 设置header
            response.getHeaders().add(
                    CommonConstant.JWT_USER_INFO_KEY,
                    null == token ? "null" : token
            );
            // 设置响应码
            response.setStatusCode(HttpStatus.OK);
            // 返回响应
            return response.setComplete();
        }

        // 3、访问其他微服务，则鉴权, 校验是否能够从 Token 中解析出用户信息
        HttpHeaders headers = request.getHeaders();
        String token = headers.getFirst(CommonConstant.JWT_USER_INFO_KEY);

        LoginUserInfo loginUserInfo = null;
        // 使用Common模块解析token
        try {
            loginUserInfo = TokenParseUtil.parseUserInfoFromToken(token);
        }catch (Exception ex) {
            log.error("parse user info from token error: [{}]", ex.getMessage(), ex);
        }
        // 获取不到登录用户信息, 返回 401
        if (Objects.isNull(loginUserInfo)){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE+2;
    }


    /**
     * <h2>从授权中心获取 Token</h2>
     * */
    private String getTokenFromAuthorityCenter(ServerHttpRequest request, String uriFormat, String body){
        // service id 就是服务名字, 负载均衡
        // 根据服务名字从注册中心中找到服务
        ServiceInstance serviceInstance =
                loadBalancerClient.choose(CommonConstant.AUTHORITY_CENTER_SERVICE_ID);

        log.info("Nacos Client Info: [{}], [{}], [{}]",
                serviceInstance.getServiceId(),
                serviceInstance.getInstanceId(),
                JSON.toJSONString(serviceInstance.getMetadata()));

        // 构造访问授权中心的URL
        String requestUrl = String.format(
                uriFormat, serviceInstance.getHost(), serviceInstance.getPort()
        );

        // 获取当前请求的body
//        String body = parseBodyFromRequest(request);
//        log.info("current request body: [{}]", body);

        // 将当前请求的body解析为UsernameAndPassword对象
        UsernameAndPassword requestBody = JSON.parseObject(body, UsernameAndPassword.class);

        log.info("login request url and body: [{}], [{}]",
                requestUrl,
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构造http请求对象，使用REST模式发送请求
        JwtToken jwtToken = restTemplate.postForObject(
                requestUrl,
                new HttpEntity<>(JSON.toJSONString(requestBody), headers),
                JwtToken.class
        );

        if (!Objects.isNull(jwtToken)) {
            return jwtToken.getToken();
        }

        return null;
    }

    /**
     * <h2>从 Post 请求中获取到请求数据body</h2>
     * 在响应式编程中，body只能获取一次
     * */
    private String parseBodyFromRequest(ServerHttpRequest request){


        // 获取请求体
        Flux<DataBuffer> body = request.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();

        // 订阅缓冲区去消费请求体中的数据
        body.subscribe(dataBuffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
            // 一定要使用 DataBufferUtils.release 释放掉, 否则, 会出现内存泄露
            DataBufferUtils.release(dataBuffer);
            bodyRef.set(charBuffer.toString());
        });

        // 获取 request body
        return bodyRef.get();
    }
}
