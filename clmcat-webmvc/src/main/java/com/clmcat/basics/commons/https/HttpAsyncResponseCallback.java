package com.clmcat.basics.commons.https;

import org.apache.http.HttpResponse;

public interface HttpAsyncResponseCallback{

	
	
	
	void response(HttpResponse result, Throwable ex, CallStatus status);
	
	
	public static enum CallStatus {
		ok, fail, cancel, safety
	}
	
}
