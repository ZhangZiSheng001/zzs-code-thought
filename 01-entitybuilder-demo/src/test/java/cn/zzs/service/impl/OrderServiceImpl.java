package cn.zzs.service.impl;

import javax.annotation.Resource;

import cn.zzs.cmd.DefaultOrderDeleteCmd;
import cn.zzs.cmd.DefaultOrderSaveCmd;
import cn.zzs.cmd.base.BaseOrderDeleteCmd;
import cn.zzs.cmd.base.BaseOrderSaveCmd;
import cn.zzs.dao.OrderDao;
import cn.zzs.domain.OrderDeleteE;
import cn.zzs.domain.OrderSaveE;
import cn.zzs.entity.Order;
import cn.zzs.entitybuilder.AbstractFlexibleEntityBuilder;
import cn.zzs.entitybuilder.OrderDeleteEntityBuilder;
import cn.zzs.entitybuilder.OrderSaveEntityBuilder;
import cn.zzs.service.OrderService;


/**
 * 订单服务层 默认实现
 * @author zzs
 * @date 2021年3月2日 上午9:00:11
 */
// @Service
public class OrderServiceImpl implements OrderService {
    
    @Resource
    private OrderDao orderDao;

    @Override
    public String save(DefaultOrderSaveCmd cmd) {
        // 获取构建器
        AbstractFlexibleEntityBuilder<BaseOrderSaveCmd, OrderSaveE> entityBuilder = getSaveEntityBuilder();
        
        // 设置基础数据
        entityBuilder.setBaseData(cmd);
        
        // 对基础数据进行部分更改，例如设置保存事务中需要进行的操作
        cmd.addSaveConsumer(order -> {
            // 有的自定义操作需要放入保存事务，如果失败，订单数据也会回滚
            // 省略代码······
        });
        
        // 对构建器进行部分更改，例如注册处理器或检验器
        // entityBuilder.registerValidator(myOrderSaveValidator);
        // entityBuilder.registerEntityBuilderPostProcessor(myOrderSavePostProcessor);
        
        // 构建保存实体
        OrderSaveE entity = entityBuilder.build();
        
        // 持久化操作
        orderDao.save(entity);
        
        return entity.getId();
    }

    private OrderSaveEntityBuilder getSaveEntityBuilder() {
        return new OrderSaveEntityBuilder();
        
        // 如果是Spring，则通过beanFactory获取（注意，必须是多例对象）
        // return beanFactory.getBean(OrderSaveEntityBuilder.class);
    }

    @Override
    public String delete(DefaultOrderDeleteCmd cmd) {
        // 获取构建器
        AbstractFlexibleEntityBuilder<BaseOrderDeleteCmd, OrderDeleteE> entityBuilder = getDeleteEntityBuilder();
        
        // 设置基础数据
        entityBuilder.setBaseData(cmd);
        
        // 对基础数据进行部分更改，例如设置删除事务中需要进行的操作
        cmd.addDeleteConsumer(order -> {
            // 有的自定义操作需要放入删除事务，如果失败，订单数据也会回滚
            // 省略代码······
        });
        
        // 对构建器进行部分更改，例如注册处理器或检验器
        // 省略代码······
        
        // 构建保存实体
        OrderDeleteE entity = entityBuilder.build();
        
        // 持久化操作
        orderDao.logicDelete(entity);
        
        return entity.getId();
    }
    


    
    private OrderDeleteEntityBuilder getDeleteEntityBuilder() {
        return new OrderDeleteEntityBuilder();
        
        // 如果是Spring，则通过beanFactory获取（注意，必须是多例对象）
        // return beanFactory.getBean(OrderSaveEntityBuilder.class);
    }
    
    
    @Override
    public Order get(String id) {
        return orderDao.get(id);
    }
}
