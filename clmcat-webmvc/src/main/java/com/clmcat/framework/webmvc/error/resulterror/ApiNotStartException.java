package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 尚未开始
 */
public class ApiNotStartException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_NOT_START;

    public ApiNotStartException() {
        super(responseStatus);
    }

    public ApiNotStartException(String message) {
        super(responseStatus, message);
    }

    public ApiNotStartException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
