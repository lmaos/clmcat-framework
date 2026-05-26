package com.clmcat.framework.webmvc.anns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.ValueConstants;

/**
 * 根据 contentType 进行参数选择获取
 * 
 * @author zhangxingyu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface Params {
	
	@AliasFor("name")
	String value() default "";

	@AliasFor("value")
	String name() default "";

	boolean required() default true;

	String defaultValue() default ValueConstants.DEFAULT_NONE;

	ParamsScope scope() default ParamsScope.PARAM;
	
	public static enum ParamsScope {
		PARAM, HEADER, COOKIE, IP, REQUEST, NONE;
	}
}
