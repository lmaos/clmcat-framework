package com.clmcat.framework.webmvc.error.resulterror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiResultException;

/**
 * 已存在当前数据
 */
public class ApiExistDataException extends ApiResultException {
	
	private static final long serialVersionUID = 1L;

    public final static ResponseStatus responseStatus = ResponseStatus.R_EXIST_DATA;

    public ApiExistDataException() {
        super(responseStatus);
    }

    public ApiExistDataException(String message) {
        super(responseStatus, message);
    }

    public ApiExistDataException(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
