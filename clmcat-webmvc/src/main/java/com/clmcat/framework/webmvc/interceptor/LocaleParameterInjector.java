package com.clmcat.framework.webmvc.interceptor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.basics.commons.util.CodecUtils.AES;
import com.clmcat.framework.webmvc.anns.GetLocale;
import com.clmcat.framework.webmvc.anns.GetLocale.HeaderEnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class LocaleParameterInjector implements HandlerMethodArgumentResolver , ApplicationContextAware{
	private ApplicationContext applicationContext;
	
	
	private static final Logger log = LoggerFactory.getLogger(LocaleParameterInjector.class);

	@Autowired(required = false)
	private LocaleParameterProperty localeParameterProperty;
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		return AnnotatedElementUtils.hasAnnotation(parameter.getParameter(), GetLocale.class) && 
				(parameter.getParameterType() == String.class
				|| parameter.getParameterType() == Locale.class
				);
	}

	@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		GetLocale getLocale = AnnotatedElementUtils.getMergedAnnotation(parameter.getParameter(), GetLocale.class);
		
		Locale userLocale = null;
		/**
		 * 自定义实现
		 */
		if (StringUtils.isNotBlank(getLocale.instanceBeanName())) {
			try {
				GetLocaleHandler getLocaleHandler = applicationContext.getBean(getLocale.instanceBeanName(), GetLocaleHandler.class);
				userLocale = getLocaleHandler.resolveArgument(getLocale, parameter, mavContainer, webRequest, binderFactory);
			} catch (Exception e) {
				log.error("getLocale instance = " + getLocale.instanceBeanName(), e);
			}
		}
		
		
		if (userLocale == null) {
			userLocale = (Locale) webRequest.getAttribute("userLocale", RequestAttributes.SCOPE_REQUEST);
		}
		
		if (userLocale == null && isEnableHeaderRead(getLocale)) {
			String localeNames[] = getLocale.value();
			for (String localeName : localeNames) {
				String localeValue = webRequest.getHeader(localeName);
				if (localeValue == null || localeName.isEmpty()) {
					if (getLocale.p()) {
						localeValue = webRequest.getParameter(localeName);
					}
				}
				if (localeValue != null && !localeValue.isEmpty()) {
					localeValue = decipher(localeValue); // 解密
					userLocale = Locale.forLanguageTag(localeValue.replace("_", "-"));
					break;
				}
			}
		}

		if (userLocale != null) {
			// 覆盖进去
			webRequest.setAttribute("userLocale", userLocale, RequestAttributes.SCOPE_REQUEST);
			if (parameter.getParameterType() == Locale.class) {
				return userLocale;
			} else {
				if (!getLocale.stringFull() || StringUtils.isBlank(userLocale.getCountry())) {
					return userLocale.getLanguage();
				} else {
					return userLocale.getLanguage() + "-" + userLocale.getCountry();
				}
			}
		} else {
			return null;
		}
	}

	private boolean isEnableHeaderRead(GetLocale getLocale) {
		boolean enable = true;
		if (this.localeParameterProperty != null) {
			enable = !localeParameterProperty.isDisabledHeader();
		}
		if (getLocale.headerEnable() != HeaderEnable.none) {
			// 强制指定当前请求是否走header获取
			enable = getLocale.headerEnable() == HeaderEnable.enable;
		}
		return enable;
	}
	
	private String decipher(String src) throws Exception {
		if (this.localeParameterProperty != null) {
			String encrypt = this.localeParameterProperty.getEncrypt();
			String decipherCipher = this.localeParameterProperty.getDecipherCipher();
			if (encrypt != null) {
				if ("aes".equalsIgnoreCase(encrypt)) {
					if (decipherCipher != null) {
						byte[] originalData = Base64.getDecoder().decode(src.getBytes());
						src = AES.decrypt(originalData, decipherCipher, StandardCharsets.UTF_8);
					}
				} else if ("base64".equalsIgnoreCase(encrypt)) {
					if (decipherCipher == null || decipherCipher.isEmpty()) {
						src = new String(Base64.getDecoder().decode(src), Charset.forName("UTF-8"));
					} else if ("url".equalsIgnoreCase(decipherCipher)) {
						src = new String(Base64.getUrlDecoder().decode(src), Charset.forName("UTF-8"));
					} else if ("mime".equalsIgnoreCase(decipherCipher)) {
						src = new String(Base64.getMimeDecoder().decode(src), Charset.forName("UTF-8"));
					} else {
						src = new String(Base64.getDecoder().decode(src), Charset.forName("UTF-8"));
					}
				}
			}
		}
		return src;
	}
}
