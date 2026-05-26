package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import com.clmcat.basics.commons.util.DefaultVal;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Params.ParamsScope;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSONObject;

public class RequestParamInjector implements HandlerMethodArgumentResolver {
	private static final String CURRENT_BODY_ATTR = "currentBody";
	private static final String CURRENT_BODY_TEXT_ATTR = "currentBodyText";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";

	private static final Map<String, List<MethodWrapper>> setMethodCaches = new ConcurrentHashMap<>();

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(Params.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Params params = getParams(parameter);
		if (params.scope() == ParamsScope.REQUEST) {
			return resolveRequestAttribute(parameter, params, webRequest);
		}
		if (shouldUseJsonResolver(params, webRequest)) {
			return applicationJson(parameter, mavContainer, webRequest, binderFactory);
		}
		return applicationForm(parameter, mavContainer, webRequest, binderFactory);
	}

	public Object applicationJson(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Params params = getParams(parameter);
		JSONObject jsonObject = getRequestJsonBody(webRequest);
		CustomRequestParameter.getOrCreate(webRequest).fill(jsonObject);

		Class<?> type = parameter.getParameterType();
		if (isSimpleType(type)) {
			return resolveSimpleJsonValue(parameter, params, webRequest, jsonObject);
		}
		return resolveJsonBean(type, params, webRequest, jsonObject);
	}

	public Object applicationForm(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Class<?> type = parameter.getParameterType();
		Params params = getParams(parameter);
		if (isSimpleType(type)) {
			String name = resolveParamName(parameter, params);
			return resolveSimpleFormValue(name, type, params, webRequest);
		}
		return resolveFormBean(type, beanPrefix(params), params, webRequest);
	}

