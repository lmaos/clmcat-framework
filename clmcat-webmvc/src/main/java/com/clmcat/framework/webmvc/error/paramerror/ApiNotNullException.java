package com.clmcat.framework.webmvc.error.paramerror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiParameterException;

/**
 * 参数不能是 NULL
 */
public class ApiNotNullException extends ApiParameterException {

	private static final long serialVersionUID = 1L;
	
	public final static ResponseStatus responseStatus = ResponseStatus.P_NOTNULL;

	public ApiNotNullException() {
		super(responseStatus);
	}

	public ApiNotNullException(String message) {
		super(responseStatus, message);
	}

	public ApiNotNullException(String message, String errplace) {
		super(responseStatus, message, errplace);
	}

}
