package com.clmcat.basics.commons.aop;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
/** aop 代理，生成代理对象
 * 
 * @author Administrator
 *
 */
public class AopProxy {
	/** aop 代理
	 * 
	 * @param bean
	 * @param pointcut 切面
	 * @param methodInterceptor 方法拦截
	 * @return 返回代理对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T proxy(T bean, ProxyPointcut pointcut, AopMethodInterceptor methodInterceptor){
		return (T) proxyBean(bean, pointcut, methodInterceptor).getObject();
	}
	public static ProxyFactoryBean proxyBean(Object bean, ProxyPointcut pointcut, AopMethodInterceptor interceptor){
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(bean);
		proxyFactoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, interceptor));
		proxyFactoryBean.setOptimize(true);
		return proxyFactoryBean;
	}
	
	
}
