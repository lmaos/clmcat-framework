package com.clmcat.framework.webmvc.error.paramerror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiParameterException;

/**
 * 参数值错误
 */
public class ApiValueErrorException extends ApiParameterException {

	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.P_VALUE_ERROR;

    public ApiValueErrorException() {
        super(responseStatus);
    }

    public ApiValueErrorException(String message) {
        super(responseStatus, message);
    }

    public ApiValueErrorException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
