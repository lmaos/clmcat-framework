package com.clmcat.basics.commons.https;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class HttpUrlParam {

	private String name;
	private String value;

	public HttpUrlParam(String name, Object value) {
		this.name = name;
		this.value = value != null ? String.valueOf(value) : null;
	}

	public static HttpUrlParam of(String name, Object value) {
		return new HttpUrlParam(name, value);
	}

	public String getValue() {
		return value;
	}

	public String getEncodeUtf8Value() {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	public String getName() {
		return name;
	}

}
