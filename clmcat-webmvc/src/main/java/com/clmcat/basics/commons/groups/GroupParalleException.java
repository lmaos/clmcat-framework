package com.clmcat.basics.commons.groups;

/**
	 * 并行执行发生异常
	 * 
	 * @author zhangxingyu
	 *
	 */
	public class GroupParalleException extends RuntimeException {

		private final long serialVersionUID = 1L;


		public GroupParalleException(String message, Throwable cause) {
			super(message, cause);
		}

		public GroupParalleException(String message) {
			super(message);
		}

		public GroupParalleException(Throwable cause) {
			super(cause);
		}
		
	}