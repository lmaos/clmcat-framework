package com.clmcat.basics.commons.tablec.classtotable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.clmcat.basics.commons.tablec.Table;
import com.clmcat.basics.commons.tablec.TableField;
import com.clmcat.basics.commons.tablec.TableFieldType;
import com.clmcat.basics.commons.tablec.TableIndex;
import com.clmcat.basics.commons.tablec.TableIndexType;

public class ClassToTable {

	private String defDbname;
	public ClassToTable() {
	}
	
	
	public ClassToTable(String defDbname) {
		this.defDbname = defDbname;
	}


	/**
	 * 
	 * @param type
	 * @param defDbname 设定默认数据库名字. 如果CTable设定了则当前值会被覆盖
	 * @return
	 */
	public Table toTable(Class<?> type) {

		Table table = new Table();
		CTable cTable = type.getAnnotation(CTable.class);
		String dbname = defDbname;
		String label = encodeName(type.getSimpleName());
		if (cTable == null) {
			table.setName(label);
			table.setLabel(label);
			table.setDbname(dbname);
		} else {
			String name = cTable.name().trim();
			if (name.isEmpty()) {
				name = label;
			}
			if (!cTable.dbname().trim().isEmpty()) {
				dbname = cTable.dbname().trim();
			}
			table.setLabel(label);
			table.setName(name);
			table.setCharsetName(cTable.charsetName());
			table.setComment(cTable.comment());
			table.setExistDrop(cTable.existDrop());
			table.setSplitLength(cTable.splitLength());
			table.setDbname(dbname);
		}

		Field[] allFields = type.getDeclaredFields();
		List<Field> fields = new ArrayList<>();
		for (Field field : allFields) {
			if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
				fields.add(field);
			}
		}
		Map<String, TableField> fieldMap = new HashMap<String, TableField>();
		List<TableField> tableFields = new ArrayList<TableField>();
		for (Field field : fields) {
			TableField tableField = new TableField();
			CField cfield = field.getAnnotation(CField.class);
			String name = null;
			TableFieldType fieldType = null;
			if (cfield == null) {
				name = encodeName(field.getName());
				fieldType = classFieldtypes.get(field.getType());
				tableField.setCanNull(!field.getType().isPrimitive());
			} else {
				name = cfield.name().trim();
				if (name.isEmpty()) {
					name = encodeName(field.getName());
				}
				fieldType = cfield.fieldType();
				if (fieldType == TableFieldType.NONE) {
					fieldType = classFieldtypes.get(field.getType());
				}
				tableField.setCanNull(cfield.canNull());
				tableField.setCharsetName(cfield.charsetName());
				tableField.setComment(cfield.comment());
				if (cfield.length() > 0) {
					tableField.setLength(cfield.length());
				}
				if (cfield.decimalLength() > 0) {
					tableField.setDecimalLength(cfield.decimalLength());
				}
				tableField.setDefaultValue(cfield.defaultValue());
				tableField.setBeforeName(encodeName(cfield.beforeName()));
			}
			if (fieldType == null) {
				fieldType = TableFieldType.VARCHAR;
			}
			tableField.setName(name);
			tableField.setFieldType(fieldType);
			fieldMap.put(field.getName(), tableField);
			tableFields.add(tableField);
		}

		List<TableIndex> indexs = new ArrayList<TableIndex>();
		List<Field> primaryFields = new ArrayList<>();
		Map<String, List<IndexEntry>> indexFields = new LinkedHashMap<String, List<IndexEntry>>();

