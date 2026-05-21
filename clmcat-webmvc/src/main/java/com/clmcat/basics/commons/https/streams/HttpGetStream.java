package com.clmcat.basics.commons.https.streams;

import com.clmcat.basics.commons.https.HttpMethod;
/**
 * 发起的GET 请求
 * 
 * @author zhangxingyu
 *
 */
public class HttpGetStream extends HttpGetStreamAbstract {
	public HttpGetStream(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
		httpRequestStream.setHttpMethod(HttpMethod.GET);
	}
}
