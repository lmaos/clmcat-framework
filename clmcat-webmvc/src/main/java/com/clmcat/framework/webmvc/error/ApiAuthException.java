package com.clmcat.framework.webmvc.error;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.HttpStatusValue;

/**
 * 权限异常 status = 610
 */
public class ApiAuthException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected ApiAuthException(ResponseStatus status, String message) {
        super(status, message);
    }

    protected ApiAuthException(ResponseStatus status, String message, String errplace) {
        super(status, message, errplace);
    }

    protected ApiAuthException(ResponseStatus status) {
        super(status);
    }

    public ApiAuthException(ErrorStatus errorStatus, Object content, String message, String errplace) {
        super(HttpStatusValue.A_ERROR_STATUS, errorStatus, content, message, errplace);
    }

    public ApiAuthException(ErrorStatus errorStatus, String message) {
        super(HttpStatusValue.A_ERROR_STATUS, errorStatus, message);
    }

    public ApiAuthException(ErrorStatus errorStatus, Object content, String message) {
        super(HttpStatusValue.A_ERROR_STATUS, errorStatus, content, message);
    }
}
