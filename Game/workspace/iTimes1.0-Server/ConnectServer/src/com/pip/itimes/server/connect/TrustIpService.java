package com.pip.itimes.server.connect;

import java.net.InetSocketAddress;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TrustIpService {

    private int[][] trustIps;

    public TrustIpService() throws Exception {
        load();
    }

    private void load() throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(System.
                getProperty("user.dir") + "/trustip.txt"));
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
        synchronized (this) {
            trustIps = new int[retList.size()][2];
            retList.toArray(trustIps);
        }
    }

    public void reload(){
        try {
            load();
        } catch (Exception ex) {
        }
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
}
