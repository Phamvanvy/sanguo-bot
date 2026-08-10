package pip;


import java.util.Vector;

import pip.io.UWAPConnection;
import pip.io.UWAPSegment;


public class ASyncRequestThread implements Runnable{
    public static final int TIME_DELAY = 50;
    //#debug
    public static final int TIME_SEND_TIMEOUT = 6000000;
    //#= public static final int TIME_SEND_TIMEOUT = 60000;

    public static ASyncRequestThread instance;

    private static Vector queue = new Vector();

    private static UWAPConnection connection = null;

    private static Vector sendedList = new Vector();

    public static void init(UWAPConnection connection){
        ASyncRequestThread.connection = connection;

        if(instance == null){
            instance = new ASyncRequestThread();
        }
    }

    public static void sendUWAPSegment(UWAPSegment segment){
        if(queue.contains(segment) || sendedList.contains(segment) || GameState.connection == null){
            return;
        }

        queue.addElement(segment);
    }

    public static void removeFromSendedList(UWAPSegment segment){
        for(int i = 0; i < sendedList.size(); i++){
            UWAPSegment seg = (UWAPSegment)sendedList.elementAt(i);

            if(seg.serial == segment.serial){
                sendedList.removeElementAt(i);

                break;
            }
        }
    }

    private UWAPSegment removeFirst(){
        if(queue.size() == 0){
            return null;
        }else{
            UWAPSegment result = (UWAPSegment)queue.firstElement();
            queue.removeElement(result);

            return result;
        }
    }

    private ASyncRequestThread(){
        new Thread(this).start();
    }

    public static int getTimeStamp(){
        return (int)System.currentTimeMillis();
    }

    private void addToSendedList(UWAPSegment segment){
        sendedList.addElement(segment);
    }

    private void clearTimeoutSegment(){
        Vector tmpList = new Vector();
        int ts = getTimeStamp();

        for(int i = 0; i < sendedList.size(); i++){
            UWAPSegment seg = (UWAPSegment)sendedList.elementAt(i);

            if(ts - seg.timeStamp > TIME_SEND_TIMEOUT){
                tmpList.addElement(seg);
            }
        }

        for(int i = 0; i < tmpList.size(); i++){
            UWAPSegment seg = (UWAPSegment)tmpList.elementAt(i);
            sendedList.removeElement(seg);
            //#debug
            World.log("remove segment " + seg.serial, true);

        }
    }

    public static long makeASyncSign(byte reqCode, short reqType, short reqID){
        long sign = ((long)reqCode << 32) | ((long)reqType << 16) | reqID;
        return sign;
    }

    public void run(){
        while(iTimesMIDlet.instance.isRunning){
            try{
                if(connection != null){
                    UWAPSegment segment = removeFirst();

                    if(segment != null){
                        connection.writeSegment(segment);
                        segment.timeStamp = getTimeStamp();

                        addToSendedList(segment);
                        //#debug
                        World.log("send segement " + segment.serial, true);
                    }else{
                        clearTimeoutSegment();
                    }
                }else{
                    clearTimeoutSegment();
                }
            }catch(Throwable e){
                //#debug
                e.printStackTrace();
            }finally{
                try{
                    Thread.sleep(TIME_DELAY);
                }catch(InterruptedException e){
                    //#debug
                    e.printStackTrace();
                }
            }
        }
    }
}