package cn.lzr.ecommerce.service.async.impl;

import cn.lzr.ecommerce.constant.GoodsConstant;
import cn.lzr.ecommerce.dao.EcommerceGoodsDao;
import cn.lzr.ecommerce.entity.EcommerceGoods;
import cn.lzr.ecommerce.goods.GoodsInfo;
import cn.lzr.ecommerce.goods.SimpleGoodsInfo;
import cn.lzr.ecommerce.service.async.IAsyncService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AsyncServiceImpl implements IAsyncService {

    private final EcommerceGoodsDao ecommerceGoodsDao;
    private final RedisTemplate redisTemplate;

    public AsyncServiceImpl(EcommerceGoodsDao ecommerceGoodsDao,
                            RedisTemplate redisTemplate) {
        this.ecommerceGoodsDao = ecommerceGoodsDao;
        this.redisTemplate = redisTemplate;
    }


    /**
     * <h2>异步任务需要加上注解, 并指定使用的线程池</h2>
     * 异步任务处理两件事:
     *  1. 将商品信息保存到数据表
     *  2. 更新商品缓存
     * */
    @Async()
    @Override
    public void asyncImportGoods(List<GoodsInfo> goodsInfos, String taskId) {
        log.info("async task running taskId: [{}]", taskId);

        StopWatch watch = StopWatch.createStarted();

        // 1.如果goodsInfo当中有重复的商品，不保存，直接返回，记录错误日志
        // 请求数据是否合法标志位
        boolean isIllegal = false;

        // 将商品信息字段 joint 在一起, 用来判断是否存在重复
        HashSet<Object> goodsJointInfos = new HashSet<>(goodsInfos.size());
        // 过滤出来的, 可以入库的商品信息(规则按照自己的业务需求自定义即可)
        List<GoodsInfo> filteredGoodsInfo = new ArrayList<>(goodsInfos.size());


        // 走一遍循环，检查是否满足基本要求不满足或有重复商品
        for (GoodsInfo goods : goodsInfos) {

            if (goods.getPrice() <= 0 || goods.getSupply() <= 0) {
                // 基本条件不满足的, 直接过滤器
                log.error("goods info is invalid: [{}]", JSON.toJSONString(goods));
                continue;
            }

            // 组合符合要求的商品信息
            String jointInfo = String.format(
                    "%s,%s,%s",
                    goods.getGoodsCategory(), goods.getBrandCategory(),
                    goods.getGoodsName()
            );

            // 是否重复
            if (goodsJointInfos.contains(jointInfo)) {
                isIllegal = true;
            }

            // 加入到两个容器中
            goodsJointInfos.add(jointInfo);
            filteredGoodsInfo.add(goods);
        }

        // 如果存在重复商品或者是没有需要入库的商品, 直接打印日志返回
        if (isIllegal || filteredGoodsInfo.size() == 0) {
            watch.stop();
            log.warn("import nothing: [{}]", JSON.toJSONString(filteredGoodsInfo));
            log.info("check and import goods done: [{}ms]",
                    watch.getTime(TimeUnit.MILLISECONDS));
            return;
        }

        // 将过滤后的商品转换为ORM实体集合
        List<EcommerceGoods> ecommerceGoods = filteredGoodsInfo.stream()
                .map(EcommerceGoods::toEcommerceGoods)
                .collect(Collectors.toList());

        // 最终要插入的商品，再次之前还要检查一遍数据库中商品是否重复
        List<EcommerceGoods> finalTargetGoods = new ArrayList<>(ecommerceGoods.size());
        ecommerceGoods.forEach(ecommerceGoods1 -> {
                    EcommerceGoods ecommerceGoodsResult =
                            ecommerceGoodsDao.findTop1ByGoodsCategoryAndBrandCategoryAndGoodsName(
                            ecommerceGoods1.getGoodsCategory(),
                            ecommerceGoods1.getBrandCategory(),
                            ecommerceGoods1.getGoodsName()
                    ).orElse(null);
                    if (Objects.isNull(ecommerceGoodsResult)) {
                        finalTargetGoods.add(ecommerceGoods1);
                    }
                });

        // 商品信息入库
        List<EcommerceGoods> saveGoods = IterableUtils
                .toList(ecommerceGoodsDao.saveAll(finalTargetGoods));

        // 将入库商品信息同步到 Redis 中
        saveNewGoodsInfoToRedis(saveGoods);

        log.info("save goods info to db and redis: [{}]", saveGoods.size());

        watch.stop();
        log.info("check and import goods success: [{}ms]",
                watch.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * <h2>将保存到数据表中的数据缓存到 Redis 中</h2>
     * dict: key -> <id, SimpleGoodsInfo(json)>
     * */
    public void saveNewGoodsInfoToRedis(List<EcommerceGoods> savedGoods){
        // 由于 Redis 是内存存储, 只存储简单商品信息
        List<SimpleGoodsInfo> simpleGoodsInfos = savedGoods.stream()
                .map(EcommerceGoods::toSimple)
                .collect(Collectors.toList());

        HashMap<String, String> saveMap = new HashMap<>(simpleGoodsInfos.size());
        simpleGoodsInfos
                .forEach(simpleGoodsInfo -> saveMap.put(simpleGoodsInfo.getId().toString(), // map的key为商品主键id
                        JSON.toJSONString(simpleGoodsInfo))); // map的value为simpleGoodsInfo

        // 保存到 Redis 中
        redisTemplate.opsForHash()
                .putAll(GoodsConstant.ECOMMERCE_GOODS_DICT_KEY,saveMap);
    }



}
