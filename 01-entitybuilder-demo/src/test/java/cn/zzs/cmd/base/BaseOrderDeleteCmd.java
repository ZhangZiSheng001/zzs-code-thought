package cn.zzs.cmd.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import cn.zzs.entity.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单删除所需基础数据
 * @author zzs
 * @date 2021年3月1日 下午4:34:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseOrderDeleteCmd extends BaseOrderRelatedObjCmd {
    /**
     * 待删除的订单
     */
    private Order deleteOrder;
    
    /**
     * 待删除的产品id列表
     */
    private List<String> deleteProductIds;
    
    /**
     * 待删除的订单附件id列表
     */
    private List<String> deleteAttachmentIds;
    
    /**
     * 需要放入删除事务中的执行的函数
     */
    private List<Consumer<Order>> deleteConsumers;
    
    public void addDeleteConsumer(Consumer<Order> consumer) {
        if(deleteConsumers == null) {
            deleteConsumers = new ArrayList<>();
        }
        deleteConsumers.add(consumer);
    }
    
}
