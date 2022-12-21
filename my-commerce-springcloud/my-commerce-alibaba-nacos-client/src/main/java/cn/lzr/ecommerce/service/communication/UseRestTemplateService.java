package cn.lzr.ecommerce.service.communication;

import cn.lzr.ecommerce.constant.CommonConstant;
import cn.lzr.ecommerce.vo.JwtToken;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UseRestTemplateService {

    /**
     * <h2>一、使用RestTemplate完成最普通的REST请求</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    public JwtToken getTokenFromAuthorityService(UsernameAndPassword usernameAndPassword){
        // 第一种方式：写死URL
        String requestUrl = "http://127.0.0.1:7010/ecommerce-authority-center" +
                "/authority/doLogin";
        log.info("RestTemplate request url and body:[{}],[{}]",
                requestUrl,
                JSON.toJSONString(usernameAndPassword));

        // 构建请求对象的Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 发送REST请求
        return new RestTemplate().postForObject(requestUrl,
                new HttpEntity<>(JSON.toJSONString(usernameAndPassword), // 在网络传输时需要将对象序列化
                        headers),
                JwtToken.class);
    }


    // 接口是在cloud.client.loadbalancer包下的LoadBalancerClient，而具体的实现是cloud.netflix.ribbon下的RibbonLoadBalancerClient类
    private final LoadBalancerClient loadBalancerClient;

    public UseRestTemplateService(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }


    /**
     * <h2>二、使用最普通的REST请求 + loadBalancerClient</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    public JwtToken getTokenFromAuthorityServiceWithLoadBalancer(UsernameAndPassword usernameAndPassword) {
        // 第二种方式: 通过注册中心拿到服务的信息(是所有的实例), 再去发起调用
        ServiceInstance serviceInstance = loadBalancerClient.choose(CommonConstant.AUTHORITY_CENTER_SERVICE_ID);

        log.info("Nacos Client Info: [{}], [{}], [{}]",
                serviceInstance.getServiceId(),
                serviceInstance.getInstanceId(),
                JSON.toJSONString(serviceInstance.getMetadata()));

        String requestUrl = String.format(
                "http://%s:%s/ecommerce-authority-center/authority/doLogin",
                serviceInstance.getHost(),
                serviceInstance.getPort()
        );

        log.info("login request url and body: [{}], [{}]", requestUrl,
                JSON.toJSONString(usernameAndPassword));
        // 构建请求对象的Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 发送REST请求，原理和方法一相同，但是不同的是我们利用注册中心动态获取到服务的ip+端口
        return new RestTemplate().postForObject(
                requestUrl,
                new HttpEntity<>(JSON.toJSONString(usernameAndPassword), headers),
                JwtToken.class
        );
    }


}
