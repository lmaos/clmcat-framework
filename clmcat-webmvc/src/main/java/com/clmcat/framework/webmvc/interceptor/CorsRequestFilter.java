package com.clmcat.framework.webmvc.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
public class CorsRequestFilter implements Filter {
    @Autowired(required = false)
    CorsConfig corsConfig = CorsConfig.DefaultCorsConfig.defaultInstance;
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        corsConfig.setCors((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) servletRequest).getMethod())) {
            ((HttpServletResponse)servletResponse).setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 标准
            return; // 直接截断，不走后续过滤器/控制器
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("RequestFilter 初始化完成");
    }
}
