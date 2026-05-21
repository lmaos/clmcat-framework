package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 数据不存在
 */
public class ApiNoExistDataException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_NOEXIST_DATA;

    public ApiNoExistDataException() {
        super(responseStatus);
    }

    public ApiNoExistDataException(String message) {
        super(responseStatus, message);
    }

    public ApiNoExistDataException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
