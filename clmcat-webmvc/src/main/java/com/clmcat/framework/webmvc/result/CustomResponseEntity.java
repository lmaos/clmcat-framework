package com.clmcat.framework.webmvc.result;

import java.util.HashMap;
import java.util.Map;

public class CustomResponseEntity {
	
	private String contentType = "text/plain";
	private byte[] data;
	private Map<String, String> headers = new HashMap<String, String>(); 
	
	public byte[] getData() {
		return data;
	}

	public CustomResponseEntity setData(byte[] data) {
		this.data = data;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public CustomResponseEntity setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	
	public CustomResponseEntity setHeader(String name, String value) {
		this.headers.put(name, value);
		return this;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}

}
