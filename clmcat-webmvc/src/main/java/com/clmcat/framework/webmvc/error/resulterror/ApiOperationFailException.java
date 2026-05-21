package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 操作失败
 */
public class ApiOperationFailException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_OPERATION_FAIL;

    public ApiOperationFailException() {
        super(responseStatus);
    }

    public ApiOperationFailException(String message) {
        super(responseStatus, message);
    }

    public ApiOperationFailException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
