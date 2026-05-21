package com.clmcat.framework.webmvc.error.usererror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiUserException;

/**
 * 用户被冻结
 */
public class ApiFreezeException extends ApiUserException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.U_FREEZE;

    public ApiFreezeException() {
        super(responseStatus);
    }

    public ApiFreezeException(String message) {
        super(responseStatus, message);
    }

    public ApiFreezeException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
