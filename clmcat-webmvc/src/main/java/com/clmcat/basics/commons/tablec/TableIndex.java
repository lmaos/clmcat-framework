package com.clmcat.basics.commons.tablec;

import java.util.ArrayList;
import java.util.List;

public class TableIndex {
	// 索引名称
	private String name;
	private List<String> fieldNames = new ArrayList<>();
	private TableIndexType indexType; // 索引类型

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public TableIndexType getIndexType() {
		return indexType;
	}

	public void setIndexType(TableIndexType indexType) {
		this.indexType = indexType;
	}

}
