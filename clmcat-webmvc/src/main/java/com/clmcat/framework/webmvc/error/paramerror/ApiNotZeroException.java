package com.clmcat.framework.webmvc.error.paramerror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiParameterException;

/**
 * 参数必须大于0
 */
public class ApiNotZeroException extends ApiParameterException {
	
	private static final long serialVersionUID = 1L;
	
    public final static ResponseStatus responseStatus = ResponseStatus.P_NOTZERO;

    public ApiNotZeroException() {
        super(responseStatus);
    }

    public ApiNotZeroException(String message) {
        super(responseStatus, message);
    }

    public ApiNotZeroException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
