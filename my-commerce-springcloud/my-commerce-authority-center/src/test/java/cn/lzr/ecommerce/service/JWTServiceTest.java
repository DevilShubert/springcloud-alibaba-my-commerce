package cn.lzr.ecommerce.service;

import cn.lzr.ecommerce.util.TokenParseUtil;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * <h1>JWT 相关服务测试类</h1>
 * */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class JWTServiceTest {
    @Autowired
    private IJWTService ijwtService;


    @Test
    public void testGenerateAndParseToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
        LoginUserInfo loginUserInfo = TokenParseUtil.parseUserInfoFromToken("eyJhbGciOiJSUzI1NiJ9" +
                ".eyJqdGkiOiI0OTA5MGJlYi0yNWY2LTQ3NjYtYmVmOC0wNmNmZWYzNGU1MDIiLCJpYXQiOjE2Njg5MzE1NTUsImV4cCI6MTY2ODk0MTE1NSwibXktY29tbWVyY2UtdXNlciI6IntcImlkXCI6MTIsXCJ1c2VybmFtZVwiOlwiMjIyQHRlc3QuY29tXCJ9In0" +
                ".EEp2V5nwe17uz8A7F_lzT1jfmYprb1xl3I8MmdxFdlJ-ncyx8TXmD_PavJrYFGp1iKNLzAs_dpnr-4ZctBcpZ2QDcnsGakx5t-1mDrexsAzN1daRVxbNN-zn_3FwGIs2gHaznUlr5a-og6AnMhaRo2_EAiYktbkz7OOqiXvti6JQ9jjtbqIdtk77k5RjKaNk75owmtBHseKwkpC7HDlIEz8d69sQofwCXA9lFZMKnfB7X7NI1Pd_90qCyaA9UoosQlLS7vFPrIKfYflwkVPXPZO2QY9cN0dOhV_quFeO5VgzdjJ8h4eaHgoflimDxCVWd59dwqvoGEITkUmYz1NrfA");
        log.info("parse token :[{}]", JSON.toJSON(loginUserInfo));
    }
}
