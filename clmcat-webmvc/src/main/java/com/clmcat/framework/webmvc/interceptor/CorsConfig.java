package com.clmcat.framework.webmvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CorsConfig {

	public void setCors(HttpServletRequest request, HttpServletResponse response);

	public final static DefaultCorsConfig defaultInstance = new DefaultCorsConfig(); 
	public static class DefaultCorsConfig implements CorsConfig {
		
		public void setCors(HttpServletRequest request, HttpServletResponse response) {
//			String origin = request.getHeader("Origin");
			String headers = request.getHeader("Access-Control-Request-Headers");
			headers = headers == null || headers.trim().isEmpty() ? "*" : headers; 
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Headers", headers);
			response.setHeader("Access-Control-Expose-Headers", headers);
			response.setHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST, PUT, DELETE");
			response.setHeader("Access-Control-Max-Age", "3600"); // 验证时间
			response.setHeader("Access-Control-Allow-Credentials", "true");
		}
	}
}
