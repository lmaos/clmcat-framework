package com.clmcat.basics.commons.tablec.classtotable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CPrimary {

	int sort() default 0;
	
	boolean autoIncrement() default false;

}
