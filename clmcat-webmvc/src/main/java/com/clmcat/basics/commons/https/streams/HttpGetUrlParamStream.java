package com.clmcat.basics.commons.https.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.clmcat.basics.commons.https.HttpUrlParam;

/**
 * Http Get请求时 URL上的参数
 * 
 * @author zhangxingyu
 *
 */
public class HttpGetUrlParamStream extends HttpGetStreamAbstract {
	private List<HttpUrlParam> httpUrlParams;

	public HttpGetUrlParamStream(HttpRequestStreamResult httpRequestStream) {
		super(httpRequestStream);
		httpUrlParams = httpRequestStream.getHttpUrlParams();
		if (httpUrlParams == null) {
			httpUrlParams = new ArrayList<>();
			httpRequestStream.setHttpUrlParams(httpUrlParams);
		}
	}

	/**
	 * 请求参数设置, 追加参数.
	 * 
	 * @param name  参数名称
	 * @param value 参数的值
	 * @return
	 */
	public HttpGetUrlParamStream add(String name, Object value) {
		if (value != null) {
			httpUrlParams.add(new HttpUrlParam(name, value));
		}
		return this;
	}

	/**
	 * 请求参数设置, 追加参数.
	 * 
	 * @param map, name 参数名称, value 参数的值
	 * @return
	 */
	public HttpGetUrlParamStream addAll(Map<String, ?> map) {
		if (map != null) {
			map.forEach((name, value) -> {
				add(name, value);
			});
		}
		return this;
	}

}
