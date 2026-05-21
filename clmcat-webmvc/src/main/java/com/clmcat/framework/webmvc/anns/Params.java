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
	
	
	/**
	 * BODY 进行授权加密方式请求, 授权的配置名 (只支持POST提交的方式)
	 * 
	 * ns.params.auth.default.aes: 
	     password: 密钥
         decode:   数据解码
	 * ns.params.auth.default.rsa: 
         password: 私钥
         decode:   数据解码
	 */
	String authName() default "default";
	/**
	 * BODY 进行授权加密方式请求 (只支持POST提交的方式)
	 */
	ParamsAuthEncrypt authEncrypt() default ParamsAuthEncrypt.NONE;
	
	String authEncryptCharset() default "UTF-8";
	
	public static enum ParamsScope {
		PARAM, HEADER, COOKIE, NONE;
	}
	
	public static enum ParamsAuthEncrypt {
		NONE, AES, BASE64, RSA
	}
	
}
