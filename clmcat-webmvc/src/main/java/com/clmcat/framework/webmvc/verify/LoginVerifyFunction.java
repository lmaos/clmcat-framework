package com.clmcat.framework.webmvc.verify;

import com.clmcat.framework.webmvc.anns.LoginVerify;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author zhangxingyu
 *
 * 登陆验证的功能。
 */
public interface LoginVerifyFunction {


	public static String KEY = "LoginVerifyFunction";
 	/**
	 * verifyLogin 判断登陆验证状态， true 登陆成功。
	 * @param loginVerify 注解，标记Method ｜Controller。
	 * @param request 请求对象
	 * @param response 应答对象
	 * @param handlerMethod 控制处理器的方法
	 * @return true 登陆成功。false 权限失败。
	 */
	boolean verifyLogin(LoginVerify loginVerify, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod);

	/**
	 * 解析token信息。
	 */
	TokenInfo parseTokenInfo(HttpServletRequest request);
}
