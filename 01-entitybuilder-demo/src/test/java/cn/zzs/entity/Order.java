package cn.zzs.entity;

import java.util.Date;

import lombok.Data;

/**
 * 订单 实体
 * @author zzs
 * @date 2021年3月1日 下午4:39:35
 */
@Data
public class Order {
    
    private String id;
    
    private String no;
    
    private String customerId;
    
    private String shopId;
    
    private Integer deleted;

    private String creator;
    private String creatorId;
    private Date createDt;

    private String lastUpdator;
    private String lastUpdatorId;
    private Date lastUpdateDt;
    
    // 其他字段······
}
