package com.clmcat.basics.commons.util.locks;

import java.util.concurrent.locks.Condition;

public interface LockConsumerWrapper {
	public static interface LockComsumer {
		void exec(Condition condition) throws Exception;
	}

	public static interface LockComsumerNotThrows {
		void exec(Condition condition);
	}

	public static interface LockExecutor {
		void exec() throws Exception;
	}

	public static interface LockExecutorrNotThrows {
		void exec();
	}

	public static interface LockCallback {
		Object exec() throws Exception;
	}

}
