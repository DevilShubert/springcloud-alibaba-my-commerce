package cn.lzr.ecommerce.service;

import cn.hutool.crypto.digest.MD5;
import cn.lzr.ecommerce.dao.EcommerceUserDao;
import cn.lzr.ecommerce.entity.EcommerceUser;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <h1>EcommerceUser 相关的测试</h1>
 * */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class EcommerceUserTest {

    @Autowired
    private EcommerceUserDao ecommerceUserDao;

    @Test
    public void createUserRecord(){
        EcommerceUser ecommerceUser = new EcommerceUser();
        ecommerceUser.setUsername("111@test.com");
        ecommerceUser.setPassword(MD5.create().digestHex("123456"));
        ecommerceUser.setExtraInfo("{}");
        EcommerceUser savedUser = ecommerceUserDao.save(ecommerceUser);
        log.info("save user: [{}]", JSON.toJSON(savedUser));
    }
}
