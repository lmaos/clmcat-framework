package com.clmcat.framework.webmvc.support;

import com.clmcat.framework.webmvc.ResponseEntityKey;

public class ConfigResponseKey implements ResponseEntityKey {
	private String statusName = "status";
	private String stateName = "state";
	private String contentName = "content";
	private String messageName = "message";
	private String errplaceName = "errplace";
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	public String getContentName() {
		return contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	public String getMessageName() {
		return messageName;
	}
	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}
	public String getErrplaceName() {
		return errplaceName;
	}
	public void setErrplaceName(String errplaceName) {
		this.errplaceName = errplaceName;
	}

	
}
