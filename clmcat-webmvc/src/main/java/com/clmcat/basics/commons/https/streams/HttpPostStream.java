package com.clmcat.basics.commons.https.streams;

import com.clmcat.basics.commons.https.HttpMethod;

/**
 * 发起的POST请求
 * 
 * @author zhangxingyu
 *
 */
public class HttpPostStream extends HttpPostStreamAbstract {
	public HttpPostStream(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
		httpRequestStream.setHttpMethod(HttpMethod.POST);
	}

}
