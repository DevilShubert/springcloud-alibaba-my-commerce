package cn.lzr.ecommerce.service.impl;

import cn.lzr.ecommerce.account.AddressInfo;
import cn.lzr.ecommerce.common.TableId;
import cn.lzr.ecommerce.dao.EcommerceAddressDao;
import cn.lzr.ecommerce.entity.EcommerceAddress;
import cn.lzr.ecommerce.filter.AccessContext;
import cn.lzr.ecommerce.service.IAddressService;
import cn.lzr.ecommerce.vo.LoginUserInfo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1>用户地址相关服务接口实现</h1>
 * */
@Service
@Slf4j
@Transactional(rollbackOn = {Exception.class})
public class AddressServiceImpl  implements IAddressService {
    private final EcommerceAddressDao ecommerceAddressDao;

    public AddressServiceImpl(EcommerceAddressDao ecommerceAddressDao) {
        this.ecommerceAddressDao = ecommerceAddressDao;
    }

    /**
     * <h2>存储一个用户的一条或多条地址信息</h2>
     * */
    @Override
    public TableId createAddressInfo(AddressInfo addressInfo) {
        // 类的静态方法 -> 方法区中的方法 -> 确定到当前线程 -> 拿到ThreadLocal
        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        // 将传递的参数转换成实体对象
        List<EcommerceAddress> ecommerceAddresses = addressInfo.getAddressItems().stream()
                .map(new Function<AddressInfo.AddressItem, EcommerceAddress>() {
                    @Override
                    public EcommerceAddress apply(AddressInfo.AddressItem addressItem) {
                        return EcommerceAddress.toOneEcommerceAddress(loginUserInfo.getId(), addressItem);
                    }
                })
                .collect(Collectors.toList());

        // 保存到数据表并把返回记录的 id 给调用方
        List<EcommerceAddress> savedRecords = ecommerceAddressDao.saveAll(ecommerceAddresses);
        List<Long> ids = savedRecords.stream()
                .map(new Function<EcommerceAddress, Long>() {
                    @Override
                    public Long apply(EcommerceAddress ecommerceAddress) {
                        return ecommerceAddress.getId();
                    }
                }).collect(Collectors.toList());
        log.info("create address info: [{}], [{}]", loginUserInfo.getId(),
                JSON.toJSONString(ids));

        // 返回插入记录的主键id值（包装为TableId）
        return new TableId(ids
                .stream()
                .map(TableId.Id::new)
                .collect(Collectors.toList()));
    }

    /**
     * <h2>获取当前用户的地址信息</h2>
     */
    @Override
    public AddressInfo getCurrentAddressInfo() {
        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();
        List<EcommerceAddress> ecommerceAddresses = ecommerceAddressDao.findAllByUserId(loginUserInfo.getId());

        // 根据 userId 查询到用户的地址信息, 再实现转换
        List<AddressInfo.AddressItem> addressItems = ecommerceAddresses
                .stream()
                .map(new Function<EcommerceAddress, AddressInfo.AddressItem>() {
                    @Override
                    public AddressInfo.AddressItem apply(EcommerceAddress ecommerceAddress) {
                        return ecommerceAddress.toAddressItem();
                    }
                })
                .collect(Collectors.toList());

        return new AddressInfo(loginUserInfo.getId(), addressItems);
    }

    /**
     * <h2>通过主键id获取用户地址信息</h2>
     */
    @Override
    public AddressInfo getAddressInfoById(Long id) {
        EcommerceAddress ecommerceAddress = ecommerceAddressDao.findById(id).orElse(null);
        if (Objects.isNull(ecommerceAddress)){
            throw new RuntimeException("this address is not exist");
        }

        // 返回一条地址记录（包装到AddressInfo中）
        List<AddressInfo.AddressItem> addressItems = Collections.singletonList(ecommerceAddress.toAddressItem());
        return new AddressInfo(
                ecommerceAddress.getId(),
                addressItems
        );
    }

    /**
     * <h2>通过 TableId （包含众多主键）获取用户地址信息</h2>
     */
    @Override
    public AddressInfo getAddressInfoByTableId(TableId tableId) {
        Stream<TableId.Id> isdStream = tableId.getIds().stream();
        List<Long> idLists = isdStream.map(TableId.Id::getId).collect(Collectors.toList());

        List<EcommerceAddress> ecommerceAddresses = ecommerceAddressDao.findAllById(idLists);
        // 如果更具TableId查找不存记录，则返回userId=-1，addressItems为空list的地址信息
        if (ecommerceAddresses.isEmpty()){
            return new AddressInfo(-1l, Collections.emptyList());
        }

        // 将查询到的记录转为地址结果AddressInfo.AddressItem
        List<AddressInfo.AddressItem> addressItems = ecommerceAddresses
                .stream()
                .map(EcommerceAddress::toAddressItem)
                .collect(Collectors.toList());

        return new AddressInfo(
                // 选择第一个记录的UserId
                ecommerceAddresses.get(0).getUserId(),
                addressItems);
    }
}
