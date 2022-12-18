package cn.lzr.ecommerce.service.impl;

import cn.lzr.ecommerce.common.TableId;
import cn.lzr.ecommerce.constant.GoodsConstant;
import cn.lzr.ecommerce.dao.EcommerceGoodsDao;
import cn.lzr.ecommerce.entity.EcommerceGoods;
import cn.lzr.ecommerce.goods.DeductGoodsInventory;
import cn.lzr.ecommerce.goods.GoodsInfo;
import cn.lzr.ecommerce.goods.SimpleGoodsInfo;
import cn.lzr.ecommerce.service.IGoodsService;
import cn.lzr.ecommerce.vo.PageSimpleGoodsInfo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1>商品微服务相关服务功能实现</h1>
 * */
@Slf4j
@Service
@Transactional(rollbackOn = {Exception.class})
public class GoodsServiceImpl implements IGoodsService {

    // 注入Redis和MySQL的dao
    private final RedisTemplate redisTemplate;
    private final EcommerceGoodsDao ecommerceGoodsDao;
    public GoodsServiceImpl(RedisTemplate redisTemplate, EcommerceGoodsDao ecommerceGoodsDao) {
        this.redisTemplate = redisTemplate;
        this.ecommerceGoodsDao = ecommerceGoodsDao;
    }

    /**
     * <h2>根据 TableId （主键）查询商品详细信息</h2>
     * <h3>方法逻辑</h3>
     * 查询的是详细的商品信息，不能从redis查，只有从db中拿  <br></br>
     * */
    @Override
    public List<GoodsInfo> getGoodsInfoByTableId(TableId tableId) {

        List<Long> ids = tableId.getIds().stream()
                .map(TableId.Id::getId)
                .collect(Collectors.toList());
        log.info("get goods info by ids: [{}]", JSON.toJSONString(ids));

        // ecommerceGoodsDao.findAllById(ids)还接返回的是Iterable可迭代对象
        List<EcommerceGoods> ecommerceGoods = IterableUtils.toList(ecommerceGoodsDao.findAllById(ids));

        return ecommerceGoods.stream().map(EcommerceGoods::toGoodsInfo).collect(Collectors.toList());
    }


    /**
     * <h2>根据 TableId （主键）查询简单商品信息</h2>
     * <h3>方法逻辑</h3>
     * 获取简单商品信息，可以从redis中拿，但redis不一定有，所以分以下3种情况处理<br></br>
     *   1、tableId全部的简单商品信息都在redis中则直接拿出<br></br>
     *   2、一部分简单商品信息在redis中（left），而另一部分不在redis中（right），这时需要把right从db中查询出来并缓存到redis中，与left合并后再返回<br></br>
     *   3、全部简单商品信息都不在redis中，则直接全部从db中查询出来并缓存到redis中，再返回<br></br>
     * */
    @Override
    public List<SimpleGoodsInfo> getSimpleGoodsInfoByTableId(TableId tableId) {
        // Redis中key都是String
        List<String> goodIds = tableId.getIds()
                .stream()
                .map(id -> id.getId().toString())
                .collect(Collectors.toList());

        List<Object> redisList = redisTemplate.opsForHash().multiGet(GoodsConstant.ECOMMERCE_GOODS_DICT_KEY, goodIds);
        // 注意，如果redis没有查询到结果，返回的redisList也不为空
        // 加入goodIds为[1,2]，则redisList返回的是[null, null]，所以需要对stream流进行一次filter过滤
        List<Object> cachedSimpleGoodsInfos = redisList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 如果从 Redis 中查到了商品信息, 分两种情况去操作
        if (CollectionUtils.isNotEmpty(cachedSimpleGoodsInfos)) {
            // 1. 如果从缓存中查询出所有需要的 SimpleGoodsInfo
            if (cachedSimpleGoodsInfos.size() == goodIds.size()) {
                log.info("get simple goods info by ids (from cache): [{}]",
                        JSON.toJSONString(goodIds));
                return parseCachedGoodsInfo(cachedSimpleGoodsInfos);
            } else {
                // 2. 一部分从 redis cache 中获取 (left), 一部分从数据表中获取 (right)
                List<SimpleGoodsInfo> left = parseCachedGoodsInfo(cachedSimpleGoodsInfos);
                // 取差集: 传递进来的参数 - 缓存中查到的 = 缓存中没有的(right)
                List<Long> goodsPart = goodIds.stream().map(Long::valueOf).collect(Collectors.toList());
                List<Long> redisPart = left.stream().map(SimpleGoodsInfo::getId).collect(Collectors.toList());

                Collection<Long> dbPartIds = CollectionUtils.subtract(goodsPart, redisPart);
                // 缓存中没有的部分（right）, 查询数据表并缓存
                List<SimpleGoodsInfo> right = queryGoodsFromDBAndCacheToRedis(new TableId(dbPartIds.stream()
                        .map(ids -> new TableId.Id(ids))
                        .collect(Collectors.toList())
                ));
                // 合并 left 和 right 并返回
                log.info("get simple goods info by ids (from db and cache): [{}]",
                        JSON.toJSONString(goodsPart));
                return new ArrayList<>(CollectionUtils.union(left, right));
            }
        } else {
            // 3.从 redis 里面什么都没有查到
            return queryGoodsFromDBAndCacheToRedis(tableId);
        }
    }

    /**
     * <h2>将缓存中的数据反序列化成 Java Pojo 对象</h2>
     * */
    private List<SimpleGoodsInfo> parseCachedGoodsInfo(List<Object> cachedSimpleGoodsInfo){
        return cachedSimpleGoodsInfo.stream()
                .map(o -> JSON.parseObject(o.toString(), SimpleGoodsInfo.class))
                .collect(Collectors.toList());
    }

