package com.pip.itimes.server.connect;

import org.apache.mina.common.IoSession;

public abstract class ClientSessionFactory {

    private static PipClientSessionFactory pipFactory = null;
    private static CmccClientSessionFactory cmccFactory = null;

    public static ClientSessionFactory getFactory(String factory){
        if("pip".equals(factory)){
            if(pipFactory==null)
                pipFactory = new PipClientSessionFactory();
            return pipFactory;
        }
        else if("cmcc".equals(factory)){
            if(cmccFactory==null)
                cmccFactory = new CmccClientSessionFactory();
            return cmccFactory;
        }
        return null;
    }

    public abstract ClientSession createSession(IoSession session);
}
