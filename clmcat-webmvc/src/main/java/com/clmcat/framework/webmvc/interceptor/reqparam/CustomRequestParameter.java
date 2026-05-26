package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

import com.alibaba.fastjson.JSONObject;

public class CustomRequestParameter {
	private static final String REQUEST_ATTRIBUTE = "CustomRequestParameter";
	private static final String PARAM_IP = "IP";
	private static final String PARAM_IP_LOWER = "ip";
	private static final String PARAM_CLIENT_IP = "clientIp";
	private static final String PARAM_CLIENT_IP_DASHED = "client-ip";
	private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String HEADER_X_REAL_IP = "X-Real-IP";
	private static final String HEADER_FORWARDED = "Forwarded";
	private static final String[] IP_HEADER_CANDIDATES = {
			HEADER_X_FORWARDED_FOR,
			HEADER_X_REAL_IP,
			HEADER_FORWARDED,
			"X-Original-Forwarded-For",
			"Proxy-Client-IP",
			"WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR",
			"HTTP_CLIENT_IP",
			"X-Cluster-Client-IP",
			"True-Client-IP",
			"CF-Connecting-IP",
			"Fastly-Client-IP"
	};

	Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	private HttpServletRequest request;
	
	public CustomRequestParameter(HttpServletRequest request) {
		super();
		this.request = request;
		initBuiltinParameters();
	}

	public static CustomRequestParameter getOrCreate(NativeWebRequest request) {
		return getOrCreate(request.getNativeRequest(HttpServletRequest.class));
	}
	
	public static CustomRequestParameter getOrCreate(HttpServletRequest request) {
		CustomRequestParameter obj = (CustomRequestParameter) request.getAttribute(REQUEST_ATTRIBUTE);
		if (obj == null) {
			obj = new CustomRequestParameter(request);
			request.setAttribute(REQUEST_ATTRIBUTE, obj);
		}
		return obj;
	}
	

	public CustomRequestParameter put(String name, Object value) {
		if (value != null) {
			parameters.put(name, value);
		}
		return this;
	}
	
	public CustomRequestParameter putIfAbsent(String name, Object value) {
		if (value != null) {
			parameters.putIfAbsent(name, value);
		}
		return this;
	}
	
	public String getParameter(String name) {
		Object value = parameters.get(name);
		if (value == null) {
			value = request.getParameter(name);
		}
		return value == null ? null : String.valueOf(value);
	}
	
	public Object getObject(String name) {
		Object value = parameters.get(name);
		return value;
	}
	
	public Map<String, Object> getAll() {
		return this.parameters;
	}

	public String getClientIp() {
		return getParameter(PARAM_CLIENT_IP);
	}
	
	public void fill(JSONObject jsonObject) {
		try {
			if (jsonObject != null && parameters != null) {
				jsonObject.putAll(parameters);
			}
		} catch (Exception e) {
			logger.error("参数填充发生一些故障! ", e);
		}
	}

	private void initBuiltinParameters() {
		String clientIp = resolveClientIp(request);
		putIfAbsent(PARAM_CLIENT_IP, clientIp);
		putIfAbsent(PARAM_CLIENT_IP_DASHED, clientIp);
		putIfAbsent(PARAM_IP, clientIp);
		putIfAbsent(PARAM_IP_LOWER, clientIp);
	}

	private String resolveClientIp(HttpServletRequest request) {
		for (String headerName : IP_HEADER_CANDIDATES) {
			String ip = resolveIpFromHeader(headerName, request.getHeader(headerName));
			if (ip != null) {
				return ip;
			}
		}
		return normalizeIp(request.getRemoteAddr());
	}

	private String resolveIpFromHeader(String headerName, String headerValue) {
		if (!StringUtils.hasText(headerValue)) {
			return null;
		}
		if (HEADER_FORWARDED.equalsIgnoreCase(headerName)) {
			return resolveForwardedIp(headerValue);
		}
		for (String token : headerValue.split(",")) {
			String ip = normalizeIp(token);
			if (ip != null) {
				return ip;
			}
		}
		return null;
	}

	private String resolveForwardedIp(String headerValue) {
		String[] entries = headerValue.split(",");
		for (String entry : entries) {
			String[] parts = entry.split(";");
			for (String part : parts) {
				String trimmed = part.trim();
				if (!trimmed.regionMatches(true, 0, "for=", 0, 4)) {
					continue;
				}
				String candidate = trimmed.substring(4).trim();
				String ip = normalizeIp(candidate);
				if (ip != null) {
					return ip;
				}
			}
		}
		return null;
	}

	private String normalizeIp(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		String normalized = value.trim();
		if ("unknown".equalsIgnoreCase(normalized)) {
			return null;
		}
		if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() > 1) {
			normalized = normalized.substring(1, normalized.length() - 1).trim();
		}
		if (normalized.startsWith("for=")) {
			normalized = normalized.substring(4).trim();
		}
		if (normalized.startsWith("[")) {
			int end = normalized.indexOf(']');
			if (end > 0) {
				return normalized.substring(1, end);
			}
		}
		if (normalized.chars().filter(ch -> ch == ':').count() == 1 && normalized.contains(".")) {
			int lastColon = normalized.lastIndexOf(':');
			if (lastColon > 0) {
				normalized = normalized.substring(0, lastColon);
			}
		}
		if ("0:0:0:0:0:0:0:1".equals(normalized)) {
			return "127.0.0.1";
		}
		return normalized;
	}
	
}
