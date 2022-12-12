package cn.lzr.ecommerce.service;

import cn.lzr.ecommerce.GateWayApplicationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@SpringBootTest(classes = {GateWayApplicationTest.class}) // 标注主启动类
@RunWith(SpringRunner.class)
public class PredicateTest {

    public static final List<String> MICRO_SERVICE = Arrays.asList(
            "nacos", "authority", "gateway", "ribbon", "feign", "hystrix", "e-commerce"
    );

    /**
     * <h2>test 方法主要用于参数符不符合规则, 返回值是 boolean</h2>
     * */
    @Test
    public void testPredicateTest(){
        Predicate<String> letterLengthLimit = s -> s.length() > 5;
        MICRO_SERVICE.stream().filter(letterLengthLimit).forEach(System.out::println);
    }

    /**
     * <h2>and 方法等同于我们的逻辑与 &&, 存在短路特性, 需要所有的条件都满足</h2>
     * */
    @Test
    public void testPredicateAnd(){
        Predicate<String> letterLengthLimit = s -> s.length() > 5;
        Predicate<String> letterStarterWith = s -> s.startsWith("gate");
        MICRO_SERVICE.stream().filter(letterLengthLimit.and(letterStarterWith)).forEach(System.out::println);
    }

    /**
     * <h2>or 等同于我们的逻辑或 ||, 多个条件主要一个满足即可</h2>
     * */
    @Test
    public void testPredicateOr() {

        Predicate<String> letterLengthLimit = s -> s.length() > 5;
        Predicate<String> letterStartWith = s -> s.startsWith("gate");

        MICRO_SERVICE.stream().filter(
                letterLengthLimit.or(letterStartWith)
        ).forEach(System.out::println);
    }

    /**
     * <h2>negate 等同于我们的逻辑非 !</h2>
     * */
    @Test
    public void testPredicateNegate() {
        Predicate<String> letterStartWith = s -> s.startsWith("gate");
        MICRO_SERVICE.stream().filter(letterStartWith.negate()).forEach(System.out::println);
    }

    /**
     * <h2>isEqual 类似于 equals(), 区别在于: 先判断对象是否为 NULL,
     * 不为 NULL 再使用 equals 进行比较</h2>
     * */
    @Test
    public void  testPredicateIsEqual(){
        Predicate<String> equalGateway = s -> {
            Predicate<Object> predicate = Predicate.isEqual("gateway");
            // Predicate接口返回值必须为Boolean值，test()方法返回值表示执行计算并返回Boolean值
            return predicate.test(s);
        };
        MICRO_SERVICE.stream().filter(equalGateway).forEach(System.out::println);
    }
}
