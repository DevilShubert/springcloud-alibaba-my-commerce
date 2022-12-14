package cn.lzr.ecommerce.conf;

import cn.lzr.ecommerce.filter.LoginUserInfoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <h1>Web Mvc 配置</h1>
 * */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    /**
     * <h2>添加自定义的拦截器配置</h2>
     * */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加用户身份统一登录拦截的拦截器，并设置路径和优先级
        registry.addInterceptor(new LoginUserInfoInterceptor())
                .addPathPatterns("/**").order(0);
    }

    /**
     * <h2>让 MVC 加载 Swagger 的静态资源</h2>
     * */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").
                addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
