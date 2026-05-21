package com.clmcat.basics.commons.tablec.split;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.clmcat.basics.commons.format.HighValueFormat;
import com.clmcat.basics.commons.tablec.MySqlTableFormat;
import com.clmcat.basics.commons.tablec.Table;
import com.clmcat.basics.commons.tablec.TableChange;
import com.clmcat.basics.commons.tablec.TableField;
import com.clmcat.basics.commons.tablec.TableFieldType;
import com.clmcat.basics.commons.tablec.classtotable.ClassToTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMySqlTableSplit implements DatabaseTableSplit {
	Logger log = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	private String catalog;
	private ClassToTable classToTable;
	MySqlTableFormat mySqlTableFormat = new MySqlTableFormat();
	ThreadLocal<TransConnection> local = new ThreadLocal<>();
	HighValueFormat valueFormat = new HighValueFormat();
	private boolean existRecordTable = false;
	String recordInsertSql = CtRecordSplitTable.toAllInsertSql();

	public SimpleMySqlTableSplit(DataSource dataSource) {
		this.dataSource = dataSource;
		try (Connection conn = dataSource.getConnection()) {
			this.catalog = conn.getCatalog();
		} catch (Exception e) {
			log.error("", e);
		}
		this.classToTable = new ClassToTable(this.catalog);
	}

	public SimpleMySqlTableSplit(DataSource dataSource, String catalog) {
		super();
		this.dataSource = dataSource;
		this.catalog = catalog;
		this.classToTable = new ClassToTable(this.catalog);
	}

	@Override
	public boolean createHashTable(Class<?> type, String tableName, int number) {
		Table table = classToTable.toTable(type);
		if (tableName == null) {
			tableName = table.getName(); 
		}
		if (valueFormat.format(tableName, "root").equals(tableName)) {
			tableName = tableName + "_${0}";
		}
		
		if (getSplitTables(table.getLabel()).size() == 0) {
			try {
				String fTableName = tableName;
				return trans(conn -> {
					initRecordTable();
					try (Statement statement = conn.createStatement();) {
						for (int i = 0; i < number; i++) {
							String currTableName = valueFormat.format(fTableName, i);
							currTableName = classToTable.encodeName(currTableName);
							table.setName(currTableName);
							if (!existTable(currTableName)) {
								String createSql = mySqlTableFormat.getCreateSql(table);
								log.info("创建HASH分区表,\n{}", createSql);
								statement.executeUpdate(createSql);
							}
						}
					}
					try (Statement statement = conn.createStatement();) {
						for (int i = 0; i < number; i++) {
							String currTableName = valueFormat.format(fTableName, i);
							currTableName = classToTable.encodeName(currTableName);
							String insertSql = valueFormat.format(recordInsertSql, table.getLabel(), currTableName, i, "hash");
							statement.executeUpdate(insertSql);
						}
					}
					return true;
				});
			} catch (Exception e) {
				log.error("创建表失败!");
				throw new DatabaseSplitException("创建表失败!", e);
			}
		}
		return true;
	}

	@Override
	public boolean createCustomTable(Class<?> type, String tableName) {
		tableName = classToTable.encodeName(tableName);
		Table table = classToTable.toTable(type);
		if (tableName != null) {
			table.setName(tableName);
		}
		tableName = table.getName();
		if (!existTable(table.getLabel(), tableName)) {
			// hash 表不可用
			if ("hash".equals(getSplitTableMode(table.getLabel()))) {
				log.error("hash 表不能自定义创建");
				return false;
			}
			
			return transx(conn -> {
				initRecordTable();
				if (createTable(table)) {
					String insertSql = valueFormat.format(recordInsertSql, table.getLabel(), table.getName(), 0, "custom");
					try (Statement statement = conn.createStatement();) {
						boolean x= statement.executeUpdate(insertSql) > 0;
						return x;
					}
				} else {
					return false;
				}
			}, "创建表失败!");
		}
		return true;
	}

	public boolean createTable(Table table) {
		String tableName = table.getName();
		if (!existTable(tableName)) {
			if (tableName != null) {
				table.setName(tableName);
			}
			String createSql = mySqlTableFormat.getCreateSql(table);
			try {
				return trans(conn -> {
					try (Statement statement = conn.createStatement();) {
						log.info("执行创建分区表,\n{}", createSql);
						statement.executeUpdate(createSql);
						return existTable(tableName);
					}
				});
			} catch (Exception e) {
				log.error("创建表失败!");
				throw new DatabaseSplitException("创建表失败!", e);
			}
		}
		return true;
	}

	private void initRecordTable() {
		if (!existRecordTable) {
			Table table = classToTable.toTable(CtRecordSplitTable.class);
			existRecordTable = createTable(table);
		}
	}

	@Override
	public boolean addTableField(Class<?> type, String fieldName) {
		TableChange change = new TableChange();
		change.putAddField(fieldName);
		change.setNolock(true);
		Table table = classToTable.toTable(type);
		List<String> splitTableNames = getSplitTables(table.getLabel());
		if (splitTableNames.size() == 0) {
			return false;
		}
		return transx(conn -> {
			try (Statement statement = conn.createStatement();) {
				for (String tableName : splitTableNames) {
					String column = classToTable.encodeName(fieldName);
					Set<String> cou = getTableColumns(tableName, column);
					if (!cou.contains(column)) {
						String sql = mySqlTableFormat.formatAlterChangeSql(table, change, tableName);
						statement.addBatch(sql);
					}
				}
				statement.executeBatch();
			}
			return true;
		}, "");

	}

	@Override
	public boolean changeTableField(Class<?> type, String oldFieldName, String newFieldName) {
		Table table = classToTable.toTable(type);
		TableChange change = new TableChange();
		change.putChangeField(newFieldName, oldFieldName);
		change.setNolock(true);
		List<String> splitTableNames = getSplitTables(table.getLabel());
		if (splitTableNames.size() == 0) {
			return false;
		}
		return transx(conn -> {
			try (Statement statement = conn.createStatement();) {
				for (String tableName : splitTableNames) {
					String sql = mySqlTableFormat.formatAlterChangeSql(table, change, tableName);
					statement.addBatch(sql);
				}
				statement.executeBatch();
			}
			return true;
		}, "");
	}
	
	/**
	 * 自动验证变更表
	 * @param type
	 * @return
	 */
	public boolean changeTable(Class<?> type) {
		Table table = classToTable.toTable(type);
		List<String> splitTableNames = getSplitTables(table.getLabel());
		if(splitTableNames.size() == 0) {
			return false;
		}
		List<TableField> fields = table.getTableFields();
		return transx(conn -> {
			try (Statement statement = conn.createStatement();) {
				for (String tableName : splitTableNames) {
					TableChange asyncChange = new TableChange();
					asyncChange.setNolock(true);
					TableChange syncChange = new TableChange();
					Map<String, ColumnPy> columns = getTableColumns(tableName);
					
					for (TableField tableField : fields) {
						ColumnPy columnPy = columns.get(tableField.getName());
						if (columnPy == null) { // 如果不存在这个列的时候
							if (!isBlank(tableField.getBeforeName())) { // 如果是修改属性则这样子
								columnPy = columns.get(tableField.getBeforeName());
							}
							if (columnPy == null) {
								asyncChange.putAddField(tableField.getName());
							}
						}  
						if (columnPy != null) { // 存在这个列, 验证其他属性
							boolean canAsyncChange = false;
							boolean canSyncChange = false;
							if (!isBlank(tableField.getBeforeName()) // 之前的存在
									&& isBlankEquals(tableField.getBeforeName(), columnPy.getColumnName()) // 之前的等于数据库
									&& !isBlankEquals(tableField.getBeforeName(), tableField.getName()) // 之前的不等于当前定义的
									) {
								canAsyncChange = true;
							} else if (!isBlankEquals(columnPy.getRemarks(), tableField.getComment())) {
								canAsyncChange = true;
							} else if (tableField.isCanNull() != columnPy.isNullable()) {
								// 不能为空的时候必须设置默认值. 否则不生效.
								if (!tableField.isCanNull()) {
									canAsyncChange = !isBlank(tableField.getDefaultValue());
								} else {
									canAsyncChange = true;
								}
							} else if (!isBlankEquals(columnPy.getColumnDef(), tableField.getDefaultValue())) {
								canAsyncChange = true;
							}
							// 下面的必须是 同步的才能变更.
							if (!columnPy.getAliasTypeName().equals(tableField.getFieldType().name())) {
								canSyncChange = true;
							} else if (tableField.getLength() != null && columnPy.getColumnSize() != tableField.getLength()) {
								if (tableField.getFieldType() == TableFieldType.VARCHAR && columnPy.getColumnSize() < tableField.getLength()) {
									canAsyncChange = true;
								} else {
									canSyncChange = true;
								}
							} else if (tableField.getDecimalLength() != null && columnPy.getDecimalDigits() != tableField.getDecimalLength()) {
								canSyncChange = true;
							} 
							if (canSyncChange) {
								syncChange.putChangeField(tableField.getName());
							} else if (canAsyncChange) {
								asyncChange.putChangeField(tableField.getName());
							}
						}
					}
					
					if (asyncChange.getAddFields() != null && asyncChange.getAddFields().size() > 0 || asyncChange.getChangeFields() != null && asyncChange.getChangeFields().size() > 0) {
						String sql = mySqlTableFormat.formatAlterChangeSql(table, asyncChange, tableName);
						statement.addBatch(sql);
						log.info("[无锁]{}表结构发生变化,变更SQL: \n{}",tableName, sql);
					}
					if (syncChange.getChangeFields() != null && syncChange.getChangeFields().size() > 0) {
						String sql = mySqlTableFormat.formatAlterChangeSql(table, syncChange, tableName);
						statement.addBatch(sql);
						log.info("[同步]{}表结构发生变化,变更SQL: \n{}",tableName, sql);
					}
				}
				int [] status = statement.executeBatch();
				return status != null && status.length > 0;
			}
		},"");
		
	}
	
	public boolean existTable(Class<?> type, String tableName) {
		Table table = classToTable.toTable(type);
		return existTable(table.getLabel(), tableName);
	}
	
	private Set<String> getTableColumns(String table, String column) {
		Set<String> cs = new LinkedHashSet<String>();
		return transx(conn -> {
			DatabaseMetaData metaData = conn.getMetaData();
			try (ResultSet rs = metaData.getColumns(catalog, null, table, column)) {
				while( rs.next()) {
					String columnName = rs.getString("COLUMN_NAME");
					cs.add(columnName);
				}
			}
			return cs;
		},"");
	}
	/**
	 * 所有列属性
	 * @param table
	 * @return
	 */
	private Map<String, ColumnPy> getTableColumns(String table) {
		Map<String, ColumnPy> cs = new LinkedHashMap<>();
		return transx(conn -> {
			DatabaseMetaData metaData = conn.getMetaData();
			try (ResultSet rs = metaData.getColumns(catalog, null, table, null)) {
				while( rs.next()) {
					ColumnPy columnPy = new ColumnPy(rs);
					cs.putIfAbsent(columnPy.getColumnName(), columnPy);
				}
			}
			return cs;
		},"");
	}

	private List<String> getSplitTables(String label) {
		if (!existTable("ct_record_split_table")) {
			return new ArrayList<String>();
		}
		
		String sql = "select table_name from ct_record_split_table where label_name = '" + label+"'";
		return transx(conn -> {
			try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(sql);) {
				List<String> tableNames = new ArrayList<>();
				while (rs.next()) {
					tableNames.add(rs.getString(1));
				}
				return tableNames;
			}
		}, "获取所有分表发生异常");
	}
	
	private String getSplitTableMode(String label) {
		if (!existTable("ct_record_split_table")) {
			return "";
		}
		
		String sql = "select mode from ct_record_split_table where label_name = '" + label+"' limit 1";
		return transx(conn -> {
			try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(sql);) {
				return rs.next() ? rs.getString(1) : "";
			}
		}, "获取所有分表发生异常");
	}
	
	private boolean existTable(String label, String table) {
		if (!existTable("ct_record_split_table")) {
			return false;
		}
		String sql = "select 1 from ct_record_split_table where label_name = '" + label+"' and table_name='"+table+"'";
		boolean existLabel = transx(conn -> {
			try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(sql);) {
				return rs.next();
			}
		}, "获取所有分表发生异常");
		
		return existLabel && existTable(table);
	}

	private boolean existTable(String tableName) {
		try {
			return trans(conn -> {
				DatabaseMetaData metaData = conn.getMetaData();
				try (ResultSet rs = metaData.getTables(catalog, null, tableName, new String[] { "TABLE" })) {
					return rs.next();
				}
			});
		} catch (Exception e) {
			throw new DatabaseSplitException(e);
		}
	}

	public <R> R trans(TransExec<R> exec) throws Exception {
		try (TransConnection tconn = getTransConnection();) {
			R r = exec.exec(tconn.conn);
			tconn.commit();
			return r;
		} catch (Exception e) {
			throw e;
		}
	}

	public <R> R transx(TransExec<R> exec, String err) {
		try {
			return trans(exec);
		} catch (DatabaseSplitException e) {
			log.error(err);
			throw e;
		} catch (Exception e) {
			throw new DatabaseSplitException(err, e);
		}
	}

	@FunctionalInterface
	private interface TransExec<R> {
		R exec(Connection conn) throws Exception;
	}

	private TransConnection getTransConnection() throws SQLException {
		TransConnection tconn = local.get();
		if (tconn == null) {
			tconn = new TransConnection(dataSource.getConnection());
			local.set(tconn);;
		}
		tconn.reference++;
		return tconn;
	}

	private class TransConnection implements AutoCloseable {

		final Connection conn;
		int reference;
		boolean autoCommit;

		public TransConnection(Connection conn) throws SQLException {
			this.conn = conn;
			this.autoCommit = conn.getAutoCommit();
			if (autoCommit) {
				conn.setAutoCommit(false);
			}
		}

		@Override
		public void close() throws Exception {
			reference--;
			if (reference == 0) {
				conn.rollback();
				if (autoCommit) {
					conn.setAutoCommit(autoCommit);
				}
				conn.close();
				local.remove();
			}
		}

		public void commit() throws SQLException {
			if (reference == 1) {
				conn.commit();
			}
		}
	}
	boolean isBlank(String xx) {
		return xx == null || xx.trim().isEmpty();
	}
	
	boolean isBlankEquals(String a, String b) {
		if (a == b) {
			return true;
		}
		a = a == null ? "" : a;
		b = b == null ? "" : b;
		return a.equals(b);
	}

}
