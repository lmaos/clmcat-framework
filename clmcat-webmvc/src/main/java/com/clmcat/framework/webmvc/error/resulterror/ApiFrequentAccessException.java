package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 频繁访问
 */
public class ApiFrequentAccessException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.L_FREQUENT_ACCESS;

    public ApiFrequentAccessException() {
        super(responseStatus);
    }

    public ApiFrequentAccessException(String message) {
        super(responseStatus, message);
    }

    public ApiFrequentAccessException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
