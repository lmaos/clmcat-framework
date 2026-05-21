package com.clmcat.framework.webmvc.anns;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从header 获取locale
 * 
 * @author zhangxingyu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Inherited
@Documented
public @interface GetLocale {

	String[] value() default { "userLocale", "locale", "Accept-Language" };

	/**
	 * 是否允许从参数查找locale
	 * 
	 * @return 默认允许
	 */
	boolean p() default true;

	/**
	 * 返回字符串时候是否使用完整命名. 默认不用.
	 */
	boolean stringFull() default false;

	/**
	 * 使用自定义的实例获取 实现 GetLocaleHandler
	 */
	String instanceBeanName() default "";
	/**
	 * 从header 获取的模式, 
	 * @return
	 */
	HeaderEnable headerEnable() default HeaderEnable.none;
	
	public static enum HeaderEnable {
		enable, disable, none
	}
}
