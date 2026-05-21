package com.clmcat.basics.commons.https.streams;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.clmcat.basics.commons.jsons.FJson;
import org.apache.http.Header;
import com.clmcat.basics.commons.util.call.LineCall;
import org.springframework.util.CollectionUtils;

/**
 * http 应答结果
 * 
 * @author zhangxingyu
 *
 */
public class HttpResponseResult {
	private HttpRequestStreamResult requestStreamResult;
	private byte[] content;
	private int httpStatus;
	private String message;
	private boolean failCache;
	private Throwable ex;

	private Map<String, List<String>> responseHeaders = new HashMap<>();

	public HttpResponseResult(HttpRequestStreamResult requestStreamResult, int httpStatus,
			String content, Throwable ex) {
		this.requestStreamResult = requestStreamResult;
		this.content = content == null ? new byte[]{} : content.getBytes(requestStreamResult.getResponseCharset());
		this.httpStatus = httpStatus;
		this.message = "fail";
		this.ex = ex;
		this.responseHeaders = headersToMap(null);

	}
	public HttpResponseResult(HttpRequestStreamResult requestStreamResult, byte[] content, int httpStatus, String message, Header[] allHeaders) {
		this.requestStreamResult = requestStreamResult;
		this.content = content;
		this.httpStatus = httpStatus;
		this.message = message;
		this.responseHeaders = headersToMap(allHeaders);
	}

	public HttpRequestStreamResult getRequestStreamResult() {
		return requestStreamResult;
	}

	public String getStringUtf8() {
		return getString("UTF-8");
	}

	public String getString(String charsetName) {
		byte[] data = getBytes();
		return new String(data, Charset.forName(charsetName));
	}

	public byte[] getBytes() {
		return content;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public String getMessage() {
		return message;
	}
	public Throwable getEx() {
		return ex;
	}
	public boolean isFailCache() {
		return failCache;
	}
	
	HttpResponseResult  ackFailCache() {
		this.failCache = true;
		return this;
	}

	public <T> T getJsonToJavaBean(Class<T> type) {
		String json = getStringUtf8();
		if (json != null) {
			json = json.trim();
			return JSON.parseObject(json, type);
		} else {
			return null;
		}
	}

	public FJson getFJson() {
		String json = getStringUtf8();
		if (json != null) {
			json = json.trim();
			return new FJson(JSON.parseObject(json));
		} else {
			return null;
		}
	}
	
	public FJson getFJson(boolean verifyJson) {
		String json = getStringUtf8();
		if (json != null) {
			json = json.trim();
			if (verifyJson) {
				if (json.startsWith("{") && json.endsWith("}")) {
					return new FJson(JSON.parseObject(json));
				} else {
					return new FJson();
				}
			} else {
				return new FJson(JSON.parseObject(json));
			}
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return httpStatus + ": " + getStringUtf8();
	}
	
	public boolean existContent() {
		return content != null && content.length > 0;
	}
	
	public LineCall.LineCallMessage<HttpResponseResult> call() {
		return LineCall.submit(this);
	}


	private Map<String, List<String>> headersToMap(Header[] allHeaders) {
		Map<String, List<String>> result = new HashMap<>();
		try {
			if (allHeaders != null) {
				for (int i = 0; i < allHeaders.length; i++) {
					Header header = allHeaders[i];
					String name = header.getName();
					String value = header.getValue();
					List<String> values = result.computeIfAbsent(name, k -> new ArrayList<>());
					values.add(value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Map<String, List<String>> getResponseHeaders() {
		return responseHeaders;
	}

	public String getResponseFirstHeader(String name) {
		Map<String, List<String>> responseHeaders = this.responseHeaders;
		if (responseHeaders != null) {
			List<String> values = responseHeaders.get(name);
			if (!CollectionUtils.isEmpty(values)) {
				return values.get(0);
			}
		}
		return "";
	}

	public String getResponseLastHeader(String name) {
		Map<String, List<String>> responseHeaders = this.responseHeaders;
		if (responseHeaders != null) {
			List<String> values = responseHeaders.get(name);
			if (!CollectionUtils.isEmpty(values)) {
				return values.get(values.size() - 1);
			}
		}
		return "";
	}
}
