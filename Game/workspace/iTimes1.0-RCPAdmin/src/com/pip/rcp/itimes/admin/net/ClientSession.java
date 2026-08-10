package com.pip.rcp.itimes.admin.net;


import java.util.ArrayList;
import java.util.List;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;


public class ClientSession extends Session{
    private List<IClientSessionListener> l = new ArrayList<IClientSessionListener>();

    public ClientSession(IoSession session){
        super(session);
    }

    public void addListener(IClientSessionListener l){
        this.l.add(l);
    }

    public void removeListener(IClientSessionListener l){
        this.l.remove(l);
    }

    public void created(){

    }

    public void idle(IdleStatus status){

    }

    public void closed(){
        fireSessionClosed();
    }

    public void handle(Packet packet){
        UWAPData data = packet.datas[0];
        if(data.getAppType() == ServerConstants.ADMIN_COMMAND){
            try{
                String s = data.readString();
                fireMessageReceive(s);
            }catch(IllegalAccessException ex){
            }
        }else{
            fireMessageReceive(packet);
        }
    }

    public void opened(){
        fireSessionOpened();
    }

    private void fireSessionOpened(){
        for(IClientSessionListener lis: l){
            lis.sessionOpened();
        }
    }

    private void fireSessionClosed(){
        for(IClientSessionListener lis: l){
            lis.sessionClosed();
        }
    }

    private void fireMessageReceive(Packet packet){
        for(IClientSessionListener lis: l){
            lis.messageReceived(packet);
        }
    }

    private void fireMessageReceive(String s){
        for(IClientSessionListener lis: l){
            lis.messageReceived(s);
        }
    }

}