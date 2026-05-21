package com.clmcat.basics.commons.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighValueFormat {
	private Map<String, List<FormatNode>> formatNodesCache = new ConcurrentHashMap<>(); // 格式缓存
	private Map<String, InvokeNode> invokeNodeCache = new ConcurrentHashMap<>(); // 调用缓存
	private static Logger log = LoggerFactory.getLogger(HighValueFormat.class);
	private int off = 0;
	public HighValueFormat() {
		
	}
	public HighValueFormat(String around) {
		this(around, "?");
	}
	public HighValueFormat(String around, int off) {
		this(around, "?", off);
	}
	public HighValueFormat(String around, String placeholder) {
		this(around, placeholder, 0);
	}
	public HighValueFormat(String around, String placeholder, int off) {
		this.off = off;
		if (around != null && around.length() > 0) {
			around = around.trim();
			int index = around.indexOf(placeholder);
			if (index != -1) {
				String prefix = around.substring(0, index).trim();
				String suffix = around.substring(index + placeholder.length()).trim();
				if (!prefix.isEmpty()) {
					this.prefix = prefix;
				}
				if (!suffix.isEmpty()) {
					this.suffix = suffix;
				}
			} else {
				this.prefix = around;
				this.suffix = around;
			}
		}
	}
	
	private String prefix = "${";
	private String suffix = "}";
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	private static interface FormatNode {
		String format(Object[] args, Map<String, HighValue> highValueMap);
	}

	private static interface InvokeNode {
		Object invoke(Object bean) throws Exception;
	}

	public String format(String text, Object... args) {
		if (args == null || args.length == 0) {
			return text;
		}
		if (args[0] != null && args[0] instanceof HighValues) {
			HighValues highValues = (HighValues) args[0];
			for (int i = 1; i < args.length; i++) {
				highValues.add(args[i]);
			}
			args = highValues.toArrays();
		}

		Map<String, HighValue> highValueMap = new HashMap<>();
		for (Object arg : args) {
			if (arg instanceof HighValue) {
				HighValue highValue = (HighValue) arg;
				highValueMap.put(highValue.getName(), highValue);
			}
		}
		// ${0.fieldName}, ${0}
		List<FormatNode> formatNodes = formatNodesCache.get(text);
		if (formatNodes == null) {
			formatNodes = parserFormatNodes(text);
			formatNodesCache.put(text, formatNodes);
		}
		StringBuilder stringBuilder = new StringBuilder(text.length() + 100);
		for (FormatNode formatNode : formatNodes) {
			String v = formatNode.format(args, highValueMap);
			if (v != null) {
				stringBuilder.append(v);
			}
		}

		return stringBuilder.toString();
	}

	private List<FormatNode> parserFormatNodes(String text) {
		// ${0.fieldName}, ${0}
		List<FormatNode> formatNodes = new ArrayList<>();
		int index = 0;

		while (index < text.length()) {
			int startIndex = text.indexOf(prefix, index);
			if (startIndex == -1) { // 结束了
				if (index < text.length()) {
					String subtext = text.substring(index, text.length());
					formatNodes.add(new TextNode(subtext));
				}
				index = text.length();
			} else {
				int endIndex = text.indexOf(suffix, startIndex + prefix.length());
				if (endIndex == -1) { // 已经没有结束位置
					
					String subtext = text.substring(index, text.length());
					formatNodes.add(new TextNode(subtext));
					
					index = text.length();
//					index = startIndex + prefix.length();
				} else {
					if (index < startIndex) {
						String subtext = text.substring(index, startIndex);
						formatNodes.add(new TextNode(subtext));
					}
					String keys = text.substring(startIndex + prefix.length(), endIndex).trim();
					index = endIndex + suffix.length();
					// 解析keys
					if (!keys.isEmpty()) {
						formatNodes.add(new ParamNode(keys, off));
					}

				}
			}
		}

		return formatNodes;
	}

	private InvokeNode getInvokeNode(Class<?> clazz, String name) {
		if (Map.class.isAssignableFrom(clazz)) {
			return new MapInvokeNode(name);
		}
		if (HighValues.class.isAssignableFrom(clazz)) {
			return new HighValuesInvokeNode(name);
		}
		
		String key = clazz.getName() + "." + name;
		InvokeNode invokeNode = this.invokeNodeCache.get(key);
		if (invokeNode == null) {
			String methodName = "get" + (char) (name.charAt(0) - 'a' + 'A') + name.substring(1);
			try {
				Method method = clazz.getDeclaredMethod(methodName);
				invokeNode = new MethodInvokeNode(method);
			} catch (Exception e) {
			}
			if (invokeNode == null) {
				try {
					Field field = clazz.getDeclaredField(name);
					invokeNode = new FieldInvokeNode(field);
				} catch (Exception e) {
				}
			}
			if (invokeNode == null) {
				invokeNode = new NullInvokeNode();
			}
			invokeNodeCache.put(key, invokeNode);
		}
		return invokeNode;
	}

	private static class NullInvokeNode implements InvokeNode {
		@Override
		public Object invoke(Object bean) throws Exception {
			return null;
		}
	}

	private static class MapInvokeNode implements InvokeNode {
		private String name;

		public MapInvokeNode(String name) {
			this.name = name;
		}

		@Override
		public Object invoke(Object bean) throws Exception {
			return ((Map<String, Object>) bean).get(name);
		}
	}
	
	private static class HighValuesInvokeNode implements InvokeNode {
		private String name;

		public HighValuesInvokeNode(String name) {
			this.name = name;
		}
		@Override
		public Object invoke(Object bean) throws Exception {
			HighValues highValues = (HighValues)bean;
			return highValues.get(name);
		}
		
	}

	private static class FieldInvokeNode implements InvokeNode {
		private Field field;

		public FieldInvokeNode(Field field) {
			this.field = field;
		}

		@Override
		public Object invoke(Object bean) throws Exception {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return field.get(bean);
		}
	}

	private class MethodInvokeNode implements InvokeNode {
		private Method method;

		public MethodInvokeNode(Method method) {
			this.method = method;
		}

		@Override
		public Object invoke(Object bean) throws Exception {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			return method.invoke(bean);
		}

	}

	private class ParamNode implements FormatNode {
		private Integer index;
		private String key;
		private String name;
		private int off;
		public ParamNode(String keys, int off) {
			this.off = off;
			int di = keys.indexOf(".");
			if (di == -1) {
				if (isNumber(keys)) {
					this.index = Integer.parseInt(keys) - off;
				} else {
					this.name = keys;
				}
			} else {
				String key = keys.substring(0, di).trim();
				if (isNumber(key)) {
					this.index = Integer.parseInt(key) - off;
				} else {
					this.key = key;
				}
				this.name = keys.substring(di + 1);
			}
		}

		@Override
		public String format(Object[] args, Map<String, HighValue> highValueMap) {
			Integer index = this.index;
			if (index == null) {
				if (highValueMap.size() > 0) {
					if (key == null) {
						HighValue highValue = highValueMap.get(name);
						if (highValue != null) {
							return highValue.toString();
						}
					} else {
						HighValue highValue = highValueMap.get(key);
						if (highValue != null) {
							try {
								InvokeNode invokeNode = getInvokeNode(highValue.getValue().getClass(), name);
								Object value = invokeNode.invoke(highValue.getValue());
								return value == null ? null : value.toString();
							} catch (Exception e) {
								log.error("highLocale-invoke error", e);
								return null;
							}
						}
					}
				}
				index = 0;
			}
			
			if (index < 0 || index >= args.length || args.length == 0) {
				return null;
			}
			
			Object arg = args[index];
			if (arg == null) {
				return null;
			}
			if (arg instanceof HighValue) {
				arg = ((HighValue) arg).getValue();
				if (arg == null) {
					return null;
				}
			}

			String value = null;
			if (name == null) {
				value = toString(arg);
			} else {
				try {
					InvokeNode invokeNode = getInvokeNode(arg.getClass(), name);
					Object fvalue = invokeNode.invoke(arg);
					if (fvalue != null) {
						value = toString(fvalue);
					}

				} catch (Exception e) {
				}
			}
			return value;
		}

		private String toString(Object arg) {
			if (arg == null) {
				return null;
			}
			String value = null;
			if (arg instanceof Date) {
				value = String.valueOf(((Date) arg).getTime());
			} else {
				value = String.valueOf(arg);
			}
			return value;
		}

		private boolean isNumber(String text) {
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c < '0' || c > '9') {
					return false;
				}
			}
			return true;
		}
	}

	private class TextNode implements FormatNode {
		private String text;

		public TextNode(String text) {
			this.text = text;
		}

		@Override
		public String format(Object[] args, Map<String, HighValue> highValueMap) {
			return text;
		}
	}

	/**
	 * 高级设值.
	 * 
	 * @author zhangxingyu
	 *
	 */
	public static class HighValues {
		List<HighValue> highValues = new ArrayList<HighValue>(16);
		Map<String, HighValue> map = new HashMap<>();

		public static HighValues start(String name, Object value) {
			return new HighValues().add(name, value);
		}

		public HighValues add(String name, Object value) {
			HighValue highValue = new HighValue(name, value);
			highValues.add(new HighValue(name, value));
			map.put(name, highValue);
			return this;
		}

		public HighValues add(Object value) {

			highValues.add(new HighValue(String.valueOf(highValues.size()), value));
			return this;
		}

		public HighValue[] toArrays() {
			return highValues.toArray(new HighValue[highValues.size()]);
		}
		
		public Object get(String name) {
			return map.get(name);
		}
	}

	public static class HighValue {
		private String name;
		private Object value;

		public static HighValue valueOf(String name, Object value) {
			return new HighValue(name, value);
		}

		public HighValue(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public String toString() {
			return value == null ? null : value.toString();
		}
	}

	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<>();
		map.put("hello", "hello world");

		HighValueFormat format = new HighValueFormat();
		System.out.println(format.format("${0},${1}", 123, 424));
		System.out.println(format.format("aabbb${0},${1}asd", 123, 424));
		// 支持0参数为扩展类型
		System.out.println(format.format("aabbb${hello},${1}asd", map, 424));
		System.out.println(format.format("aabbb${hello},${1}asd", HighValues.start("hello", "21").add("dd")));
	}
}