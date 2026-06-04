package com.clmcat.framework.webmvc.error.usererror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiUserException;

/**
 * 在黑名单里
 */
public class ApiBlacklistException extends ApiUserException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_BLACKLIST;

    public ApiBlacklistException() {
        super(responseStatus);
    }

    public ApiBlacklistException(String message) {
        super(responseStatus, message);
    }

    public ApiBlacklistException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
