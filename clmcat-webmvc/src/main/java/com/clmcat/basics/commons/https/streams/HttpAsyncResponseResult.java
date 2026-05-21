package com.clmcat.basics.commons.https.streams;

@FunctionalInterface
public interface HttpAsyncResponseResult {
	
	void response(HttpResponseResult result);

}
