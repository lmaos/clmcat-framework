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
}
