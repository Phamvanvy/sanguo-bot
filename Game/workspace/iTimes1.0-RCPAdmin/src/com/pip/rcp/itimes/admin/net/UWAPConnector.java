package com.pip.rcp.itimes.admin.net;


import java.util.concurrent.Executor;

import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;


public class UWAPConnector extends SocketConnector{

    private static final ProtocolCodecFilter filter = new ProtocolCodecFilter(UWAPEncoder.class, UWAPDecoder.class);

    public UWAPConnector(int processNum, Executor executor){
        super(processNum, executor);
        setFilterChainBuilder(new UWAPFilterChainBuilder());
    }

    class UWAPFilterChainBuilder implements IoFilterChainBuilder{

        public void buildFilterChain(IoFilterChain chain) throws Exception{
            chain.addFirst("codec", filter);
            //            chain.addLast("log", logFilter);
        }

    }

}
