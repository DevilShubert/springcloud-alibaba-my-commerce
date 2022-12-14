package cn.lzr.ecommerce.dao;

import cn.lzr.ecommerce.entity.EcommerceBalance;
import org.springframework.data.repository.CrudRepository;

/**
 * <h1>EcommerceBalance Dao 接口定义</h1>
 * */
public interface EcommerceBalanceDao extends CrudRepository<EcommerceBalance, Long> {
    /** 根据 userId 查询 EcommerceBalance 对象 */
    EcommerceBalance findByUserId(Long userId);
}
