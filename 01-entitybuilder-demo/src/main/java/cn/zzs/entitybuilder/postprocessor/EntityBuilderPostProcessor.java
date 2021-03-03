package cn.zzs.entitybuilder.postprocessor;

import cn.zzs.entitybuilder.support.PrimarySupport;

/**
 * 实体构建器 处理器
 * @author zzs
 * @date 2021年3月1日 下午3:42:40
 * @param <T> 构建的结果对象
 * @param <R> 构建所需的基础数据
 */
public interface EntityBuilderPostProcessor<T, R> extends PrimarySupport {
    
    void postAfterInit(T baseData);
    
    void postProcessBeforeBuild(R entity, T baseData);
    
    void postProcessAfterBuild(R entity, T baseData);
}
