package cn.lzr.ecommerce.service;

import cn.lzr.ecommerce.filter.AccessContext;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class BaseTest {

    // 防控控制修饰符 protect 表示静态变量 loginUserInfo 只对统一包内类和所有子类可见（例如在别的包下实例化的子类就不可见）
    protected final LoginUserInfo loginUserInfo = new LoginUserInfo(
            10L, "111@test.com"
    );

    /**
     * <h2>ThreadLocal工作原理</h2>
     * 1.开启测试Test线程
     * 2.将实例化的loginUserInfo对象存入当前线程的ThreadLocal中
     */
    @Before
    public void init(){
        AccessContext.setLoginUserInfo(loginUserInfo);
    }

    @After
    public void destroy(){
        AccessContext.clearLoginUserInfo();
    }
}
