package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 已经开始
 */
public class ApiAlreadyStartException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_ALREADY_START;

    public ApiAlreadyStartException() {
        super(responseStatus);
    }

    public ApiAlreadyStartException(String message) {
        super(responseStatus, message);
    }

    public ApiAlreadyStartException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
