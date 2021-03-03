package cn.zzs.entitybuilder;

import javax.annotation.Resource;

import cn.zzs.cmd.base.BaseOrderDeleteCmd;
import cn.zzs.domain.OrderDeleteE;
import cn.zzs.entity.Order;
import cn.zzs.service.OrderService;

/**
 * 订单删除实体构建器
 * @author zzs
 * @date 2021年3月2日 上午9:41:43
 */
// @Component
// @Scope("prototype")
public class OrderDeleteEntityBuilder extends AbstractFlexibleEntityBuilder<BaseOrderDeleteCmd, OrderDeleteE> {
    
    @Resource
    private OrderService orderService;
    
    @Override
    protected void defaultInit() {
        // 初始化待删除的订单
        initDeleteOrder();
        
        // 初始化操作人
        initUser();
    }
    
    

    private void initDeleteOrder() {
        BaseOrderDeleteCmd baseData = getBaseData();
        // 这里加个判断是为了避免重复赋值
        if(baseData.getDeleteOrder() == null) {
            // 获取订单并赋值
            Order order = orderService.get(baseData.getId());
            baseData.setDeleteOrder(order);
        }
    }




    private void initUser() {
        BaseOrderDeleteCmd baseData = getBaseData();
        // 这里加个判断是为了避免重复赋值
        if(baseData.getUser() == null) {
            // 获取用户并赋值
            // 省略代码······
        }
    }

    @Override
    protected boolean defaultValidate() {
        // 校验基础数据是否完整
        validateRequired();
        
        // 校验用户是否能够删除订单
        validateUser();
        
        // 校验订单本身是否允许删除
        validateOrder();
        
        // 校验订单关联对象是否允许删除
        validateOther();
        
        return true;
    }

    private void validateRequired() {
        // 省略代码······
    }



    private void validateUser() {
        // 省略代码······
    }



    private void validateOrder() {
        // 省略代码······
    }


    private void validateOther() {
        // 省略代码······
    }



    @Override
    protected void defaultBuild(OrderDeleteE entity) {
        // 构建订单本身的数据
        // 省略代码······
    }

    @Override
    protected void finalExtend(OrderDeleteE entity) {
        // 省略代码······ 
    }

    @Override
    protected OrderDeleteE getSourceEntity() {
        return new OrderDeleteE();
    }

}
