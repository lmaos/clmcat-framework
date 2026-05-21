package com.clmcat.framework.webmvc.error.usererror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiUserException;

/**
 * 注册失败
 */
public class ApiRegisterFailException extends ApiUserException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.U_REGISTER_FAIL;

    public ApiRegisterFailException() {
        super(responseStatus);
    }

    public ApiRegisterFailException(String message) {
        super(responseStatus, message);
    }

    public ApiRegisterFailException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
