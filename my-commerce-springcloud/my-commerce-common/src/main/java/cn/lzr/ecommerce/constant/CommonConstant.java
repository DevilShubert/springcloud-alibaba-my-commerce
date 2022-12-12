package cn.lzr.ecommerce.constant;

/**
 * <h1>通用模块常量定义</h1>
 * */
public class CommonConstant {
    /** RSA 公钥 */
    public static final String PUBLIC_KEY ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBC" +
            "gKCAQEAyCVqVQwPMcdrQP+OnL5creWiJwKKWsRx0aAKmSShvPQm04LJsfItg94/E12o1" +
            "ey6lwwolXoF96cPqTIWFMnY1+iQV8MMOlG4wLU/OFTpvgxvnIbqLUz8gmrH0U+TSJLd1" +
            "zP8MBon1L+iKrNf/yk972VU7oGrwkl5GBk+NZxa9qJQc/fYAzvU5SPjmVMufvDWtXVpB" +
            "7W1yOAzxARDym0gwDvR3cqWjE896tMUmrqjRXp45myEb3Nk6oXLh0qIqsHNwWjRrtc6b" +
            "IxH4cvcg7iLwYQe+TwtJ0pdV1N6SIuIvFuTxlcopPVa/w0/MgzxmYRvrnufKEMIHUlWU" +
            "aAbc77s+QIDAQAB";

    /** JWT 中存储用户信息的 key */
    public static final String JWT_USER_INFO_KEY = "my-commerce-user";

    /** 授权中心的 service-id，用于注册中心使用 */
    public static final String AUTHORITY_CENTER_SERVICE_ID = "my-commerce-authority-center";
}
