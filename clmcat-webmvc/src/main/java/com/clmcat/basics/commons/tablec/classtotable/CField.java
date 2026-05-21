package com.clmcat.basics.commons.tablec.classtotable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clmcat.basics.commons.tablec.TableFieldType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CField {

	String name() default "";

	TableFieldType fieldType() default TableFieldType.NONE;
	
	int length() default -1;
	
	int decimalLength() default -1;

	boolean canNull() default true;

	String defaultValue() default "";

	String charsetName() default "";

	String comment() default "";
	
	/**
	 * 变更前的名字, 如果字段发生名字变更, 期望设置一下当前值.
	 */
	String beforeName() default "";

}
