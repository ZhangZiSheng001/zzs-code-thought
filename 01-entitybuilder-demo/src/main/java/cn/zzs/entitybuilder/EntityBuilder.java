package cn.zzs.entitybuilder;

/**
 * 实体构建器接口
 * <p> 可单纯校验，而不构建
 * @author zzs
 * @date 2021年3月1日 下午2:52:18
 * @param T 构建的结果对象
 * @param R 构建所需的基础数据
 */
public interface EntityBuilder<T, R> {
    
    T getBaseData();
    
    void setBaseData(T baseData);
    
    boolean validate();
    
    R build();
}
