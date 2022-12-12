package cn.lzr.ecommerce.config;

import brave.sampler.RateLimitingSampler;
import brave.sampler.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SleuthConfig {
//    采样百分比为0.5
//    @Bean
//    public Sampler rateSampler(){
//        return Sampler.create(0.5f);
//    }

    // 每秒钟最多100次采样
    @Bean
    public Sampler timesSampler(){
        return RateLimitingSampler.create(100);
    }
}
