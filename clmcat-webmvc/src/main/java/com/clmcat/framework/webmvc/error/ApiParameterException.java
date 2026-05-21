package com.clmcat.framework.webmvc.error;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.HttpStatusValue;

/**
 * 参数异常 status = 620
 */
public class ApiParameterException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected ApiParameterException(ResponseErrorStatus status, String message) {
        super(status, message);
    }

    protected ApiParameterException(ResponseErrorStatus status, String message, String errplace) {
        super(status, message, errplace);
    }

    protected ApiParameterException(ResponseErrorStatus status) {
        super(status);
    }

    public ApiParameterException(ErrorStatus errorStatus, Object content, String message, String errplace) {
        super(HttpStatusValue.P_ERROR_STATUS, errorStatus, content, message, errplace);
    }

    public ApiParameterException(ErrorStatus errorStatus, String message) {
        super(HttpStatusValue.P_ERROR_STATUS, errorStatus, message);
    }

    public ApiParameterException(ErrorStatus errorStatus, Object content, String message) {
        super(HttpStatusValue.P_ERROR_STATUS, errorStatus, content, message);
    }
}
