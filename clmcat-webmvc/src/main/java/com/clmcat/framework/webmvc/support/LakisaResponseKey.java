package com.clmcat.framework.webmvc.support;

import com.clmcat.framework.webmvc.ResponseEntityKey;

public class LakisaResponseKey implements ResponseEntityKey {

	@Override
	public String getStatusName() {
		return "status";
	}

	@Override
	public String getStateName() {
		return "state";
	}

	@Override
	public String getContentName() {
		return "data";
	}

	@Override
	public String getMessageName() {
		return "msg";
	}

	@Override
	public String getErrplaceName() {
		return "errplace";
	}

	
}
