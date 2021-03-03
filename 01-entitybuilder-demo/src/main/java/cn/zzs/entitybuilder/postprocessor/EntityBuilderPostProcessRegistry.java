package cn.zzs.entitybuilder.postprocessor;

import java.util.ArrayList;
import java.util.List;

import cn.zzs.entitybuilder.support.PrimarySupport;

/**
 * 实体构建器处理器 注册表
 * @author zzs
 * @date 2020年6月9日 下午1:12:12
 */
public class EntityBuilderPostProcessRegistry<T, R> {
    
    /**
     * 处理器
     */
    private List<EntityBuilderPostProcessor<T, R>> postProcessors = new ArrayList<>();

    public void registerEntityBuilderPostProcessor(EntityBuilderPostProcessor<T, R> postProcessor) {
        if(postProcessor != null) {
            postProcessors.add(postProcessor);
            // 按优先级排序
            sort(postProcessors);
        }
    }

    public List<EntityBuilderPostProcessor<T, R>> getEntityBuilderPostProcessors() {
        return postProcessors;
    }
    
    private void sort(List<? extends PrimarySupport> list) {
        list.sort((x, y) -> {
            return x.getPrimary() - y.getPrimary();
        });
    }
}
