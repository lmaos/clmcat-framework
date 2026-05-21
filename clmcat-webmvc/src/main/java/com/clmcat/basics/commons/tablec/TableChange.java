package com.clmcat.basics.commons.tablec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Table 变更
 * 
 * @author zhangxingyu
 *
 */
public class TableChange {

	private List<String> addFields;
	private List<String> remvoeFields;
	private Map<String, String> changeFields;
	private boolean nolock;
//	private Map<String, V>
	public List<String> getAddFields() {
		return addFields;
	}

	public void setAddFields(List<String> addFields) {
		this.addFields = addFields;
	}
	public List<String> getRemvoeFields() {
		return remvoeFields;
	}
	public void setRemvoeFields(List<String> remvoeFields) {
		this.remvoeFields = remvoeFields;
	}

	public Map<String, String> getChangeFields() {
		return changeFields;
	}
	
	public void setChangeFields(Map<String, String> changeFields) {
		this.changeFields = changeFields;
	}

	public boolean isNolock() {
		return nolock;
	}
	
	public void setNolock(boolean nolock) {
		this.nolock = nolock;
	}
	
	public TableChange putAddField(String... names) {
		if (names != null && names.length > 0) {
			if (addFields == null) {
				addFields = new ArrayList<String>();
			}
			for (String name : names) {
				addFields.add(name);
			}
		}
		return this;
	}
	
	public TableChange putRemoveField(String... names) {
		if (names != null && names.length > 0) {
			if (remvoeFields == null) {
				remvoeFields = new ArrayList<String>();
			}
			for (String name : names) {
				remvoeFields.add(name);
			}
		}
		return this;
	}
	
	/**
	 * 变更字段
	 * @param changeName  变更后的 
	 * @param beforeName  变更之前
	 * @return
	 */
	public TableChange putChangeField(String changeName, String beforeName) {
		if (changeFields == null) {
			changeFields = new LinkedHashMap<>();
		}
		changeFields.put(changeName, beforeName);
		return this;
	}
	/**
	 * 变更成为这个字段
	 * 
	 * @param changeName
	 * @return
	 */
	public TableChange putChangeField(String changeName) {
		return putChangeField(changeName, "");
	}
}
