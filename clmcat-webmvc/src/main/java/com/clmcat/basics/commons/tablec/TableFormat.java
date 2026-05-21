package com.clmcat.basics.commons.tablec;

public interface TableFormat {

	String getCreateSql(Table table);
	String[] getCreateSqls(Table table, TableSplit tableSplit);

	String getAlterChangeSql(Table table,TableChange tableChange);
	String[] getAlterChangeSql(Table table,TableChange tableChange, TableSplit tableSplit);
}
