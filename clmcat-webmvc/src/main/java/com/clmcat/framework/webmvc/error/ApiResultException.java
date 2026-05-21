package com.clmcat.framework.webmvc.error;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.HttpStatusValue;

/**
 * 结果异常 status = 200
 */
public class ApiResultException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ApiResultException(ResponseErrorStatus status, String message) {
		super(status, message);
	}

	public ApiResultException(ResponseErrorStatus status, String message, String errplace) {
		super(status, message, errplace);
	}

	public ApiResultException(ResponseErrorStatus status) {
		super(status);
	}

	public ApiResultException(ErrorStatus errorStatus, Object content, String message, String errplace) {
		super(HttpStatusValue.R_ERROR_STATUS, errorStatus, content, message, errplace);
	}

	public ApiResultException(ErrorStatus errorStatus, String message) {
		super(HttpStatusValue.R_ERROR_STATUS, errorStatus, message);
	}

	public ApiResultException(ErrorStatus errorStatus, Object content, String message) {
		super(HttpStatusValue.R_ERROR_STATUS, errorStatus, content, message);
	}
}
