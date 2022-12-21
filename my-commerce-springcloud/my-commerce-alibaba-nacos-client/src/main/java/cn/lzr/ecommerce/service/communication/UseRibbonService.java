package cn.lzr.ecommerce.service.communication;

import cn.lzr.ecommerce.constant.CommonConstant;
import cn.lzr.ecommerce.vo.JwtToken;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import com.alibaba.fastjson.JSON;
import com.netflix.loadbalancer.*;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static rx.Observable.just;

/**
 * <h1>使用 Ribbon 实现微服务通信</h1>
 * */
@Slf4j
@Service
public class UseRibbonService {

    private final RestTemplate restTemplate;


    public UseRibbonService(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    /**
     * <h2>三、单独使用Ribbon进行REST请求，会自动有服务调用+负载均衡的功能</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    public JwtToken getTokenFromAuthorityServiceByRibbon(UsernameAndPassword usernameAndPassword){
        // 注意到 url 中的 ip 和端口换成了服务名称，Ribbon会自动设置ip+端口号
        String requestUrl = String.format("http://%s/ecommerce-authority-center/authority/doLogin",
                CommonConstant.AUTHORITY_CENTER_SERVICE_ID);

        log.info("login request url and body: [{}], [{}]", requestUrl,
                JSON.toJSONString(usernameAndPassword));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 使用Ribbon这里的RestTemplate就是我们注入的Bean，会自动配置Ribbon完成工作
        return restTemplate.postForObject(
                requestUrl,
                new HttpEntity<>(JSON.toJSONString(usernameAndPassword), headers),
                JwtToken.class
        );
    }

    /**
     * <h2>四、使用原生的 Ribbon Api, 看看 Ribbon 是如何完成: 服务调用 + 负载均衡</h2>
     * 功能：从授权服务中获取 JwtToken
     * */

    private final DiscoveryClient discoveryClient;

    public JwtToken thinkingInRibbon(UsernameAndPassword usernameAndPassword){
        String urlFormat = "http://%s/ecommerce-authority-center/authority/doLogin";

        // 1. 找到服务提供方的地址和端口号
        List<ServiceInstance> targetInstances = discoveryClient
                .getInstances(CommonConstant.AUTHORITY_CENTER_SERVICE_ID);

        // 构造 Ribbon 服务列表
        ArrayList<Server> servers = new ArrayList<>(targetInstances.size());
        targetInstances.forEach(new Consumer<ServiceInstance>() {
            @Override
            public void accept(ServiceInstance serviceInstance) {
                servers.add(new Server(serviceInstance.getHost(), serviceInstance.getPort()));
                log.info("found target instance: [{}] -> [{}]", serviceInstance.getHost(), serviceInstance.getPort());
            }
        });

        // 2. 使用负载均衡策略实现远端服务调用
        // 构建 Ribbon 负载实例器（应该去NetFlix包下去寻找）
        BaseLoadBalancer loadBalancer = LoadBalancerBuilder.newBuilder()
                .buildFixedServerListLoadBalancer(servers);
        // 设置负载均衡策略,这里的300为最大重试时间（这里的意思是如果在500ms内没有找到合适的服务）
        loadBalancer.setRule(new RetryRule(new RandomRule(), 300));

        // 通过之前获取到的服务servers以及选取好的负载均衡策略，进行一次Rest通信
        Observable<Object> submit = LoadBalancerCommand.builder().withLoadBalancer(loadBalancer)
                .build().submit(new ServerOperation<Object>() {
                    @Override
                    public Observable<Object> call(Server server) {
                        // 构建请求URL
                        String targetUrl = String.format(
                                urlFormat,
                                String.format("%s:%s", server.getHost(), server.getPort())
                        );
                        log.info("target request url: [{}]", targetUrl);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        String tokenStr = new RestTemplate().postForObject(
                                targetUrl,
                                new HttpEntity<>(JSON.toJSONString(usernameAndPassword), headers),
                                String.class
                        );

                        // Rx 反应式编程
                        Observable<Object> just = just(tokenStr);
                        return just;
                    }
                });
        // 3、rx包中反应式的书写方式，获得第一个传过来的元素
        String s = submit.toBlocking().first().toString();
        return JSON.parseObject(s, JwtToken.class);
    }
}
