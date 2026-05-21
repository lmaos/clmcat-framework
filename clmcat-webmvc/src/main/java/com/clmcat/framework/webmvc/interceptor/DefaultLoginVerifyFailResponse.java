package com.clmcat.framework.webmvc.interceptor;

import com.clmcat.framework.webmvc.ResponseStatusAdapter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登陆验证失败的结果应答
 * 
 * @author zhangxingyu
 *
 */
public class DefaultLoginVerifyFailResponse implements LoginVerifyFailResponse, ApplicationContextAware {
	private final static DefaultLoginVerifyFailResponse defaultInstance = new DefaultLoginVerifyFailResponse();
	private final static String defaultBeanName = "defaultLoginVerifyFailResponse";
	
	public static LoginVerifyFailResponse getDefaultinstance(ApplicationContext applicationContext) {
		if (applicationContext == null) {
			return defaultInstance;
		}
		
		if (!applicationContext.containsBean(defaultBeanName)) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry)applicationContext;
			BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(DefaultLoginVerifyFailResponse.class)
			.getBeanDefinition();
			registry.registerBeanDefinition(defaultBeanName, beanDefinition);
		}
		return applicationContext.getBean(defaultBeanName, LoginVerifyFailResponse.class);
	}
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	@Override
	public void doApi(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
		try {
			ResponseStatusAdapter responseStatusAdapter = (ResponseStatusAdapter) request.getAttribute(ResponseStatusAdapter.KEY);
			responseStatusAdapter.noPermission().create().setApplicationContext(applicationContext).build(request).out(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void doPage(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
		doApi(request, response, handlerMethod);
	}
}
