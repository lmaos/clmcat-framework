package com.clmcat.basics.commons.jsons;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 
 * @author zhangxingyu
 * FJson.getString("data.xx.cc");
 */
public class FJson {
	private JSONObject jsonObject;
	private FParserConfig parserConfig;

	public FJson(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	public FJson() {
		jsonObject = new JSONObject();
	}
	
	public FJson setParserConfig(FParserConfig parserConfig) {
		this.parserConfig = parserConfig;
		return this;
	}
	
	public boolean getBooleanVaule(String node) {
		return getBoolean(node, false);
	}
	
	public Boolean getBoolean(String node, Boolean def) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return def;
		}
		Boolean value = curr.getBoolean(lastNode(node));
		return value == null ? def : value;
	}

	public int getIntValue(String node) {
		return getInteger(node, 0);
	}

	public Integer getInteger(String node, Integer def) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return def;
		}
		Integer value = curr.getInteger(lastNode(node));
		return value == null ? def : value;
	}

	public long getLongValue(String node) {
		return getLong(node, 0L);
	}

	public Long getLong(String node, Long def) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return def;
		}
		Long value = curr.getLong(lastNode(node));
		return value == null ? def : value;
	}

	public String getString(String node) {
		return getString(node, null);
	}

	public String getString(String node, String def) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return def;
		}
		String value = curr.getString(lastNode(node));
		return value == null ? def : value;
	}

	public FJson getObject(String node) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return null;
		}
		return new FJson(curr);
	}

	public <T> T getObject(String node, Class<T> type) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return null;
		}
		JSONObject array = curr.getJSONObject(lastNode(node));
		if (parserConfig != null) {
			return array == null ? null : parserConfig.toJavaObject(array, type);
		} else {
			return array == null ? null : array.toJavaObject(type);
		}
	}
	
	public JSONObject getJSONObject(String node) {
		JSONObject curr = parseNode(node);
		return curr;
	}

	public <T> List<T> getList(String node, Class<T> type) {
		JSONArray array = getJSONArray(node);
		return array == null ? null : array.toJavaList(type);
	}

	public JSONArray getJSONArray(String node) {
		JSONObject curr = parseNode(node);
		if (curr == null) {
			return null;
		}
		return curr.getJSONArray(lastNode(node));
	}

	private JSONObject parseNode(String nodes) {
		String elements[] = nodes.split("\\.");
		JSONObject next = this.jsonObject;
		int i = 0;
		int len = elements.length - 1;
		while (next != null && i < len) {
			next = next.getJSONObject(elements[i++]);
		}
		return next;
	}

	private String lastNode(String nodes) {
		int index = nodes.lastIndexOf(".");
		if (index == -1) {
			return nodes;
		}
		return nodes.substring(index + 1);
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	@Override
	public String toString() {
		return jsonObject.toJSONString();
	}
	public static void main(String[] args) {
		String json = "{\"status\":\"0\",\"msg\":\"\",\"data\":{\"advs\":[{\"advName\":\"扭屁股\",\"advLocale\":\"*\",\"advModule\":\"homelist\",\"advType\":\"image\",\"advIndex\":\"3\",\"advContent\":{\"name\":\"newbieSale\",\"cover\":\"https://img1.baidu.com/it/u=992490148,975709827&fm=26&fmt=auto&gp=0.jpg\",\"golink\":\"https://apply-test.lita.cool/?type=discount-order\"},\"createTime\":\"1617954887955\"}],\"userId\":\"110\",\"locale\":\"en\"}}";
		FJson fJson = new FJson(JSON.parseObject(json));
		System.out.println(fJson.getLong("data.userId", null));
	}
}
