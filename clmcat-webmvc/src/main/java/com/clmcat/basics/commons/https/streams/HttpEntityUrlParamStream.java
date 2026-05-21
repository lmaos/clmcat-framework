package com.clmcat.basics.commons.https.streams;

import java.util.ArrayList;
import java.util.List;

import com.clmcat.basics.commons.https.HttpUrlParam;

public class HttpEntityUrlParamStream {
	private HttpEntityContentStream httpEntityContentStream;
	
	private List<HttpUrlParam> httpUrlParams = new ArrayList<HttpUrlParam>();
	
	
	public HttpEntityUrlParamStream(HttpEntityContentStream httpEntityContentStream) {
		this.httpEntityContentStream = httpEntityContentStream;
	}
	/**
	 * 添加参数
	 * 
	 * @param name 参数名
	 * @param value 参数值
	 * @return
	 */
	public HttpEntityUrlParamStream add(String name, Object value) {
		if (value != null) {
			httpUrlParams.add(HttpUrlParam.of(name, value));
		}
		return this;
	}


	/**
	 * 结束构建, 返回content流程 (上一流程)
	 * @return
	 */
	public HttpEntityContentStream end() {
		if (httpUrlParams.size() > 0) {
			StringBuilder stringBuilder = new StringBuilder();
			for (HttpUrlParam httpUrlParam : httpUrlParams) {
				stringBuilder.append(httpUrlParam.getName())
				.append("=").append(httpUrlParam.getEncodeUtf8Value())
				.append("&");
			}
			String content = stringBuilder.substring(0, stringBuilder.length() - 1);
			httpEntityContentStream.setContent(content, "application/x-www-form-urlencoded");
		}
		
		return httpEntityContentStream;
	}
	
}
