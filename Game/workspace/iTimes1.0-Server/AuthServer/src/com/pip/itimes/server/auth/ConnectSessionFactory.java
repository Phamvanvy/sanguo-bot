package com.pip.itimes.server.auth;

import org.apache.mina.common.IoSession;
import org.apache.commons.configuration.Configuration;

public abstract class ConnectSessionFactory {

    private static PipConnectSessionFactory pipFactory;
    private static CmccConnectSessionFactory cmccFactory;

    public static ConnectSessionFactory getFactory(String factory,Configuration config){
       if("pip".equals(factory)){
           if(pipFactory==null)
               pipFactory = new PipConnectSessionFactory();
           return pipFactory;
       }
       else if("cmcc".equals(factory)){
           if(cmccFactory==null)
               cmccFactory = new CmccConnectSessionFactory(config);
           return cmccFactory;
       }
       return null;
    }

    public abstract ConnectSession createSession(IoSession session);
}
