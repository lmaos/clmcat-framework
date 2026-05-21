package com.clmcat.basics.commons.groups;

import java.util.Arrays;

/**
	 * 快速分组 KEY
	 * 
	 * @author zhangxingyu
	 *
	 */
	public class GroupKey {
		Object[] values;
		String value;
		public GroupKey(Object[] values) {
			this.values = values;
			StringBuilder stringBuilder = new StringBuilder();
			for (Object object : values) {
				stringBuilder.append(object).append("-");
			}
			value = stringBuilder.toString();
		}
		
		public GroupKey of(Object... values) {
			return new GroupKey(values);
		}
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			
			if (obj instanceof GroupKey) {
				return value.equals(((GroupKey) obj).value);
			}
			return obj == this;
		}
		
		@SuppressWarnings("unchecked")
		public <T>T getObject(int i) {
			return (T)values[i];
		}
		
		public String getString(int i) {
			return values[i] == null ? null : String.valueOf(values[i]);
		}
		public int size() {
			return values.length;
		}
		@Override
		public String toString() {
			
			return Arrays.toString(values);
		}

	}
	