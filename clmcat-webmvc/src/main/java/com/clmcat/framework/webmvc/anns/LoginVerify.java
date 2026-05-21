package com.clmcat.framework.webmvc.anns;


import com.clmcat.framework.webmvc.interceptor.LoginVerifyFailResponse;
import com.clmcat.framework.webmvc.verify.LoginVerifyFunction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记API需要登陆验证。 使用时可以通过业务的注解继承当前注解、并实现： loginVerify()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
public @interface LoginVerify {

    String token() default "token";
    String userId() default "userId";
    /**
     * 登陆错误输出应答的方案.
     * 
     * @return
     */
    Class<? extends LoginVerifyFailResponse> loginError() default LoginVerifyFailResponse.class;
    /**
     * 登陆验证的函数,如果有自己的验证方案,实现LoginVerifyFunction 接口,并注入容器. 在这里配置实现的类.
     * @return
     */
    Class<? extends LoginVerifyFunction> loginVerify() default LoginVerifyFunction.class;
    
    /**
	 * 是否必须登陆. 默认true 校验登陆状态 , 值等于 false 时 不进行校验登陆状态.
	 */
    boolean mustLogin() default true;

}