	private boolean shouldUseJsonResolver(Params params, NativeWebRequest webRequest) throws Exception {
		if (params.scope() == ParamsScope.HEADER || params.scope() == ParamsScope.COOKIE) {
			return false;
		}
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			return false;
		}
		String method = request.getMethod();
		if ("GET".equalsIgnoreCase(method)) {
			return false;
		}
		if (!supportsRequestBody(method)) {
			return false;
		}
		String contentType = webRequest.getHeader(HEADER_CONTENT_TYPE);
		if (contentType != null && contentType.startsWith("application/json")) {
			return true;
		}
		if (isFormContentType(contentType)) {
			return false;
		}
		return looksLikeJsonBody(webRequest);
	}

	private boolean supportsRequestBody(String method) {
		return "POST".equalsIgnoreCase(method)
				|| "PUT".equalsIgnoreCase(method)
				|| "PATCH".equalsIgnoreCase(method)
				|| "DELETE".equalsIgnoreCase(method);
	}

	private JSONObject getRequestJsonBody(NativeWebRequest webRequest) throws Exception {
		JSONObject jsonObject = (JSONObject) webRequest.getAttribute(CURRENT_BODY_ATTR, RequestAttributes.SCOPE_REQUEST);
		if (jsonObject != null) {
			return jsonObject;
		}
		jsonObject = new JSONObject();
		String body = getRequestBody(webRequest);
		if (StringUtils.isNotBlank(body)) {
			if (body.startsWith("{") && body.endsWith("}")) {
				jsonObject = JSONObject.parseObject(body);
			}
		}
		webRequest.setAttribute(CURRENT_BODY_ATTR, jsonObject, RequestAttributes.SCOPE_REQUEST);
		return jsonObject;
	}

	private String getRequestBody(NativeWebRequest webRequest) throws Exception {
		String body = (String) webRequest.getAttribute(CURRENT_BODY_TEXT_ATTR, RequestAttributes.SCOPE_REQUEST);
		if (body != null) {
			return body;
		}
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			return null;
		}
		body = readRequestBody(request);
		if (body != null) {
			webRequest.setAttribute(CURRENT_BODY_TEXT_ATTR, body, RequestAttributes.SCOPE_REQUEST);
		}
		return body;
	}

	private String readRequestBody(HttpServletRequest request) throws Exception {
		try (InputStream in = request.getInputStream()) {
			byte[] data = in.readAllBytes();
			if (data.length == 0) {
				return null;
			}
			return new String(data, StandardCharsets.UTF_8).trim();
		}
	}

	private boolean looksLikeJsonBody(NativeWebRequest webRequest) throws Exception {
		String body = getRequestBody(webRequest);
		return StringUtils.isNotBlank(body) && body.startsWith("{") && body.endsWith("}");
	}

	private boolean isFormContentType(String contentType) {
		return contentType != null && (contentType.startsWith("application/x-www-form-urlencoded")
				|| contentType.startsWith("multipart/form-data"));
	}

	private Object resolveSimpleJsonValue(MethodParameter parameter, Params params, NativeWebRequest webRequest,
			JSONObject jsonObject) throws MissingServletRequestParameterException {
		Class<?> type = parameter.getParameterType();
		String name = resolveParamName(parameter, params);
		Object value = null;
		if (params.scope() != ParamsScope.PARAM && params.scope() != ParamsScope.NONE) {
			value = convertValue(getParamValue(name, params, webRequest), type);
		} else {
			value = getJson(name, jsonObject, type);
		}
		if (value == null) {
			value = convertValue(getParamValue(name, params, webRequest), type);
		}
		return applyDefaultValue(name, type, params, value);
	}

	private Object resolveSimpleFormValue(String name, Class<?> type, Params params, NativeWebRequest webRequest)
			throws MissingServletRequestParameterException {
		Object value = convertValue(getParamValue(name, params, webRequest), type);
		return applyDefaultValue(name, type, params, value);
	}

	private Object resolveJsonBean(Class<?> type, Params params, NativeWebRequest webRequest, JSONObject jsonObject)
			throws Exception {
		Object result = jsonObject.toJavaObject(type);
		if (result == null) {
			result = createInstance(type);
		}
		List<MethodWrapper> methods = getSetMethods(type, beanPrefix(params));
		for (MethodWrapper wrapper : methods) {
			Object fieldValue = convertValue(getParamValue(wrapper, params, webRequest), wrapper.getType());
			if (fieldValue != null) {
				invokeMethod(wrapper.method, result, fieldValue);
				continue;
			}
			if (wrapper.hasValue(result)) {
				continue;
			}
			if (wrapper.params != null) {
				fieldValue = resolveFieldDefaultValue(wrapper);
				if (fieldValue != null) {
					invokeMethod(wrapper.method, result, fieldValue);
					continue;
				}
			}
			if (params.required() && wrapper.params != null && !wrapper.hasValue(result)) {
				throw new MissingServletRequestParameterException(wrapper.paramName, wrapper.getType().getSimpleName());
			}
		}
		return result;
	}

	private Object resolveFormBean(Class<?> type, String prefix, Params params, NativeWebRequest webRequest)
			throws Exception {
		Object value = createInstance(type);
		List<MethodWrapper> methods = getSetMethods(type, prefix);
		for (MethodWrapper wrapper : methods) {
			Object fieldValue = convertValue(getParamValue(wrapper, params, webRequest), wrapper.getType());
			if (fieldValue == null) {
				fieldValue = resolveFieldDefaultValue(wrapper);
			}
			if (fieldValue != null) {
				invokeMethod(wrapper.method, value, fieldValue);
			}
		}
		return value;
	}

	private Object resolveFieldDefaultValue(MethodWrapper wrapper) throws MissingServletRequestParameterException {
		Params params = wrapper.params;
		if (params == null) {
			return null;
		}
		Class<?> type = wrapper.getType();
		if (!ValueConstants.DEFAULT_NONE.equals(params.defaultValue())) {
			return parse(params.defaultValue(), type);
		}
		if (params.required()) {
			throw new MissingServletRequestParameterException(wrapper.paramName, type.getSimpleName());
		}
		return null;
	}

	private Object applyDefaultValue(String name, Class<?> type, Params params, Object value)
			throws MissingServletRequestParameterException {
		if (value != null) {
			return value;
		}
		if (!ValueConstants.DEFAULT_NONE.equals(params.defaultValue())) {
			return parse(params.defaultValue(), type);
		}
		if (params.required()) {
			throw new MissingServletRequestParameterException(name, type.getSimpleName());
		}
		return DefaultVal.getDefaultValue(type);
	}

	private Object resolveRequestAttribute(MethodParameter parameter, Params params, NativeWebRequest webRequest) throws Exception {
		Class<?> type = parameter.getParameterType();
		String name = resolveParamName(parameter, params);
		Object requestValue = getRequestAttribute(name, webRequest);
		if (isSimpleType(type)) {
			Object value = convertValue(requestValue, type);
			return applyDefaultValue(name, type, params, value);
		}
		if (requestValue != null && type.isInstance(requestValue)) {
			return requestValue;
		}
		return resolveFormBean(type, beanPrefix(params), params, webRequest);
	}

	private Object getParamValue(MethodWrapper wrapper, Params parentParams, NativeWebRequest webRequest) {
		Params params = wrapper.params;
		if (parentParams.scope() == ParamsScope.HEADER
				|| parentParams.scope() == ParamsScope.COOKIE
				|| parentParams.scope() == ParamsScope.REQUEST) {
			params = parentParams;
		}
		if (params == null) {
			params = parentParams;
		}
		return getParamValue(wrapper.paramName, params, webRequest);
	}

	private Object getParamValue(String paramName, Params params, NativeWebRequest webRequest) {
		ParamsScope scope = params == null ? ParamsScope.PARAM : params.scope();
		if (scope == ParamsScope.NONE) {
			return webRequest.getParameter(paramName);
		}
		if (scope == ParamsScope.PARAM) {
			return CustomRequestParameter.getOrCreate(webRequest).getParameter(paramName);
		}
		if (scope == ParamsScope.HEADER) {
			return webRequest.getHeader(paramName);
		}
		if (scope == ParamsScope.COOKIE) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (paramName.equals(cookie.getName())) {
							return cookie.getValue();
						}
					}
				}
			}
			return null;
		}
		if (scope == ParamsScope.IP) {
			return CustomRequestParameter.getOrCreate(webRequest).getClientIp();
		}
		if (scope == ParamsScope.REQUEST) {
			return getRequestAttribute(paramName, webRequest);
		}
		return webRequest.getParameter(paramName);
	}

	private Object getRequestAttribute(String paramName, NativeWebRequest webRequest) {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null || StringUtils.isBlank(paramName)) {
			return null;
		}
		return request.getAttribute(paramName);
	}

	private void invokeMethod(Method method, Object target, Object value) throws Exception {
		if (!method.canAccess(target)) {
			method.setAccessible(true);
		}
		method.invoke(target, value);
	}

	private List<MethodWrapper> getSetMethods(Class<?> type, String prefix) {
		String cacheKey = buildMethodCacheKey(type, prefix);
		return setMethodCaches.computeIfAbsent(cacheKey, key -> List.copyOf(buildSetMethods(type, prefix)));
	}

	private List<MethodWrapper> buildSetMethods(Class<?> type, String prefix) {
		List<MethodWrapper> methods = new ArrayList<>();
		Set<String> set = new HashSet<>();
		Map<String, Field> fieldMap = new HashMap<>();
		Class<?> targetType = type;
		while (targetType != null && targetType != Object.class) {
			Field[] fields = targetType.getDeclaredFields();
			for (Field field : fields) {
				fieldMap.putIfAbsent(field.getName(), field);
			}
			targetType = targetType.getSuperclass();
		}
		targetType = type;
		while (targetType != null && targetType != Object.class) {
			Method[] declaredMethods = targetType.getDeclaredMethods();
			for (Method method : declaredMethods) {
				int mod = method.getModifiers();
				if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || !method.getName().startsWith("set")
						|| method.getParameterCount() != 1 || !set.add(method.getName())) {
					continue;
				}
				String fieldName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
				String paramName = fieldName;
				if (StringUtils.isNotBlank(prefix)) {
					paramName = prefix + "." + fieldName;
				}
				Field field = fieldMap.get(fieldName);
				Params params = null;
				if (field != null) {
					params = AnnotatedElementUtils.getMergedAnnotation(field, Params.class);
					if (params != null && StringUtils.isNotBlank(params.name())) {
						paramName = params.name();
					}
				}
				methods.add(new MethodWrapper(field, method, paramName, params));
			}
			targetType = targetType.getSuperclass();
		}
		return methods;
	}

	private String buildMethodCacheKey(Class<?> type, String prefix) {
		return type.getName() + "#" + StringUtils.defaultString(prefix);
	}

	private String beanPrefix(Params params) {
		return params == null ? null : params.name();
	}

	private Object parse(String value, Class<?> type) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		if (type == int.class || type == Integer.class) {
			return NumberUtils.toInt(value);
		}
		if (type == long.class || type == Long.class) {
			return NumberUtils.toLong(value);
		}
		if (type == byte.class || type == Byte.class) {
			return (byte) NumberUtils.toInt(value);
		}
		if (type == short.class || type == Short.class) {
			return (short) NumberUtils.toInt(value);
		}
		if (type == float.class || type == Float.class) {
			return (float) NumberUtils.toDouble(value);
		}
		if (type == double.class || type == Double.class) {
			return NumberUtils.toDouble(value);
		}
		if (type == boolean.class || type == Boolean.class) {
			return "true".equals(value);
		}
		if (type == String.class) {
			return value;
		}
		if (type == Date.class) {
			return new Date(NumberUtils.toLong(value));
		}
		if (type == Timestamp.class) {
			return new Timestamp(NumberUtils.toLong(value));
		}
		if (type == java.sql.Date.class) {
			return new java.sql.Date(NumberUtils.toLong(value));
		}
		if (type == BigDecimal.class) {
			return new BigDecimal(value);
		}
		return DefaultVal.getDefaultValue(type);
	}

	private Object convertValue(Object value, Class<?> type) {
		if (value == null) {
			return null;
		}
		if (type.isInstance(value)) {
			return value;
		}
		return parse(String.valueOf(value), type);
	}

	private Object getJson(String name, JSONObject jsonObject, Class<?> type) {
		if (jsonObject == null || StringUtils.isBlank(name)) {
			return null;
		}
		String[] parts = StringUtils.split(name, '.');
		if (parts == null || parts.length == 0) {
			return null;
		}
		JSONObject node = jsonObject;
		for (int i = 0; i < parts.length - 1; i++) {
			Object child = node.get(parts[i]);
			if (!(child instanceof JSONObject)) {
				return null;
			}
			node = (JSONObject) child;
		}
		String nodeName = parts[parts.length - 1];
		if (!node.containsKey(nodeName)) {
			return null;
		}
		if (type == int.class || type == Integer.class) {
			return node.getInteger(nodeName);
		}
		if (type == long.class || type == Long.class) {
			return node.getLong(nodeName);
		}
		if (type == byte.class || type == Byte.class) {
			return node.getByte(nodeName);
		}
		if (type == short.class || type == Short.class) {
			return node.getShort(nodeName);
		}
		if (type == float.class || type == Float.class) {
			return node.getFloat(nodeName);
		}
		if (type == double.class || type == Double.class) {
			return node.getDouble(nodeName);
		}
		if (type == boolean.class || type == Boolean.class) {
			return node.getBoolean(nodeName);
		}
		if (type == String.class) {
			return node.getString(nodeName);
		}
		if (type == Date.class) {
			return node.getDate(nodeName);
		}
		if (type == BigDecimal.class) {
			return node.getBigDecimal(nodeName);
		}
		return node.get(nodeName);
	}

	private boolean isSimpleType(Class<?> type) {
		return DefaultVal.getConvertType(type) != null;
	}

	private Params getParams(MethodParameter parameter) {
		return AnnotatedElementUtils.getMergedAnnotation(parameter.getParameter(), Params.class);
	}

	private String resolveParamName(MethodParameter parameter, Params params) {
		if (StringUtils.isNotBlank(params.name())) {
			return params.name();
		}
		String parameterName = parameter.getParameterName();
		if (StringUtils.isNotBlank(parameterName)) {
			return parameterName;
		}
		return parameter.getParameter().getName();
	}

	private Object createInstance(Class<?> type) throws Exception {
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	private static class MethodWrapper {
		private final Field field;
		private final Method method;
		private final String paramName;
		private final Params params;

		private MethodWrapper(Field field, Method method, String paramName, Params params) {
			this.field = field;
			this.method = method;
			this.paramName = paramName;
			this.params = params;
		}

		private Class<?> getType() {
			if (field != null) {
				return field.getType();
			}
			return method.getParameterTypes()[0];
		}

		private boolean hasValue(Object result) {
			if (field == null) {
				return false;
			}
			try {
				if (!field.canAccess(result)) {
					field.setAccessible(true);
				}
				return field.get(result) != null;
			} catch (Exception e) {
				return false;
			}
		}
	}
}
