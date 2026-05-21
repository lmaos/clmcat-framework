package com.clmcat.framework.webmvc.interceptor;

import java.util.Locale;

import com.clmcat.framework.webmvc.anns.GetLocale;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public interface GetLocaleHandler {

	public Locale resolveArgument(GetLocale getLocale, MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception;
}
