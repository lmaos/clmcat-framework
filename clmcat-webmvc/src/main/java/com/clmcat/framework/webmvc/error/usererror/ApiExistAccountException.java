package com.clmcat.framework.webmvc.error.usererror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiUserException;

/**
 * 已经存在账户
 */
public class ApiExistAccountException extends ApiUserException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.U_EXIST_ACCOUNT;

    public ApiExistAccountException() {
        super(responseStatus);
    }

    public ApiExistAccountException(String message) {
        super(responseStatus, message);
    }

    public ApiExistAccountException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
