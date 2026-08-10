package com.pip.itimes.server.world;

import org.apache.mina.common.IoSession;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPData;

public class CmccConnectSessionFactory extends ConnectSessionFactory {
    public CmccConnectSessionFactory() {
        super();
    }


    public ConnectSession createSession(IoSession session) {
        return new CmccConnectSession(session);
    }


}
