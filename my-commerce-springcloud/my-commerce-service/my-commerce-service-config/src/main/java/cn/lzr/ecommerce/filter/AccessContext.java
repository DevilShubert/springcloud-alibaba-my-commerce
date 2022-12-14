package cn.lzr.ecommerce.filter;

import cn.lzr.ecommerce.vo.LoginUserInfo;

/**
 * <h1>使用 ThreadLocal 去单独存储每一个线程携带的 LoginUserInfo 信息</h1>
 * 要及时的清理我们保存到 ThreadLocal 中的用户信息:
 *  1. 保证没有资源泄露
 *  2. 保证线程在重用时, 不会出现数据混乱
 */
public class AccessContext {
    private static final ThreadLocal<LoginUserInfo> loginUserInfo = new ThreadLocal<>();

    /**
     * <h1>返回每个线程保存的loginUserInfo对象</h1>
     * @return
     */
    public static LoginUserInfo getLoginUserInfo() {
        return loginUserInfo.get();
    }

    /**
     * <h2>每个线程主动去设置loginUserInfo对象</h2>
     * @param loginUserInfo_
     */
    public static void setLoginUserInfo(LoginUserInfo loginUserInfo_){
        loginUserInfo.set(loginUserInfo_);
    }

    /**
     * <h2>清除每个线程保存的loginUserInfo对象</h2>
     */
    public static void clearLoginUserInfo(){
        loginUserInfo.remove();
    }
}
