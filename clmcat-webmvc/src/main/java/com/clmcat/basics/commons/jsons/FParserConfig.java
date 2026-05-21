package com.clmcat.basics.commons.jsons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;

public class FParserConfig extends ParserConfig {

	public <T> T toJavaObject(JSONObject jsonObject, Class<T> type) {

		return jsonObject.toJavaObject(type, this, 0);
	}

	public <T> T parseObject(String json, Class<T> type, Feature... features) {

		return JSON.parseObject(json, type, this, features);
	}

}
