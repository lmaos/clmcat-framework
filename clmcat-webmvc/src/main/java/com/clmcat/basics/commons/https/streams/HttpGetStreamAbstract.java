package com.clmcat.basics.commons.https.streams;

public abstract class HttpGetStreamAbstract extends HttpStream {

	public HttpGetStreamAbstract(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
	}
	
	/**
	 * 进行请求头的构建
	 * 
	 * @return
	 */
	public HttpGetHeadersStream header() {
		if (this.getClass() == HttpGetHeadersStream.class) {
			return (HttpGetHeadersStream) this;
		}
		return new HttpGetHeadersStream(getHttpRequestStream());
	}
	
	/**
	 * 进行URL的参数构建, url?xxx=cc&fff=vv
	 * 
	 * @return
	 */
	public HttpGetUrlParamStream params() {
		if (this.getClass() == HttpGetUrlParamStream.class) {
			return (HttpGetUrlParamStream) this;
		}
		return new HttpGetUrlParamStream(getHttpRequestStream());
	}
}
