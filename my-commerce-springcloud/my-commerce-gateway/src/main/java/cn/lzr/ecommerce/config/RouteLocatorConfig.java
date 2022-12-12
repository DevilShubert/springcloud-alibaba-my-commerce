package cn.lzr.ecommerce.config;


import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * <h1>使用代码的方式定义一组路由</h1>
 * 效果等同于用Nacos的动态配置
 * 实际最终都会生成一个RouteDefinition
 */
@Configuration
public class RouteLocatorConfig {
    /**
     * <h2>使用代码定义路由规则</h2>
     * 在网关层面拦截下登录和注册接口，并且通过转发的方式
     * */
    @Bean
    public RouteLocator loginRouteLocator(RouteLocatorBuilder builder){
        // 手动定义 Gateway 路由规则需要指定 id、path 和 uri
        return builder.routes().route(
                "e_commerce_authority", // 路由id
                new Function<PredicateSpec, Route.AsyncBuilder>() {
                    @Override
                    public Route.AsyncBuilder apply(PredicateSpec r) {
                        Route.AsyncBuilder uri = r.path(
                                "/imooc/my-commerce/login",
                                "/imooc/my-commerce/register"
                        ).uri("http://localhost:xxxx/"); // 这里的转发的uri并不会生效，因为在过滤器链中就完成了转发
                        return uri;
                    }
                }
        ).build();
    }
}
