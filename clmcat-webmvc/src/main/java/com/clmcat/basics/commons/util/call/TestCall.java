package com.clmcat.basics.commons.util.call;

public class TestCall {

	
	// 如果是真则执行, 如果是假则执行
	// iftrue().iffalse().get()
	
	public static TestCallMessage<?> test(boolean value) {
		return new TestCallMessage<>(value, null);
	}
	public static <P>TestCallMessage<P> test(boolean value, P param) {
		return new TestCallMessage<>(value, param);
	}
	
	public static interface TestCallback<P> {
		boolean test(P p);
	}
	public static interface TestExecute<P> {
		void exec(P p);
	}

	public static class TestCallMessage<P> {
		private boolean value;
		private P param;

		TestCallMessage(boolean value, P p) {
			this.value = value;
		}

		public TestCallMessage<P> iftrue(TestCallback<P> testCallback) {
			if (value) {
				value = testCallback.test(param);
			}
			return this;
		}

		public TestCallMessage<P> iffalse(TestCallback<P> testCallback) {
			if (!value) {
				value = testCallback.test(param);
			}
			return this;
		}
		public TestCallMessage<P> iftrue(TestExecute<P> testExecute) {
			if (value) {
				testExecute.exec(param);
			}
			return this;
		}
		
		public TestCallMessage<P> iffalse(TestExecute<P> testExecute) {
			if (!value) {
				testExecute.exec(param);
			}
			return this;
		}

		public boolean get() {
			return value;
		}
	}
	
	public static void main(String[] args) {
		boolean ok = test(true)
		.iftrue(p->{
			System.out.println("iftrue - 1");
			return false;
		})
		.iftrue(p -> {
			System.out.println("iftrue - 2");
		})
		.iftrue(p-> {
			System.out.println("iftrue - 3");
		}).iffalse(p -> {
			System.out.println("iffalse - 1");
		})
		.get();
		
		System.out.println(ok);
	}
}
