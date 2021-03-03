package cn.zzs.domain;

import java.util.List;
import java.util.function.Consumer;

import cn.zzs.entity.Order;
import cn.zzs.entity.OrderAttachment;
import cn.zzs.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单 保存对象
 * @author zzs
 * @date 2021年3月1日 下午4:42:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSaveE extends Order {
    /**
     * 待新增的产品列表
     */
    private List<Product> insertProducts;
    
    /**
     * 待新增的订单附件列表
     */
    private List<OrderAttachment> insertAttachments;
    
    /**
     * 需要放入保存事务中的执行的函数
     */
    private List<Consumer<Order>> saveConsumers;
}
