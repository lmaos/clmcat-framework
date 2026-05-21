package com.clmcat.framework.webmvc.error;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.HttpStatusValue;

/**
 * 用户异常 status = 620
 */
public class ApiUserException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected ApiUserException(ResponseErrorStatus status, String message) {
        super(status, message);
    }

    protected ApiUserException(ResponseErrorStatus status, String message, String errplace) {
        super(status, message, errplace);
    }

    protected ApiUserException(ResponseErrorStatus status) {
        super(status);
    }

    public ApiUserException(ErrorStatus errorStatus, Object content, String message, String errplace) {
        super(HttpStatusValue.U_ERROR_STATUS, errorStatus, content, message, errplace);
    }

    public ApiUserException(ErrorStatus errorStatus, String message) {
        super(HttpStatusValue.U_ERROR_STATUS, errorStatus, message);
    }

    public ApiUserException(ErrorStatus errorStatus, Object content, String message) {
        super(HttpStatusValue.U_ERROR_STATUS, errorStatus, content, message);
    }
}
