package cn.lzr.ecommerce.controller;

import cn.lzr.ecommerce.service.communication.AuthorityFeignClient;
import cn.lzr.ecommerce.service.communication.UseFeignApi;
import cn.lzr.ecommerce.service.communication.UseRestTemplateService;
import cn.lzr.ecommerce.service.communication.UseRibbonService;
import cn.lzr.ecommerce.vo.JwtToken;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>微服务通信 Controller</h1>
 * */
@RestController
@RequestMapping("/communication")
public class CommunicationController {
    public CommunicationController(UseRestTemplateService useRestTemplateService, UseRibbonService useRibbonService, AuthorityFeignClient feignClient, UseFeignApi useFeignApi) {
        this.useRestTemplateService = useRestTemplateService;
        this.useRibbonService = useRibbonService;
        this.feignClient = feignClient;
        this.useFeignApi = useFeignApi;
    }

    // 方式一和方式二的Service类
    private final UseRestTemplateService useRestTemplateService;

    /**
     * <h2>一、使用RestTemplate完成最普通的REST请求</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    @PostMapping("/rest-template")
    public JwtToken getTokenFromAuthorityService(
            @RequestBody UsernameAndPassword usernameAndPassword) {
        return useRestTemplateService.getTokenFromAuthorityService(usernameAndPassword);
    }


    /**
     * <h2>二、使用RestTemplate + loadBalancerClient 完成REST请求</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    @PostMapping("/rest-template-load-balancer")
    public JwtToken getTokenFromAuthorityServiceWithLoadBalancer(
            @RequestBody UsernameAndPassword usernameAndPassword) {
        return useRestTemplateService.getTokenFromAuthorityServiceWithLoadBalancer(
                usernameAndPassword);
    }




    /**
     * <h2>三、单独使用Ribbon进行REST请求，会自动有服务调用+负载均衡的功能</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    private final UseRibbonService useRibbonService;

    @PostMapping("/ribbon")
    public JwtToken getTokenFromAuthorityServiceByRibbon(
            @RequestBody UsernameAndPassword usernameAndPassword) {
        return useRibbonService.getTokenFromAuthorityServiceByRibbon(usernameAndPassword);
    }

    /**
     * <h2>四、使用原生的 Ribbon Api, 看看 Ribbon 内部是如何完成: 服务调用 + 负载均衡</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    @PostMapping("/thinking-in-ribbon")
    public JwtToken thinkingInRibbon(@RequestBody UsernameAndPassword usernameAndPassword) {
        return useRibbonService.thinkingInRibbon(usernameAndPassword);
    }




    /**
     * <h2>五、使用OpenFegin高度封装来进行REST请求</h2>
     * 功能：从授权服务中获取 JwtToken
     * */
    private final AuthorityFeignClient feignClient;

    @PostMapping("/token-by-feign")
    public JwtToken getTokenByFeign(@RequestBody UsernameAndPassword usernameAndPassword) {
        return feignClient.getTokenByFeign(usernameAndPassword);
    }



    private final UseFeignApi useFeignApi;

    /**
     * <h2>6、使用 Feign 原生 api 调用远端服务</h2>
     * Feign 默认配置初始化、设置自定义配置、生成代理对象
     * */
    @PostMapping("/thinking-in-feign")
    public JwtToken thinkingInFeign(@RequestBody UsernameAndPassword usernameAndPassword) {
        return useFeignApi.thinkingInFeign(usernameAndPassword);
    }
}
