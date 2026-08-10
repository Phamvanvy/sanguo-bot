package com.pip.itimes.server.world;

import org.apache.mina.common.IoSession;

public abstract class ConnectSessionFactory {

    private static PipConnectSessionFactory pipFactory = null;
    private static CmccConnectSessionFactory cmccFactory = null;
    private static QQConnectSessionFactory qqFactory = null;

    public static ConnectSessionFactory getFactory(String factory){
        if("pip".equals(factory)){
            if(pipFactory==null)
                pipFactory = new PipConnectSessionFactory();
            return pipFactory;
        }
        else if("cmcc".equals(factory)){
            if(cmccFactory==null)
                cmccFactory = new CmccConnectSessionFactory();
            return cmccFactory;
        }
        else if("qq".equals(factory)){
            if(qqFactory==null)
                qqFactory = new QQConnectSessionFactory();
            return qqFactory;
        }
        return null;
    }

    public abstract ConnectSession createSession(IoSession session);
}
