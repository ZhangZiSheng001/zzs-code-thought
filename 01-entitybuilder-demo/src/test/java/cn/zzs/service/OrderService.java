package cn.zzs.service;

import cn.zzs.cmd.DefaultOrderDeleteCmd;
import cn.zzs.cmd.DefaultOrderSaveCmd;
import cn.zzs.entity.Order;

/**
 * 订单服务层 接口
 * @author zzs
 * @date 2021年3月1日 下午4:32:13
 */
public interface OrderService {
    
    String save(DefaultOrderSaveCmd cmd);
    
    String delete(DefaultOrderDeleteCmd cmd);
    
    Order get(String id);
}
