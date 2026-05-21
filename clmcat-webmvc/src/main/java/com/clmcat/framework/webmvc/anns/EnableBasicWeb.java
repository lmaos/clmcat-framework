package com.clmcat.framework.webmvc.anns;

import com.clmcat.framework.webmvc.WebMvcConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangxingyu
 *
 * 启用基础的WEB功能。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Import(WebMvcConfiguration.class)
@Inherited
@Documented
public @interface EnableBasicWeb {

}
