package com.clmcat.basics.commons.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HighValueFormat {
	private final Map<String, List<FormatNode>> formatNodesCache = new ConcurrentHashMap<>();
	private final Map<String, InvokeNode> invokeNodeCache = new ConcurrentHashMap<>();
	private final int off;

	private volatile String prefix = "${";
	private volatile String suffix = "}";

	public HighValueFormat() {
		this.off = 0;
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
		configureAround(around, placeholder);
	}

	public void setPrefix(String prefix) {
		if (prefix != null && !prefix.isEmpty()) {
			this.prefix = prefix;
			formatNodesCache.clear();
		}
	}

	public void setSuffix(String suffix) {
		if (suffix != null && !suffix.isEmpty()) {
			this.suffix = suffix;
			formatNodesCache.clear();
		}
	}

	public String format(String text, Object... args) {
		if (text == null) {
			return null;
		}
		if (args == null || args.length == 0) {
			return text;
		}
		Object[] values = normalizeArgs(args);
		Map<String, HighValue> highValueMap = buildHighValueMap(values);
		List<FormatNode> formatNodes = formatNodesCache.computeIfAbsent(text, this::parserFormatNodes);
		StringBuilder stringBuilder = new StringBuilder(text.length() + 100);
		for (FormatNode formatNode : formatNodes) {
			String value = formatNode.format(values, highValueMap);
			if (value != null) {
				stringBuilder.append(value);
			}
		}
		return stringBuilder.toString();
	}

	private void configureAround(String around, String placeholder) {
		if (around == null || around.isEmpty()) {
			return;
		}
		String trimmedAround = around.trim();
		String trimmedPlaceholder = placeholder == null ? "" : placeholder.trim();
		int index = trimmedPlaceholder.isEmpty() ? -1 : trimmedAround.indexOf(trimmedPlaceholder);
		if (index != -1) {
			String customPrefix = trimmedAround.substring(0, index).trim();
			String customSuffix = trimmedAround.substring(index + trimmedPlaceholder.length()).trim();
			if (!customPrefix.isEmpty()) {
				this.prefix = customPrefix;
			}
			if (!customSuffix.isEmpty()) {
				this.suffix = customSuffix;
			}
			return;
		}
		this.prefix = trimmedAround;
		this.suffix = trimmedAround;
	}

	private Object[] normalizeArgs(Object[] args) {
		if (!(args[0] instanceof HighValues highValues)) {
			return args;
		}
		List<HighValue> merged = new ArrayList<>(highValues.highValues);
		for (int i = 1; i < args.length; i++) {
			merged.add(HighValue.valueOf(String.valueOf(merged.size()), args[i]));
		}
		return merged.toArray(new HighValue[0]);
	}

	private Map<String, HighValue> buildHighValueMap(Object[] args) {
		Map<String, HighValue> highValueMap = new HashMap<>();
		for (Object arg : args) {
			if (arg instanceof HighValue highValue) {
				highValueMap.put(highValue.getName(), highValue);
			}
		}
		return highValueMap;
	}

	private List<FormatNode> parserFormatNodes(String text) {
		List<FormatNode> formatNodes = new ArrayList<>();
		int index = 0;
		String currentPrefix = this.prefix;
		String currentSuffix = this.suffix;

		while (index < text.length()) {
			int startIndex = text.indexOf(currentPrefix, index);
			if (startIndex == -1) {
				if (index < text.length()) {
					formatNodes.add(new TextNode(text.substring(index)));
				}
				break;
			}
			if (index < startIndex) {
				formatNodes.add(new TextNode(text.substring(index, startIndex)));
			}
			int endIndex = text.indexOf(currentSuffix, startIndex + currentPrefix.length());
			if (endIndex == -1) {
				formatNodes.add(new TextNode(text.substring(startIndex)));
				break;
			}
			String keys = text.substring(startIndex + currentPrefix.length(), endIndex).trim();
			if (!keys.isEmpty()) {
				formatNodes.add(new ParamNode(keys, off));
			}
			index = endIndex + currentSuffix.length();
		}

		return List.copyOf(formatNodes);
	}

	private InvokeNode getInvokeNode(Class<?> clazz, String name) {
		if (Map.class.isAssignableFrom(clazz)) {
			return new MapInvokeNode(name);
		}
		if (HighValues.class.isAssignableFrom(clazz)) {
			return new HighValuesInvokeNode(name);
		}

		String key = clazz.getName() + "." + name;
		return invokeNodeCache.computeIfAbsent(key, k -> createInvokeNode(clazz, name));
	}

	private InvokeNode createInvokeNode(Class<?> clazz, String name) {
		Method method = findGetter(clazz, name);
		if (method != null) {
			return new MethodInvokeNode(method);
		}
		Field field = findField(clazz, name);
		if (field != null) {
			return new FieldInvokeNode(field);
		}
		return NullInvokeNode.INSTANCE;
	}

	private Method findGetter(Class<?> clazz, String name) {
		String methodSuffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		Method method = findMethod(clazz, "get" + methodSuffix);
		if (method != null) {
			return method;
		}
		return findMethod(clazz, "is" + methodSuffix);
	}

	private Method findMethod(Class<?> clazz, String methodName) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			try {
				return current.getDeclaredMethod(methodName);
			} catch (Exception e) {
				current = current.getSuperclass();
			}
		}
		try {
			return clazz.getMethod(methodName);
		} catch (Exception e) {
			return null;
		}
	}

	private Field findField(Class<?> clazz, String name) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			try {
				return current.getDeclaredField(name);
			} catch (Exception e) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	private interface FormatNode {
		String format(Object[] args, Map<String, HighValue> highValueMap);
	}

	private interface InvokeNode {
		Object invoke(Object bean) throws Exception;
	}

	private enum NullInvokeNode implements InvokeNode {
		INSTANCE;

		@Override
		public Object invoke(Object bean) {
			return null;
		}
	}

	private static class MapInvokeNode implements InvokeNode {
		private final String name;

		private MapInvokeNode(String name) {
			this.name = name;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object invoke(Object bean) {
			return ((Map<String, Object>) bean).get(name);
		}
	}

	private static class HighValuesInvokeNode implements InvokeNode {
		private final String name;

		private HighValuesInvokeNode(String name) {
			this.name = name;
		}

		@Override
		public Object invoke(Object bean) {
			return ((HighValues) bean).get(name);
		}
	}

	private static class FieldInvokeNode implements InvokeNode {
		private final Field field;

		private FieldInvokeNode(Field field) {
			this.field = field;
		}

		@Override
		public Object invoke(Object bean) throws Exception {
			if (!field.canAccess(bean)) {
				field.setAccessible(true);
			}
			return field.get(bean);
		}
	}

	private static class MethodInvokeNode implements InvokeNode {
		private final Method method;

		private MethodInvokeNode(Method method) {
			this.method = method;
		}

		@Override
		public Object invoke(Object bean) throws Exception {
			if (!method.canAccess(bean)) {
				method.setAccessible(true);
			}
			return method.invoke(bean);
		}
	}

	private class ParamNode implements FormatNode {
		private final Integer index;
		private final String highValueName;
		private final List<String> fallbackPath;
		private final List<String> highValuePath;

		private ParamNode(String keys, int off) {
			String[] segments = splitSegments(keys);
			String root = segments[0];
			if (isNumber(root)) {
				this.index = Integer.parseInt(root) - off;
				this.highValueName = null;
				this.highValuePath = List.of();
				this.fallbackPath = toPath(segments, 1);
			} else {
				this.index = null;
				this.highValueName = root;
				this.highValuePath = toPath(segments, 1);
				this.fallbackPath = toPath(segments, 0);
			}
		}

		@Override
		public String format(Object[] args, Map<String, HighValue> highValueMap) {
			Object value;
			if (index != null) {
				value = resolveFromArgs(args, index, fallbackPath);
			} else {
				value = resolveFromHighValue(highValueMap, highValueName, highValuePath);
				if (value == null) {
					value = resolveFromArgs(args, 0, fallbackPath);
				}
			}
			return toStringValue(value);
		}

		private Object resolveFromHighValue(Map<String, HighValue> highValueMap, String name, List<String> path) {
			if (highValueMap.isEmpty()) {
				return null;
			}
			HighValue highValue = highValueMap.get(name);
			if (highValue == null) {
				return null;
			}
			Object value = highValue.getValue();
			return resolvePath(value, path);
		}

		private Object resolveFromArgs(Object[] args, int argIndex, List<String> path) {
			if (argIndex < 0 || argIndex >= args.length) {
				return null;
			}
			Object value = unwrap(args[argIndex]);
			return resolvePath(value, path);
		}

		private Object resolvePath(Object value, List<String> path) {
			Object current = unwrap(value);
			if (path.isEmpty()) {
				return current;
			}
			for (String name : path) {
				if (current == null) {
					return null;
				}
				try {
					InvokeNode invokeNode = getInvokeNode(current.getClass(), name);
					current = unwrap(invokeNode.invoke(current));
				} catch (Exception e) {
					return null;
				}
			}
			return current;
		}

		private Object unwrap(Object value) {
			if (value instanceof HighValue highValue) {
				return highValue.getValue();
			}
			return value;
		}

		private String toStringValue(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof Date date) {
				return String.valueOf(date.getTime());
			}
			return String.valueOf(value);
		}

		private String[] splitSegments(String text) {
			String[] rawSegments = text.split("\\.");
			List<String> segments = new ArrayList<>(rawSegments.length);
			for (String rawSegment : rawSegments) {
				String segment = rawSegment.trim();
				if (!segment.isEmpty()) {
					segments.add(segment);
				}
			}
			return segments.toArray(new String[0]);
		}

		private List<String> toPath(String[] segments, int startIndex) {
			if (startIndex >= segments.length) {
				return List.of();
			}
			List<String> path = new ArrayList<>(segments.length - startIndex);
			for (int i = startIndex; i < segments.length; i++) {
				path.add(segments[i]);
			}
			return path;
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

	private static class TextNode implements FormatNode {
		private final String text;

		private TextNode(String text) {
			this.text = text;
		}

		@Override
		public String format(Object[] args, Map<String, HighValue> highValueMap) {
			return text;
		}
	}

	public static class HighValues {
		private final List<HighValue> highValues = new ArrayList<>(16);
		private final Map<String, HighValue> map = new HashMap<>();

		public static HighValues start(String name, Object value) {
			return new HighValues().add(name, value);
		}

		public HighValues add(String name, Object value) {
			HighValue highValue = new HighValue(name, value);
			highValues.add(highValue);
			map.put(name, highValue);
			return this;
		}

		public HighValues add(Object value) {
			return add(String.valueOf(highValues.size()), value);
		}

		public HighValue[] toArrays() {
			return highValues.toArray(new HighValue[0]);
		}

		public Object get(String name) {
			HighValue highValue = map.get(name);
			return highValue == null ? null : highValue.getValue();
		}
	}

	public static class HighValue {
		private final String name;
		private final Object value;

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
		System.out.println(format.format("aabbb${hello},${1}asd", map, 424));
		System.out.println(format.format("aabbb${hello},${1}asd", HighValues.start("hello", "21").add("dd")));
	}
}
