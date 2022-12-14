package cn.lzr.ecommerce.service.impl;

import cn.lzr.ecommerce.account.BalanceInfo;
import cn.lzr.ecommerce.dao.EcommerceBalanceDao;
import cn.lzr.ecommerce.entity.EcommerceBalance;
import cn.lzr.ecommerce.filter.AccessContext;
import cn.lzr.ecommerce.service.IBalanceService;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * <h1>用于余额相关服务接口实现</h1>
 * */
@Service
@Slf4j
@Transactional(rollbackOn = {Exception.class})
public class BalanceServiceImpl implements IBalanceService {
    private final EcommerceBalanceDao ecommerceBalanceDao;

    public BalanceServiceImpl(EcommerceBalanceDao ecommerceBalanceDao) {
        this.ecommerceBalanceDao = ecommerceBalanceDao;
    }

    /**
     * <h2>获取当前用户余额信息</h2>
     * */
    @Override
    public BalanceInfo getCurrentUserBalanceInfo() {
        Long userId = AccessContext.getLoginUserInfo().getId();
        EcommerceBalance ecommerceBalance = ecommerceBalanceDao.findByUserId(userId);

        // 用户返回的用户余额信息
        BalanceInfo balanceInfo = new BalanceInfo();
        balanceInfo.setUserId(userId);

        if (Objects.isNull(ecommerceBalance)) {
            // 根据userId没有查询到用于余额，则做初始化，为该用户插入他的记录，并余额设定为0
            EcommerceBalance newBalance = new EcommerceBalance();
            newBalance.setUserId(userId);
            newBalance.setBalance(10000L);
            Long recordId = ecommerceBalanceDao.save(newBalance).getId();
            log.info("init user balance record: [{}]", recordId);
            balanceInfo.setBalance(newBalance.getBalance());
        } else {
            // 有该用户的余额记录，则直接返回
            balanceInfo.setBalance(ecommerceBalance.getBalance());
        }
        return balanceInfo;
    }

    /**
     * <h2>扣减用户余额</h2>
     * */
    @Override
    public BalanceInfo deductBalance(BalanceInfo deductInfo) {
        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();
        Long userId = loginUserInfo.getId();

        // 扣减用户余额的一个基本原则: 扣减额 <= 当前用户余额
        EcommerceBalance ecommerceBalance = ecommerceBalanceDao.findByUserId(userId);
        if (Objects.isNull(ecommerceBalance) || ecommerceBalance.getBalance() - deductInfo.getBalance() < 0){
            throw new RuntimeException("user balance is not enough!");
        }

        Long sourceBalance = ecommerceBalance.getBalance();
        ecommerceBalance.setBalance(sourceBalance - deductInfo.getBalance());
        EcommerceBalance deductedBalance = ecommerceBalanceDao.save(ecommerceBalance);
        log.info("execute deduct balance: primary key - [{}], before - [{}], after  - [{}]",
                deductedBalance.getId(),
                sourceBalance,
                deductedBalance.getBalance());
        return new BalanceInfo(userId, deductedBalance.getBalance());
    }
}
