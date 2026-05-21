package com.clmcat.basics.commons.tablec;

public enum TableFieldType {

	INT(11), BIGINT(20), VARCHAR(256), TEXT, JSON, DATETIME, BLOB, BINARY(1024), DECIMAL(20, 8), NONE

	;

	private TableFieldType() {
	}

	private TableFieldType(String typeName, int length) {
		this.typeName = typeName;
		this.length = length;
	}

	private TableFieldType(String typeName, Integer length, Integer decimalLength) {
		this.typeName = typeName;
		this.length = length;
		this.decimalLength = decimalLength;
	}

	private TableFieldType(Integer length, Integer decimalLength) {
		this.length = length;
		this.decimalLength = decimalLength;
	}

	private TableFieldType(int length) {
		this.length = length;
	}

	public String typeName;
	public Integer length;
	public Integer decimalLength;

	public String toString(Integer length, Integer decimalLength) {
		if (length == null) {
			length = this.length;
		}
		if (decimalLength == null) {
			decimalLength = this.decimalLength;
		}
		if (this.length != null && this.decimalLength != null) {
			return String.format(getTypeName() + "(%d, %d)", length, decimalLength);
		} else if (this.length != null) {
			return String.format(getTypeName() + "(%d)", length);
		} else {
			return name();
		}
	}

	public String getTypeName() {
		return typeName == null ? name() : typeName;
	}

	@Override
	public String toString() {
		return toString(length, decimalLength);
	}

}
