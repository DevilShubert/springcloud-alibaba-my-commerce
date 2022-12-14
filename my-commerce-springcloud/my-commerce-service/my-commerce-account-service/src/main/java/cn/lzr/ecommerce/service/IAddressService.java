package cn.lzr.ecommerce.service;

import cn.lzr.ecommerce.account.AddressInfo;
import cn.lzr.ecommerce.common.TableId;

/**
 * <h1>用户地址相关服务接口</h1>
 */
public interface IAddressService {

    /**
     * <h2>创建用户地址信息</h2>
     * @param addressInfo 1个或n个地址信息
     * @return TableId 返回数据表主键id
     */
    TableId createAddressInfo(AddressInfo addressInfo);

    /**
     * <h2>获取当前用户的地址信息</h2>
     * @return AddressInfo 1个或n个地址信息
     */
    AddressInfo getCurrentAddressInfo();

    /**
     * <h2>通过主键id获取用户地址信息</h2>
     * @param id 主键id
     * @return AddressInfo（一条地址）
     */
    AddressInfo getAddressInfoById(Long id);

    /**
     * <h2>通过 TableId 获取用户地址信息</h2>
     * @param tableId 一个或多个主键id
     * @return 用户地址信息（一条或多条地址）
     */
    AddressInfo getAddressInfoByTableId(TableId tableId);
}
