package com.pip.server.auth.cmcc;

import org.apache.mina.common.IoSession;
import org.apache.commons.configuration.Configuration;

import com.pip.server.auth.ConnectSession;
import com.pip.server.auth.ConnectSessionFactory;

/**
 * 卓望版本连接会话工厂。
 */
public class CmccConnectSessionFactory extends ConnectSessionFactory {
    private CmccUserCache cache;
    private CmccService cmccUserService;

    public CmccConnectSessionFactory(Configuration configuration) throws Exception {
        super();
        cache = new CmccUserCache();
        cmccUserService = new CmccService(
                configuration.getString("cmccurl"), 
                configuration.getString("cmccsender"), 
                configuration.getString("cmccchannelid"), 
                configuration.getString("cmcccpid"), 
                configuration.getString("cmcccpserviceid"),
                configuration.getString("cmccversion")
        );
        cache.setCmccService(cmccUserService);
    }

    public CmccUserCache getUserCache() {
        return cache;
    }
    
    public CmccService getUserService() {
        return cmccUserService;
    }

    public ConnectSession createSession(IoSession session) {
        CmccConnectSession ret = new CmccConnectSession(session);
        ret.setCmccService(cmccUserService);
        ret.setCmccUserCache(cache);
        return ret;
    }
    
    public void shutdown() {
        cache.close();
        cmccUserService.shutdown();
    }
}
