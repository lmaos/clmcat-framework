package com.clmcat.basics.commons.token.entity;

/**
 * 简单TOKEN
 * 
 * @author zhangxingyu
 *
 */
public class SimpleToken {
	private long userId;
	private long invalidTime;

	public SimpleToken(long userId, long invalidTime) {
		this.userId = userId;
		this.invalidTime = invalidTime;
	}

	public long getUserId() {
		return userId;
	}

	public long getInvalidTime() {
		return invalidTime;
	}

	public boolean isInvalid() {
		return System.currentTimeMillis() >= invalidTime;
	}

	@Override
	public String toString() {
		StringBuilder json = new StringBuilder(100);
		json.append("{\"userId\":\"").append(userId).append("\",")
		.append(" \"invalidTime\":\"").append(invalidTime).append("\",")
		.append(" \"isInvalid\":").append(isInvalid()).append("}");
		return json.toString();
	}

}
