package com.clmcat.basics.commons.aop;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
/** 代理 AOP 的切面
 * 
 * @author lmaos
 *
 */
public class ProxyPointcut implements Pointcut{
	protected ProxyPointcut() {
		classFilterFunction = clazz -> true;
		methodFilterFunction = (method, clazz) -> true;
	}

	protected Function<Class<?>, Boolean> classFilterFunction;
	protected BiFunction<Method, Class<?>, Boolean> methodFilterFunction;
	/** 切面
	 * 
	 * @param classFilter 类过滤 {@code (函数接口 Function<Class<?>, Boolean> ) }
	 * @param methodFilter 方法过滤 {@code (函数接口 BiFunction<Method, Class<?>, Boolean>) }
	 */
	public ProxyPointcut(Function<Class<?>, Boolean> classFilter, BiFunction<Method, Class<?>, Boolean> methodFilter) {
		this.classFilterFunction = classFilter;
		this.methodFilterFunction = methodFilter;
	}
	/** 类过滤
	 * 
	 * @param clazz 当前类 
	 * @return true 可以AOP，false 不可以
	 */
	protected boolean classFilter(Class<?> clazz) {
		if (classFilterFunction == null) {
			return true;
		}
		return classFilterFunction.apply(clazz);
	}
	/** 方法过滤
	 * 
	 * @param method 当前方法
	 * @param targetClass 方法处于的目标类型
	 * @return true 可以做AOP，false 不可以
	 */
	protected boolean methodFilter(Method method, Class<?> targetClass) {
		if (methodFilterFunction == null) {
			return true;
		}
		return methodFilterFunction.apply(method, targetClass);
	}
	
	@Override
	public ClassFilter getClassFilter() {
		// 
		return new ClassFilter() {
			@Override
			public boolean matches(Class<?> clazz) {
				return classFilter(clazz);
			}
		};
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return new MethodMatcher() {
			
			@Override
			public boolean matches(Method method, Class<?> targetClass, Object... args) {
				
				return matches(method, targetClass);
			}
			
			@Override
			public boolean matches(Method method, Class<?> targetClass) {
				return methodFilter(method, targetClass);
			}
			
			@Override
			public boolean isRuntime() {
				
				return true;
			}
		};
	}

}
