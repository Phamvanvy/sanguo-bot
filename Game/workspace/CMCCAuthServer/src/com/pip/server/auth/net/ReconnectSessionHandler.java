package com.pip.server.auth.net;

import org.apache.mina.common.*;

public abstract class ReconnectSessionHandler extends SessionHandler {

    private static final long RECONNECT_TIME = 5*1000L;

    private IoConnector connector;

    private boolean reconnecting = false;

    public ReconnectSessionHandler(SessionRegistry registry) {
        super(registry);
    }

    public void setConnector(IoConnector connector){
        this.connector = connector;
    }

    public void sessionClosed(IoSession session) throws Exception {
        Session s = registry.removeSession(session);
        if(!reconnecting){
            new Thread(new ReconnectWorker(session)).start();
        }
    }

    class ReconnectWorker implements Runnable{

        private IoSession session  = null;

        public ReconnectWorker(IoSession session){
            this.session = session;
        }

        public void run(){
            while(true){
                try {
                    Thread.sleep(RECONNECT_TIME);
                }
                catch (InterruptedException ex) {
                }
                System.out.println("tryreconnect");
                ConnectFuture future = connector.connect(session.getRemoteAddress(),
                                  ReconnectSessionHandler.this);
                future.join(5000L);
                if(future.isConnected()){
                    System.out.println("reconnect ok");
                    break;
                }
            }
        }
    }

}
