package com.clmcat.framework.webmvc.verify;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.basics.commons.util.MapWrapper;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.error.ApiException;
import com.clmcat.framework.webmvc.error.ApiSystemException;
import com.clmcat.framework.webmvc.interceptor.DefaultLoginVerifyFailResponse;
import com.clmcat.framework.webmvc.interceptor.LoginVerifyFailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登陆验证处理服务
 * 
 * @author zhangxingyu
 *
 */
public class LoginVerifyService implements ApplicationContextAware {
	
	
	private static final Logger log = LoggerFactory.getLogger(LoginVerifyService.class);

	
	private MapWrapper<Method, LoginVerifyFunction> loginVerifyFunctions = new MapWrapper<>(new ConcurrentHashMap<>());
	private MapWrapper<Method, LoginVerifyFailResponse> loginVerifyFailResponses = new MapWrapper<>(new ConcurrentHashMap<>());

	private MapWrapper<Class<? extends LoginVerifyFunction>, LoginVerifyFunction> loginVerifyFunctionBeans = new MapWrapper<>(new ConcurrentHashMap<>());
	private MapWrapper<Class<? extends LoginVerifyFailResponse>, LoginVerifyFailResponse> loginVerifyFailResponseBeans = new MapWrapper<>(new ConcurrentHashMap<>());
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * 获得登陆验证功能
	 * @param request 请求
	 * @param methodParameter 方法参数
	 */
	public LoginVerifyFunction getLoginVerifyFunction(HttpServletRequest request, MethodParameter methodParameter) throws Exception {
		// 如果存在请求对象， 直接从请求获取当前请求使用的 ： LoginVerifyFunction
		if (request != null) {
			Object value = request.getAttribute(LoginVerifyFunction.KEY);
			if (value != null) {
				return (LoginVerifyFunction) value;
			}
		}

		// 从方法内解析
		return getLoginVerifyFunction(methodParameter);
	}

	public LoginVerifyFunction getLoginVerifyFunction(MethodParameter methodParameter) throws Exception {

		return getLoginVerifyFunction(methodParameter.getMethod());
	}

	public LoginVerifyFunction getLoginVerifyFunction(HandlerMethod handlerMethod) throws Exception {

		return getLoginVerifyFunction(handlerMethod.getMethod());
	}
	public LoginVerifyFunction getLoginVerifyFunction(Method method) throws Exception {
		// 方法处理器对应的验证函数
		return loginVerifyFunctions.get1(method, (m)->{
			
			LoginVerify loginVerify = AnnotatedElementUtils.getMergedAnnotation(m, LoginVerify.class);
			if (loginVerify == null) {
				loginVerify = AnnotatedElementUtils.getMergedAnnotation(m.getDeclaringClass(), LoginVerify.class);
			}
			
			if (loginVerify == null) {
				return DefaultLoginVerifyFunction.getDefaultinstance(applicationContext);
			}
			
			Class<? extends LoginVerifyFunction> funcClass = loginVerify.loginVerify();
			if (funcClass == LoginVerifyFunction.class || funcClass == DefaultLoginVerifyFunction.class) {
				return DefaultLoginVerifyFunction.getDefaultinstance(applicationContext);
			}
			
			// bean 构造缓存查询
			return loginVerifyFunctionBeans.get0(funcClass,()->{
				LoginVerifyFunction result = null;
				try {
					result = applicationContext.getBean(funcClass);
				} catch (Exception e) {
					
				}
				
				if (result == null) {
					try {
						BeanDefinitionRegistry registry = (BeanDefinitionRegistry)applicationContext;
						BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(funcClass)
						.getBeanDefinition();
						String beanName = funcClass.getSimpleName();
						beanName = beanName.substring(0,1).toLowerCase() + beanName.substring(1);
						registry.registerBeanDefinition(beanName, beanDefinition);
						result = applicationContext.getBean(beanName, funcClass);
					} catch (ApiException e) {
						throw e;
					} catch (Exception e) {
						log.error(funcClass.getName() + ", 验证函数创建失败!", e);
						throw new ApiSystemException("验证函数创建失败");
					}
				}
				
				return result;
			});
			
		});
	}

	
	public LoginVerifyFailResponse getLoginVerifyFailResponse(Method method) {
		return loginVerifyFailResponses.get1(method, m -> {
			LoginVerify loginVerify = AnnotatedElementUtils.getMergedAnnotation(m, LoginVerify.class);
			if (loginVerify == null) {
				loginVerify = AnnotatedElementUtils.getMergedAnnotation(m.getDeclaringClass(), LoginVerify.class);
			}
			if (loginVerify == null) {
				return DefaultLoginVerifyFailResponse.getDefaultinstance(applicationContext);
	        }
			Class<? extends LoginVerifyFailResponse> loginError = loginVerify.loginError();
			if (loginError == LoginVerifyFailResponse.class || loginError == DefaultLoginVerifyFailResponse.class) {
				return DefaultLoginVerifyFailResponse.getDefaultinstance(applicationContext);
	        }
			
			return  loginVerifyFailResponseBeans.get0(loginError, ()->{
				LoginVerifyFailResponse loginVerifyFailResponse = null;
                try {
                    if (applicationContext != null) {
                    	loginVerifyFailResponse = applicationContext.getBean(loginError);
                    }
                } catch (Exception e) { }
                
                if (loginVerifyFailResponse == null) {
                	try {
                		BeanDefinitionRegistry registry = (BeanDefinitionRegistry)applicationContext;
						BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(loginError)
						.getBeanDefinition();
						String beanName = loginError.getSimpleName();
						beanName = beanName.substring(0,1).toLowerCase() + beanName.substring(1);
						registry.registerBeanDefinition(beanName, beanDefinition);
						loginVerifyFailResponse = applicationContext.getBean(beanName, loginError);
					} catch (ApiException e) {
						throw e;
					} catch (Exception e) {
						log.error(loginError.getName() + ", 登陆失败结果创建失败!", e);
						throw new ApiSystemException("登陆失败结果创建失败");
					}
				}
				return loginVerifyFailResponse;
			});
		});
	}
	
	
	public static String getToken(HttpServletRequest request, LoginVerify loginVerify) {
		return getValue(request, loginVerify.token());
	}
	
	/**
	 * 从请求里获取某种值, 获取顺序 header->parameter->cookie
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getValue(HttpServletRequest request, String name) {
		String value = null;
		value = request.getHeader(name);
		if (StringUtils.isBlank(value)) {
			value = request.getParameter(name);
			if (StringUtils.isBlank(value)) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals(value)) {
							value = cookie.getValue();
							break;
						}
					}
				}
			}
		}

		return value;
	}

}
