package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 已经结束
 */
public class ApiAlreadyOverException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_ALREADY_OVER;

    public ApiAlreadyOverException() {
        super(responseStatus);
    }

    public ApiAlreadyOverException(String message) {
        super(responseStatus, message);
    }

    public ApiAlreadyOverException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
