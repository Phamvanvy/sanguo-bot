package com.pip.server.auth;

import org.apache.mina.common.IoSession;
import org.apache.commons.configuration.Configuration;

import com.pip.server.auth.cmcc.CmccConnectSessionFactory;

/**
 * 连接会话工厂。
 */
public abstract class ConnectSessionFactory {
    private static CmccConnectSessionFactory cmccFactory;

    public static ConnectSessionFactory getFactory(String factory, Configuration config) throws Exception {
        if ("cmcc".equals(factory)) {
            if (cmccFactory == null)
                cmccFactory = new CmccConnectSessionFactory(config);
            return cmccFactory;
        }
        return null;
    }

    public abstract ConnectSession createSession(IoSession session);
    
    public abstract void shutdown();
}
