package com.pip.itimes.server.connect;

import java.net.*;

import org.apache.commons.configuration.Configuration;
import java.io.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class IpdService implements Runnable{

    private ClientService clientService;
    private String[] url = null;

    private Configuration configuration;

    private String protocol;

    public IpdService(String protocol) {
        this.protocol = protocol;
    }

    public void setClientService(ClientService clientService){
        this.clientService = clientService;
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
        url = configuration.getStringArray("ipd");
    }

    private URL buildUrl(String url) throws Exception {
        String s = null;
        if(protocol.equals("http")){
            s = url + "?name=" +
                       URLEncoder.encode(configuration.getString("serverid"), "GBK") +
                       "&url=" +
                       URLEncoder.encode(protocol + "://" +
                                         configuration.getString("localip") + ":" +
                                         configuration.getString("port")+"/", "GBK") +
                       "&maxnum=" +
                       clientService.getMaxPlayer() + "&curnum=" +
                   clientService.size();
        }else
            s = url + "?name=" +
                   URLEncoder.encode(configuration.getString("serverid"), "GBK") +
                   "&url=" +
                   URLEncoder.encode(protocol + "://" +
                                     configuration.getString("localip") + ":" +
                                     configuration.getString("port"), "GBK") +
                   "&maxnum=" +
                   clientService.getMaxPlayer() + "&curnum=" +
                   clientService.size();
        return new URL(s);
    }

    private void connect() throws Exception{
        for(int i=0;i<url.length;i++){
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) (buildUrl(url[i]).openConnection());
                System.out.println(conn.getResponseCode());
            } catch (IOException ex) {
            } catch (Exception ex) {
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }
    }

    public void start(){
        new Thread(this).start();
    }

    public void run(){
        while(true){
            try {
                connect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException ex1) {
            }
        }
    }

//    public static void main(String[] args) throws Exception{
//        String s = "Http://211.100.18.94:7000/itimesipd/report?" + "name=" + URLEncoder.encode("»ÃÏëiÊ±´ú","GBK") +
//                   "&url=" + URLEncoder.encode("socket://211.100.18.94","GBK") + "&maxnum=" +
//                   700 + "&curnum=" +
//                   500;
//        System.out.println(s);
//        try {
//            URL u = new URL(s);
//            HttpURLConnection conn = (HttpURLConnection)u.openConnection();
//            System.out.println(conn.getResponseCode());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
