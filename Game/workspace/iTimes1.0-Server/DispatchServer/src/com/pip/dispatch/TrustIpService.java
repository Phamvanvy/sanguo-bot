package com.pip.dispatch;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TrustIpService implements Runnable{
    private int[][] trustIps;
    private File file;
    private long lastModified;
    
    private Thread workingThread;
    private boolean stopped = false;

    public TrustIpService(File file) throws Exception {
        this.file = file;
        this.lastModified = file.lastModified();
        trustIps = load(file);
        workingThread = new Thread(this);
        workingThread.start();
    }
    
    public void shutdown() {
        stopped = true;
        workingThread.interrupt();
    }

    private int[][] load(File f) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(f));
        List retList = new ArrayList();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] secs = line.split("-");
            if (secs.length != 2) {
                continue;
            }
            int[] arr = new int[2];
            arr[0] = strToIP(secs[0]);
            arr[1] = strToIP(secs[1]);
            retList.add(arr);
        }
        reader.close();
        int[][] ret = new int[retList.size()][2];
        retList.toArray(ret);
        return ret;
    }

    public int strToIP(String s) {
        String[] secs = s.split("\\.");
        return ((Integer.parseInt(secs[0]) << 24) & 0xFF000000) |
                ((Integer.parseInt(secs[1]) << 16) & 0xFF0000) |
                ((Integer.parseInt(secs[2]) << 8) & 0xFF00) |
                (Integer.parseInt(secs[3]) & 0xFF);
    }

    public int addressToIP(InetSocketAddress address){
        byte[] bytes = address.getAddress().getAddress();
        return (((bytes[0]&0xFF)<<24)&0xFF000000)|(((bytes[1]&0xFF)<<16)&0xFF0000)|(((bytes[2]&0xFF)<<8)&0xFF00)|(bytes[3]&0xFF);
    }

    public boolean isTrustIp(InetSocketAddress address){
        long ip = addressToIP(address) & 0xFFFFFFFFL;
        synchronized(this){
            for (int i = 0; i < trustIps.length; i++) {
                long start = trustIps[i][0] & 0xFFFFFFFFL;
                long end = trustIps[i][1] & 0xFFFFFFFFL;
                if (ip >= start && ip <= end) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addTrustIp(int begin,int end){
        int[][] newTrustIps = new int[trustIps.length+1][2];
        System.arraycopy(trustIps,0,newTrustIps,0,trustIps.length);
        newTrustIps[trustIps.length][0] = begin;
        newTrustIps[trustIps.length][1] = end;
        trustIps = newTrustIps;
    }


    public void run(){
        while(!stopped){
            long t = file.lastModified();
            if(t!=lastModified){
                try {
                    trustIps = load(file);
                }
                catch (Exception ex) {
                }
                lastModified = t;
            }
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex1) {
            }
        }
    }

}
