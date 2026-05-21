package com.clmcat.basics.commons.actives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.clmcat.basics.commons.util.locks.SingleLock;

/**
 * 选择一个或多个满足范围条件的.
 * 
 * @author zhangxingyu
 *
 */
public class Selects {

	private List<SelectEntity> selectEntities = new ArrayList<Selects.SelectEntity>();
	private List<SelectEntity> selects = null;
	private SingleLock lock = new SingleLock();

	public static Selects of() {
		return new Selects();
	}

	public static Selects of(Value... values) {
		Selects selects = new Selects();
		Arrays.sort(values);
		List<SelectEntity> selectEntities = new ArrayList<Selects.SelectEntity>();
		SelectEntity prev = null;
		for (Value value : values) {
			SelectEntity entity = new SelectEntity();
			entity.min = value.num;
			entity.max = Long.MAX_VALUE;
			entity.value = value.value;
			if (prev != null && value.num != prev.min) { // == 如果这个和之前到值一样
				prev.max = value.num - 1;
			}
			prev = entity;
			selectEntities.add(entity);
		}

		selects.selectEntities = selectEntities;
		return selects;
	}

	public Selects add(int min, int max, Object value) {
		lock.synchronizedLockNotThrows(() -> {
			SelectEntity selectEntity = new SelectEntity();
			selectEntity.min = min;
			selectEntity.max = max;
			selectEntities.add(selectEntity);
			this.selects = null;
		});
		return this;
	}

	public Selects sort(boolean desc) { // 排序一下, 排序可以优化查询
		lock.synchronizedLockNotThrows(() -> {
			if (desc) { // 倒序
				selectEntities.sort((o1, o2) -> {
					if (o2.min == o1.min) {
						return ((Long) o2.max).compareTo(o1.max);
					} else {
						return ((Long) o2.min).compareTo(o1.min);
					}
				});
			} else { // 正序
				selectEntities.sort((o1, o2) -> {
					if (o1.min == o2.min) {
						return ((Long) o1.max).compareTo(o2.max);
					} else {
						return ((Long) o1.min).compareTo(o2.min);
					}
				});
			}
			this.selects = null;
		});
		return this;
	}

	private List<SelectEntity> getSelects() {
		List<SelectEntity> selects = this.selects;
		if (selects == null) {
			try {
				selects = lock.synchronizedLock(() -> {
					if (this.selects == null) {
						this.selects = new ArrayList<>(selectEntities);
					}
					return this.selects;
				});
			} catch (Exception e) {
				return null;
			}
		}
		return selects;
	}

	public Object get(long num) { // 获得1个
		List<SelectEntity> selects = getSelects();
		if (selects == null) {
			return null;
		}

		for (SelectEntity se : selects) {
			if (num >= se.min && num <= se.max) {
				return se.value;
			}
		}

		return null;
	}

	public List<Object> getAll(long num) {
		List<Object> list = new ArrayList<Object>();
		List<SelectEntity> selects = getSelects();
		if (selects == null) {
			return list;
		}
		for (SelectEntity se : selects) {
			if (num >= se.min && num <= se.max) {
				list.add(se.value);
			}
		}
		return list;
	}

	/**
	 * 选择实体
	 * 
	 * @author zhangxingyu
	 *
	 */
	private static class SelectEntity {
		private long min;
		private long max;
		private Object value;

		@Override
		public String toString() {
			return "SelectEntity [min=" + min + ", max=" + max + ", value=" + value + "]";
		}

	}

	public static class Value implements Comparable<Value> {
		private long num;
		private Object value;

		public static Value of(long num, Object value) {
			Value selectValue = new Value();
			selectValue.num = num;
			selectValue.value = value;
			return selectValue;
		}

		@Override
		public int compareTo(Value o) {
			return ((Long) num).compareTo(o.num);
		}
	}

	public static void main(String[] args) {
		Selects selects = Selects.of(Value.of(10, 1), Value.of(30, 2));
		// 获取满足这个条件范围的对象.
		System.out.println(selects.get(20));
	}
}
