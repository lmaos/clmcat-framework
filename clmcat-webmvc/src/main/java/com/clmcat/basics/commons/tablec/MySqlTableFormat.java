package com.clmcat.basics.commons.tablec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MySqlTableFormat implements TableFormat {
	private final TableSplit defaultTableSplit = new SimpleTableSplit();

	@Override
	public String getCreateSql(Table tb) {

		return formatCreateSql(tb, tb.getName());
	}

	@Override
	public String[] getCreateSqls(Table table, TableSplit tableSplit) {
		if (tableSplit == null) {
			tableSplit = defaultTableSplit;
		}
		String[] names = tableSplit.getTableName(table);
		String[] sqls = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			sqls[i] = formatCreateSql(table, names[i]);
		}

		return sqls;
	}
	
	private void appendFieldAttr(StringBuilder sql, TableField tf) {
		String charsetName = tf.getCharsetName();
		boolean canNull = tf.isCanNull();
		String defaultValue = tf.getDefaultValue();
		String comment = tf.getComment();
		boolean autoIncrement = tf.isAutoIncrement();
		TableFieldType ftype = tf.getFieldType();
		Integer length = tf.getLength();
		Integer decimalLength = tf.getDecimalLength();
		append(sql, " ", ftype.toString(length, decimalLength));
		if (charsetName != null && !charsetName.isEmpty()) {
			append(sql, " CHARACTER SET ", charsetName);
		}
		if (!canNull) {
			append(sql, " NOT NULL ");
		}
		if (defaultValue != null && !defaultValue.isEmpty()) {
			append(sql, " DEFAULT '", defaultValue, "'");
		}
		if (comment != null && !comment.isEmpty()) {
			append(sql, " COMMENT '", comment, "'");
		}
		if (autoIncrement) {
			append(sql, " AUTO_INCREMENT");
		}
	}

	private String formatCreateSql(Table tb, String tableName) {
		StringBuilder sql = new StringBuilder(1024);
		if (tb.isExistDrop()) {
			if (isBlank(tb.getDbname())) {
				append(sql, "DROP TABLE IF EXISTS `", tableName, "`;\n");
			} else {
				append(sql, "DROP TABLE IF EXISTS `", tb.getDbname(), "`.`", tableName, "`;\n");
			}
		}
		if (isBlank(tb.getDbname())) {
			append(sql, "CREATE TABLE `", tableName, "` ( \n");
		} else {
			append(sql, "CREATE TABLE `", tb.getDbname(), "`.`", tableName, "` ( \n");
		}
		List<TableField> tableFields = tb.getTableFields();
		for (TableField tf : tableFields) {
			String fname = tf.getName();
			append(sql, "`", fname, "`");
			appendFieldAttr(sql, tf);
			append(sql, ",\n");
		}

		List<TableIndex> tableIndexs = tb.getTableIndexs();
		if (tableIndexs != null && tableIndexs.size() > 0) {
			for (TableIndex ti : tableIndexs) {
				String name = ti.getName();
				List<String> fieldNames = ti.getFieldNames();
				TableIndexType indexType = ti.getIndexType(); // 索引类型
				if (indexType == TableIndexType.PRIMARY) {
					append(sql, "PRIMARY KEY (");
				}
				if (indexType == TableIndexType.INDEX) {
					append(sql, "KEY ", name, " (");
				}
				if (indexType == TableIndexType.UNIQUE) {
					append(sql, "UNIQUE KEY ", name, " (");
				}
				for (int i = 0; i < fieldNames.size(); i++) {
					String fname = fieldNames.get(i);
					if (i > 0) {
						append(sql, ",", "`", fname, "`");
					} else {
						append(sql, "`", fname, "`");
					}
				}
				append(sql, "),\n");
			}
		}
		if (sql.charAt(sql.length() - 2) == ',') {
			sql.setLength(sql.length() - 2);
		}
		sql.append("\n) ");
		String charsetName = tb.getCharsetName();
		String engine = tb.getEngine();
		String comment = tb.getComment();
		engine = engine == null ? "InnoDB" : engine;
		charsetName = charsetName == null ? "utf8" : charsetName;
		append(sql, "ENGINE=", engine, " DEFAULT CHARSET=", charsetName);
		if (comment != null && !comment.isEmpty()) {
			append(sql, " COMMENT '", comment,"'");
		}
		sql.append(";");
		return sql.toString();
	}

	private void append(StringBuilder sqlBuild, Object... args) {
		for (Object arg : args) {
			sqlBuild.append(arg);
		}
	}

	public static void main(String[] args) {
		Table table = new Table();
		table.setName("test_x");
		List<TableField> tableFields = new ArrayList<TableField>();
		TableField tableField = new TableField();
		tableField.setName("id");
		tableField.setFieldType(TableFieldType.BIGINT);
		tableField.setAutoIncrement(true);
		tableFields.add(tableField);
		table.setTableFields(tableFields);
		List<TableIndex> tableIndexs = new ArrayList<TableIndex>();
		TableIndex tableIndex = new TableIndex();
		tableIndex.setIndexType(TableIndexType.PRIMARY);
		tableIndex.setFieldNames(Arrays.asList("id"));
		tableIndexs.add(tableIndex);
		table.setTableIndexs(tableIndexs);
		;
		table.setSplitLength(100);
		table.setExistDrop(true);
		MySqlTableFormat format = new MySqlTableFormat();
//		System.out.println(format.getInsertSql(table));
		String[] sqls = format.getCreateSqls(table, null);
		for (String sql : sqls) {
			System.out.println(sql);
		}
	}

	@Override
	public String getAlterChangeSql(Table table, TableChange tableChange) {
		return formatAlterChangeSql(table, tableChange, table.getName());
	}
	
	@Override
	public String[] getAlterChangeSql(Table table, TableChange tableChange, TableSplit tableSplit) {
		if (tableSplit == null) {
			tableSplit = defaultTableSplit;
		}
		String[] names = tableSplit.getTableName(table);
		String[] sqls = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			sqls[i] = formatAlterChangeSql(table, tableChange, names[i]);
		}

		return sqls;
	}
	
	public String formatAlterChangeSql(Table table, TableChange tableChange, String tableName) {
		//ADD COLUMN `ddd` VARCHAR(45) NULL FIRST;
		/**
		    ALTER TABLE `funbit`.`gift_settle` 
			ADD COLUMN `gift_settlecol` VARCHAR(45) NULL AFTER `create_time`,
			CHANGE COLUMN `free` `free` TINYINT(4) NULL DEFAULT NULL COMMENT '免费的' ,
			CHANGE COLUMN `sender_id` `sender_id` BIGINT(20) NOT NULL COMMENT '发送用户.' ,
			CHANGE COLUMN `receive_time` `receive_time2` BIGINT(20) NOT NULL COMMENT '发送时间' ,
			DROP PRIMARY KEY,
			ADD PRIMARY KEY (`id`, `order_no`),
			DROP INDEX `idx_giftsend_orderno` ,
			ADD UNIQUE INDEX `idx_giftsend_xorderno` (`order_no` ASC),
			ADD INDEX `eee` (`locale` ASC),
			ALGORITHM=INPLACE, LOCK=NONE;
		 */
		List<TableField> tableFields = table.getTableFields();
		Map<String, TableField> tableFieldMap = new HashMap<String, TableField>();
		Map<String, String> prevFieldNames = new HashMap<String, String>();
		String prevFieldName= null;
		for (TableField tableField : tableFields) {
			String fname = tableField.getName();
			tableFieldMap.put(fname, tableField);
			if (prevFieldName != null) {
				prevFieldNames.put(fname, prevFieldName);
			}
			prevFieldName = fname;
		}
		StringBuilder sql = new StringBuilder(1024);
		if (isBlank(table.getDbname())) {
			append(sql, "ALTER TABLE `", tableName, "` \n");
		} else {
			append(sql, "ALTER TABLE `", table.getDbname(), "`.`", tableName, "` \n");
		}
		// DROP COLUMN `no`;
		List<String> removeFields = tableChange.getRemvoeFields();
		if (removeFields != null) {
			for (String fieldName : removeFields) {
				TableField tf = tableFieldMap.get(fieldName);
				if (tf == null) {
					append(sql, "DROP COLUMN `", fieldName, "`,\n");
				}
			}
		}
		List<String> addFields = tableChange.getAddFields();
		if (addFields != null) {
			for (String fieldName : addFields) {
				TableField tf = tableFieldMap.get(fieldName);
				if (tf != null) {
					append(sql, "ADD COLUMN `", fieldName, "`");
					appendFieldAttr(sql, tf);
					String prevFName = prevFieldNames.get(fieldName);
					if (prevFName == null) {
						append(sql, " FIRST");
					} else {
						append(sql, " AFTER ", prevFName);
					}
					append(sql, ",\n");
				}
			}
		}
		Map<String, String> changeFileds = tableChange.getChangeFields();
		if (changeFileds != null) {
			for (Entry<String, String> entry : changeFileds.entrySet()) {
				String changeName = entry.getKey();
				TableField tf = tableFieldMap.get(changeName);
				String beforeName = isBlank(entry.getValue()) ? tf.getBeforeName() : entry.getValue();
				beforeName = isBlank(beforeName) ? changeName : beforeName;
				if (tf != null) {
					append(sql, "CHANGE COLUMN `", beforeName, "` `", changeName, "`");
					appendFieldAttr(sql, tf);
					append(sql, ",\n");
				}
			}
		}
		if (sql.charAt(sql.length() - 2) == ',') {
			sql.setLength(sql.length() - 2);
		}
		if (tableChange.isNolock()) {
			sql.append(",ALGORITHM=INPLACE, LOCK=NONE");
		}
		sql.append(";");
		
		return sql.toString();
	}
	
	boolean isBlank(String xx) {
		return xx == null || xx.trim().isEmpty();
	}
}
