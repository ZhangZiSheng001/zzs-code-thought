package cn.zzs.entitybuilder;

import javax.annotation.Resource;

import cn.zzs.cmd.base.BaseOrderSaveCmd;
import cn.zzs.dao.OrderDao;
import cn.zzs.domain.OrderSaveE;

/**
 * 订单保存实体 构建器
 * @author zzs
 * @date 2021年3月2日 上午9:05:32
 */
//@Component
//@Scope("prototype")
public class OrderSaveEntityBuilder extends AbstractFlexibleEntityBuilder<BaseOrderSaveCmd, OrderSaveE> {
    
    @Resource
    private OrderDao orderDao;

    @Override
    protected void defaultInit() {
        // 初始化订单所属客户
        initCustomer();
        
        // 初始化订单所属门店
        initShop();
        
        // 初始化操作人
        initUser();
    }

    private void initCustomer() {
        BaseOrderSaveCmd baseData = getBaseData();
        // 这里加个判断是为了避免重复赋值
        if(baseData.getCustomer() == null) {
            // 获取客户并赋值
            // 省略代码······
        } 
    }

    private void initShop() {
        BaseOrderSaveCmd baseData = getBaseData();
        // 这里加个判断是为了避免重复赋值
        if(baseData.getShop() == null) {
            // 获取门店并赋值
            // 省略代码······
        }
    }

    private void initUser() {
        BaseOrderSaveCmd baseData = getBaseData();
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
        
        // 校验基础数据是否满足规则
        validateRule();
        
        // 校验基础数据是否合法
        validateLegal();
        
        // 校验基础数据中的关联对象
        validateOther();
        
        return true;
    }

    private void validateRequired() {
        BaseOrderSaveCmd baseData = getBaseData();
        // orderValidateDomainService.validateRequired(baseData);
    }

    private void validateRule() {
        BaseOrderSaveCmd baseData = getBaseData();
        // orderValidateDomainService.validateRule(cmd);
    }

    private void validateLegal() {
        BaseOrderSaveCmd baseData = getBaseData();
        // orderValidateDomainService.validateLegal(cmd);
    }

    private void validateOther() {
        BaseOrderSaveCmd baseData = getBaseData();
        // orderValidateDomainService.validateOther(cmd);
    }

    @Override
    protected void defaultBuild(OrderSaveE entity) {
        BaseOrderSaveCmd baseData = getBaseData();
        // 省略代码······
    }

    @Override
    protected void finalExtend(OrderSaveE entity) {
        BaseOrderSaveCmd baseData = getBaseData();
        // 省略代码······
        
    }

    @Override
    protected OrderSaveE getSourceEntity() {
        return new OrderSaveE();
    }

}
