package com.clmcat.basics.commons.https.streams;

import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;

public class HttpGetHeadersStream extends HttpGetStreamAbstract {

	private HeaderGroup headerGroup;

	public HttpGetHeadersStream(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
		headerGroup = httpRequestStream.getHeaderGroup();
		if (headerGroup == null) {
			headerGroup = new HeaderGroup();
			httpRequestStream.setHeaderGroup(headerGroup);
		}
	}

	/**
	 * 设置请求头, 存在则替换
	 * @param name 请求头名称
	 * @param value 请求头值
	 * @return
	 */
	public HttpGetHeadersStream set(String name, Object value) {
		if (value != null) {
			headerGroup.updateHeader(new BasicHeader(name, String.valueOf(value)));
		}
		return this;
	}
	
	/**
	 * 设置请求头, 存在则替换
	 * @param name 请求头名称
	 * @param value 请求头值
	 * @return
	 */
	public HttpGetHeadersStream set(Map<String, ?> map) {
		map.forEach((name, value)->{
			this.set(name, value);
		});
		return this;
	}

	/**
	 * 增加请求头. 
	 * @param name 请求头名称
	 * @param value 请求头值
	 * @return
	 */
	public HttpGetHeadersStream add(String name, String value) {
		headerGroup.addHeader(new BasicHeader(name, value));
		return this;
	}
	/**
	 * 设置请求类型 Content-Type: xxxx
	 * 
	 * @param contentType
	 * @return
	 */
	public HttpGetHeadersStream setContentType(String contentType) {
		return set(HttpHeaders.CONTENT_TYPE, contentType);
	}

}
