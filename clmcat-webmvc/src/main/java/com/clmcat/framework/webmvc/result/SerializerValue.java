package com.clmcat.framework.webmvc.result;

import java.util.Date;

import com.alibaba.fastjson.serializer.ValueFilter;

public class SerializerValue implements ValueFilter {
	public final static SerializerValue defaultSerializerValue = new SerializerValue();

	@Override
	public Object process(Object object, String name, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) { // 布尔类型原封不动
			return value;
		}
		if (value instanceof Number || value instanceof Character) { // 数字类型就将就将就.
			return String.valueOf(value);
		}
		if (value instanceof Date) { // 时间类型使用时间戳
			return String.valueOf(((Date) value).getTime());
		}
		return value;
	}

}
