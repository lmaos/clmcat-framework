package com.clmcat.basics.commons.localex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlLocaleValueFormat implements LocaleValueFormat {

	private Map<String, List<FormatNode>> formatNodesCache = new ConcurrentHashMap<>();

	public static interface FormatNode {
		String format(Object[] args);
	}

	public static class TextFormatNode implements FormatNode {
		private StringBuilder stringBuilder = new StringBuilder(100);
		private String text;

		@Override
		public String format(Object[] args) {
			if (text == null) {
				text = stringBuilder.toString();
			}
			return text;
		}

		private void append(char c) {
			stringBuilder.append(c);
			text = null;
		}

	}

	public static class ArgFormatNode implements FormatNode {
		private int index;

		public ArgFormatNode(int index) {
			this.index = index;
		}

		@Override
		public String format(Object[] args) {
			// 如果在这里发生了数组越界的错误, 那一定是: 你自己写的参数数量不足. 请自行检查一下你的文本 %1%s %2%s
			return String.valueOf(args[index]);
		}

	}

	public String format(String text, Object... args) {
		if (args == null || args.length == 0) {
			return text;
		}
		List<FormatNode> formatNodes = getFormatNodes(text);
		StringBuilder builder = new StringBuilder(text.length() + 30);
		for (FormatNode formatNode : formatNodes) {
			builder.append(formatNode.format(args));
		}
		return builder.toString();
	}

	public List<FormatNode> getFormatNodes(String text) {

		List<FormatNode> formatNodes = formatNodesCache.get(text);
		if (formatNodes != null) {
			return formatNodes;
		}
		formatNodes = new ArrayList<FormatNode>();
		TextFormatNode node = new TextFormatNode();
		formatNodes.add(node);
		int index = -1;
		int start = -1;
		int end = -1;
		int bs = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '%') {
				if (start == -1) {
					start = i;
					end = i;
					bs = 1;
				} else if (bs == 1) {
					bs = 2;
					end = i;
				} else {
					for (int j = start; j <= end; j++) {
						node.append(text.charAt(j));
					}
					start = -1;
					end = -1;
					index = -1;
					bs = 0;
				}
			} else if (start != -1) {
				if (isNumber(c)) { // 计数

					if (bs == 1) {
						if (index == -1) {
							index = 0;
						}
						index = index * 10 + (c - '0');
						end = i;
					} else {
						for (int j = start; j <= end; j++) {
							node.append(text.charAt(j));
						}
						start = -1;
						end = -1;
						index = -1;
						bs = 0;
					}
				} else if (bs == 2 && c == 's') {
					formatNodes.add(new ArgFormatNode(index - 1));
					node = new TextFormatNode();
					formatNodes.add(node);
					start = -1;
					end = -1;
					index = -1;
					bs = 0;
				} else {
					for (int j = start; j <= end; j++) {
						node.append(text.charAt(j));
					}
					start = -1;
					end = -1;
					index = -1;
					bs = 0;
				}
			} else {
				node.append(c);
			}
		}
		formatNodesCache.put(text, formatNodes);
		return formatNodes;
	}

	private boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	public static void main(String[] args) {
		GlLocaleValueFormat format = new GlLocaleValueFormat();
		System.out.println(format.format("qwe%1%s, %2%s, %3%sghjhgh", 0,1,2,3));
	}
}
