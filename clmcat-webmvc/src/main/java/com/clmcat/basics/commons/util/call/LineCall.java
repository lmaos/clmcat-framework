package com.clmcat.basics.commons.util.call;
/**
 * 线性调用
 * @author zhangxingyu
 *
 */
public class LineCall {

	public static <R> LineCallMessage<R> call(LineCallExecute<R> exec) { // 执行一个数据的调用
		return new LineCallMessage<R>(exec);
	}

	public static <R> LineCallMessage<R> submit(R r) { // 执行一个数据的调用
		return new LineCallMessage<R>(r);
	}

	public static class LineCallMessage<R> {
		private Object result;
		private ErrorPatam error;

		public LineCallMessage(LineCallExecute<R> exec) {
			try {
				this.result = exec.exec();
			} catch (Exception e) {
				error = new ErrorPatam();
				error.setError(e);
			}
		}

		public LineCallMessage(Object result) {
			this.result = result;
		}

		public <X> LineCallMessage<X> next(LineCallThen<X, R> exec) {
			try {
				this.result = result == null ? null : exec.exec((R) result);
			} catch (Exception e) {
				error = new ErrorPatam();
				error.setError(e);
				result = null; // 中断传递
			}
			return (LineCallMessage<X>) this;
		}

		public R getResult() {
			return (R) result;
		}

		public R getResult(R def) {
			if (result == null) {
				return def;
			} else {
				return (R) result;
			}
		}

		public LineCallCatchMessage<R> callCatch(LineCallCatch<R> c) {
			R r = getResult();
			R nr = c != null && error != null ? c.catchCall(error) : r;
			return new LineCallCatchMessage<R>(nr, error == null ? null : error.getError());
		}

		public LineCallCatchMessage<R> execCatch(LineCallCatchNoResult c) {
			R r = getResult();
			if (c != null && error != null) {
				c.catchCall(error);
			}
			return new LineCallCatchMessage<R>(r, error == null ? null : error.getError());
		}
	}

	public static class LineCallCatchMessage<R> {

		private R result;
		private Exception error;

		public LineCallCatchMessage(R result, Exception error) {
			super();
			this.result = result;
		}

		public R getResult() {
			return (R) result;
		}

		public R getResult(R def) {
			if (result == null) {
				return def;
			} else {
				return (R) result;
			}
		}

		public R throwOrGetResult() throws Exception {
			if (error != null) {
				throw error;
			} else {
				return getResult();
			}
		}

		public R throwOrGetResult(R def) throws Exception {
			if (error != null) {
				throw error;
			} else {
				if (result == null) {
					return def;
				} else {
					return (R) result;
				}
			}
		}
	}

	public static interface LineCallCatch<R> {
		R catchCall(ErrorPatam e);
	}

	public static interface LineCallCatchNoResult {
		void catchCall(ErrorPatam e);
	}

	public static interface LineCallExecute<R> {
		R exec() throws Exception;
	}

	public static interface LineCallThen<R, T> {
		R exec(T t) throws Exception;
	}

	public static class ErrorPatam {
		private Exception error;

		void setError(Exception error) {
			this.error = error;
		}

		public Exception getError() {
			return error;
		}

	}

}