package com.pip.itimes.server.world;

/**
 * @author Jeffery
 * @version 1.0
 */
public class PrivateSendMessage implements ISendMessage {

    private int srcId;
    private int destId;
    private String srcName;
    private String msg;

    public PrivateSendMessage(int srcId,String srcName,int destId,String msg) {
        this.srcId = srcId;
        this.srcName = srcName;
        this.destId = destId;
        this.msg = msg;
    }


    public int getSrcId() {
        return srcId;
    }

    public String getSrcName() {
        return srcName;
    }

    public int[] getDestIds(){
        return new int[]{srcId,destId};
    }



    public boolean isPrivate() {
        return true;
    }

    public String getMessage(){
        return msg;
    }
}
