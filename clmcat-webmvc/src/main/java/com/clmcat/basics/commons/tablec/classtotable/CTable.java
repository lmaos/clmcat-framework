package com.clmcat.basics.commons.tablec.classtotable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CTable {

	String name() default "";
	
	String dbname() default "";

	String charsetName() default "utf8";

	String comment() default "";
	
	boolean existDrop() default false;
	
	int splitLength() default 1;

}
