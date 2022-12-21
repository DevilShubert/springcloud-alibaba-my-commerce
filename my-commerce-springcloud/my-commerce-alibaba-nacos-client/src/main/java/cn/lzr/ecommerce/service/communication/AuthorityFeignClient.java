package cn.lzr.ecommerce.service.communication;

import cn.lzr.ecommerce.vo.JwtToken;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(contextId = "AuthorityFeignClient", value = "my-commerce-authority-center")
public interface AuthorityFeignClient {
    /**
     * <h2>通过 OpenFeign 访问 Authority 获取 Token</h2>
     * */
    @RequestMapping(value = "/ecommerce-authority-center/authority/doLogin",
            method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    JwtToken getTokenByFeign(@RequestBody UsernameAndPassword usernameAndPassword);
}
