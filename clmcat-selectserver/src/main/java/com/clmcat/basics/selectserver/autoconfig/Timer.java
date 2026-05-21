package com.clmcat.basics.selectserver.autoconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.core.annotation.AliasFor;

/**
 * @author zhangxingyu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
public @interface Timer {

	@AliasFor("masterName")
	String value() default ""; // master name - 不存在则取一个

	@AliasFor("value")
	String masterName() default ""; // master name

	int delay() default 0;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	String cron() default "";

	String timeZone() default "";

}
