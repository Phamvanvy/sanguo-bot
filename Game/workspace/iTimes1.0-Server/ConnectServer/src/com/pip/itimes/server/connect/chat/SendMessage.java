package com.pip.itimes.server.connect.chat;


/**
 * @author Jeffery
 * @version 1.0
 */
public class SendMessage implements ISendMessage {

    public int srcId;
    public String srcName;
    public int[] destIds;
    public String msg;

    public SendMessage(int srcId,String srcName,int[] destIds,String msg) {
        this.srcId = srcId;
        this.srcName = srcName;
        this.destIds = destIds;
        this.msg = msg;
    }

    public int getSrcId(){
        return srcId;
    }

    public String getSrcName(){
        return srcName;
    }

    public int[] getDestIds(){
        return destIds;
    }

    public String getMessage(){
        return msg;
    }



    public boolean isPrivate() {
        return false;
    }
}
