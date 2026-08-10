package com.pip.itimes.server.world;

import java.util.Calendar;
import java.util.Locale;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TimeService implements Runnable{

    private ConnectService connectService;
    private int count = 0;

    public TimeService() {
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException ex) {
            }
            count++;
            int time = (int)((System.currentTimeMillis()+8*3600*1000)/1000);
            connectService.syncTime(time);
            if(count==200){
                count = 0;
                connectService.logOnline();
            }
        }
    }

    public void start(){
        new Thread(this).start();
    }
}
