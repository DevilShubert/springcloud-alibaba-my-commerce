package cn.lzr.ecommerce.conf;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * <h1>Swagger 配置类</h1>
 * 原生: /swagger-ui.html
 * 美化: /doc.html
 * */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket docket(){
        Predicate<RequestHandler> basePackage = RequestHandlerSelectors.basePackage("cn.lzr.ecommerce");
        return new Docket(DocumentationType.SWAGGER_2)
                // 展示在 Swagger 页面上的自定义工程描述信息
                .apiInfo(apiInfo())
                //
                .select()
                // 只有 com.imooc.ecommerce 包内的才去展示
                .apis(basePackage)
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * <h2>Swagger 的描述信息</h2>
     * */
    public ApiInfo apiInfo() {

        return new ApiInfoBuilder()
                .title("lzr-commerce-micro-service")
                .description("my-commerce-springcloud-service")
                .contact(new Contact(
                        "lzr",
                        "https://github.com/DevilShubert/springcloud-alibaba-my-commerce",
                        "2576128280@qq.com"
                ))
                .version("1.0")
                .build();
    }
}
