package com.clmcat.basics.commons.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
/** AOP后的方法拦截, 是一个函数接口 invoke(MethodInvocation invocation)
 * 
 * @author lmaos
 * 
 */
@FunctionalInterface
public interface AopMethodInterceptor extends MethodInterceptor{
	/** 调用执行方法，
	 * 
	 */
	Object invoke(MethodInvocation invocation) throws Throwable;
}
