package com.pip.sanguo.performancetest.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;

import com.pip.sanguo.performancetest.View;

public class ClientManager implements Runnable{
    private static List<SanguoClient> clients = new Vector<SanguoClient>();

    public static String datafle;
    public static boolean dirty;
    public static boolean running;
    public static Random rand = new Random(System.currentTimeMillis());
    
    public static View view;
    
    public static boolean consoleMode = false;
    public static long lastLogtime = 0;
    
    public static void main(String[] args){
        consoleMode = true;
        datafle = args[0];
        
        loadClients();
        start();
    }
    
    public static void pringLog(){
        if(System.currentTimeMillis() - lastLogtime > 10000){
            System.out.println();
            System.out.println();
            System.out.println();
            
            System.out.print("帐户名\t\t");
            System.out.print("密码\t\t");
            System.out.print("角色名\t\t");
            System.out.print("状态\t\t");
            System.out.print("发送流量\t");
            System.out.print("接收流量\t");
            System.out.print("move包数量\t");
            System.out.print("战斗包数量\t");
            System.out.print("聊天包数量\t");
            System.out.print("网络延时\t");
            System.out.print("运行时间\t");
            System.out.print("平均流量");
            System.out.println();
        
            for(SanguoClient client : clients){
                System.out.println(client.toLogString());
            }
            
            System.out.println();
            System.out.println();
            System.out.println();
            
            lastLogtime = System.currentTimeMillis();
        }
    }

    public void run(){
        stopClients();
        startClients();

        running = true;
        setDirty(false);

        while(running){
            try{
                Thread.sleep(100);

                if(getDirty()){
                    saveClients();
                    setDirty(false);

                    if(consoleMode){
                        pringLog();
                    }else{
                        Display.getDefault().syncExec(new Thread(){
                            public void run(){
                                view.loadClients();
                            }
                        });
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if(consoleMode){
            pringLog();
        }else{
            try{
                Thread.sleep(1000);
            }catch(Exception e){
            }
            
            Display.getDefault().syncExec(new Thread(){
                public void run(){
                    view.loadClients();
                }
            });
        }
    }

    public static void start(){
        new Thread(new ClientManager()).start();
    }

    public static void stop(){
        stopClients();
        running = false;
    }

    public synchronized static void setDirty(boolean dt){
        dirty = dt;
    }

    public synchronized static boolean getDirty(){
        return dirty;
    }

    public static List<SanguoClient> getClients(){
        return clients;
    }

    public static void saveClients(){
    	PrintWriter bw = null;

        try{
            bw = new PrintWriter(new FileWriter(datafle));

            for(SanguoClient client : clients){
            	bw.println(client.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(bw != null){
                    bw.close();
                }
            }catch(Exception e){
            }
        }
    }

    public static void loadClients(){
        BufferedReader br = null;

        try{
            br = new BufferedReader(new FileReader(datafle));

            clients.clear();
            String line = br.readLine();

            while(line != null){
            	try {
            		clients.add(SanguoClient.createFromString(line));
            	} catch (Exception e) {
            	}
                line = br.readLine();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(br != null){
                    br.close();
                }
            }catch(Exception e){
            }
        }
    }

    public static void createClients(String prefix, int begin, int end){
        clearClients();

        String calPara = "";
        int len = String.valueOf(end).length();

        for(int i = 0; i < Math.log10(end); i++){
            calPara += "0";
        }

        for(int i = begin; i <= end; i++){
            String tmp = calPara + (i);
            String accountName = prefix + tmp.substring(tmp.length() - len);

            SanguoClient client = new SanguoClient();
            client.setAccountName(accountName);
            client.setAccountPassword("?");
            clients.add(client);
        }
    }

    public static void clearClients(){
        stopClients();
        clients.clear();
    }

    public static void stopClients(){
        for(SanguoClient client : clients){
            client.setRunning(false);
        }
    }

    public static void startClients(){
        for(SanguoClient client : clients){
            client.setRunning(true);
            new Thread(client).start();
        }
    }
}
