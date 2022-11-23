package cn.lzr.ecommerce.service.impl;

import cn.lzr.ecommerce.constant.AuthorityConstant;
import cn.lzr.ecommerce.constant.CommonConstant;
import cn.lzr.ecommerce.dao.EcommerceUserDao;
import cn.lzr.ecommerce.entity.EcommerceUser;
import cn.lzr.ecommerce.service.IJWTService;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import cn.lzr.ecommerce.vo.UsernameAndPassword;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.crypto.SecretKey;
import javax.transaction.Transactional;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@Transactional(rollbackOn = {Exception.class})
public class JWTServiceImpl implements IJWTService {

    private final EcommerceUserDao ecommerceUserDao;

    public JWTServiceImpl(EcommerceUserDao ecommerceUserDao) {
        this.ecommerceUserDao = ecommerceUserDao;
    }


    @Override
    public String generateToken(String username, String password) throws Exception {
        return generateToken(username, password, 0);
    }

    @Override
    public String generateToken(String username, String password, long expire) throws Exception {
        EcommerceUser ecommerceUser = ecommerceUserDao.findByUsernameAndPassword(username, password);
        if (Objects.isNull(ecommerceUser)) {
            log.error("can not find user: [{}], [{}]", username, password);
            return null;
        }

        // Token 中塞入对象, 即 JWT 中存储的信息, 后端拿到这些信息就可以知道是哪个用户在操作
        LoginUserInfo loginUserInfo = new LoginUserInfo(
                ecommerceUser.getId(), ecommerceUser.getUsername()
        );

        if (expire <= 0) {
            expire = AuthorityConstant.DEFAULT_EXPIRE_TIME;
        }

        // 当前时间
        long nowMillis = System.currentTimeMillis();

        return Jwts.builder()
                // jwt id
                .setId(UUID.randomUUID().toString())
                // jwt time & date
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(nowMillis + expire))
                // jwt payload
                .claim(CommonConstant.JWT_USER_INFO_KEY, JSON.toJSONString(loginUserInfo))
                // jwt signed
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();

    }

    @Override
    public String registerUserAndGenerateToken(UsernameAndPassword usernameAndPassword) throws Exception {
// 先去校验用户名是否存在, 如果存在, 不能重复注册
        EcommerceUser oldUser = ecommerceUserDao.findByUsername(
                usernameAndPassword.getUsername());

        if (!Objects.isNull(oldUser)) {
            log.error("username is registered: [{}]", oldUser.getUsername());
            return null;
        }

        EcommerceUser ecommerceUser = new EcommerceUser();
        ecommerceUser.setUsername(usernameAndPassword.getUsername());
        ecommerceUser.setPassword(usernameAndPassword.getPassword());   // MD5 编码以后
        ecommerceUser.setExtraInfo("{}");

        // 注册一个新用户, 写一条记录到数据表中
        ecommerceUser = ecommerceUserDao.save(ecommerceUser);
        log.info("register user success: [{}], [{}]", ecommerceUser.getUsername(),
                ecommerceUser.getId());

        // 生成 token 并返回
        return generateToken(ecommerceUser.getUsername(), ecommerceUser.getPassword());
    }

    /**
     * <h2>根据本地存储的私钥获取到 PrivateKey 对象</h2>
     * */
    private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Decoders.BASE64.decode(AuthorityConstant.PRIVATE_KEY)));
    }

}
