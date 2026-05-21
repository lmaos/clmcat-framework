package com.clmcat.framework.webmvc.protobuf.param;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author zhangxingyu
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.PARAMETER)
public @interface ProtoRequestBody {

}
