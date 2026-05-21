package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.Charset;
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
import org.apache.tomcat.util.http.fileupload.IOUtils;
import com.clmcat.basics.commons.util.CodecUtils;
import com.clmcat.basics.commons.util.DefaultVal;
import com.clmcat.basics.commons.util.RSAUtil;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Params.ParamsAuthEncrypt;
import com.clmcat.framework.webmvc.anns.Params.ParamsScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger log = LoggerFactory.getLogger(RequestParamInjector.class);
	private static final Map<Class<?>, List<MethodWrapper>> setMethodCaches = new ConcurrentHashMap<>();
	private static final Map<Class<?>, List<MethodWrapper>> setParamMethodCaches = new ConcurrentHashMap<>();

	private static final Map<String, ParamAuth> authMap = new ConcurrentHashMap<>();
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		boolean ok = parameter.hasParameterAnnotation(Params.class);
		return ok;
	}
	
	private Environment environment;
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
		
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		String method = httpServletRequest.getMethod();
		String contentType = webRequest.getHeader("Content-Type");
		String bodyAuthName = webRequest.getHeader("body-auth-name");
		Params params = AnnotatedElementUtils.getMergedAnnotation(parameter.getParameter(), Params.class);
		if (params.scope() == ParamsScope.HEADER || params.scope() == ParamsScope.COOKIE) {
			return applicationForm(parameter, mavContainer, webRequest, binderFactory);
		}
		if ("GET".equalsIgnoreCase(method)) {
			return applicationForm(parameter, mavContainer, webRequest, binderFactory);
		} else if (contentType != null && contentType.startsWith("application/json")) {
			return applicationJson(parameter, mavContainer, webRequest, binderFactory);
		} else if (StringUtils.isNotBlank(bodyAuthName)) { // 授权解析
			return applicationJson(parameter, mavContainer, webRequest, binderFactory);
		} else {
			return applicationForm(parameter, mavContainer, webRequest, binderFactory);
		}
	}

	public Object applicationJson(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		Params params = AnnotatedElementUtils.getMergedAnnotation(parameter.getParameter(), Params.class);
		String contentLength = webRequest.getHeader("Content-Length");
		JSONObject jsonobj = (JSONObject) webRequest.getAttribute("currentBody", RequestAttributes.SCOPE_REQUEST);
		if (jsonobj == null) {
			InputStream in = request.getInputStream();
			int length = NumberUtils.toInt(contentLength);
			if (length > 0) {
				byte[] data = new byte[length];
				IOUtils.readFully(in, data, 0, data.length);
				String json = new String(data, Charset.forName("UTF-8")).trim();
				// 验证是否加密了 
				json = bodyAuthDecipher(params, webRequest, json);
				if (json.startsWith("{") && json.endsWith("}")) {
					jsonobj = JSONObject.parseObject(json);
				}
			}
			if (jsonobj == null) {
				jsonobj = new JSONObject();
			}
			webRequest.setAttribute("currentBody", jsonobj, RequestAttributes.SCOPE_REQUEST);
		}
		// // // // // // // // // // // // // // // // // /// // // // 
		CustomRequestParameter.getOrCreate(webRequest).fill(jsonobj);
		
		Class<?> type = parameter.getParameterType();

		if (DefaultVal.getConvertType(type) != null) {
			
			String name = params.name();
			if (name.isEmpty()) {
				name = parameter.getParameter().getName();
			}
			Object value = null;
			if (params.scope() != ParamsScope.PARAM && params.scope() != ParamsScope.NONE) { // header, cookie
				String param = getParam(name, params, webRequest);
				value = parse(param, type);
			} else {
				value = getJson(name, jsonobj, type);
			}
			
			if (value == null) { // 从参数获取.
//				String param = webRequest.getParameter(name);
				String param = getParam(name, params, webRequest);
				value = parse(param, type);
			}

			if (value == null) {

				if (!ValueConstants.DEFAULT_NONE.equals(params.defaultValue())) {
					value = parse(params.defaultValue(), type);
				} else if (params.required()) { //
					throw new MissingServletRequestParameterException(name, type.getSimpleName());
				} else {
					value = DefaultVal.getDefaultValue(type);
				}
			}

			// binderFactory.createBinder(webRequest, value, name).validate();

			return value;
		} else {
			// bean

			Object result = jsonobj.toJavaObject(type);
			
			List<MethodWrapper> methods = getSetParamMethods(type, null);
			for (MethodWrapper mw : methods) {
				if (mw.params != null) {
					Object fvalue = null;
					if (mw.params.scope() != ParamsScope.PARAM && mw.params.scope() != ParamsScope.NONE) {
						String param = getParam(mw, params, webRequest);
						fvalue = parse(param, mw.getType());
						if (fvalue != null) {
							mw.method.invoke(result, fvalue);
						}
					}
					
					if (params.required() && fvalue == null) {
						if (!mw.hasValue(result)) {
							throw new MissingServletRequestParameterException(mw.paramName, mw.getType().getSimpleName());
						}
					}
				}
			}
			
			return result;
		}
	}

	public Object applicationForm(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		// webRequest.setAttribute(null, binderFactory,
		// RequestAttributes.SCOPE_REQUEST);
		Class<?> type = parameter.getParameterType();
		Params params = AnnotatedElementUtils.getMergedAnnotation(parameter.getParameter(), Params.class);
		String name = params.name();
		if (DefaultVal.getConvertType(type) != null) {
			if (name.isEmpty()) {
				name = parameter.getParameterName();
			}
			
//			String param = webRequest.getParameter(name);
			String param = getParam(name, params, webRequest);
			Object value = parse(param, type);
			if (value == null) {

				if (!ValueConstants.DEFAULT_NONE.equals(params.defaultValue())) {
					value = parse(params.defaultValue(), type);
				} else if (params.required()) { //
					throw new MissingServletRequestParameterException(name, type.getSimpleName());
				} else {
					value = DefaultVal.getDefaultValue(type);
				}
			}
			return value;
		} else {
			Constructor<?> constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			Object value = constructor.newInstance();
			List<MethodWrapper> methods = getSetMethods(type, name);
			for (MethodWrapper mw : methods) {
				// String paramName = mw.paramName;
				Method method = mw.method;
//				String param = webRequest.getParameter(paramName);
				String param = getParam(mw, params, webRequest);
				Params fparams = mw.params; // 字段的参数注解.
				Class<?> ftype = method.getParameterTypes()[0];
				Object fvalue = parse(param, ftype);
				if (fvalue == null) {
					if (fparams == null) {
						// fvalue = DefaultVal.getDefaultValue(ftype);
					} else if (!ValueConstants.DEFAULT_NONE.equals(fparams.defaultValue())) {
						fvalue = parse(fparams.defaultValue(), ftype);
					} else if (fparams.required()) { //
						throw new MissingServletRequestParameterException(mw.paramName, ftype.getSimpleName());
					} else {
						// fvalue = DefaultVal.getDefaultValue(ftype);
					}
				}
				if (fvalue != null) {
					method.invoke(value, fvalue);
				}
				
			}

			return value;
		}
	}
	
	private String bodyAuthDecipher(Params fparams, NativeWebRequest webRequest,  String body) throws Exception {
		if (fparams.authEncrypt() == ParamsAuthEncrypt.NONE) {
			return body;
		}
		
		ParamsAuthEncrypt authEncrypt = fparams.authEncrypt();
		String headerAuthEncrypt = webRequest.getHeader("body-auth-encrypt"); // 加密方式
		if (StringUtils.isNotBlank(headerAuthEncrypt)) {
			if ("AES".equalsIgnoreCase(headerAuthEncrypt)) {
				authEncrypt = ParamsAuthEncrypt.AES;
			} else if ("RSA".equalsIgnoreCase(headerAuthEncrypt)) {
				authEncrypt = ParamsAuthEncrypt.RSA;
			}
		}
		if (authEncrypt == ParamsAuthEncrypt.AES) {
			ParamAuth auth = getParamAuth(fparams, authEncrypt, webRequest);
			Charset charset = Charset.forName(fparams.authEncryptCharset());
			String password = auth.getPassword();
			// 权限验证失败.
			if (auth == null || StringUtils.isBlank(password)) {
				ResponseStatus.AUTH_VERIFY_FAIL.throwResEx();
			}
			byte[] data = auth.decodeBody(body, charset);
			byte[] bodyBytes = CodecUtils.AES.decrypt(data, password);
			body = new String(bodyBytes, charset);
		} else if (authEncrypt == ParamsAuthEncrypt.BASE64) {
			Charset charset = Charset.forName(fparams.authEncryptCharset());
			byte[] bodyBytes = Base64.getDecoder().decode(body.getBytes(charset));
			body = new String(bodyBytes, charset);
		} else if (authEncrypt == ParamsAuthEncrypt.RSA) {
			ParamAuth auth = getParamAuth(fparams, authEncrypt, webRequest);
			Charset charset = Charset.forName(fparams.authEncryptCharset());
			String password = auth.getPassword();
			// 权限验证失败.
			if (auth == null || StringUtils.isBlank(password)) {
				ResponseStatus.AUTH_VERIFY_FAIL.throwResEx();
			}
			byte[] data = auth.decodeBody(body, charset);
			PrivateKey privateKey = RSAUtil.getPrivateKey(password);
			byte[] bodyBytes = RSAUtil.decrypt(data, privateKey);
			body = new String(bodyBytes, charset);
		}
		return body;
	}
	
	private String getParam(MethodWrapper mw, Params parentParams,NativeWebRequest webRequest) {
		Params fparams = mw.params;
		String paramName = mw.paramName;
		if (parentParams.scope() == ParamsScope.HEADER || parentParams.scope() == ParamsScope.COOKIE) {
			fparams = parentParams;
		}
		fparams = fparams == null ? parentParams : fparams;
		return getParam(paramName, fparams, webRequest);
	}
	
	private String getParam(String paramName, Params fparams, NativeWebRequest webRequest) {
		String value = null;
		ParamsScope scope = ParamsScope.PARAM;
		if (fparams != null && fparams.scope() != ParamsScope.NONE) {
			 scope = fparams.scope();
		}
		
		if (scope == ParamsScope.PARAM) {
			value = CustomRequestParameter.getOrCreate(webRequest).getParameter(paramName);
//			value = webRequest.getParameter(paramName);
		} else if (scope == ParamsScope.HEADER) {
			value = webRequest.getHeader(paramName);
		} else if (scope == ParamsScope.COOKIE) {
			Cookie[] cookies = webRequest.getNativeRequest(HttpServletRequest.class).getCookies();

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (paramName.equals(cookie.getName())) {
						value = cookie.getValue();
						break;
					}
				}
			}
		} else {
			value = webRequest.getParameter(paramName);
		}
		return value;
	}

	private List<MethodWrapper> getSetParamMethods(Class<?> type, String prefix) {
		List<MethodWrapper> methods = setParamMethodCaches.get(type);
		if (methods == null) {
			methods = new ArrayList<>();
			List<MethodWrapper> list = getSetMethods(type, prefix);
			for (MethodWrapper methodWrapper : list) {
				if (methodWrapper.params != null) {
					methods.add(methodWrapper);
				}
			}
			setParamMethodCaches.put(type, methods);
		}
		return methods;
		
	}
	private List<MethodWrapper> getSetMethods(Class<?> type, String prefix) {
		List<MethodWrapper> methods = setMethodCaches.get(type);
		if (methods == null) {
			methods = new ArrayList<MethodWrapper>();
			Set<String> set = new HashSet<String>();
			Class<?> targetType = type;
			Map<String, Field> fieldMap = new HashMap<>();
			while (targetType != null && targetType != Object.class) {
				Field fields[] = targetType.getDeclaredFields();
				for (Field field : fields) {
					fieldMap.putIfAbsent(field.getName(), field);
				}
				targetType = targetType.getSuperclass();
			}
			targetType = type;
			while (targetType != null && targetType != Object.class) {
				Method[] ms = targetType.getDeclaredMethods();
				for (Method method : ms) {
					int mod = method.getModifiers();
					if (Modifier.isPublic(mod) && !Modifier.isAbstract(mod) && method.getName().startsWith("set")
							&& method.getParameterCount() == 1) {
						if (set.add(method.getName())) {
							String paramName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
							if (prefix != null && StringUtils.isNotBlank(prefix)) {
								paramName = prefix + "." + paramName;
							}
							Params params = null;
							Field field = fieldMap.get(paramName);
							if (field != null) {
								params = AnnotatedElementUtils.getMergedAnnotation(field, Params.class);
								if (params != null && StringUtils.isNotBlank(params.name())) {
									paramName = params.name();
								}
							}
							methods.add(new MethodWrapper(field, method, paramName, params));
						}
					}
				}
				targetType = targetType.getSuperclass();
			}
			setMethodCaches.put(type, methods);
		}
		return methods;
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
		if (jsonObject == null) {
			return null;
		}
		int startIndex = 0;
		String nodeName = null;
		JSONObject node = jsonObject;
		for (int i = 0; i < name.length() && node != null; i++) {
			char c = name.charAt(i);
			if (c == '.') {
				nodeName = name.substring(startIndex, i);
				startIndex = i + 1;
				node = jsonObject.getJSONObject(nodeName);
			}
		}
		if (node == null) {
			return null;
		}
		if (nodeName == null) {
			nodeName = name;
		} else {
			nodeName = name.substring(startIndex);
		}
		if (!jsonObject.containsKey(nodeName)) {
			return null;
		}

		if (type == int.class || type == Integer.class) {
			return jsonObject.getInteger(nodeName);
		}
		if (type == long.class || type == Long.class) {
			return jsonObject.getLong(nodeName);
		}

		if (type == byte.class || type == Byte.class) {

			return jsonObject.getByte(nodeName);
		}
		if (type == short.class || type == Short.class) {

			return jsonObject.getByte(nodeName);
		}

		if (type == float.class || type == Float.class) {
			return jsonObject.getFloat(nodeName);
		}

		if (type == double.class || type == Double.class) {
			return jsonObject.getDouble(nodeName);
		}
		if (type == boolean.class || type == Boolean.class) {
			return jsonObject.getBoolean(nodeName);
		}
		if (type == String.class) {
			return jsonObject.getString(nodeName);
		}
		if (type == Date.class) {
			return jsonObject.getDate(nodeName);
		}
		if (type == BigDecimal.class) {
			return jsonObject.getBigDecimal(nodeName);
		}

		return jsonObject.get(nodeName);
	}
	private static class MethodWrapper {
		private final Field field;
		private final Method method;
		private final String paramName;
		private final Params params;
		public MethodWrapper(Field field, Method method, String paramName, Params params) {
			super();
			this.field = field;
			this.method = method;
			this.paramName = paramName;
			this.params = params;
			
		}
		public <T extends Annotation> T getAnnotation(Class<T> type) {
			T ann = null;
			if (field != null) {
				ann = field.getAnnotation(type);
			}
			
			if (ann == null) {
				ann = method.getAnnotation(type);
			}
			return ann;
		}
		
		public Class<?> getType() {
			if (field != null) {
				return field.getType();
			}
			return method.getParameterTypes()[0];
		}
		
		public boolean hasValue(Object result) {
			if (field == null) {
				return false;
			}
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			try {
				return field.get(result) != null;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	/**
	 * 参数鉴权
	 * @param fparams
	 * @param webRequest
	 * @return
	 */
	private ParamAuth getParamAuth(Params fparams, ParamsAuthEncrypt authEncrypt, NativeWebRequest webRequest) {
		// 授权名字
		String authName = webRequest.getHeader("body-auth-name");
		if (StringUtils.isBlank(authName)) {
			authName = fparams.authName();
		}
		String key = authEncrypt.name() + "-" + authName;
		ParamAuth paramAuth = authMap.get(key);
		if (paramAuth == null) {
			String decode = webRequest.getHeader("body-auth-decode");
			Charset charset = Charset.forName(fparams.authEncryptCharset());
			String evnName = "ns.params.auth." + authName + "." + authEncrypt.name().toLowerCase();
			String password = environment.getProperty(evnName + ".password");
			if (StringUtils.isBlank(decode)) {
				decode = environment.getProperty(evnName + ".decode", "base64");
			}
			paramAuth = new ParamAuth();
			paramAuth.setDecode(decode);
			paramAuth.setPassword(password);
			paramAuth.setCharset(charset);
			authMap.put(key, paramAuth);

		}
		return paramAuth;
	}
}
