package com.pip.itimes.server.auth;

public class CmccUserKey {

    private String userId;
    private String key;

    public CmccUserKey(String userId,String key) {
        this.userId = userId;
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public String getKey() {
        return key;
    }
}
