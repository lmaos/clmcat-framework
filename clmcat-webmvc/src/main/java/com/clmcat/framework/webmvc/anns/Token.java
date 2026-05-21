package com.clmcat.framework.webmvc.anns;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangxingyu
 *
 * 参数Token 标记注入。@Token long userId, @Token String token.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Inherited
@Documented
public @interface Token {
	
	String value() default "token";

	String userId() default "userId";

	/**
	 * 未登录的时候, 是否允许从参数查找用户ID
	 * 
	 * @return 默认允许
	 */
	boolean canParamGetUserId() default true;
}
