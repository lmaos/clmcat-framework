package com.clmcat.basics.commons.https.streams;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clmcat.basics.commons.https.HttpAsyncResponseCallback;
import com.clmcat.basics.commons.https.safety.HttpSafetyConfig;

public class HttpStreamAsyncResponseCallback implements HttpAsyncResponseCallback {
	
	private static final Logger log = LoggerFactory.getLogger(HttpStreamAsyncResponseCallback.class);

	private HttpAsyncResponseResult responseResult;
	private HttpSafetyConfig httpSafetyConfig;
	private HttpRequestStreamResult httpRequestStream;
	private String defFailResult;
	private AtomicBoolean handler = new AtomicBoolean(false);
	
	
	public HttpStreamAsyncResponseCallback(HttpRequestStreamResult httpRequestStream, HttpAsyncResponseResult responseResult, HttpSafetyConfig httpSafetyConfig, String defFailResult) {
		this.httpRequestStream = httpRequestStream;
		this.responseResult = responseResult;
		this.httpSafetyConfig = httpSafetyConfig;
		this.defFailResult = defFailResult;
	}

	public void safetyResponse(String failResult) {
		response(null, null, CallStatus.safety, failResult);
	}
	
	public void errorResponse(String failResult, Throwable ex) {
		response(null, ex, CallStatus.fail, failResult);
	}
	
	@Override
	public void response(HttpResponse resp, Throwable ex, CallStatus status) {
		response(resp, ex, status, defFailResult);
	}
	
	private void response(HttpResponse resp, Throwable ex, CallStatus status, String failResult) {
		if (!handler.compareAndSet(false, true)) {
			return;
		}
		
		if (status == CallStatus.cancel) { // 客户端关闭
			responseResult.response(new HttpResponseResult(httpRequestStream, 499, failResult, (Throwable)null));
			return;
		}
		if (status == CallStatus.safety) { // 服务端安全验证
			responseResult.response(new HttpResponseResult(httpRequestStream, 304, failResult, (Throwable)null));
			return;
		}
		try {
			if (status == CallStatus.ok) {
				Header[] allHeaders = resp.getAllHeaders();
				byte[] content = EntityUtils.toByteArray(resp.getEntity());
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
				HttpResponseResult result = new HttpResponseResult(httpRequestStream, content, httpStatus, message, allHeaders);
				responseResult.response(result);
			} else if (ex != null) {
				if (httpSafetyConfig != null) {
					httpSafetyConfig.failRecordStartsWith(ex.getClass().getSimpleName())// 异常类计数
					.failRecordStartsWithGroup("ERROR-EX", "EX", "FAIL"); // 异常计数
				}
				HttpResponseResult result = new HttpResponseResult(httpRequestStream, 500, failResult, ex);
				responseResult.response(result);
				log.error("HTTP异步请求出现异常:", ex);
			} else {
				HttpResponseResult result = new HttpResponseResult(httpRequestStream, 500, failResult, (Throwable)null);
				responseResult.response(result);
			}
		} catch (Exception e) {
			if (httpSafetyConfig != null) {
				httpSafetyConfig.failRecordStartsWith(e.getClass().getSimpleName())// 异常类计数
								.failRecordStartsWithGroup("ERROR-EX", "EX", "FAIL"); // 异常计数
				HttpResponseResult result = new HttpResponseResult(httpRequestStream, 500, failResult, e);
				responseResult.response(result);
			}
		}
	}

}
