package com.clmcat.basics.commons.tablec.split;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ColumnPy {
	
	static Set<String> signed = new HashSet<>();
	{
		signed.add("BIGINT");
		signed.add("INT");
	}
	static Map<String, String> alias = new HashMap<String, String>();
	{
		
	}

	public ColumnPy(ResultSet rs) throws SQLException {
		tableName = rs.getString("TABLE_NAME");
		columnName = rs.getString("COLUMN_NAME");
		typeName = rs.getString("TYPE_NAME");
		columnSize = rs.getInt("COLUMN_SIZE");
		decimalDigits = rs.getInt("DECIMAL_DIGITS");
		remarks = rs.getString("REMARKS");
		columnDef = rs.getString("COLUMN_DEF");
		nullable = rs.getInt("NULLABLE") == 1;
//		System.out.println(String.format("%s.%s,%s", tableName,columnName, nullable));
		if (signed.contains(typeName)) {
			columnSize++;
		}
		this.aliasTypeName = alias.getOrDefault(typeName, typeName);
	}
	private String tableName;
	private String columnName;
	private String typeName;
	private String aliasTypeName;
	private int columnSize;
	private int decimalDigits;
	private String remarks;
	private String columnDef;
	private boolean nullable;
	public String getTableName() {
		return tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public String getAliasTypeName() {
		return aliasTypeName;
	}
	public String getTypeName() {
		return typeName;
	}
	public int getColumnSize() {
		return columnSize;
	}
	public int getDecimalDigits() {
		return decimalDigits;
	}
	public String getRemarks() {
		return remarks;
	}
	public String getColumnDef() {
		return columnDef;
	}
	/**
	 * true 可以是null, false 不可以null
	 */
	public boolean isNullable() {
		return nullable;
	}
	
	@Override
	public int hashCode() {
		return columnName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ColumnPy) {
			return ((ColumnPy) obj).columnName.equals(this.columnName);
		}
		return false;
	}
	

/*
 * 
TABLE_CAT=test
TABLE_SCHEM=null
TABLE_NAME=test_month_table_202212
COLUMN_NAME=status
DATA_TYPE=12
TYPE_NAME=VARCHAR
COLUMN_SIZE=10
BUFFER_LENGTH=65535
DECIMAL_DIGITS=null
NUM_PREC_RADIX=10
NULLABLE=0
REMARKS=
COLUMN_DEF=create
SQL_DATA_TYPE=0
SQL_DATETIME_SUB=0
CHAR_OCTET_LENGTH=10
ORDINAL_POSITION=4
IS_NULLABLE=NO
SCOPE_CATALOG=null
SCOPE_SCHEMA=null
SCOPE_TABLE=null
SOURCE_DATA_TYPE=null
IS_AUTOINCREMENT=NO
IS_GENERATEDCOLUMN=NO
 */
}
