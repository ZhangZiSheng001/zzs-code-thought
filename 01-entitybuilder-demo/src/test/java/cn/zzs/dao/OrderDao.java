package cn.zzs.dao;

import cn.zzs.domain.OrderDeleteE;
import cn.zzs.domain.OrderSaveE;
import cn.zzs.entity.Order;

/**
 * 订单持久层 接口
 * @author zzs
 * @date 2021年3月2日 下午2:10:53
 */
public interface OrderDao {
    String save(OrderSaveE entity);
    
    String logicDelete(OrderDeleteE entity);
    
    Order get(String id);
}
