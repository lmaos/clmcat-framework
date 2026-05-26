package com.clmcat.framework.webmvc.interceptor;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.framework.webmvc.anns.GetLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * locale 参数注入
 * 
 * @author zhangxingyu
 *
 */
public class LocaleParameterInjector implements HandlerMethodArgumentResolver {
	private static final String ATTR_USER_LOCALE = "userLocale";
	private static final String ATTR_USER_LANGUAGE = "userLanguage";
	private static final String HEADER_LOCALE = "locale";
	private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
	private static final Logger log = LoggerFactory.getLogger(LocaleParameterInjector.class);
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		return AnnotatedElementUtils.hasAnnotation(parameter.getParameter(), GetLocale.class) && 
				(parameter.getParameterType() == String.class
				|| parameter.getParameterType() == Locale.class
				);
	}
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		// RequestInterceptor 会优先写入 userLocale，这里先直接复用请求内的最终值。
		Locale userLocale = resolveRequestLocaleAttribute(webRequest);
		if (userLocale == null) {
			// 兼容未经过拦截器的直接调用场景。
			userLocale = resolveLocaleFromRequest(webRequest);
		}
		if (userLocale == null) {
			userLocale = resolveServletLocale(webRequest);
		}
		if (userLocale == null) {
			return null;
		}
		webRequest.setAttribute(ATTR_USER_LOCALE, userLocale, RequestAttributes.SCOPE_REQUEST);
		if (parameter.getParameterType() == Locale.class) {
			return userLocale;
		}
		if (StringUtils.isBlank(userLocale.toLanguageTag())) {
			return userLocale.getLanguage();
		}
		return userLocale.toLanguageTag();
	}

	private Locale resolveRequestLocaleAttribute(NativeWebRequest webRequest) {
		Locale locale = (Locale) webRequest.getAttribute(ATTR_USER_LOCALE, RequestAttributes.SCOPE_REQUEST);
		if (locale != null) {
			return locale;
		}
		return (Locale) webRequest.getAttribute(ATTR_USER_LANGUAGE, RequestAttributes.SCOPE_REQUEST);
	}

	private Locale resolveLocaleFromRequest(NativeWebRequest webRequest) {
		Locale locale = parseSingleLocale(readRequestValue(webRequest, ATTR_USER_LANGUAGE));
		if (locale != null) {
			return locale;
		}
		locale = parseSingleLocale(readRequestValue(webRequest, HEADER_LOCALE));
		if (locale != null) {
			return locale;
		}
		return parseAcceptLanguage(readRequestValue(webRequest, HEADER_ACCEPT_LANGUAGE));
	}

	private String readRequestValue(NativeWebRequest webRequest, String name) {
		String value = webRequest.getHeader(name);
		if (StringUtils.isBlank(value)) {
			value = webRequest.getParameter(name);
		}
		return value;
	}

	private Locale parseAcceptLanguage(String localeValue) {
		if (StringUtils.isBlank(localeValue)) {
			return null;
		}
		try {
			List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(localeValue);
			for (Locale.LanguageRange range : ranges) {
				String rangeValue = range.getRange();
				if ("*".equals(rangeValue)) {
					continue;
				}
				Locale locale = parseSingleLocale(rangeValue);
				if (locale != null) {
					return locale;
				}
			}
		} catch (IllegalArgumentException e) {
			log.debug("parse Accept-Language fail: {}", localeValue, e);
		}
		return null;
	}

	private Locale parseSingleLocale(String localeValue) {
		if (StringUtils.isBlank(localeValue)) {
			return null;
		}
		String normalized = localeValue.trim().replace('_', '-');
		int commaIndex = normalized.indexOf(',');
		if (commaIndex >= 0) {
			normalized = normalized.substring(0, commaIndex).trim();
		}
		int semicolonIndex = normalized.indexOf(';');
		if (semicolonIndex >= 0) {
			normalized = normalized.substring(0, semicolonIndex).trim();
		}
		if (normalized.isEmpty() || "*".equals(normalized)) {
			return null;
		}
		Locale locale = Locale.forLanguageTag(normalized);
		if (StringUtils.isBlank(locale.getLanguage())) {
			return null;
		}
		return locale;
	}

	private Locale resolveServletLocale(NativeWebRequest webRequest) {
		Locale locale = webRequest.getLocale();
		if (locale != null && StringUtils.isNotBlank(locale.getLanguage())) {
			return locale;
		}
		Enumeration<Locale> locales = webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class) != null
				? webRequest.getNativeRequest(jakarta.servlet.http.HttpServletRequest.class).getLocales()
				: null;
		if (locales != null) {
			while (locales.hasMoreElements()) {
				Locale candidate = locales.nextElement();
				if (candidate != null && StringUtils.isNotBlank(candidate.getLanguage())) {
					return candidate;
				}
			}
		}
		return null;
	}
}
