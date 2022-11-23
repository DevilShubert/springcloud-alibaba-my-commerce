package cn.lzr.ecommerce.util;

import cn.lzr.ecommerce.constant.CommonConstant;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;



import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Objects;

/**
 * <h1>JWT Token 解析工具类</h1>
 * */
public class TokenParseUtil {
    /**
     * <h2>从 JWT Token 中解析 LoginUserInfo 对象</h2>
     * */
    public static LoginUserInfo parseUserInfoFromToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (Objects.isNull(token)) {
            return null;
        }

        Jws<Claims> claimsJws = paresToken(token, getPublicKey());
        Claims body = claimsJws.getBody();

        // 如果 Token 已经过期了, 返回 null
        if (body.getExpiration().before(Calendar.getInstance().getTime())) {
            return null;
        }

        // 返回 Token 中保存的用户信息，反序列化
        return JSON.parseObject(
                body.get(CommonConstant.JWT_USER_INFO_KEY).toString(), // 取出存储的UserInfo对象
                LoginUserInfo.class
        );

    }

    /**
     * <h2>通过公钥去解析 JWT Token</h2>
     * */
    private static Jws<Claims> paresToken(String token,PublicKey publicKey) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
    }

    /**
     * <h2>根据本地存储的公钥获取到 PublicKey 对象</h2>
     * */
    private static PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(Decoders.BASE64.decode(CommonConstant.PUBLIC_KEY)));
    }
}
