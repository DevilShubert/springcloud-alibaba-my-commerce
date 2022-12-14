package cn.lzr.ecommerce.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>用户账户金额信息</h1>
 * 既可表示用户剩余余额<br></br>
 * 也可以表示用于操作的余额（例如扣除金额实体）<br></br>
 * */
@ApiModel(description = "用户账户余额信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceInfo {
    @ApiModelProperty(value = "用户 id")
    private Long userId;

    @ApiModelProperty(value = "用户账户余额")
    private Long balance;
}
