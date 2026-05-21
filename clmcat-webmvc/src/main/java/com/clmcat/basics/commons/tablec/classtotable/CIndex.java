package com.clmcat.basics.commons.tablec.classtotable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 索引, 索引的名字=prefix()+name() [默认=INDEX_xxxname]
 * @author zhangxingyu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(CIndexs.class)
public @interface CIndex {

	String name();

	int sort() default 0;
	
	String prefix() default "INDEX_";

}
