package com.pip.itimes.server.auth;

import com.pip.itimes.net.UWAPSegment;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ConnectService {

    private ConnectSession[] connects = new ConnectSession[10];

    public ConnectService() {
    }

    public void addConnect(ConnectSession session){

       synchronized(this){
           for(int i=0;i<connects.length;i++){
               if(connects[i]==null){
                   connects[i] = session;
                   break;
               }
           }
       }
   }

   public void broadcast(UWAPSegment seg){
//        synchronized(connects){

           for (int i = 0; i < connects.length; i++) {
               if(connects[i]!=null)
                   connects[i].write(seg);
           }
//        }
   }



   public void removeConnect(ConnectSession session){
       synchronized (this) {
           for(int i=0;i<connects.length;i++){
               if(connects[i]==session)
                   connects[i] = null;
           }
       }
   }

}
