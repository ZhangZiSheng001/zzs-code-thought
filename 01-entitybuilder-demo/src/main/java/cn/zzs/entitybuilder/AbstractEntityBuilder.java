package cn.zzs.entitybuilder;

/**
 * 实体构建器抽象类
 * @author zzs
 * @date 2021年3月1日 下午2:51:53
 * @param <T> 构建的结果对象
 * @param <R> 构建所需的基础数据
 */
public abstract class AbstractEntityBuilder<T, R> implements EntityBuilder<T, R> {

    private T baseData;

    private boolean initialized;

    @Override
    public T getBaseData() {
        return baseData;
    }

    @Override
    public void setBaseData(T baseData) {
        this.baseData = baseData;
    }

    @Override
    public boolean validate() {
        // 判断基础数据是否已初始化
        if(!initialized) {
            init();
        }
        return doValidate();
    }
    
    @Override
    public R build() {
        // 校验
        if(!validate()) {
            throw new RuntimeException("baseData is not allowed to build entity!!!");
        }
        // 获取构建对象
        R entity = getSourceEntity();
        // 进行构建操作
        return doBuild(entity);
    }
    
    private void init() {
        doInit();
        this.initialized = true;
    }

    protected abstract void doInit();

    protected abstract boolean doValidate();

    protected abstract R getSourceEntity();
    
    protected abstract R doBuild(R entity);
}
