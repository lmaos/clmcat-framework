package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
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
import com.clmcat.basics.commons.util.CodecUtils;
import com.clmcat.basics.commons.util.DefaultVal;
import com.clmcat.basics.commons.util.RSAUtil;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Params.ParamsAuthEncrypt;
import com.clmcat.framework.webmvc.anns.Params.ParamsScope;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSONObject;

public class RequestParamInjector implements HandlerMethodArgumentResolver, EnvironmentAware {
	private static final String CURRENT_BODY_ATTR = "currentBody";
	private static final String CURRENT_BODY_TEXT_ATTR = "currentBodyText";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_BODY_AUTH_NAME = "body-auth-name";
	private static final String HEADER_BODY_AUTH_ENCRYPT = "body-auth-encrypt";
	private static final String HEADER_BODY_AUTH_DECODE = "body-auth-decode";

	private static final Map<String, List<MethodWrapper>> setMethodCaches = new ConcurrentHashMap<>();
	private static final Map<String, String> authPasswordCache = new ConcurrentHashMap<>();

	private Environment environment;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(Params.class);
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Params params = getParams(parameter);
		if (shouldUseJsonResolver(params, webRequest)) {
			return applicationJson(parameter, mavContainer, webRequest, binderFactory);
		}
		return applicationForm(parameter, mavContainer, webRequest, binderFactory);
	}

	public Object applicationJson(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Params params = getParams(parameter);
		JSONObject jsonObject = getRequestJsonBody(webRequest, params);
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
		if (params.authEncrypt() != ParamsAuthEncrypt.NONE) {
			return true;
		}
		if (StringUtils.isNotBlank(webRequest.getHeader(HEADER_BODY_AUTH_NAME))) {
			return true;
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

	private JSONObject getRequestJsonBody(NativeWebRequest webRequest, Params params) throws Exception {
		JSONObject jsonObject = (JSONObject) webRequest.getAttribute(CURRENT_BODY_ATTR, RequestAttributes.SCOPE_REQUEST);
		if (jsonObject != null) {
			return jsonObject;
		}
		jsonObject = new JSONObject();
		String body = getRequestBody(webRequest);
		if (StringUtils.isNotBlank(body)) {
			body = bodyAuthDecipher(params, webRequest, body);
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
			value = parse(getParam(name, params, webRequest), type);
		} else {
			value = getJson(name, jsonObject, type);
		}
		if (value == null) {
			value = parse(getParam(name, params, webRequest), type);
		}
		return applyDefaultValue(name, type, params, value);
	}

	private Object resolveSimpleFormValue(String name, Class<?> type, Params params, NativeWebRequest webRequest)
			throws MissingServletRequestParameterException {
		Object value = parse(getParam(name, params, webRequest), type);
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
			Object fieldValue = parse(getParam(wrapper, params, webRequest), wrapper.getType());
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
			Object fieldValue = parse(getParam(wrapper, params, webRequest), wrapper.getType());
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

	private String bodyAuthDecipher(Params params, NativeWebRequest webRequest, String body) throws Exception {
		ParamsAuthEncrypt authEncrypt = resolveAuthEncrypt(params, webRequest.getHeader(HEADER_BODY_AUTH_ENCRYPT));
		if (authEncrypt == ParamsAuthEncrypt.NONE) {
			return body;
		}
		Charset charset = Charset.forName(params.authEncryptCharset());
		if (authEncrypt == ParamsAuthEncrypt.BASE64) {
			byte[] bodyBytes = Base64.getDecoder().decode(body.getBytes(charset));
			return new String(bodyBytes, charset);
		}
		ParamAuth auth = getParamAuth(params, authEncrypt, webRequest);
		String password = auth.getPassword();
		if (StringUtils.isBlank(password)) {
			ResponseStatus.AUTH_VERIFY_FAIL.throwResEx();
		}
		byte[] data = auth.decodeBody(body, charset);
		if (authEncrypt == ParamsAuthEncrypt.AES) {
			byte[] bodyBytes = CodecUtils.AES.decrypt(data, password);
			return new String(bodyBytes, charset);
		}
		PrivateKey privateKey = RSAUtil.getPrivateKey(password);
		byte[] bodyBytes = RSAUtil.decrypt(data, privateKey);
		return new String(bodyBytes, charset);
	}

	private ParamsAuthEncrypt resolveAuthEncrypt(Params params, String headerAuthEncrypt) {
		if (StringUtils.isBlank(headerAuthEncrypt)) {
			return params.authEncrypt();
		}
		if ("AES".equalsIgnoreCase(headerAuthEncrypt)) {
			return ParamsAuthEncrypt.AES;
		}
		if ("RSA".equalsIgnoreCase(headerAuthEncrypt)) {
			return ParamsAuthEncrypt.RSA;
		}
		if ("BASE64".equalsIgnoreCase(headerAuthEncrypt)) {
			return ParamsAuthEncrypt.BASE64;
		}
		return params.authEncrypt();
	}

	private String getParam(MethodWrapper wrapper, Params parentParams, NativeWebRequest webRequest) {
		Params params = wrapper.params;
		if (parentParams.scope() == ParamsScope.HEADER || parentParams.scope() == ParamsScope.COOKIE) {
			params = parentParams;
		}
		if (params == null) {
			params = parentParams;
		}
		return getParam(wrapper.paramName, params, webRequest);
	}

	private String getParam(String paramName, Params params, NativeWebRequest webRequest) {
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
		return webRequest.getParameter(paramName);
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

	private ParamAuth getParamAuth(Params params, ParamsAuthEncrypt authEncrypt, NativeWebRequest webRequest) {
		String authName = webRequest.getHeader(HEADER_BODY_AUTH_NAME);
		if (StringUtils.isBlank(authName)) {
			authName = params.authName();
		}
		String envName = "ns.params.auth." + authName + "." + authEncrypt.name().toLowerCase();
		String password = authPasswordCache.computeIfAbsent(buildAuthCacheKey(authEncrypt, authName),
				key -> environment == null ? null : environment.getProperty(envName + ".password"));
		String decode = webRequest.getHeader(HEADER_BODY_AUTH_DECODE);
		if (StringUtils.isBlank(decode) && environment != null) {
			decode = environment.getProperty(envName + ".decode", "base64");
		}
		if (StringUtils.isBlank(decode)) {
			decode = "base64";
		}
		ParamAuth paramAuth = new ParamAuth();
		paramAuth.setDecode(decode);
		paramAuth.setPassword(password);
		paramAuth.setCharset(Charset.forName(params.authEncryptCharset()));
		return paramAuth;
	}

	private String buildAuthCacheKey(ParamsAuthEncrypt authEncrypt, String authName) {
		return authEncrypt.name() + "-" + authName;
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
