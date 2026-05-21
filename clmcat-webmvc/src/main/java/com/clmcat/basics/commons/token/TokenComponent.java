package com.clmcat.basics.commons.token;

/**
 * token组建
 * 
 * @author zhangxingyu
 *
 * @param <T>
 */
public interface TokenComponent<T> {
	/**
	 * 获得TOKEN
	 * 
	 * @param param
	 * @param clientCheck 客户端校验码, 每个客户端唯一(客户端传递,可以是设备号.)
	 * @return
	 * @throws TokenComponentException
	 */
	String getToken(T param, String clientCheck) throws TokenComponentException;

	/**
	 * 解析 token
	 * 
	 * @param token
	 * @return
	 * @throws TokenComponentException
	 */
	T parseToken(String token) throws TokenComponentException;

	/**
	 * 验证token有效性
	 * 
	 * @param token       生成的 token
	 * @param clientCheck 客户端校验码, 每个客户端唯一(客户端传递,可以是设备号.)
	 * @return
	 */
	boolean verifyToken(String token, String clientCheck);

	/**
	 * 直接从token 获取BEAN 不进行校验.(如果解析失败则返回null)
	 * 
	 * @param token
	 * @return
	 */
	default T getTokenBeanNoVerify(String token) {
		try {
			return parseToken(token);
		} catch (TokenComponentException e) {
			return null;
		}
	}
}
