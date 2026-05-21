package com.clmcat.framework.webmvc.verify;

public interface TokenInfo {
	/**
	 * 用户ID
	 * @return
	 */
	Long getUserId();
	/**
	 * 是否失效
	 */
	boolean isInvalid();
    
}
