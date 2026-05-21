package com.clmcat.basics.commons.util.call;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 线性选择, 如果没有选择到会继续选择.
 * 
 * @author zhangxingyu
 *
 */
public class LineSelect {

	
	/*
	 * 
	 * 使用方式
	 * LineSelect.submit(param)
	 * .select(v->null)
	 * .select(v->null)
	 * .select(v->null)
	 * .getResult(def); // 一直选择下去, 如果没有则返回默认值
	 * 
	 * 
	 */
	
	public static <T, R> LineSelectMessage<T, R> submit(T param) {
		return new LineSelectMessage<T, R>(param);
	} 
	
	public static <T, R> LineSelectMessage<T, R> submit(T param, Class<R> resultType) {
		return new LineSelectMessage<T, R>(param);
	}
	
	public static <T, R, KEY> LineSelectCallback<T, R, KEY> select(T param, LineSelectCall<KEY, R> call) {
		return new LineSelectCallback<T, R, KEY>(param, call);
	}
	
	
	public static class LineSelectCallback<T, R, KEY> extends LineSelectAbstrace<R> {
		private T param;
		private LineSelectCall<KEY, R> call;
		
		
		
		public LineSelectCallback(T param, LineSelectCall<KEY, R> call) {
			super();
			this.param = param;
			this.call = call;
		}

		public LineSelectCallback<T, R, KEY> condition(LineSelectCondition<T, KEY> c) {
			if (param == null || c == null) {
				return this;
			}
			if (!isDone()) {
				KEY key = c.condition(param);
				if (key != null) {
					this.result = call.select(key);
					verifyDone();
				}
			}
			return this;
		}
		
		public LineSelectCallback<T, R, KEY> condition(KEY key) {
			if (key != null && !isDone()) {
				if (key != null) {
					this.result = call.select(key);
					verifyDone();
				}
			}
			return this;
		}
		
		@Override
		public LineSelectCallback<T, R, KEY> notEmpty() {
			super.notEmpty();
			return this;
		}
		@Override
		public LineSelectCallback<T, R, KEY> gtZore() {
			super.gtZore();
			return this;
		}
	}
	
	public static class LineSelectMessage<T, R> extends LineSelectAbstrace<R> {
		
		private T param;
		
		public LineSelectMessage(T param) {
			this.param = param;
		}
		
		public  LineSelectMessage<T, R> select(LineSelectCall<T, R> call) {
			if (param == null || call == null) {
				return this;
			}
			if (!isDone()) {
				this.result = call.select(param);
				verifyDone();
			}
			return this;
		}
		
		public  LineSelectMessage<T, R> select(LineSelectGet<R> call) {
			if (call != null && !isDone()) {
				this.result = call.get();
				verifyDone();
			}
			return this;
		}
		
		
		@Override
		public LineSelectMessage<T, R>  notEmpty() {
			super.notEmpty();
			return this;
		}
		@Override
		public LineSelectMessage<T, R>  gtZore() {
			super.gtZore();
			return this;
		}
	}

	public static abstract class LineSelectAbstrace<R> {
		private boolean done;
		// 验证非空
		private boolean verifyNotEmpty;
		// 验证大于0
		private boolean verifyGtZore;
		
		protected R result;
		
		protected LineSelectAbstrace<R> notEmpty() {
			this.verifyNotEmpty = true;
			return this;
		}
		
		protected LineSelectAbstrace<R> gtZore() {
			this.verifyGtZore = true;
			return this;
		}
		
		private boolean isEmpty() {
			if (result == null) {
				return true;
			}
			if (result instanceof Collection) {
				return ((Collection<?>) result).isEmpty();
			}
			if (result instanceof Map<?, ?>) {
				return ((Map<?, ?>) result).isEmpty();
			}
			if (result instanceof CharSequence) {
				return ((CharSequence) result).length() == 0;
			}
			if (result.getClass().isArray()) {
				return Array.getLength(result) == 0;
			}
			return false;
		}
		
		private boolean isLteZore() {
			if (result == null) {
				return true;
			}
			if (result instanceof Number) {
				if (result instanceof Double) {
					return ((Double) result).compareTo(0D) <= 0;
				} else if (result instanceof Float) {
					return ((Float) result).compareTo(0F) <= 0;
				} else if (result instanceof BigDecimal) {
					return ((BigDecimal) result).compareTo(BigDecimal.ZERO) <= 0;
				} else {
					return ((Number) result).longValue() <= 0;
				}
			}
			return false;
		}
		public boolean isDone() {
			return done;
		}
		
		protected void verifyDone() {
			done = this.result != null;
			done = done && !(verifyNotEmpty && isEmpty());
			done = done && !(verifyGtZore && isLteZore());
		}
		
		public R getResult() {
			return result;
		}
		
		public R getResult(R def) {
			return result == null ? def : result;
		}
		
		public R getResult(LineSelectGet<R> def) {
			return result == null ? def.get() : result;
		}
		
	}
	
	public static interface LineSelectCall<T, R> {
		R select(T param);
	}
	
	public static interface LineSelectGet<R> {
		R get();
	}
	public static interface LineSelectCondition<T, KEY> {
		KEY condition(T param);
	}
	public static void main(String[] args) {
		
		int a = LineSelect.submit("123", int.class)
				.gtZore()
				.select(n -> 0)
				.select(n -> 5)
				.getResult();
				
		System.out.println(a);
		
		Map<String, Integer> mape = new HashMap<>();
		mape.put("a", 1234);
		mape.put("*", 999);
		
		int b = LineSelect.select("b", mape::get)
				.condition(n->n)
				.condition(n->"*")
				.getResult();
		
		System.out.println(b);
	}
}
