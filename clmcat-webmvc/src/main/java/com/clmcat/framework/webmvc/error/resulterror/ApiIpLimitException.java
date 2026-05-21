package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * IP 限制
 */
public class ApiIpLimitException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_IP_LIMIT;

    public ApiIpLimitException() {
        super(responseStatus);
    }

    public ApiIpLimitException(String message) {
        super(responseStatus, message);
    }

    public ApiIpLimitException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
