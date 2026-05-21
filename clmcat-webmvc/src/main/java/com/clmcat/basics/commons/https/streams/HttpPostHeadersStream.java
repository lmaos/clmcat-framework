package com.clmcat.basics.commons.https.streams;

import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
/**
 * POST 请求头配置
 * 
 * @author zhangxingyu
 *
 */
public class HttpPostHeadersStream extends HttpPostStreamAbstract {
	private HeaderGroup headerGroup;

	public HttpPostHeadersStream(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
		headerGroup = httpRequestStream.getHeaderGroup();
		if (headerGroup == null) {
			headerGroup = new HeaderGroup();
			httpRequestStream.setHeaderGroup(headerGroup);
		}
	}
	/**
	 * 设置头信息-存在则替换
	 * 
	 * @param name header的名称
	 * @param value header的值
	 * @return
	 */
	public HttpPostHeadersStream set(String name, String value) {
		headerGroup.updateHeader(new BasicHeader(name, value));
		return this;
	}
	/**
	 * 添加头信息.
	 * 
	 * @param name header的名称
	 * @param value header的值
	 * @return
	 */
	public HttpPostHeadersStream add(String name, String value) {
		headerGroup.addHeader(new BasicHeader(name, value));
		return this;
	}
	/**
	 * 设置内容的类型
	 * 
	 * @param contentType
	 * @return
	 */
	public HttpPostHeadersStream setContentType(String contentType) {
		return set(HttpHeaders.CONTENT_TYPE, contentType);
	}

	/**
	 * 设置内容的类型为: Content-Type: application/x-www-form-urlencoded
	 * @return
	 */
	public HttpPostHeadersStream setContentTypeForm() {
		return setContentType("application/x-www-form-urlencoded");
	}
	
	/**
	 * 设置内容的类型为: Content-Type: application/json
	 * @return
	 */
	public HttpPostHeadersStream setContentTypeJson() {
		return setContentType("application/json");
	}
	
	/**
	 * 设置内容的类型为: Content-Type: application/json;charset=utf-8
	 * @return
	 */
	public HttpPostHeadersStream setContentTypeJsonUtf8() {
		return setContentType("application/json;charset=utf-8");
	}

}
