package com.clmcat.basics.commons.https.safety;
/**
 * HTTP 安全流配置方式
 * @author zhangxingyu
 *
 */
public class HttpSafetyStreamConfig extends HttpSafetyConfig {

	public HttpSafetyStreamConfig(Object defaultFailResult) {
		super(defaultFailResult);
	}

	/**
	 * 全局异常配置
	 * 
	 * @param name
	 * @param failRecord
	 */
	public void configEX(String name, HttpSafetyFailRecord failRecord) {
		setFailConfig("EX" + name, failRecord);
	}

	/**
	 * 某状态失败配置 500 400
	 * 
	 * @param code
	 * @param failRecord
	 */
	public void configSN(int code, HttpSafetyFailRecord failRecord) {
		setFailConfig("S" + code, failRecord);
	}

	/**
	 * 全局失败配置
	 * 
	 * @param name
	 * @param failRecord
	 */
	public void configFAIL(String name, HttpSafetyFailRecord failRecord) {
		setFailConfig("FAIL" + name, failRecord);
	}
	
	/**
	 * 直接错误状态配置
	 * @param code
	 * @param failRecord
	 */
	public void configCode(int code, HttpSafetyFailRecord failRecord) {
		setFailConfig(code+"", failRecord);
	}
	
	/**
	 * 直接错误异常配置
	 * @param code
	 * @param failRecord
	 */
	public void configCode(Exception ex, HttpSafetyFailRecord failRecord) {
		setFailConfig(ex.getClass().getName(), failRecord);
	}

}
