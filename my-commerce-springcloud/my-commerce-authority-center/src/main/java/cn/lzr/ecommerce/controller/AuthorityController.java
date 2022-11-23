package cn.lzr.ecommerce.controller;

import cn.lzr.ecommerce.annotation.IgnoreResponseAdvice;
import cn.lzr.ecommerce.service.IJWTService;
import cn.lzr.ecommerce.service.impl.JWTServiceImpl;
import cn.lzr.ecommerce.vo.JwtToken;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>对外暴露的授权服务接口</h1>
 * */
@Slf4j
@RestController
@RequestMapping("/authority")
public class AuthorityController {
    private final IJWTService  ijwtService;

    public AuthorityController(IJWTService ijwtService) {
        this.ijwtService = ijwtService;
    }


    /**
     * <h2>从授权中心获取 Token (其实就是登录功能), 且返回信息中没有统一响应的包装</h2>
     * <h2>使用IgnoreResponseAdvice注解作用是直接返回token内容</h2>
     * */
    @IgnoreResponseAdvice
    @PostMapping("/doAuthority")
   public JwtToken doAuthority(@RequestBody UsernameAndPassword usernameAndPassword) throws Exception {
        log.info("request to get token with param: [{}]", JSON.toJSONString(usernameAndPassword));
       String jwtToken = ijwtService.generateToken(usernameAndPassword.getUsername(), usernameAndPassword.getPassword());
       return new JwtToken(jwtToken);
   }


    /**
     * <h2>注册用户并返回当前注册用户的 Token, 即通过授权中心创建用户</h2>
     * */
    @IgnoreResponseAdvice
    @PostMapping("/token")
    public JwtToken doRegister(@RequestBody UsernameAndPassword usernameAndPassword) throws Exception {
        log.info("register user with param: [{}]", JSON.toJSONString(usernameAndPassword));
        return new JwtToken(ijwtService.registerUserAndGenerateToken(usernameAndPassword));
    }
}
