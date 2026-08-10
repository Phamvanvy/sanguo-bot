package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TalkEffect extends Effect{

    private String channel;
    private String message;

    public TalkEffect() {
    }

    public String getMessage() {
        return message;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public byte getType(){
        return 14;
    }
}
