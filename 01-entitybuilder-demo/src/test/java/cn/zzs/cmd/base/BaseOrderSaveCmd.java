package cn.zzs.cmd.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import cn.zzs.entity.Order;
import cn.zzs.entity.OrderAttachment;
import cn.zzs.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单保存所需的基础数据
 * @author zzs
 * @date 2021年3月1日 下午4:34:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseOrderSaveCmd extends BaseOrderRelatedObjCmd {
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
    
    
    public void addSaveConsumer(Consumer<Order> consumer) {
        if(saveConsumers == null) {
            saveConsumers = new ArrayList<>();
        }
        saveConsumers.add(consumer);
    }
    
}
