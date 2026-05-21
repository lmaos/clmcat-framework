package com.clmcat.framework.webmvc.interceptor;

import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登陆验证失败的结果应答
 * @author zhangxingyu
 *
 */
public interface LoginVerifyFailResponse {
	

    public void doApi(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) ;
    public void doPage(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) ;
}
