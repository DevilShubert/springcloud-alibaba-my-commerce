package cn.lzr.ecommerce.dao;

import cn.lzr.ecommerce.constant.BrandCategory;
import cn.lzr.ecommerce.constant.GoodsCategory;
import cn.lzr.ecommerce.entity.EcommerceGoods;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

/**
 * <h1>EcommerceGoods Dao 接口定义</h1>
 * */
public interface EcommerceGoodsDao extends PagingAndSortingRepository<EcommerceGoods, Long> {

    /**
     * <h2>根据查询条件查询商品表, 并限制返回结果</h2>
     * select * from t_ecommerce_goods where goods_category = ? and brand_category = ?
     * and goods_name = ? limit 1;
     * */
    Optional<EcommerceGoods> findTop1ByGoodsCategoryAndBrandCategoryAndGoodsName(
            GoodsCategory goodsCategory, BrandCategory brandCategory,
            String goodsName
    );
}
