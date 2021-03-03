package cn.zzs.entitybuilder.validator;

import java.util.ArrayList;
import java.util.List;

import cn.zzs.entitybuilder.support.PrimarySupport;

/**
 * 校验器注册表 接口
 * @author zzs
 * @date 2020年6月9日 下午1:12:12
 */
public class ValidatorRegistry<T> {
    
    /**
     * 校验器注册表
     */
    private List<Validator<T>> validators = new ArrayList<>();


    public void registerValidator(Validator<T> validator) {
        if(validator != null) {
            validators.add(validator);
            // 按优先级排序
            sort(validators);
        }
    }

    public List<Validator<T>> getValidators() {
        return validators;
    }
    
    private void sort(List<? extends PrimarySupport> list) {
        list.sort((x, y) -> {
            return x.getPrimary() - y.getPrimary();
        });
    }
}
