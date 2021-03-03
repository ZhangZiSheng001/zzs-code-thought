package cn.zzs.entitybuilder.validator;

import cn.zzs.entitybuilder.support.PrimarySupport;

/**
 * 校验器通用接口
 * @author zzs
 * @date 2021年3月1日 下午3:47:09
 * @param <T>
 */
public interface Validator<T> extends PrimarySupport {
    
    boolean validate(T baseData);
}
