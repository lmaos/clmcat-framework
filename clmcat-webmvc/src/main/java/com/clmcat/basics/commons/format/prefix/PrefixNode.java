package com.clmcat.basics.commons.format.prefix;

import java.util.HashMap;
import java.util.Map;

/**
 * 前缀单词节点
 * 
 * @author zhangxingyu
 *
 */
public class PrefixNode {

	private char word;

	private Object value;

	private String prefixText;

	private Map<Character, PrefixNode> next = new HashMap<>();

	PrefixNode() {
		
	}
	private PrefixNode(char c) {
		this.word = c;
	}

	public PrefixNode createOrGet(char c) {
		PrefixNode node = next.get(c);
		if (node == null) {
			node = new PrefixNode(c);
			next.put(c, node);
		}
		return node;
	}

	public PrefixNode next(char c) {
		PrefixNode node = next.get(c);
		return node;
	}

	public Object setValue(Object value) {
		Object old = this.value;
		this.value = value;
		return old;
	}

	public Object getValue() {
		return value;
	}

	public void setPrefixText(String prefixText) {
		this.prefixText = prefixText;
	}

	public String getPrefixText() {
		return prefixText;
	}

	public char getWord() {
		return word;
	}

}
