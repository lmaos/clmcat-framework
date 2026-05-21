package com.clmcat.basics.commons.tablec.split;

import com.clmcat.basics.commons.tablec.classtotable.CField;
import com.clmcat.basics.commons.tablec.classtotable.CPrimary;
import com.clmcat.basics.commons.tablec.classtotable.CTable;

@CTable
public class CtRecordSplitTable {
	@CPrimary
	private String labelName;
	@CPrimary
	private String tableName;
	@CField(canNull = false)
	private String mode; // hash, custom
	@CField(canNull = false)
	private int number;
	
	public static String toAllInsertSql() {
		
		return "insert into ct_record_split_table (label_name, table_name, `number`, `mode`) select '${0}','${1}', '${2}','${3}' where "
				+ " not exists(select 1 from ct_record_split_table where label_name='${0}' and table_name='${1}')";
	}
	
}
