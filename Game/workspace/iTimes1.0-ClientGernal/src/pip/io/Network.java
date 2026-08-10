package pip.io;


import javax.microedition.io.HttpConnection;

import pip.GameState;
import pip.World;


public class Network extends Thread{
    private static boolean detected = false;
    private static boolean proxyFlagGlobal = true;

    private boolean proxyFlag;


    public Network(boolean p){
        proxyFlag = p;
    }
    
    public static boolean useProxy(){
        if(!detected){
            detectNetwork();
        }
        
        return proxyFlagGlobal;
    }

    public static boolean detectNetwork(){
        if(!detected){
        	//#if UseProxy == true
            //# if(!detected){
            //#     proxyFlagGlobal = true;
            //#     detected = true;
            //#     return detected;
            //# }
        	//#elif UseProxy == false
            //# if(!detected){
            //#     proxyFlagGlobal = false;
            //#     detected = true;
            //#     return detected;
            //# }
        	//#endif
            boolean initValue = true;
            if (GameState.DIRECT_CONNECT) {
                initValue = false;
            }
            byte[] savedData = World.getData("proxy", (byte)0);
            if (savedData != null &&  savedData.length == 1) {
                initValue = (savedData[0] == 1);
            }
            new Network(initValue).start();
            
            for(int i = 0; i < 1200 && !detected; i++){
                try {
                    Thread.sleep(50);
                    
                    if(detected) {
                        break;
                    }
                    if (i == 100) {
                        new Network(!initValue).start();
                    }
                }catch(Exception e){
                }
            }
        }
        
        return detected;
    }

    public void run(){
        HttpConnection conn = null;
        try{
            conn = UWAPSegment.getConnection(GameState.entryURL, proxyFlag);
            int code = conn.getResponseCode();
            if (code != 200 && code != 302) {
                throw new Exception();
            }
            
            if(!detected){
                proxyFlagGlobal = proxyFlag;
                detected = true;
                World.saveData("proxy", new byte[] { (byte)(proxyFlag ? 1 : 0) }, (byte)0);
                
                //#debug
                System.out.println("proxy detected: " + proxyFlagGlobal);
            }
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }finally{
            if(conn != null){
                try{
                    conn.close();
                }catch(Exception e){
                }
            }
        }
    }
}