package cn.lzr.ecommerce.service;


import cn.lzr.ecommerce.constant.AuthorityConstant;
import cn.lzr.ecommerce.constant.CommonConstant;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.SecretKey;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.UUID;


/**
 * <h1>RSA 非对称加密算法: 生成公钥和私钥</h1>
 * */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RSATest {

    @Test
    public void generateKeyBytes() throws NoSuchAlgorithmException, InvalidKeySpecException {
        //私钥 MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDIJWpVDA8xx2tA/46cvlyt5aInAopaxHHRoAqZJKG89CbTgsmx8i2D3j8TXajV7LqXDCiVegX3pw+pMhYUydjX6JBXwww6UbjAtT84VOm+DG+chuotTPyCasfRT5NIkt3XM/wwGifUv6Iqs1//KT3vZVTugavCSXkYGT41nFr2olBz99gDO9TlI+OZUy5+8Na1dWkHtbXI4DPEBEPKbSDAO9HdypaMTz3q0xSauqNFenjmbIRvc2TqhcuHSoiqwc3BaNGu1zpsjEfhy9yDuIvBhB75PC0nSl1XU3pIi4i8W5PGVyik9Vr/DT8yDPGZhG+ue58oQwgdSVZRoBtzvuz5AgMBAAECggEASwaziHP/0iI2E2R5GkrFxS46jbMHGCVhoToEHwo7UvCXCVySrRDifPc339ORMbgO7gZRxE68o+xNE5jB7EMJ9U2ppixojggKYLrQmxZBlMtmn193s0mTsnPCubTBukfLvxsuov17BuZbLPSWvLjCDdaHcuzK7QbLzPvRdRlKg/8Ewh/sMQZAi84KbVOx4XLj069ZyA5fZqHr3za3xAVu4+3T5q5g/XeehsNJMyRs8OKzTPw058Lgcm2Ggd1mDzdX6VqCSpB8bA3FXBfC+wmlDM/rpacrSNLk0iXiEbuxiJg+MC7hTLqCnRl+IQKNizN+O6cienImW7JcfPXc9mIibQKBgQDj1cLYnOm/R3fez9RE6SbN2iDBNpDEOup0jl4dzexWush8EA0p/2Rt1dbe4Oq0DvffRoZeHDNeKABMhY5glOv1RMDE0ZrCeKHvGMLhVpb3FnibPfaAZReHjnZHjSitcb0irVopp2W82iJjBEyTtk2aBxb5mpQF0G93GXDodsOVfwKBgQDg42RVqTcsPPljbA3Tw1xAfwOG96V5uAt/51qRn0ovzgbkFw43qwlYS2clLx6ZCESN+k/wiPWkkIDhaeuCD0wokk5Kutcr01bHLmopbKs0AMOhGdRc+BITqThastKvuxmVOJJxC/bO59zaQZBn6iS4IZHJb3/WZnpbGvDarrZphwKBgQC8TCrhA65cHCEB0RpKabdcYm63wPUceOTUt+UpFMvDUlPn1QxMLXs/G4Cea50Oe2B487yRgxUhOEYm+CqXv/zTlKMNB360QybyiLBntDzIsZW0GB3mN6aerisrL4ry7hG22zatPmBjGJVa6G6xlfXE2x8t44d61q2I7UnXlnBG2QKBgGaD4fIWoDr/VKQY1rjI3p3cCIyXCYzR9BQLL8MgPN5L6eJCDE4eHrVsMgnGUAlstmuORrCcjnXFFasr8JqqRIpeH2h7gajNv0ovA1/81JJxIrymnKMAxqTFyBW4XMiU3tWiI0d7L7gkBA5OQvjLyI9TZjTQKhNfRbmQL2lQqYCzAoGBAM72X5HE5aiDrSQKkDAoEbcF/4D334g7bYqW7rna3CeZ4w4osDR8SVFv82gTgRRnEme22+FJcX3iA8+7SX+kegSL9bL2U3RfxxzbhHJayEj0aZQdwSr3iJqHD9umkjdi7QzdgXsi/uwPtH8rRLopdqu66KSeJukDhO78PEmO7FgR
        //公钥 MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyCVqVQwPMcdrQP+OnL5creWiJwKKWsRx0aAKmSShvPQm04LJsfItg94/E12o1ey6lwwolXoF96cPqTIWFMnY1+iQV8MMOlG4wLU/OFTpvgxvnIbqLUz8gmrH0U+TSJLd1zP8MBon1L+iKrNf/yk972VU7oGrwkl5GBk+NZxa9qJQc/fYAzvU5SPjmVMufvDWtXVpB7W1yOAzxARDym0gwDvR3cqWjE896tMUmrqjRXp45myEb3Nk6oXLh0qIqsHNwWjRrtc6bIxH4cvcg7iLwYQe+TwtJ0pdV1N6SIuIvFuTxlcopPVa/w0/MgzxmYRvrnufKEMIHUlWUaAbc77s+QIDAQAB
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

        String encodedPrivateKeyBase64 = Encoders.BASE64.encode(keyPair.getPrivate().getEncoded());
        String encodedPublicKeyBase64 = Encoders.BASE64.encode(keyPair.getPublic().getEncoded());

        byte[] encodedPublicKeyBytes = Decoders.BASE64.decode(encodedPublicKeyBase64);
        byte[] encodedPrivateKeyBytes = Decoders.BASE64.decode(encodedPrivateKeyBase64);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKeyBytes));

        Jwts.builder()
                // jwt id
                .setId(UUID.randomUUID().toString())
                // jwt time & date
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();



    }
}
