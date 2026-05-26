package com.clmcat.framework.webmvc;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import com.clmcat.framework.webmvc.ResponseEntityKey.DefaultResponseEntityKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author zhangxingyu
 *
 * 应答实体构造
 */
public class ResponseEntityBuild {

	private static final Logger log = LoggerFactory.getLogger(ResponseEntityBuild.class);

	private Integer httpStatus;

	private String requestId; // 请求ID(请求携带才会有)

	private Integer status; // 应答的数字展示状态

	private String state; // 应答状态--文字展示的状态

	private Object content; // 应答内容

	private String message; // 应答消息

	private String errplace; // 错误位置

	private String callback; // 函数回调

	private String localeMessage; // 国际化的 messageKey
	
	private String localeSelect; // 国际化的 选择实例.

	private Object[] messageArgs;

	private ResponseEntityKey entityKey;

	private ApplicationContext applicationContext;
	
	private Locale locale;

	private ResponseEntityResultAdapter resultAdapter;
	
	private ResponseEntityBuild() {
	}

	public static final ResponseEntityBuild create() {
		return new ResponseEntityBuild();
	}

	public ResponseEntityBuild setRequestId(String requestId) {
		this.requestId = requestId;
		return this;
	}

	public ResponseEntityBuild setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
		return this;
	}

	public ResponseEntityBuild setStatus(Integer status) {
		this.status = status;
		return this;
	}

	public ResponseEntityBuild setState(String state) {
		this.state = state;
		return this;
	}

	public ResponseEntityBuild setContent(Object content) {
		this.content = content;
		return this;
	}

	public ResponseEntityBuild setMessage(String message) {
		this.message = message;
		return this;
	}

	public ResponseEntityBuild setErrplace(String errplace) {
		this.errplace = errplace;
		return this;
	}

	public ResponseEntityBuild setCallback(String callback) {
		this.callback = callback;
		return this;
	}

	public ResponseEntityBuild setEntityKey(ResponseEntityKey entityKey) {
		this.entityKey = entityKey;
		return this;
	}
	
	public ResponseEntityBuild setResultAdapter(ResponseEntityResultAdapter resultAdapter) {
		this.resultAdapter = resultAdapter;
		return this;
	}

	public ResponseEntityBuild setResponseEntity(ResponseEntityBuild entityBuild) {
		if (entityBuild.httpStatus != null) {
			setHttpStatus(entityBuild.httpStatus);
		}
		if (StringUtils.isNotBlank(entityBuild.state)) {
			setState(entityBuild.state);
		}
		if (StringUtils.isNotBlank(entityBuild.requestId)) {
			setRequestId(entityBuild.requestId);
		}
		if (entityBuild.content != null) {
			setContent(entityBuild.content);
		}
		if (StringUtils.isNotBlank(entityBuild.message)) {
			setMessage(entityBuild.message);
		}
		if (StringUtils.isNotBlank(entityBuild.errplace)) {
			setErrplace(entityBuild.errplace);
		}
		if (StringUtils.isNotBlank(entityBuild.callback)) {
			setCallback(entityBuild.callback);
		}
		return this;
	}

	public ResponseEntityBuild setResponseEntity(ResponseEntity entity) {
		if (entity.getHttpStatus() != null) {
			setHttpStatus(entity.getHttpStatus());
		}
		if (StringUtils.isNotBlank(entity.getState())) {
			setState(entity.getState());
		}
		if (StringUtils.isNotBlank(entity.getRequestId())) {
			setRequestId(entity.getRequestId());
		}
		if (entity.getContent() != null) {
			setContent(entity.getContent());
		}
		if (StringUtils.isNotBlank(entity.getMessage())) {
			setMessage(entity.getMessage());
		}
		if (StringUtils.isNotBlank(entity.getErrplace())) {
			setErrplace(entity.getMessage());
		}
		if (StringUtils.isNotBlank(entity.getCallback())) {
			setCallback(entity.getCallback());
		}
		return this;
	}

	public ResponseEntityBuild setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		return this;
	}

	public ResponseEntityBuild setLocaleMessage(String localeMessage) {
		this.localeMessage = localeMessage;
		return this;
	}
	/**
	 * 国际化 localeMessage 时候 选择用哪个实例进行.
	 * @param localeSelect
	 * @return
	 */
	public ResponseEntityBuild setLocaleSelect(String localeSelect) {
		this.localeSelect = localeSelect;
		return this;
	}

	public ResponseEntityBuild setMessageArgs(Object[] messageArgs) {
		this.messageArgs = messageArgs;
		return this;
	}
	
	public ResponseEntityBuild setLocale(Locale locale) {
		if (locale != null) {
			this.locale = locale;
		}
		return this;
	}
	public ResponseEntityBuild setLocale(String locale) {
		if (locale != null && locale.length() > 1) {
			this.locale = Locale.forLanguageTag(locale.replace("_", "-"));
		}
		return this;
	}

    public ResponseEntity build() {
		ResponseEntityKey entityKey;
		if (this.entityKey != null) {
			entityKey = this.entityKey;
		} else {
			entityKey = DefaultResponseEntityKey.defaultInstance; 
		}
		String message = getMessage(null);
		return new ResponseEntity(httpStatus, requestId, status, state, content, message, errplace, callback,
				localeMessage, entityKey, resultAdapter);
    }

	public ResponseEntity build(HttpServletRequest request) {
		if (request == null) {
			return build();
		}
		/// 默认语言不存在, 设置默认语言。
		if (this.locale == null) {
			this.locale = (Locale)request.getAttribute(ResponseInternationalization.DEFAULT_LOCALE);
			this.locale = this.locale == null ? Locale.ENGLISH : this.locale;
		}

		String callback = this.callback; 
		String requestId = this.requestId; 
		if (StringUtils.isBlank(requestId)) {
			requestId = getRequestId(request);
		}
		if (StringUtils.isBlank(callback)) {
			callback = getCallbackValue(request);
		}
		// 
		ResponseEntityKey entityKey;
		if (this.entityKey != null) {
			entityKey = this.entityKey;
		} else {
			entityKey = (ResponseEntityKey) request.getAttribute(ResponseEntityKey.KEY);
		}
		
		// 应答结果适配器
		ResponseEntityResultAdapter resultAdapter;
		if (this.resultAdapter != null) {
			resultAdapter = this.resultAdapter;
		} else {
			resultAdapter = (ResponseEntityResultAdapter) request.getAttribute(ResponseEntityResultAdapter.KEY);
		}
		
		

		String message = getMessage(request);

		return new ResponseEntity(httpStatus, requestId, status, state, content, message, errplace, callback,
				localeMessage, entityKey, resultAdapter);
	}
	
	public static String getRequestId(HttpServletRequest request) {
		try {
			String requestId = (String) request.getAttribute("requestId");
			if (StringUtils.isBlank(requestId)) {
				requestId = request.getParameter("requestId");
			}
			if (StringUtils.isBlank(requestId)) {
				requestId = request.getHeader("requestId");
			}
			return requestId;
		} catch (Exception e) {
			log.error("获得请求ID发生异常", e);
			return null;
		}
	}
	public static String getCallbackValue(HttpServletRequest request) {
		try {
			String callback = (String) request.getAttribute("callback"); // 先从属性获取
			if (StringUtils.isBlank(callback)) {
				callback = request.getParameter("callback");
			}
			if (StringUtils.isBlank(callback)) {
				callback = request.getHeader("callback");
			}
			return callback;
		} catch (Exception e) {
			log.error("获得Callback发生异常", e);
			return null;
		}
	}

	/**
	 * 国际化操作
	 * 
	 * @param request
	 * @return
	 */
	private Locale getLocale(HttpServletRequest request) {
		if (request == null) {
			return this.locale;
		}
		// RequestInterceptor 会先把当前请求最终使用的语言写到 userLocale。
		Locale locale = (Locale) request.getAttribute("userLocale");
		if (locale != null) {
			return locale;
		}
		locale = (Locale) request.getAttribute("userLanguage");
		if (locale != null) {
			request.setAttribute("userLocale", locale);
			return locale;
		}
		ResponseInternationalization ri =  (ResponseInternationalization)request.getAttribute(ResponseInternationalization.KEY);
		if (ri != null) {
			locale = ri.getLocale(request);
			if (locale != null) {
				request.setAttribute("userLocale", locale);
				return locale;
			}
		}
		locale = request.getLocale();
		if (locale != null && StringUtils.isNotBlank(locale.getLanguage())) {
			request.setAttribute("userLocale", locale);
			return locale;
		}
		if (this.locale != null) {
			return this.locale;
		}
		return (Locale) request.getAttribute(ResponseInternationalization.DEFAULT_LOCALE);
	}
	
	/**
	 * 获得国际化的 message
	 * 
	 * @return
	 */
	private String getMessage(HttpServletRequest request) {
		
		if (applicationContext == null) {
			return formatMessage();
		}
		if (localeMessage == null || localeMessage.isEmpty()) {
			return formatMessage();
		}
		Locale locale = getLocale(request);
		if (locale == null) {
			return formatMessage();
		}
		String message = parseLocaleMessage(request, locale, localeMessage, messageArgs);
		if (message == null) {
			message = applicationContext.getMessage(localeMessage, messageArgs, formatMessage(), locale);
		}
		return message;
	}

	private String formatMessage() {
		if (message == null || message.isEmpty()) {
			return message;
		}
		try {
			if (messageArgs != null && messageArgs.length > 0) {
				return String.format(message, messageArgs);
			}
		} catch (Exception e) {
			log.error("responseBuildFormatMessage-ERROR", e);
		}
		return message;
	}
	
	private String parseLocaleMessage(HttpServletRequest request, Locale locale, String localeMessage, Object... messageArgs) {
		// 解析国家化多语言
		ResponseInternationalization ri =  (ResponseInternationalization)request.getAttribute(ResponseInternationalization.KEY);
		if (ri != null) {
			ResponseInternationalization.Message message = new ResponseInternationalization.Message();
			message.setLocale(locale);
			message.setLocaleMessage(localeMessage);
			message.setMessageArgs(messageArgs);
			message.setRequest(request);
			return ri.convertResponseMessage(message);
		}
		return null;
	}

//	private static LocaleService LOCALE_SERVICE;
//
//	private LocaleService getLocaleService(ApplicationContext applicationContext) {
//		if (LOCALE_SERVICE == null) {
//			if (applicationContext != null && applicationContext.containsBean("localeService")) {
//				try {
//					LOCALE_SERVICE = applicationContext.getBean(LocaleService.class);
//				} catch (Exception e) {
//					log.error("", e);
//				}
//			}
//			if (LOCALE_SERVICE == null) {
//				LOCALE_SERVICE = LocaleService.EMPTY;
//			}
//		}
//		return LOCALE_SERVICE;
//	}
//
//	private String getLocaleMessageByLocaleService(Locale locale, String key, Object ...args) {
//		LocaleService localeService = getLocaleService(applicationContext);
//		if (localeService == LocaleService.EMPTY) {
//			return null;
//		}
//		return localeService.select(localeSelect).get(locale, key, args);
//	}
}
