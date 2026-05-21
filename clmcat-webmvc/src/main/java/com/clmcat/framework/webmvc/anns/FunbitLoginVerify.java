package com.clmcat.framework.webmvc.anns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rainyhao
 * @since 2021/6/2 下午7:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@LoginVerify(token = "AccessToken")
public @interface FunbitLoginVerify {
}