    /**
     * <h2>从数据表中查询数据, 并缓存到 Redis 中</h2>
     * */
    private List<SimpleGoodsInfo> queryGoodsFromDBAndCacheToRedis(TableId tableId){
        // 从数据表中查询数据并做转换
        List<Long> ids = tableId.getIds().stream().map(TableId.Id::getId).collect(Collectors.toList());

        log.info("get simple goods info by ids (from db): [{}]",
                JSON.toJSONString(ids));
        List<EcommerceGoods> ecommerceGoods = IterableUtils.toList(
                ecommerceGoodsDao.findAllById(ids)
        );
        List<SimpleGoodsInfo> result = ecommerceGoods.stream()
                .map(EcommerceGoods::toSimple).collect(Collectors.toList());
        // 将结果缓存, 下一次可以直接从 redis cache 中查询
        log.info("cache goods info: [{}]", JSON.toJSONString(ids));
        // 缓存到redis中
        HashMap<String, String> saveMap = new HashMap<>(result.size());
        result.stream()
                .forEach(simpleGoodsInfo -> saveMap.put(simpleGoodsInfo.getId().toString(),
                        JSON.toJSONString(simpleGoodsInfo)));
        redisTemplate.opsForHash()
                .putAll(GoodsConstant.ECOMMERCE_GOODS_DICT_KEY,saveMap);
        return result;
    }

    /**
     * <h2>获取分页的商品信息</h2>
     * <h3>方法逻辑</h3>
     * 构造分页查询对象PageRequest<br></br>
     * 查询完之后检查是否有更多的页数允许下一页查询返回<br></br>
     * */
    @Override
    public PageSimpleGoodsInfo getSimpleGoodsInfoByPage(int page) {
        // 分页不能从 redis cache 中去拿
        if (page <= 1) {
            page = 1;   // 默认是第一页
        }

        // 这里分页的规则(你可以自由修改): 1页10调数据, 按照 id 倒序排列（新加入的商品在前）
        PageRequest pageable = PageRequest.of(page - 1, 10,
                Sort.by("id").descending());

        Page<EcommerceGoods> orderPage = ecommerceGoodsDao.findAll(pageable);

        // 是否还有更多页: 总页数是否大于当前给定的页
        boolean hasMore = orderPage.getTotalPages() > page;

        return new PageSimpleGoodsInfo(
                orderPage.getContent().stream()
                        .map(EcommerceGoods::toSimple).collect(Collectors.toList()),
                hasMore);
    }

    /**
     * <h2>扣减商品库存</h2>
     * <h3>方法逻辑</h3>
     * 检查扣减信息对象是否合理，之后检查扣减的商品是否都存在，如果存在再检查够不够扣减，如果这三个条件都满足则再扣减<br></br>
     * */
    @Override
    public Boolean deductGoodsInventory(List<DeductGoodsInventory> deductGoodsInventories) {
        // 检验下参数是否合法
        deductGoodsInventories.forEach(d -> {
            if (d.getCount() <= 0) {
                throw new RuntimeException("purchase goods count need > 0");
            }
        });

        List<EcommerceGoods> ecommerceGoods = IterableUtils.toList(
                ecommerceGoodsDao.findAllById(
                        deductGoodsInventories.stream()
                                .map(DeductGoodsInventory::getGoodsId)
                                .collect(Collectors.toList())
                )
        );
        // 根据传递的 goodsIds 查询不到商品对象, 抛异常
        if (CollectionUtils.isEmpty(ecommerceGoods)) {
            throw new RuntimeException("can not found any goods by request");
        }
        // 查询出来的商品数量与传递的不一致, 抛异常
        if (ecommerceGoods.size() != deductGoodsInventories.size()) {
            throw new RuntimeException("request is not valid");
        }


        // 直接将一个stream转换为map
        Map<Long, DeductGoodsInventory> goodsId2Inventory = deductGoodsInventories
                .stream()
                .collect(Collectors.toMap(new Function<DeductGoodsInventory, Long>() {
            @Override
            public Long apply(DeductGoodsInventory deductGoodsInventory) {
                return deductGoodsInventory.getGoodsId();
            }
        }, new Function<DeductGoodsInventory, DeductGoodsInventory>() {
            @Override
            public DeductGoodsInventory apply(DeductGoodsInventory deductGoodsInventory) {
                return deductGoodsInventory;
            }
        }));

        //        Function.identity()返回一个输出跟输入一样的Lambda表达式对象
        //        Map<Long, DeductGoodsInventory> goodsId2Inventory =
        //                deductGoodsInventories.stream().collect(
        //                        Collectors.toMap(DeductGoodsInventory::getGoodsId,
        //                                Function.identity())
        //                );


        // 检查是不是可以扣减库存, 再去扣减库存
        ecommerceGoods.forEach(oneEcommerceGoods -> {
            Long currentInventory = oneEcommerceGoods.getInventory();
            Integer needDeductInventory = goodsId2Inventory.get(oneEcommerceGoods.getId()).getCount();
            if (currentInventory - needDeductInventory < 0) {
                log.error("goods inventory is not enough: [{}], [{}]",
                        currentInventory, needDeductInventory);
                throw new RuntimeException("goods inventory is not enough: " + oneEcommerceGoods.getId());
            }
            // 扣减库存
            oneEcommerceGoods.setInventory(currentInventory - needDeductInventory);
            log.info("deduct goods inventory: [{}], [{}], [{}]", oneEcommerceGoods.getId(),
                    currentInventory, oneEcommerceGoods.getInventory());
        });

        ecommerceGoodsDao.saveAll(ecommerceGoods);
        log.info("deduct goods inventory done");

        return true;
    }
}
