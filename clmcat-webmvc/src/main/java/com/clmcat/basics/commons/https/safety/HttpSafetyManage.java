package com.clmcat.basics.commons.https.safety;

/**
 * http 安全
 * @author zhangxingyu
 *
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clmcat.basics.commons.lang.StringUtils;

public class HttpSafetyManage {
	private final static HttpSafetyManage httpSafetyManage = new HttpSafetyManage();

	private HttpSafetyManage() {
	}

	public static HttpSafetyManage getHttpSafetyManage() {
		return httpSafetyManage;
	}

	/**
	 * http 安全配置
	 */
	private Map<String, HttpSafetyConfig> configs = new ConcurrentHashMap<>(256);

	/**
	 * 获得安全配置
	 * 
	 * @param url
	 * @return
	 */
	public HttpSafetyConfig getConfig(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		url = formatUrl(url);

		return this.configs.get(url);
	}

	public void setConfig(String url, HttpSafetyConfig config) {
		if (StringUtils.isBlank(url)) {
			return;
		}
		url = formatUrl(url);
		this.configs.put(url, config);

	}

	private String formatUrl(String url) {
		url = url.trim();
		int index = url.indexOf("?");
		if (index != -1) {
			url = url.substring(0, index);
		}
		if (url.startsWith("https://")) {
			url = url.substring("https://".length());
		}
		if (url.startsWith("http://")) {
			url = url.substring("http://".length());
		}
		return url;
	}
	
	public static void main(String[] args) throws Exception {
		
		/**
		 * HTTP 安全管理
		 */
		HttpSafetyManage.getHttpSafetyManage().setConfig("https://qwer", 
				new HttpSafetyConfig("{'status':0}")
				.setFailConfig("500-1", new HttpSafetyFailRecord(5, 10000, 1000, "{'status':0}"))
				.setFailConfig("404-2", new HttpSafetyFailRecord(7, 20000, 1000, "{'status':1}")));
		
		for (int i = 0; i < 200; i++) {
			System.out.println(i+1 + ": ");
			Object failResult = HttpSafetyManage.getHttpSafetyManage().getConfig("qwer?ddd").getFailResult();
			if (failResult == null) {
				HttpSafetyManage.getHttpSafetyManage().getConfig("qwer").failRecord();
			} else {
				System.out.println(failResult);
			}
			Thread.sleep(500);
		}
		
	}
}
