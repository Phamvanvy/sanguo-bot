package com.pip.itimes.net;


import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
//import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import java.util.concurrent.Executor;

public class UWAPConnector extends SocketConnector {

    private static final ProtocolCodecFilter filter = new ProtocolCodecFilter(
            UWAPEncoder.class, UWAPDecoder.class);
//    private static final LoggingFilter logFilter = new LoggingFilter();

    public UWAPConnector(int processNum,Executor executor) {
        super(processNum,executor);
        setFilterChainBuilder(new UWAPFilterChainBuilder());
    }


    class UWAPFilterChainBuilder implements IoFilterChainBuilder {

        public void buildFilterChain(IoFilterChain chain) throws Exception {
            chain.addFirst("codec", filter);
//            chain.addLast("log", logFilter);
        }

    }

}
