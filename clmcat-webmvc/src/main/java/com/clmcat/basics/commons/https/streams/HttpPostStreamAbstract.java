package com.clmcat.basics.commons.https.streams;

public abstract class HttpPostStreamAbstract extends HttpStream {

	public HttpPostStreamAbstract(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
	}

	/**
	 * 进行URL的参数构建, url?xxx=cc&fff=vv
	 * 
	 */
	public HttpPostUrlParamStream params() {
		if (this.getClass() == HttpPostUrlParamStream.class) {
			return (HttpPostUrlParamStream) this;
		}
		return new HttpPostUrlParamStream(getHttpRequestStream());
	}

	/**
	 * 进行请求头的构建
	 */
	public HttpPostHeadersStream header() {
		if (this.getClass() == HttpPostHeadersStream.class) {
			return (HttpPostHeadersStream) this;
		}
		return new HttpPostHeadersStream(getHttpRequestStream());
	}
	/**
	 * 进行请求内容的构建 ---- HttpEntity 的创建.
	 */
	public HttpEntityContentStream content() {
		if (this.getClass() == HttpEntityContentStream.class) {
			return (HttpEntityContentStream) this;
		}
		return new HttpEntityContentStream(getHttpRequestStream());
	}
}
