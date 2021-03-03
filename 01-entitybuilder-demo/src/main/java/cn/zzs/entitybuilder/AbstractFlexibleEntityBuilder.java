package cn.zzs.entitybuilder;

import cn.zzs.entitybuilder.postprocessor.EntityBuilderPostProcessRegistry;
import cn.zzs.entitybuilder.postprocessor.EntityBuilderPostProcessor;
import cn.zzs.entitybuilder.validator.Validator;
import cn.zzs.entitybuilder.validator.ValidatorRegistry;

/**
 * 支持注册校验器和处理器 的实体构建器
 * <p>整个构建流程如下：
 * <p>{@link AbstractFlexibleEntityBuilder#defaultInit()}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link EntityBuilderPostProcessor#postAfterInit(Object)}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link AbstractFlexibleEntityBuilder#defaultValidate()}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link Validator#validate(Object)}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link EntityBuilderPostProcessor#postProcessBeforeBuild(Object, Object)}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link AbstractFlexibleEntityBuilder#defaultBuild(Object)}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link EntityBuilderPostProcessor#postProcessAfterBuild(Object, Object)}
 * <p>                      ||
 * <p>                      \/
 * <p>{@link AbstractFlexibleEntityBuilder#finalExtend(Object)}
 * @author zzs
 * @date 2021年3月1日 下午2:55:22
 * @param <T> 构建的结果对象
 * @param <R> 构建所需的基础数据
 */
public abstract class AbstractFlexibleEntityBuilder<T, R> extends AbstractEntityBuilder<T, R> {
    /**
     * 处理器 注册表
     */
    private EntityBuilderPostProcessRegistry<T, R> postProcessRegistry;

    /**
     * 校验器 注册表
     */
    private ValidatorRegistry<T> validatorRegistry;
    
    public void registerEntityBuilderPostProcessor(EntityBuilderPostProcessor<T, R> postProcessor) {
        postProcessRegistry.registerEntityBuilderPostProcessor(postProcessor);
    }

    public void registerValidator(Validator<T> validator) {
        validatorRegistry.registerValidator(validator);
    }

    @Override
    protected void doInit() {
        // 执行默认初始化
        defaultInit();
        // 执行执行器里的初始化
        applyPostProcessorAfterInit();
    }

    @Override
    protected boolean doValidate() {
        // 执行默认校验
        if(!defaultValidate()) {
            return false;
        }
        // 执行注册校验器里的校验
        if(!applyValidateBaseData()) {
            return false;
        }
        return true;
    }

    @Override
    protected R doBuild(R entity) {
        // 构建前执行处理器
        applyPostProcessorsBeforeBuild(entity);

        // 构建实体
        defaultBuild(entity);

        // 构建后执行处理器
        applyPostProcessorsAfterBuild(entity);

        // 对实体对象进行最后的改造
        finalExtend(entity);

        return entity;
    }

    private boolean applyValidateBaseData() {
        for(Validator<T> validator : validatorRegistry.getValidators()) {
            if(!validator.validate(getBaseData())) {
                return false;
            }
        }
        return true;
    }

    private void applyPostProcessorAfterInit() {
        postProcessRegistry.getEntityBuilderPostProcessors().forEach(x -> x.postAfterInit(getBaseData()));
    }

    private void applyPostProcessorsBeforeBuild(R entity) {
        postProcessRegistry.getEntityBuilderPostProcessors().forEach(x -> x.postProcessBeforeBuild(entity, getBaseData()));
    }

    private void applyPostProcessorsAfterBuild(R entity) {
        postProcessRegistry.getEntityBuilderPostProcessors().forEach(x -> x.postProcessAfterBuild(entity, getBaseData()));

    }


    protected abstract void defaultInit();

    protected abstract boolean defaultValidate();

    protected abstract void defaultBuild(R entity);

    protected abstract void finalExtend(R entity);
    
    
}
