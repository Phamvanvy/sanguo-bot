package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountService {

    private AtomicInteger requestIds = new AtomicInteger(1);

    private Map<Integer,AccountRequest> requests = new HashMap<Integer,AccountRequest>();

    public AccountService() {
    }

    public AccountRequest registerRequest(byte appType,int serial,int sessionId, ConnectSession session) {
        AccountRequest request = new AccountRequest(requestIds.incrementAndGet(), appType,serial,sessionId, session);
        requests.put(request.id, request);
        return request;
    }

    public ReloginRequest registerReloginRequest(byte appType,int serial,int sessionId,ConnectSession session,String playerName){
        ReloginRequest request = new ReloginRequest(requestIds.incrementAndGet(),appType,serial,sessionId,session,playerName);
        requests.put(request.id,request);
        return request;
    }

    public  AccountRequest removeRequest(int id){
        return requests.remove(id);
    }

}
