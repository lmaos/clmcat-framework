package com.clmcat.framework.webmvc;

import com.clmcat.framework.webmvc.error.ApiException;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 应答错误状态的抽象
 * 
 * @author zhangxingyu
 *
 */
public interface ResponseErrorStatus {

	/**
	 * HTTP 状态码
	 * 
	 */
	int getHttpStatus();

	/**
	 * 枚举状态名
	 */
	String getState();

	/**
	 * int 状态
	 */
	Integer getStatus();

	/**
	 * 错误消息
	 */
	String getMessage();

	/**
	 * 描述--并不会展示到输出结果种,只是定义.
	 * 
	 * @return
	 */
	default String getDescribe() {
		return null;
	};

	/**
	 * 国际化消息
	 */
	default String getLocaleMessage() {
		return getState();
	}

	default ResponseEntityBuild create() {
		return ResponseEntityBuild.create() // build
				.setHttpStatus(getHttpStatus()) // http 状态
				.setStatus(getStatus()) // 状态
				.setState(getState()) // 枚举
				.setMessage(getMessage()) // 错误内容
				.setLocaleMessage(getLocaleMessage()); // message 国际化代码
	}

	/**
	 * 转换为结果异常
	 */
	default ApiResultException resEx() {
		return new ApiResultException(this);
	}

	/**
	 * 转换为api异常
	 */
	default ApiException apiEx() {
		return new ApiException(this);
	}

	/**
	 * 直接抛出结果异常--中断在这一行
	 */
	default void throwResEx(Object... messageArgs) throws ApiResultException {
		throw resEx().setMessageArgs(messageArgs);
	}

	/**
	 * 直接抛出Api异常--中断在这一行
	 */
	default void throwApiEx(Object... messageArgs) throws ApiException {
		throw apiEx().setMessageArgs(messageArgs);
	}
	
	/**
	 * 
	 * @param condition 满足这样的一个条件才允许抛出异常
	 * @param messageArgs 异常需要的参数
	 * @throws ApiResultException
	 */
	default void assertThrowResEx(boolean condition, Object... messageArgs) throws ApiResultException {
		if (condition) {
			throw resEx().setMessageArgs(messageArgs);
		}
	}
	
	/**
	 * 
	 * @param condition 满足这样的一个条件才允许抛出异常
	 * @param messageArgs 异常需要的参数
	 * @throws ApiResultException
	 */
	default void assertThrowApiEx(boolean condition, Object... messageArgs) throws ApiResultException {
		if (condition) {
			throw apiEx().setMessageArgs(messageArgs);
		}
	}
	
	/**
	 * 例如 RespError.PARAMS_REQUIRED.assertThrowResEx(userId == null); 用户ID空时会发生异常
	 * 
	 * @param 提示错误内容
	 * @param condition 满足这样的一个条件才允许抛出异常
	 * @param messageArgs 异常需要的参数
	 * @throws ApiResultException
	 */
	default void assertThrowResEx(String message, boolean condition, Object... messageArgs) throws ApiResultException {
		if (condition) {
			throw new ApiResultException(this, message).setMessageArgs(messageArgs);
		}
	}
	
	/**
	 * 例如 RespError.PARAMS_REQUIRED.assertThrowApiEx(userId == null); 用户ID空时会发生异常
	 * 
	 * @param 提示错误内容
	 * @param condition 满足这样的一个条件才允许抛出异常
	 * @param messageArgs 异常需要的参数
	 * @throws ApiResultException
	 */
	default void assertThrowApiEx(String message, boolean condition, Object... messageArgs) throws ApiResultException {
		if (condition) {
			throw new ApiException(this, message).setMessageArgs(messageArgs);
		}
	}
	
	default boolean equals(String name) {
		return getState().equals(name);
	}
}
