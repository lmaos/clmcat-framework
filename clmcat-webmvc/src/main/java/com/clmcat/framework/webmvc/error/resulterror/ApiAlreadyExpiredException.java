package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 已经过期，数据过期
 */
public class ApiAlreadyExpiredException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_ALREADY_EXPIRED;

    public ApiAlreadyExpiredException() {
        super(responseStatus);
    }

    public ApiAlreadyExpiredException(String message) {
        super(responseStatus, message);
    }

    public ApiAlreadyExpiredException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
