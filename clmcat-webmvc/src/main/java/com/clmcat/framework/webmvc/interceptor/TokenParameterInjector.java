package com.clmcat.framework.webmvc.interceptor;

import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.framework.webmvc.verify.LoginVerifyFunction;
import com.clmcat.framework.webmvc.verify.LoginVerifyService;
import com.clmcat.framework.webmvc.verify.TokenInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author zhangxingyu
 *
 * token 参数注入的处理方式。
 */
public class TokenParameterInjector implements HandlerMethodArgumentResolver {
	@Autowired
	LoginVerifyService loginVerifyService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> parameterType = parameter.getParameterType();
		// 只支持这几种类型
		if (parameterType != TokenInfo.class && parameterType != Long.class && parameterType != long.class
				&& parameterType != String.class) {
			return false;
		}
		return parameter.hasParameterAnnotation(Token.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		Token tokenAnn = parameter.getParameterAnnotation(Token.class);
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

		Class<?> type = parameter.getParameterType();
		if (type == String.class) {
			return LoginVerifyService.getValue(request, tokenAnn.value());
		}

		LoginVerifyFunction loginVerifyFunction = loginVerifyService.getLoginVerifyFunction(request, parameter);

		TokenInfo tokenInfo = loginVerifyFunction.parseTokenInfo(request);
		
		if (tokenInfo != null) {
			if (TokenInfo.class.isAssignableFrom(type)) {
				return tokenInfo;
			}
			if (type == Long.class || type == long.class) {
				return tokenInfo.getUserId();
			}
		}
		
		// 从参数获取
		if (tokenAnn.canParamGetUserId() && (type == long.class || type == Long.class)) {
			String userId = webRequest.getParameter(tokenAnn.userId());
			if (userId != null && userId.length() > 0) {
				return Long.parseLong(userId);
			}
		}

		if (type == long.class) {
			return 0L;
		}
		if (type == int.class) {
			return 0L;
		}
		
		return null;
	}
}
