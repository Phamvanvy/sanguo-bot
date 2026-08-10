package com.pip.itimes.server.connect;

public class ShutDownHook extends Thread {
    public ShutDownHook() {
        super();
    }

    public void run(){
        System.out.println("shutdown");
    }
}
