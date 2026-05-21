package com.clmcat.framework.webmvc.interceptor.reqparam;

import java.util.HashMap;
import java.util.Map;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.NativeWebRequest;

import com.alibaba.fastjson.JSONObject;

public class CustomRequestParameter {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	private HttpServletRequest request;
	
	public CustomRequestParameter(HttpServletRequest request) {
		super();
		this.request = request;
	}

	public static CustomRequestParameter getOrCreate(NativeWebRequest request) {
		return getOrCreate(request.getNativeRequest(HttpServletRequest.class));
	}
	
	public static CustomRequestParameter getOrCreate(HttpServletRequest request) {
		CustomRequestParameter obj = (CustomRequestParameter) request.getAttribute("CustomRequestParameter");
		if (obj == null) {
			obj = new CustomRequestParameter(request);
			request.setAttribute("CustomRequestParameter", obj);
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
	
	public void fill(JSONObject jsonObject) {
		try {
			if (jsonObject != null && parameters != null) {
				jsonObject.putAll(parameters);
			}
		} catch (Exception e) {
			logger.error("参数填充发生一些故障! ", e);
		}
	}
	
}
