package com.pip.itimes.server.auth;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class CmccUserCache {

    private static final Logger log = Logger.getLogger(CmccUserCache.class);

    private Map<String,CmccUserKey> users = new HashMap<String,CmccUserKey>();

    public void addUserKey(CmccUserKey userKey){
        log.info("add Cmcc["+userKey.getUserId()+","+userKey.getKey()+"]");
        users.put(userKey.getUserId(),userKey);
    }

    public CmccUserKey getUserKey(String userId){
        return users.get(userId);
    }

    public boolean isValid(String userId,String key){
        log.info("verify Cmcc["+userId+","+key+"]");
        CmccUserKey u = users.get(userId);
        if(u==null)
            return false;
        if(u.getKey().equals(key))
            return true;
        return false;
    }
}
