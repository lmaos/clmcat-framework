package com.clmcat.basics.commons.util;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DefaultVal {

	@SuppressWarnings("rawtypes")
	public static final List emptyList = Collections.unmodifiableList(new ArrayList<>(0));
	@SuppressWarnings("rawtypes")
	public static final Set emptySet = Collections.unmodifiableSet(new HashSet<>(0));
	@SuppressWarnings("rawtypes")
	public static final Map emptyMap = Collections.unmodifiableMap(new HashMap<>(0));
	public static final String emptyStr = "";
	public static final String[] emptyStrs = {};
	public static final byte[] emptyBytes = {};
	public static final int[] emptyInts = {};
	public static final long[] emptyLongs = {};
	public static final Object[] emptyObjects = {};

	public static int get(Integer num) {

		return getOrDefault(num, 0);
	}

	public static int getOrDefault(Integer num, int def) {
		if (num == null) {
			return def;
		}
		return num;
	}

	public static long get(Long num) {
		return getOrDefault(num, 0L);
	}

	public static long getOrDefault(Long num, long def) {
		if (num == null) {
			return def;
		}
		return num;
	}
	
	public static String getOrDefault(String str, String def) {
		if (str == null) {
			return def;
		}
		return str;
	}
	public static <T>T getOrDefault(T str, T def) {
		if (str == null) {
			return def;
		}
		return str;
	}
	
	public static <X, T> T getOrDefault(X src, Function<X, T> desFunc, T def) {
		if (src == null || desFunc == null) {
			return def;
		}
		T des = desFunc.apply(src);
		return getOrDefault(des, def);
	}
	
	public static String getIfEmptyDefault(String str, String def) {
		if (str == null || str.trim().isEmpty()) {
			return def;
		}
		return str;
	}
	
	/**
	 * 如果小于等于0,则使用默认值
	 */
	public static long getIfLteZeroDefault(Long num, long def) {
		if (num == null || num <= 0) {
			return def;
		}
		return num;
	}
	
	/**
	 * 如果小于等于0,则使用默认值
	 */
	public static int getIfLteZeroDefault(Integer num, Integer def) {
		if (num == null || num <= 0) {
			return def;
		}
		return num;
	}

	/**
	 * 获得默认值
	 * 
	 * @param type
	 * @return
	 */
	public static Object getDefaultValue(Class<?> type) {
		Object value = defaultValues.get(type);
		if (value != null && value instanceof DefaultValueBuild) {
			value = ((DefaultValueBuild)value).build(type);
		}
		return value;
	}
	public static Object getConvertDefaultValue(Class<?> type) {
		return getDefaultValue(getConvertType(type));
		
	}
	
	public static Object getDefaultNotNullValue(Class<?> type) {
		return getDefaultNotNullValue(type, null);
	}
	public static Object getDefaultNotNullValue(Class<?> type, DefaultValueBuild defaultValueBuild) {
		Object value = defaultNotNullValues.get(type);
		if (value == null) {
			value = defaultNotNullValues.get(getConvertType(type));
		}
		if (value == null) {
			if (defaultValueBuild != null) {
				value = defaultValueBuild.build(type);
			}
		} else if (value instanceof DefaultValueBuild) {
			value = ((DefaultValueBuild) value).build(type);
		}
		return value;
	}
	
	public static Class<?> getConvertType(Class<?> type) {
		return canConvertTypes.get(type);
	}

	/**
	 * 字符类型通用转换. 默认不进行自动识别JSON,除非对象实现 JsonConvertValue 或 Serializable 接口
	 * 
	 * @param value
	 * @param type
	 * @return
	 * @throws ConvertValueException
	 */
	public static Object convertValue(String value, Class<?> type) throws ConvertValueException {
		return convertValue(value, type, false);
	}

	/**
	 * 字符串类型通用转换
	 * 
	 * @param value
	 * @param type
	 * @param autoJsonType 自动进行识别是否 json 类型
	 * @return
	 * @throws ConvertValueException
	 */
	public static Object convertValue(String value, Class<?> type, boolean autoJsonType) throws ConvertValueException {
		if (value == null || value.length() == 0) {
			return getDefaultValue(type);
		}
		Class<?> bindType = canConvertTypes.get(type);
		if (bindType == null) {
			if (JsonConvertValue.class.isAssignableFrom(type)) {
				bindType = JsonConvertValue.class;
			} else if (Serializable.class.isAssignableFrom(type)) {
				bindType = JsonConvertValue.class;
			} else if (autoJsonType) {
				String json = value.trim();
				if ((json.startsWith("{") && json.endsWith("}")) || (json.startsWith("[") && json.endsWith("]"))) {
					bindType = JsonConvertValue.class;
				}
			}
			if (bindType == null) {
				return null;
			} else {
				// 缓存这个绑定类型.
				canConvertTypes.put(type, bindType);
			}
		}
		try {
			if (bindType == String.class) {
				return value;
			} else if (bindType == int.class) {
				if ((value = intvalueFormat(value)) == null) {
					return getDefaultValue(type);
				}
				return Integer.parseInt(value);
			} else if (bindType == long.class) {
				if ((value = intvalueFormat(value)) == null) {
					return getDefaultValue(type);
				}
				return Long.parseLong(value.trim());
			} else if (bindType == byte.class) {
				if ((value = intvalueFormat(value)) == null) {
					return getDefaultValue(type);
				}
				return (byte) Integer.parseInt(value.trim());
			} else if (bindType == short.class) {
				if ((value = intvalueFormat(value)) == null) {
					return getDefaultValue(type);
				}
				return (short) Integer.parseInt(value.trim());
			} else if (bindType == float.class) {
				return Float.parseFloat(value.trim());
			} else if (bindType == double.class) {
				return Double.parseDouble(value.trim());
			} else if (bindType == char.class) {
				return value.charAt(0);
			} else if (bindType == boolean.class) {
				return "true".equals(value.trim().toLowerCase());
			} else if (bindType == Date.class) {
				if ((value = intvalueFormat(value)) == null) {
					return getDefaultValue(type);
				}
				try {
					Constructor<?> constructor = type.getDeclaredConstructor(long.class);
					constructor.setAccessible(true);
					return constructor.newInstance(Long.parseLong(value));
				} catch (Exception e) {
					throw new ConvertValueException("Date类型需要有一个 long 构造参", e);
				}

			} else if (bindType == JsonConvertValue.class) {
				String json = value.trim();
				if (json.startsWith("{") && json.endsWith("}")) {
					return JSON.parseObject(json, type);
				} else if (json.startsWith("[") && json.endsWith("]")) {
					return JSON.parseArray(json, type);
				} else {
					if (JsonConvertValue.class.isAssignableFrom(type)) {
						throw new ConvertValueException("类型转换错误！ 不是一个有效 JSON 类型 : " + value);
					} else {
						return getDefaultValue(type);
					}
				}
			} else {
				return getDefaultValue(type);
			}

		} catch (Exception e) {
			throw new ConvertValueException("类型转换错误！", e);
		}
	}

	public static String intvalueFormat(String value) {
		if (value == null) {
			return null;
		}
		value = value.trim(); // 清空
		if (value.length() == 0) {
			return null;
		}
		int index = value.indexOf(".");
		if (index == -1) {
			return value;
		}
		return value.substring(0, index);
	}

	private static final Map<Class<?>, Object> defaultNotNullValues = new HashMap<Class<?>, Object>();
	private static final Map<Class<?>, Object> defaultValues = new HashMap<Class<?>, Object>();
	private static final Map<Class<?>, Class<?>> canConvertTypes = new ConcurrentHashMap<Class<?>, Class<?>>();
	static {
		// 默认值
		defaultValues.put(int.class, 0);
		defaultValues.put(long.class, 0L);
		defaultValues.put(byte.class, (byte) 0);
		defaultValues.put(short.class, (short) 0);
		defaultValues.put(double.class, 0D);
		defaultValues.put(float.class, 0F);
		defaultValues.put(char.class, '\0');
		defaultValues.put(boolean.class, false);
		
		defaultNotNullValues.putAll(defaultValues);
		defaultNotNullValues.put(Integer.class, 0);
		defaultNotNullValues.put(Long.class, 0L);
		defaultNotNullValues.put(Byte.class, (byte) 0);
		defaultNotNullValues.put(Short.class, (short) 0);
		defaultNotNullValues.put(Double.class, 0.0D);
		defaultNotNullValues.put(Float.class, 0.0f);
		defaultNotNullValues.put(Character.class, '\0');
		defaultNotNullValues.put(Boolean.class, false);
		defaultNotNullValues.put(String.class, "");
		defaultNotNullValues.put(Date.class, (DefaultValueBuild)type -> new Date());
		defaultNotNullValues.put(BigDecimal.class, BigDecimal.ZERO);
		defaultNotNullValues.put(BigInteger.class, BigInteger.ZERO);
		
		// 可以转换的类型
		canConvertTypes.put(int.class, int.class);
		canConvertTypes.put(long.class, long.class);
		canConvertTypes.put(byte.class, byte.class);
		canConvertTypes.put(short.class, short.class);
		canConvertTypes.put(double.class, double.class);
		canConvertTypes.put(float.class, float.class);
		canConvertTypes.put(char.class, char.class);
		canConvertTypes.put(boolean.class, boolean.class);
		canConvertTypes.put(Integer.class, int.class);
		canConvertTypes.put(Long.class, long.class);
		canConvertTypes.put(Byte.class, byte.class);
		canConvertTypes.put(Short.class, short.class);
		canConvertTypes.put(Double.class, double.class);
		canConvertTypes.put(Float.class, float.class);
		canConvertTypes.put(Character.class, char.class);
		canConvertTypes.put(Boolean.class, boolean.class);
		canConvertTypes.put(String.class, String.class);
		canConvertTypes.put(Date.class, Date.class);
		canConvertTypes.put(java.sql.Date.class, Date.class);
		canConvertTypes.put(java.sql.Timestamp.class, Date.class);
		canConvertTypes.put(BigDecimal.class, BigDecimal.class);
		canConvertTypes.put(BigInteger.class, BigInteger.class);

	}

	
	public static interface DefaultValueBuild {
		Object build(Class<?> type);
	}
	
	
	public static interface JsonConvertValue {
	}

	public static class ConvertValueException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ConvertValueException() {
			super();
		}

		public ConvertValueException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public ConvertValueException(String message, Throwable cause) {
			super(message, cause);
		}

		public ConvertValueException(String message) {
			super(message);
		}

		public ConvertValueException(Throwable cause) {
			super(cause);
		}
	}

}
