package com.clmcat.basics.commons.https.streams;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;

import com.clmcat.basics.commons.https.HttpMethod;
import com.clmcat.basics.commons.https.HttpUrlParam;
import com.clmcat.basics.commons.https.HttpUtils;
import com.clmcat.basics.commons.https.safety.HttpSafetyConfig;
import com.clmcat.basics.commons.https.safety.HttpSafetyManage;

public abstract class HttpStream {
	private HttpRequestStreamResult httpRequestStream;

	public HttpStream(HttpRequestStreamResult httpRequestStream) {
		this.httpRequestStream = httpRequestStream;
	}

	protected HttpRequestStreamResult getHttpRequestStream() {
		return httpRequestStream;
	}
	
	public void request(HttpAsyncResponseResult asyncResponseResult) {
		request(asyncResponseResult, null);
	}
	/**
	 * 异步请求
	 * @param asyncResponseResult
	 * @param defFailResult
	 */
	public void request(HttpAsyncResponseResult asyncResponseResult, String defFailResult) {
		
		String uri = httpRequestStream.getUrl();
		// 验证当前接口是否可用, 如果不可用则使用默认值填充、
		HttpSafetyConfig httpSafetyConfig = HttpSafetyManage.getHttpSafetyManage().getConfig(uri);
		
		HttpStreamAsyncResponseCallback callback = new HttpStreamAsyncResponseCallback(httpRequestStream, asyncResponseResult, httpSafetyConfig, defFailResult);
		if (httpSafetyConfig != null) { // http 安全配置存在. 用于防止雪崩
			
			String failResult = httpSafetyConfig.getFailResultString();
			
			if (failResult != null) { // 直接失败
				if (HttpSafetyConfig.DEFAULT_FAIL_RESULT.equals(failResult)) {
					failResult = ""; // 如果是默认则就是空字符
				}
				callback.safetyResponse(failResult);
				return;
			}
		}
		
		HttpMethod httpMethod = httpRequestStream.getHttpMethod();
		HeaderGroup headers = httpRequestStream.getHeaderGroup();
		HttpEntity httpEntity = httpRequestStream.getHttpEntity();
		List<HttpUrlParam> httpUrlParams = httpRequestStream.getHttpUrlParams();
		boolean trace = httpRequestStream.isTrace();
		RequestConfig requestConfig = httpRequestStream.getRequestConfig(); // 请求配置
		// 异步应答
		try {
			if (asyncResponseResult != null) {
				if (httpMethod == HttpMethod.POST) {
					HttpUtils.post(uri, headers, httpEntity, httpUrlParams, requestConfig, callback, trace);
				} else {
					HttpUtils.get(uri, headers, httpUrlParams, requestConfig, callback, trace);
				}
			}
		} catch (Throwable e) {
			if (httpSafetyConfig != null) {
				httpSafetyConfig.failRecordStartsWith(e.getClass().getSimpleName())// 异常类计数
								.failRecordStartsWithGroup("ERROR-EX", "EX", "FAIL"); // 异常计数
			}
			callback.errorResponse(defFailResult, e);
		}
		
	}
	
	
	/**
	 * 同步请求
	 * @return
	 * @throws HttpStreamException
	 */
	public HttpResponseResult request() throws HttpStreamException {
		String uri = httpRequestStream.getUrl();
		// 验证当前接口是否可用, 如果不可用则使用默认值填充、
		HttpSafetyConfig httpSafetyConfig = HttpSafetyManage.getHttpSafetyManage().getConfig(uri);
		
		if (httpSafetyConfig != null) { // http 安全配置存在. 用于防止雪崩
			
			String failResult = httpSafetyConfig.getFailResultString();
			if (failResult != null) { // 直接失败
				if (HttpSafetyConfig.DEFAULT_FAIL_RESULT.equals(failResult)) {
					failResult = ""; // 如果是默认则就是空字符
				}
				return new HttpResponseResult(httpRequestStream, 304, failResult, (Throwable)null)
						.ackFailCache();
			}
		}
		
		HttpMethod httpMethod = httpRequestStream.getHttpMethod();
		HeaderGroup headers = httpRequestStream.getHeaderGroup();
		HttpEntity httpEntity = httpRequestStream.getHttpEntity();
		List<HttpUrlParam> httpUrlParams = httpRequestStream.getHttpUrlParams();
		boolean trace = httpRequestStream.isTrace();
		RequestConfig requestConfig = httpRequestStream.getRequestConfig(); // 请求配置
		try {
			CloseableHttpResponse closeableHttpResponse;
			if (httpMethod == HttpMethod.POST) {
				closeableHttpResponse = HttpUtils.post(uri, headers, httpEntity, httpUrlParams, requestConfig, trace);
			} else {
				closeableHttpResponse = HttpUtils.get(uri, headers, httpUrlParams, requestConfig, trace);
			}
			try (CloseableHttpResponse resp = closeableHttpResponse) {
				byte[] content = EntityUtils.toByteArray(closeableHttpResponse.getEntity());
				Header[] allHeaders = resp.getAllHeaders();
				int httpStatus = resp.getStatusLine().getStatusCode();
				String message = resp.getStatusLine().getReasonPhrase();
				
				// 有状态 但是状态有问题时计数
				if (httpStatus / 100 != 2 && httpSafetyConfig != null) { // 安全配置存在
					if (httpStatus < 600) { 
						httpSafetyConfig.failRecordStartsWithGroup("ERROR-STATUS-0",
								String.valueOf(httpStatus),
								"S" + String.valueOf(httpStatus / 100),
								"FAIL"); // http 状态计数
					} else {
						httpSafetyConfig.failRecordStartsWithGroup("ERROR-STATUS-1",
								String.valueOf(httpStatus),
								"S" + String.valueOf(httpStatus / 100)); // http 状态计数
					}
				}
				
				return new HttpResponseResult(httpRequestStream, content, httpStatus, message, allHeaders);
			}
		} catch (Exception e) {
			if (httpSafetyConfig != null) {
				httpSafetyConfig.failRecordStartsWith(e.getClass().getSimpleName())// 异常类计数
								.failRecordStartsWithGroup("ERROR-EX", "EX", "FAIL"); // 异常计数
			}
			throw new HttpStreamException(httpRequestStream, e);
		}

	}

}
