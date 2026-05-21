package com.clmcat.basics.commons.format.prefix;

public class PrefixResult {
	private String prefixText;
	private Object value;

	public PrefixResult(String prefixText, Object value) {
		super();
		this.prefixText = prefixText;
		this.value = value;
	}

	public String getPrefixText() {
		return prefixText;
	}

	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
