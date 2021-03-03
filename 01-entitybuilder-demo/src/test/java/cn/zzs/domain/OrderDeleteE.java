package cn.zzs.domain;

import java.util.List;
import java.util.function.Consumer;

import cn.zzs.entity.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单删除对象
 * @author zzs
 * @date 2021年3月1日 下午4:42:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDeleteE extends Order {
    
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
}
