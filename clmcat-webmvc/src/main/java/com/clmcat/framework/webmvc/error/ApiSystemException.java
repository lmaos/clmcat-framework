package com.clmcat.framework.webmvc.error;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus.HttpStatusValue;

/**
 * 系统异常 status = 500
 */
public class ApiSystemException extends ApiException {

	private static final long serialVersionUID = 1L;

	protected ApiSystemException(ResponseErrorStatus status, String message) {
        super(status, message);
    }

    protected ApiSystemException(ResponseErrorStatus status, String message, String errplace) {
        super(status, message, errplace);
    }

    protected ApiSystemException(ResponseErrorStatus status) {
        super(status);
    }

    public ApiSystemException(ErrorStatus errorStatus, Object content, String message, String errplace) {
        super(HttpStatusValue.S_ERROR_STATUS, errorStatus, content, message, errplace);
    }

    public ApiSystemException(ErrorStatus errorStatus, String message) {
        super(HttpStatusValue.S_ERROR_STATUS, errorStatus, message);
    }

    public ApiSystemException(ErrorStatus errorStatus, Object content, String message) {
        super(HttpStatusValue.S_ERROR_STATUS, errorStatus, content, message);
    }
    
    public ApiSystemException(String message) {
    	 super(ResponseStatus.SYSTEM_ERROR, message);
	}
}
