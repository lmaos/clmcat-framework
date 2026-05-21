package com.clmcat.basics.commons.https.streams;

public class HttpStreamException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private HttpRequestStreamResult requestStreamResult;
	public HttpStreamException(HttpRequestStreamResult requestStreamResult, Throwable e) {
		super(requestStreamResult.getUrl(), e);
		this.requestStreamResult = requestStreamResult;
	}
	
	public HttpRequestStreamResult getRequestStreamResult() {
		return requestStreamResult;
	}

}
