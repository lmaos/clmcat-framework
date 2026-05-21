package com.clmcat.framework.webmvc.verify;

import org.apache.commons.lang3.StringUtils;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 默认的登陆验证函数
 * 
 * @author zhangxingyu
 *
 */
public class DefaultLoginVerifyFunction implements LoginVerifyFunction {
	private final static DefaultLoginVerifyFunction defaultInstance = new DefaultLoginVerifyFunction();
	private final static String defaultBeanName = "defaultLoginVerifyFunction";
	public static LoginVerifyFunction getDefaultinstance(ApplicationContext applicationContext) {
		if (applicationContext == null) {
			return defaultInstance;
		}
		
		if (!applicationContext.containsBeanDefinition(defaultBeanName)) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry)applicationContext;
			BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(DefaultLoginVerifyFunction.class)
			.getBeanDefinition();
			registry.registerBeanDefinition(defaultBeanName, beanDefinition);
		}
		return applicationContext.getBean(defaultBeanName, LoginVerifyFunction.class);
	}

	@Override
	public boolean verifyLogin(LoginVerify loginVerify, HttpServletRequest request, HttpServletResponse response,
							   HandlerMethod handlerMethod) {
		// 需要校验

		String token = LoginVerifyService.getToken(request, loginVerify);

		// 登录失败
		boolean loginState = false;
		request.setAttribute(DefaultTokenCodec.TOKEN_STATUS_KEY, "0");
		if (StringUtils.isNotBlank(token)) {
			TokenInfo tokenInfo = DefaultTokenCodec.parse(token);
			if (tokenInfo != null && !tokenInfo.isInvalid()) {
				loginState = true; // 验证成功
				request.setAttribute(DefaultTokenCodec.TOKEN_INFO_KEY, tokenInfo);
			}
		}

		return loginState;
	}

	@Override
	public TokenInfo parseTokenInfo(HttpServletRequest request) {
		return (TokenInfo) request.getAttribute(DefaultTokenCodec.TOKEN_INFO_KEY);
	}

}
