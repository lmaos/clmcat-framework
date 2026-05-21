package com.clmcat.basics.commons.https.streams;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * Http 请求体构建
 * 
 * @author zhangxingyu
 *
 */
public class HttpEntityContentStream extends HttpPostStreamAbstract {

	public HttpEntityContentStream(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
	}

	public HttpEntityContentStream setContent(HttpEntity httpEntity) {
		getHttpRequestStream().setHttpEntity(httpEntity);
		return this;
	}

	public HttpEntityContentStream setContent(byte[] data) {
		return setContent(data, null);
	}

	public HttpEntityContentStream setContent(String data) {
		return setContent(data, null);
	}
	
	/**
	 * 设置请求体, 并指定Content-Type的值
	 * @param data
	 * @param contentType
	 * @return
	 */
	public HttpEntityContentStream setContent(byte[] data, String contentType) {
		BasicHttpEntity httpEntity = new BasicHttpEntity();
		httpEntity.setContentLength(data.length);
		httpEntity.setContentType(contentType);
		httpEntity.setContent(new ByteArrayInputStream(data));
//		httpEntity.setContentEncoding(contentType);
		return setContent(httpEntity);
	}
	
	/**
	 * 设置 JSON 内容  ,contentType: application/json
	 * @param data  内容
	 */
	public HttpEntityContentStream setContentJson(Object data) {
		if (data instanceof String) {
			return setContent((String)data, "application/json");
		} else {
			return setContent(JSON.toJSONString(data, SerializerFeature.WriteNonStringValueAsString,SerializerFeature.DisableCircularReferenceDetect), "application/json");
		}
	}
	/**
	 * 设置 String 内容
	 * @param data  内容
	 * @param contentType 内容的类型 例如: application/json
	 */
	public HttpEntityContentStream setContent(String data, String contentType) {
		String charsetName = "UTF-8";
		if (contentType != null && contentType.length() > 0) {
			int index = contentType.indexOf("charset");
			if (index != -1) {
				index = contentType.indexOf("=", index + 6);
				if (index != -1) {
					charsetName = contentType.substring(index + 1).trim();
					if (charsetName.isEmpty()) {
						charsetName = "UTF-8";
					}
				}
			}
		}
		return setContent(data.getBytes(Charset.forName(charsetName)), contentType);
	}
	
	/**
	 * 构建 表单提交的结构.<BR>
	 * 
	 * content:  key=val&xx=cc 填充包体. Content-Type: application/x-www-form-urlencoded
	 * 
	 * @return
	 */
	public HttpEntityUrlParamStream buildUrlContent() {
		return new HttpEntityUrlParamStream(this);
	}

}
