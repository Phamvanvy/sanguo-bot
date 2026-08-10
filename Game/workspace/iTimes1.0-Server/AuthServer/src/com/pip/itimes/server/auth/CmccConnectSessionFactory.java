package com.pip.itimes.server.auth;

import org.apache.mina.common.IoSession;
import org.apache.commons.configuration.Configuration;

public class CmccConnectSessionFactory extends ConnectSessionFactory {

    private CmccUserCache cache;
    private CmccService cmccUserService;

    public CmccConnectSessionFactory(Configuration configuration) {
        super();
        cache = new CmccUserCache();
        cmccUserService = new CmccService(configuration.getString("cmccsender"),
                                          configuration.getString(
                "cmccchannelid"), configuration.getString("cmcccpid"),
                                          configuration.getString(
                "cmcccpserviceid"));
    }

    public CmccUserCache getUserCache(){
        return cache;
    }


    public ConnectSession createSession(IoSession session) {
        CmccConnectSession ret = new CmccConnectSession(session);
        ret.setCmccService(cmccUserService);
        ret.setCmccUserCache(cache);
        return ret;
    }
}
