package com.clmcat.basics.commons.tablec;

public class TableField {

	private String name;

	private TableFieldType fieldType;

	private Integer length; // 字段长度

	private Integer decimalLength; // 字段长度

	private boolean canNull = true;

	private String defaultValue; // 默认值

	private boolean autoIncrement; // 自动增长

	private String charsetName;

	private String comment;

	private String beforeName;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TableFieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(TableFieldType fieldType) {
		this.fieldType = fieldType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDecimalLength() {
		return decimalLength;
	}

	public void setDecimalLength(Integer decimalLength) {
		this.decimalLength = decimalLength;
	}

	public boolean isCanNull() {
		return canNull;
	}

	public void setCanNull(boolean canNull) {
		this.canNull = canNull;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue == null ? null : String.valueOf(defaultValue);
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getBeforeName() {
		return beforeName;
	}
	
	public void setBeforeName(String beforeName) {
		this.beforeName = beforeName;
	}
	
	public int length() {
		return getLength() == null ? 0 : getLength();
	}
	public int decimalLength() {
		return getDecimalLength() == null ? 0 : getDecimalLength();
	}
}
