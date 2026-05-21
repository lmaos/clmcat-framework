package com.clmcat.basics.commons.https.safety;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;

/**
 * Http 安全配置
 * 
 * @author zhangxingyu
 *
 */
public class HttpSafetyConfig {
	public final static String DEFAULT_FAIL_RESULT = "###FAIL###";
	private Map<String, HttpSafetyFailRecord> failConfigs = new ConcurrentHashMap<>();
	private Map<String, List<HttpSafetyFailRecord>> startWithFailConfigs = new ConcurrentHashMap<>();
	private Object defaultFailResult = DEFAULT_FAIL_RESULT;

	public HttpSafetyConfig(Object defaultFailResult) {
		if (defaultFailResult != null) {
			this.defaultFailResult = defaultFailResult;
		}
	}

	/**
	 * 设置失败配置
	 * 
	 * @param type       异常处理: EX*, 状态失败通用FAIL*, 细节状态(404*,500*), 范状态(S5*,S4*),
	 *                   细节异常处理(XxxException)
	 * @param failRecord
	 * @return
	 */
	public HttpSafetyConfig setFailConfig(String type, HttpSafetyFailRecord failRecord) {
		this.failConfigs.put(type, failRecord);
		this.startWithFailConfigs.clear(); 
		return this;
	}

	/**
	 * 通过访问
	 * 
	 * @return
	 */
	public boolean isPass() {
		for (Entry<String, HttpSafetyFailRecord> entry : failConfigs.entrySet()) {
			HttpSafetyFailRecord record = entry.getValue();
			if (record != null && record.isFail()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 失败结果
	 * 
	 */
	public Object getFailResult() {

		for (Entry<String, HttpSafetyFailRecord> entry : failConfigs.entrySet()) {
			HttpSafetyFailRecord record = entry.getValue();
			if (record != null && record.isFail()) {
				Object result = record.getFailResult();
				if (result == null) {
					result = defaultFailResult;
				}
				return result;
			}
		}

		return null;
	}
	/**
	 * 失败结果 String 值
	 * 
	 */
	public String getFailResultString() {
		Object object = getFailResult();
		return toStringByResult(object);
	}
	
	private String toStringByResult(Object result) {
		if (result == null) {
			return null;
		}
		if (result instanceof String || result instanceof Number || result instanceof Character
				|| result instanceof Boolean) {
			return String.valueOf(result);
		}
		if (result instanceof Date) {
			return String.valueOf(((Date) result).getTime());
		}
		return JSON.toJSONString(result);
	}

	/**
	 * 失败记录 +1
	 */
	public HttpSafetyConfig failRecord(String type, String... types) {
		// 哪种失败类型+1
		HttpSafetyFailRecord record = null;
		if (type != null) {
			record = failConfigs.get(type);
			if (record != null) {
				record.incrBy();
			}
		}
		if (types != null) {
			for (String typeTmp : types) {
				record = failConfigs.get(typeTmp);
				if (record != null) {
					record.incrBy();
				}
			}
		}
		return this;
	}

	public HttpSafetyConfig failRecord() {
		for (Entry<String, HttpSafetyFailRecord> entry : failConfigs.entrySet()) {
			HttpSafetyFailRecord record = entry.getValue();
			if (record != null) {
				record.incrBy();
			}
		}
		return this;
	}

	public HttpSafetyConfig failRecordStartsWithGroup(String group, String... starts) {

		List<HttpSafetyFailRecord> records = startWithFailConfigs.get(group);
		if (records != null) {
			for (HttpSafetyFailRecord record : records) {
				record.incrBy();
			}
			return this;
		}
		records = new ArrayList<>();
		if (starts != null && starts.length > 0) {
			for (Entry<String, HttpSafetyFailRecord> entry : failConfigs.entrySet()) {
				for (String start : starts) { // 验证开始
					if (entry.getKey().startsWith(start)) {
						HttpSafetyFailRecord record = entry.getValue();
						if (record != null) {
							record.incrBy();
							records.add(record);
						}
					}
				}
			}
		}
		startWithFailConfigs.put(group, records);
		return this;
	}

	/**
	 * 这个开头的 计数
	 * 
	 * @param start
	 * @return
	 */
	public HttpSafetyConfig failRecordStartsWith(String start) {
		List<HttpSafetyFailRecord> records = startWithFailConfigs.get(start);
		if (records != null) {
			for (HttpSafetyFailRecord record : records) {
				record.incrBy();
			}
			return this;
		}
		records = new ArrayList<>();
		for (Entry<String, HttpSafetyFailRecord> entry : failConfigs.entrySet()) {
			if (entry.getKey().startsWith(start)) {
				HttpSafetyFailRecord record = entry.getValue();
				if (record != null) {
					record.incrBy();
					records.add(record);
				}
			}
		}
		startWithFailConfigs.put(start, records);
		return this;
	}
}
