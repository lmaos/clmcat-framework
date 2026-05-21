package com.clmcat.basics.selectserver.selectenv.anns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clmcat.basics.selectserver.selectenv.condition.SelectEnvCondition;
import org.springframework.context.annotation.Conditional;


/**
 * 选择这个环境，bean 会被注入。
 * 
 * 
 * function.timer.{value}.enable=true
 * 
 * @author zhangxingyu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(SelectEnvCondition.class)
public @interface SelectEnv {
	String value();
}
