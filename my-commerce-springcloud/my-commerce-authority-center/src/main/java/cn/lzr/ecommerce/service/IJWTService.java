package cn.lzr.ecommerce.service;

import cn.lzr.ecommerce.vo.UsernameAndPassword;

/**
 * <h1>JWT 相关服务接口定义</h1>
 * */
public interface IJWTService {
    String generateToken(String username, String password) throws Exception;

    String generateToken(String username, String password, long expire) throws Exception;

    String registerUserAndGenerateToken(UsernameAndPassword usernameAndPassword)
            throws Exception;
}
