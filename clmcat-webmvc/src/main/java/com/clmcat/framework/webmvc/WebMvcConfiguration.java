package com.clmcat.framework.webmvc;

import java.util.ArrayList;
import java.util.List;

import com.clmcat.framework.webmvc.error.ExceptionHandler;
import com.clmcat.framework.webmvc.interceptor.LocaleParameterInjector;
import com.clmcat.framework.webmvc.interceptor.RequestInterceptor;
import com.clmcat.framework.webmvc.interceptor.TokenParameterInjector;
import com.clmcat.framework.webmvc.interceptor.reqparam.RequestParamInjector;
import com.clmcat.framework.webmvc.result.CustomResponseHandler;
import com.clmcat.framework.webmvc.result.ComponentResponseHandler;
import com.clmcat.framework.webmvc.verify.LoginVerifyService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author zhangxingyu
 *
 * WEB 配置， 请求拦截器、应答拦截器、错误拦截器、参数。
 *
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer, SmartInitializingSingleton, ApplicationContextAware {

    
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
    // 请求拦截
    @Bean
    RequestInterceptor requestInterceptor() {
        return new RequestInterceptor();
    }
    // 登录验证
    @Bean
	LoginVerifyService loginVerifyService() {
        return new LoginVerifyService();
    }
    // json应答处理
    @Bean
    ComponentResponseHandler componentResponseHandler() {
        return new ComponentResponseHandler();
    }
    @Bean
    CustomResponseHandler customResponseHandler () {
    	return new CustomResponseHandler();
    }
    // 错误拦截器
    @Bean
    ExceptionHandler exceptionHandler() {
        return new ExceptionHandler();
    }
    // token 参数注入
    @Bean
	TokenParameterInjector tokenParameterInjector() {
        return new TokenParameterInjector();
    }
    
    @Bean
    LocaleParameterInjector localeParameterInjector() {
    	return new LocaleParameterInjector();
    }

    @Bean
    RequestParamInjector requestParamInjector() {
    	return new RequestParamInjector();
    }

    @Bean
    @ConditionalOnMissingBean
    FormContentFilter formContentFilter() {
        return new FormContentFilter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor()).order(-1);
    }

    @Override
    public void afterSingletonsInstantiated() {
    	// TODO Auto-generated method stub
    	
//    }
//    @Override
//    public void afterPropertiesSet() {
    	// 请求映射处理适配器
    	RequestMappingHandlerAdapter requestMappingHandlerAdapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
    	// 参数处理
    	List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
    	List<HandlerMethodArgumentResolver> newArgumentResolvers = new ArrayList<HandlerMethodArgumentResolver>();
    	newArgumentResolvers.add(localeParameterInjector());
    	newArgumentResolvers.add(tokenParameterInjector());
        newArgumentResolvers.add(requestParamInjector());
    	newArgumentResolvers.addAll(argumentResolvers);
    	requestMappingHandlerAdapter.setArgumentResolvers(newArgumentResolvers);
    	// 结果处理
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newReturnValueHandlers = new ArrayList<>();
        newReturnValueHandlers.add(customResponseHandler());
        newReturnValueHandlers.add(componentResponseHandler());
        newReturnValueHandlers.addAll(returnValueHandlers);
        requestMappingHandlerAdapter.setReturnValueHandlers(newReturnValueHandlers);
    }
}
