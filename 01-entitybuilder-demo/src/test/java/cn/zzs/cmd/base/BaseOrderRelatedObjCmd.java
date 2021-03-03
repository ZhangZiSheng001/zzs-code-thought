package cn.zzs.cmd.base;

import cn.zzs.entity.Customer;
import cn.zzs.entity.Order;
import cn.zzs.entity.Shop;
import cn.zzs.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单及其关联对象
 * 例如，订单所属的客户，订单所属门店，当前操作人等等，因为cmd会在实体构建器的整个生命周期中传递，所以这些关联对象只需要init一次，可以有效减少IO
 * @author zzs
 * @date 2021年3月1日 下午4:49:26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseOrderRelatedObjCmd extends Order {

    /**
     * 订单所属客户
     */
    private Customer customer;

    /**
     * 订单所属门店
     */
    private Shop shop;

    /**
     * 操作人
     */
    private User user;

    /**
     * 操作人id
     */
    private String userId;
    // 其他字段······
}
