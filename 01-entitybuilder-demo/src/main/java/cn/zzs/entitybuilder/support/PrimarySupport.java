package cn.zzs.entitybuilder.support;

/**
 * 
 * 优先级接口
 * @author zzs
 * @date 2020年6月11日 下午5:04:03
 */
public interface PrimarySupport {
    
    public static final int PRIMARY_01 = 1;
    
    
    public static final int PRIMARY_05 = 5;
    
    /**
     * 默认
     */
    public static final int PRIMARY_10 = 10;
    
    
    public static final int PRIMARY_15 = 15;
    
    
    default int getPrimary() {
        return PRIMARY_10;
    }
}
