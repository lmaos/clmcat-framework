package com.clmcat.basics.commons.https.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.clmcat.basics.commons.https.HttpUrlParam;
/**
 * POST URL 参数填充.
 * @author zhangxingyu
 *
 */
public class HttpPostUrlParamStream extends HttpPostStreamAbstract {
	private List<HttpUrlParam> httpUrlParams;

	public HttpPostUrlParamStream(HttpRequestStreamResult httpRequestStream) {
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
	public HttpPostUrlParamStream add(String name, Object value) {
		if (value != null) {
			httpUrlParams.add(new HttpUrlParam(name, value));
		}
		return this;
	}
	
	/**
	 * 请求参数设置, 追加参数.
	 * 
	 * @param map, name  参数名称, value 参数的值
	 * @return
	 */
	public HttpPostUrlParamStream addAll(Map<String, Object> map) {
		if (map != null) {
			map.forEach((name, value) -> {
				add(name, value);
			});
		}
		return this;
	}

}
