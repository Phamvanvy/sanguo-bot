package com.pip.rcp.itimes.admin.net;


import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;


public abstract class Session{

    private static final long RECONECT_TIME = 5 * 1000L;

    protected IoSession session;
    volatile protected int sessionId;
    private boolean reconnecting;

    public Session(IoSession session){
        this.session = session;
    }

    public Session(){

    }

    public abstract void handle(Packet packet);

    public abstract void closed();

    public abstract void created();

    public abstract void opened();

    public abstract void idle(IdleStatus status);

    public IoSession getIoSession(){
        return session;
    }

    public void setIoSession(IoSession session){
        this.session = session;
    }

    public void close(){
        if(session != null && !session.isClosing())
            session.close();
    }

    public void write(UWAPSegment seg){
        session.write(seg);
    }

    public void reply(UWAPData data){
        UWAPSegment seg = new UWAPSegment(data.getAppType(), data.toBytes(), data.getSerial(), data.getSessionId(), data.needCompress());

        write(seg);
    }

    public int getSessionId(){
        return sessionId;
    }

    public void forward(UWAPData data, int sessionId){
        UWAPSegment seg = new UWAPSegment(data, sessionId);
        write(seg);
    }

    public void reconnect(){
        if(!reconnecting){
            new Thread(new ReconnectWorker()).start();
        }
    }

    class ReconnectWorker implements Runnable{
        public void run(){
            try{
                Thread.sleep(RECONECT_TIME);
            }catch(InterruptedException ex){
            }

        }
    }
}
