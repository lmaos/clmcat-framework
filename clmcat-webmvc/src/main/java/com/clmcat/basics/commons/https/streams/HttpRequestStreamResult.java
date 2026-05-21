package com.clmcat.basics.commons.https.streams;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.HeaderGroup;

import com.clmcat.basics.commons.https.HttpMethod;
import com.clmcat.basics.commons.https.HttpUrlParam;

public class HttpRequestStreamResult {

	private String url;
	private HeaderGroup headerGroup; // 头信息
	private List<HttpUrlParam> httpUrlParams; // url参数
	private HttpEntity httpEntity;
	private HttpMethod httpMethod;
	private RequestConfig requestConfig;
	private Charset responseCharset = Charset.forName("UTF-8");
	private boolean trace;
	/**
	 * 请求的地址
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 请求头集合
	 * @return
	 */
	public HeaderGroup getHeaderGroup() {
		return headerGroup;
	}

	public void setHeaderGroup(HeaderGroup headerGroup) {
		this.headerGroup = headerGroup;
	}
	/**
	 * URL 参数
	 * @return
	 */
	public List<HttpUrlParam> getHttpUrlParams() {
		return httpUrlParams;
	}

	public void setHttpUrlParams(List<HttpUrlParam> httpUrlParams) {
		this.httpUrlParams = httpUrlParams;
	}
	/**
	 * HTTP 请求体
	 * @return
	 */
	public HttpEntity getHttpEntity() {
		return httpEntity;
	}

	public void setHttpEntity(HttpEntity httpEntity) {
		this.httpEntity = httpEntity;
	}
	/**
	 * HTTP 请求方法
	 * @return
	 */
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}
	
	public RequestConfig getRequestConfig() {
		return requestConfig;
	}
	
	public void setRequestConfig(RequestConfig requestConfig) {
		this.requestConfig = requestConfig;
	}
	
	public Charset getResponseCharset() {
		return responseCharset;
	}
	public boolean isTrace() {
		return trace;
	}
	public void setTrace(boolean trace) {
		this.trace = trace;
	}
}
