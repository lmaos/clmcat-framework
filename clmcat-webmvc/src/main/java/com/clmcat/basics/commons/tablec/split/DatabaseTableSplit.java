package com.clmcat.basics.commons.tablec.split;

public interface DatabaseTableSplit {

	/**
	 * 创建hash表
	 * 
	 * @param bean   表关系映射
	 * @param number 分表数
	 * @return true 分表成功, false 分表失败
	 */
	boolean createHashTable(Class<?> type, String tableName, int number);
	/**
	 * 创建表
	 * @param bean
	 * @param tableName
	 * @return
	 */
	boolean createCustomTable(Class<?> type, String tableName);
	
	/**
	 * 新增字段名称
	 * @param bean
	 * @param fieldName
	 * @return
	 */
	boolean addTableField(Class<?> type, String fieldName);
	
	/**
	 * 重命名字段名称
	 * @param bean
	 * @param fieldName
	 * @return
	 */
	boolean changeTableField(Class<?> type, String oldFieldName, String newFieldName);
	
	/**
	 * 变更表字段, 只增加字段, 或改名字,  不删除字段.
	 */
	public boolean changeTable(Class<?> type) ;
	
	/**
	 * 表是否存在
	 * @param type
	 * @param tableName
	 * @return
	 */
	public boolean existTable(Class<?> type, String tableName);
}