		for (Field field : fields) {
			if (field.isAnnotationPresent(CPrimary.class)) {
				primaryFields.add(field);
			}
			CIndex cIndexs[] = field.getAnnotationsByType(CIndex.class);
			for (CIndex cIndex : cIndexs) {
				String name = cIndex.prefix() + cIndex.name();
				List<IndexEntry> cifields = indexFields.get(name);
				if (cifields == null) {
					indexFields.put(name, cifields = new ArrayList<IndexEntry>());
				}
				TableField tableField = fieldMap.get(field.getName());
				cifields.add(new IndexEntry(name, cIndex.sort(), tableField, TableIndexType.INDEX));
			}

			CUniqe cUniqes[] = field.getAnnotationsByType(CUniqe.class);
			for (CUniqe cUniqe : cUniqes) {
				String name = cUniqe.prefix() + cUniqe.name();
				List<IndexEntry> cufields = indexFields.get(name);
				if (cufields == null) {
					indexFields.put(name, cufields = new ArrayList<IndexEntry>());
				}
				TableField tableField = fieldMap.get(field.getName());
				cufields.add(new IndexEntry(name, cUniqe.sort(), tableField, TableIndexType.UNIQUE));
			}
		}
		Collections.sort(primaryFields, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				CPrimary cPrimary1 = o1.getAnnotation(CPrimary.class);
				CPrimary cPrimary2 = o2.getAnnotation(CPrimary.class);
				return ((Integer) cPrimary1.sort()).compareTo(cPrimary2.sort());
			}
		});
		if (primaryFields.size() > 0) {
			TableIndex primaryIndex = new TableIndex();
			List<String> primaryIndexFieldNames = new ArrayList<>(primaryFields.size());
			for (Field field : primaryFields) {
				TableField tableField = fieldMap.get(field.getName());
				tableField.setCanNull(false);
				CPrimary cPrimary = field.getAnnotation(CPrimary.class);
				tableField.setAutoIncrement(cPrimary.autoIncrement());
				primaryIndexFieldNames.add(tableField.getName());
			}
			primaryIndex.setFieldNames(primaryIndexFieldNames);
			primaryIndex.setIndexType(TableIndexType.PRIMARY);
			indexs.add(primaryIndex);
		}
		for (Entry<String, List<IndexEntry>> entry : indexFields.entrySet()) {
			Collections.sort(entry.getValue());
			TableIndex index = new TableIndex();
			List<String> indexFieldNames = new ArrayList<>(primaryFields.size());
			index.setName(entry.getKey());
			index.setFieldNames(indexFieldNames);
			for (IndexEntry indexEntry : entry.getValue()) {
				index.setIndexType(indexEntry.indexType);
				indexFieldNames.add(indexEntry.tableField.getName());
			}
			indexs.add(index);
		}

		table.setTableFields(tableFields);
		table.setTableIndexs(indexs);
		return table;
	}

	public String encodeName(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		StringBuilder nname = new StringBuilder(name.length() + 32);
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				if (i > 0) {
					nname.append("_");
				}
				nname.append((char) ('a' + (c - 'A')));
			} else {
				nname.append(c);
			}
		}
		return nname.toString();
	}

	public static Map<Class<?>, TableFieldType> classFieldtypes = new HashMap<>();
	static {
		classFieldtypes.put(boolean.class, TableFieldType.INT);
		classFieldtypes.put(Boolean.class, TableFieldType.INT);
		classFieldtypes.put(int.class, TableFieldType.INT);
		classFieldtypes.put(Integer.class, TableFieldType.INT);
		classFieldtypes.put(short.class, TableFieldType.INT);
		classFieldtypes.put(Short.class, TableFieldType.INT);
		classFieldtypes.put(byte.class, TableFieldType.INT);
		classFieldtypes.put(Byte.class, TableFieldType.INT);
		classFieldtypes.put(long.class, TableFieldType.BIGINT);
		classFieldtypes.put(Long.class, TableFieldType.BIGINT);
		classFieldtypes.put(double.class, TableFieldType.DECIMAL);
		classFieldtypes.put(Double.class, TableFieldType.DECIMAL);
		classFieldtypes.put(float.class, TableFieldType.DECIMAL);
		classFieldtypes.put(Float.class, TableFieldType.DECIMAL);
		classFieldtypes.put(BigDecimal.class, TableFieldType.DECIMAL);
		classFieldtypes.put(java.util.Date.class, TableFieldType.DATETIME);
		classFieldtypes.put(java.sql.Date.class, TableFieldType.DATETIME);
		classFieldtypes.put(java.sql.Timestamp.class, TableFieldType.DATETIME);
		classFieldtypes.put(String.class, TableFieldType.VARCHAR);
		classFieldtypes.put(Object.class, TableFieldType.JSON);
	}

	private static class IndexEntry implements Comparable<IndexEntry> {

		public IndexEntry(String name, int sort, TableField tableField, TableIndexType indexType) {
			super();
			this.name = name;
			this.sort = sort;
			this.tableField = tableField;
			this.indexType = indexType;
		}

		String name;
		int sort;
		TableField tableField;
		TableIndexType indexType;

		@Override
		public int compareTo(IndexEntry o) {
			return ((Integer) sort).compareTo(o.sort);
		}
	}
}