package com.clmcat.framework.webmvc.verify;

import java.net.InetAddress;

public class DefaultTokenInfo implements TokenInfo {

    private long userId;
    private InetAddress remoteAddr;
    private long invalidTime;

    public DefaultTokenInfo(long userId, InetAddress remoteAddr, long invalidTime) {
        this.userId = userId;
        this.remoteAddr = remoteAddr;
        this.invalidTime = invalidTime;
    }
    @Override
    public Long getUserId() {
        return userId;
    }

    public InetAddress getRemoteAddr() {
        return remoteAddr;
    }

    public long getInvalidTime() {
        return invalidTime;
    }

    @Override
    public String toString() {
        return "TokenInfo{" +
                "userId=" + userId +
                ", remoteAddr=" + remoteAddr +
                ", invalidTime=" + invalidTime +
                '}';
    }
    @Override
    public boolean isInvalid() {
        return System.currentTimeMillis() > invalidTime;
    }
}
