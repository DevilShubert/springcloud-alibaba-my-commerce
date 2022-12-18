package cn.lzr.ecommerce.constant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <h1>商品状态枚举类</h1>
 * */
@Slf4j
public enum GoodsStatus {

    ONLINE(101,"上线"),
    OFFLINE(102,"下线"),
    STOCK_OUT(103, "缺货");

    /** 状态码 */
    // final修饰成员变量表示只能赋值一次
    private final Integer status;

    /** 状态描述 */
    private final String description;

    GoodsStatus(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    /**
     * <h2>根据 code 获取到 GoodsStatus</h2>
     * */
    public static GoodsStatus of(Integer status){
        // 如果为null则抛出空指针异常
        Objects.requireNonNull(status);

        GoodsStatus targetGoodsStatus = null;

        // 获取枚举类型数组
        Stream<GoodsStatus> goodsStatusStream = Arrays.stream(values());
        try {
            targetGoodsStatus = goodsStatusStream
                    .filter(goodsStatus -> status.equals(goodsStatus.getStatus()))
                    .findAny()
                    .orElseThrow(new Supplier<Throwable>() {
                        @Override
                        public Throwable get() {
                            return new IllegalArgumentException(status + " not exists");
                        }
                    });
        } catch (Throwable e) {
            log.error("status :[{}] not exists ", status);
        }
        return targetGoodsStatus;

//        targetGoodsStatus = goodsStatusStream
//                .filter(goodsStatus -> status.equals(goodsStatus.getStatus()))
//                .findAny()
//                .orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException(status + " not exists"));
//        return targetGoodsStatus;

    }
}
