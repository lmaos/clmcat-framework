package com.clmcat.framework.webmvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.framework.webmvc.ResponseEntityKey.DefaultResponseEntityKey;

import com.alibaba.fastjson.serializer.ValueFilter;
import com.clmcat.framework.webmvc.result.SerializerValue;

public interface ResponseEntityResultAdapter {

	public static final String KEY = "ResponseEntityResultAdapter";

	// Map<String, Object> toMap(ResponseEntity responseEntity);

	void out(HttpServletResponse response, ResponseEntity responseEntity) throws IOException;
	
	default ValueFilter valueFormat(ResponseEntity responseEntity) {
		return responseEntity.getEntityKey().valueformat();
	}

	public static final DefaultResponseEntityResultAdapter defaultInstance = new DefaultResponseEntityResultAdapter();

	public static class DefaultResponseEntityResultAdapter implements ResponseEntityResultAdapter {

		@Override
		public void out(HttpServletResponse response, ResponseEntity responseEntity) throws IOException {
			// response type

			String contentType = "application/json;charset=UTF-8";
			if (org.apache.commons.lang3.StringUtils.isNotBlank(responseEntity.getCallback())) {
				contentType = "application/javascript;charset=UTF-8";
			}

			response.setContentType(contentType);
			// body
			String json = toJson(responseEntity.getCallback(), responseEntity);
			byte[] body = json.getBytes(StandardCharsets.UTF_8);
			ServletOutputStream out = response.getOutputStream();
			out.write(body);
			out.flush();
		}
		public String toJson(String callback, ResponseEntity responseEntity) {

			ValueFilter valueFilter = valueFormat(responseEntity);
			if (valueFilter == null) {
				valueFilter = SerializerValue.defaultSerializerValue;
			}

			// 应答包装
			Map<String, Object> responseWrapper = toMap(responseEntity);
			// 转化为 JSON
			String json = JSON.toJSONString(responseWrapper, valueFilter, SerializerFeature.DisableCircularReferenceDetect);

			// 判断是否是 JSONP的消息
			if (org.apache.commons.lang3.StringUtils.isNotBlank(callback)) {
				return callback + "(" + json + ")";
			} else {
				return json;
			}
		}
		public Map<String, Object> toMap(ResponseEntity responseEntity) {
			ResponseEntityKey entityKey = responseEntity.getEntityKey();
			if (entityKey == null) {
				entityKey = DefaultResponseEntityKey.defaultInstance;
			}
			Map<String, Object> responseWrapper = new HashMap<>();
			if (entityKey.existRequestIdName() && StringUtils.isNotBlank(responseEntity.getRequestId())) {
				responseWrapper.put(entityKey.getRequestIdName(), responseEntity.getRequestId());
			}
			if (entityKey.existStatusName() && responseEntity.getStatus() != null) {
				responseWrapper.put(entityKey.getStatusName(), responseEntity.getStatus());
			}
			if (entityKey.existStateName() && StringUtils.isNotBlank(responseEntity.getState())) {
				responseWrapper.put(entityKey.getStateName(), responseEntity.getState());
			}
			if (entityKey.existContentName() && responseEntity.getContent() != null) {
				responseWrapper.put(entityKey.getContentName(), responseEntity.getContent());
			}
			if (entityKey.existMessageName() && responseEntity.getMessage() != null) {
				responseWrapper.put(entityKey.getMessageName(), responseEntity.getMessage());
			}
			if (entityKey.existErrplaceName() && StringUtils.isNotBlank(responseEntity.getErrplace())) {
				responseWrapper.put(entityKey.getErrplaceName(), responseEntity.getErrplace());
			}
			if (entityKey.existLocaleMessageName() && StringUtils.isNotBlank(responseEntity.getLocaleMessage())) {
				responseWrapper.put(entityKey.getLocaleMessageName(), responseEntity.getLocaleMessage());
			}
			return responseWrapper;
		}

	}

}
