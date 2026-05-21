package com.clmcat.basics.commons.https.streams;

import org.apache.http.client.config.RequestConfig;

/**
 * 请求开始
 * @author zhangxingyu
 *
 */
public class HttpRequestStream {
	private HttpRequestStreamResult httpRequestStreamResult;

	public HttpRequestStream(String url) {
		httpRequestStreamResult = new HttpRequestStreamResult();
		httpRequestStreamResult.setUrl(url);
	}
	
	public HttpRequestStream trace() {
		httpRequestStreamResult.setTrace(true);
		return this;
	}

	/**
	 * 进行POST请求
	 * @return
	 */
	public HttpPostStream post() {
		return new HttpPostStream(httpRequestStreamResult);
	}

	/**
	 * 进行GET请求
	 * @return
	 */
	public HttpGetStream get() {
		return new HttpGetStream(httpRequestStreamResult);
	}
	
	/**
	 * 请求配置
	 * @param requestConfig
	 * @return
	 */
	public HttpRequestStream setRequestConfig(RequestConfig requestConfig) {
		if (requestConfig != null) {
			httpRequestStreamResult.setRequestConfig(requestConfig);
		}
		return this;
	}
}
