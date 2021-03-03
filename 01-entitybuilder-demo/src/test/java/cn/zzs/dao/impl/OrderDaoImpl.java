package cn.zzs.dao.impl;

import cn.zzs.dao.OrderDao;
import cn.zzs.domain.OrderDeleteE;
import cn.zzs.domain.OrderSaveE;
import cn.zzs.entity.Order;


/**
 * 订单持久化层 默认实现
 * @author zzs
 * @date 2021年3月3日 上午9:04:17
 */
// @Repository
public class OrderDaoImpl implements OrderDao {

    @Override
    // @Transactional
    public String save(OrderSaveE entity) {
        // 保存订单
        // 省略代码······
        
        // 保存产品
        // 省略代码······
        
        
        // 保存附件
        // 省略代码······
        
        
        // 执行保存事务中的函数
        entity.getSaveConsumers().forEach(x -> x.accept(entity));
        
        return entity.getId();
    }

    @Override
    // @Transactional
    public String logicDelete(OrderDeleteE entity) {
        // 删除订单
        // 省略代码······
        
        // 删除产品
        // 省略代码······
        
        
        // 删除附件
        // 省略代码······
        
        
        // 执行删除事务中的函数
        entity.getDeleteConsumers().forEach(x -> x.accept(entity));
        
        return entity.getId();
    }

    @Override
    public Order get(String id) {
        // TODO Auto-generated method stub
        return null;
    }

}
