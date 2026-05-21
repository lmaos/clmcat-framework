package com.clmcat.basics.commons.token;

public class TokenComponentException extends Exception {

	private static final long serialVersionUID = 1L;

	public TokenComponentException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TokenComponentException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public TokenComponentException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public TokenComponentException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public TokenComponentException(Throwable cause) {
		super(cause);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}
}
