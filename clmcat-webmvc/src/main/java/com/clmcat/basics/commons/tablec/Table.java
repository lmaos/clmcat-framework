package com.clmcat.basics.commons.tablec;

import java.util.List;

public class Table {
	private String dbname; // 数据库名字
	private String name;
	private String charsetName;
	private String engine;
	private String comment; // 备注信息
	private List<TableField> tableFields; // 表字段
	private List<TableIndex> tableIndexs; // 索引
	private int splitLength = 1;
	private boolean existDrop;
	private String label;
	
	public String getDbname() {
		return dbname;
	}
	
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<TableField> getTableFields() {
		return tableFields;
	}

	public void setTableFields(List<TableField> tableFields) {
		this.tableFields = tableFields;
	}

	public List<TableIndex> getTableIndexs() {
		return tableIndexs;
	}

	public void setTableIndexs(List<TableIndex> tableIndexs) {
		this.tableIndexs = tableIndexs;
	}

	public int getSplitLength() {
		return splitLength;
	}

	public void setSplitLength(int splitLength) {
		this.splitLength = splitLength;
	}

	public boolean isExistDrop() {
		return existDrop;
	}

	public void setExistDrop(boolean existDrop) {
		this.existDrop = existDrop;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

}
