package cn.lzr.ecommerce.constant;

/**
 * <h1>授权需要使用的一些常量信息</h1>
 * */
public final class AuthorityConstant {
    /** RSA 私钥, 除了授权中心以外, 应该暴露给任何客户端 */
    public final static String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwgg" +
            "SkAgEAAoIBAQDIJWpVDA8xx2tA/46cvlyt5aInAopaxHHRoAqZJKG89CbTgsmx8i2D3j8TX" +
            "ajV7LqXDCiVegX3pw+pMhYUydjX6JBXwww6UbjAtT84VOm+DG+chuotTPyCasfRT5NIkt3X" +
            "M/wwGifUv6Iqs1//KT3vZVTugavCSXkYGT41nFr2olBz99gDO9TlI+OZUy5+8Na1dWkHtbX" +
            "I4DPEBEPKbSDAO9HdypaMTz3q0xSauqNFenjmbIRvc2TqhcuHSoiqwc3BaNGu1zpsjEfhy9" +
            "yDuIvBhB75PC0nSl1XU3pIi4i8W5PGVyik9Vr/DT8yDPGZhG+ue58oQwgdSVZRoBtzvuz5A" +
            "gMBAAECggEASwaziHP/0iI2E2R5GkrFxS46jbMHGCVhoToEHwo7UvCXCVySrRDifPc339OR" +
            "MbgO7gZRxE68o+xNE5jB7EMJ9U2ppixojggKYLrQmxZBlMtmn193s0mTsnPCubTBukfLvxs" +
            "uov17BuZbLPSWvLjCDdaHcuzK7QbLzPvRdRlKg/8Ewh/sMQZAi84KbVOx4XLj069ZyA5fZq" +
            "Hr3za3xAVu4+3T5q5g/XeehsNJMyRs8OKzTPw058Lgcm2Ggd1mDzdX6VqCSpB8bA3FXBfC+" +
            "wmlDM/rpacrSNLk0iXiEbuxiJg+MC7hTLqCnRl+IQKNizN+O6cienImW7JcfPXc9mIibQKB" +
            "gQDj1cLYnOm/R3fez9RE6SbN2iDBNpDEOup0jl4dzexWush8EA0p/2Rt1dbe4Oq0DvffRoZ" +
            "eHDNeKABMhY5glOv1RMDE0ZrCeKHvGMLhVpb3FnibPfaAZReHjnZHjSitcb0irVopp2W82i" +
            "JjBEyTtk2aBxb5mpQF0G93GXDodsOVfwKBgQDg42RVqTcsPPljbA3Tw1xAfwOG96V5uAt/5" +
            "1qRn0ovzgbkFw43qwlYS2clLx6ZCESN+k/wiPWkkIDhaeuCD0wokk5Kutcr01bHLmopbKs0" +
            "AMOhGdRc+BITqThastKvuxmVOJJxC/bO59zaQZBn6iS4IZHJb3/WZnpbGvDarrZphwKBgQC" +
            "8TCrhA65cHCEB0RpKabdcYm63wPUceOTUt+UpFMvDUlPn1QxMLXs/G4Cea50Oe2B487yRgx" +
            "UhOEYm+CqXv/zTlKMNB360QybyiLBntDzIsZW0GB3mN6aerisrL4ry7hG22zatPmBjGJVa6" +
            "G6xlfXE2x8t44d61q2I7UnXlnBG2QKBgGaD4fIWoDr/VKQY1rjI3p3cCIyXCYzR9BQLL8Mg" +
            "PN5L6eJCDE4eHrVsMgnGUAlstmuORrCcjnXFFasr8JqqRIpeH2h7gajNv0ovA1/81JJxIry" +
            "mnKMAxqTFyBW4XMiU3tWiI0d7L7gkBA5OQvjLyI9TZjTQKhNfRbmQL2lQqYCzAoGBAM72X5" +
            "HE5aiDrSQKkDAoEbcF/4D334g7bYqW7rna3CeZ4w4osDR8SVFv82gTgRRnEme22+FJcX3iA" +
            "8+7SX+kegSL9bL2U3RfxxzbhHJayEj0aZQdwSr3iJqHD9umkjdi7QzdgXsi/uwPtH8rRLop" +
            "dqu66KSeJukDhO78PEmO7FgR";

    /** 默认的 Token 超时时间, 一天 */
    public static final long DEFAULT_EXPIRE_TIME = 160 * 60 * 1000;

}
