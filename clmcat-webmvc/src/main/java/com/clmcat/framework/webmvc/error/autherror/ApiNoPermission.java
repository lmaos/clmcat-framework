package com.clmcat.framework.webmvc.error.autherror;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiAuthException;

/**
 * 无权限， 通用失败。
 */
public class ApiNoPermission extends ApiAuthException {

	private static final long serialVersionUID = 1L;
	
	public final static ResponseStatus responseStatus = ResponseStatus.AUTH_NO_PERMISSION;

    public ApiNoPermission() {
        super(responseStatus);
    }

    public ApiNoPermission(String message) {
        super(responseStatus, message);
    }

    public ApiNoPermission(String message, String errplace) {
        super(responseStatus, message, errplace);
    }

}
