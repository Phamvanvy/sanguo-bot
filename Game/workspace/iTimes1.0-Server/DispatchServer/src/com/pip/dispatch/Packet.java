package com.pip.dispatch;

import org.apache.mina.common.ByteBuffer;

public class Packet {

    public enum TYPE{BUFFER,UWAPDATA};

    public TYPE type = TYPE.BUFFER;
    public UWAPData data;
    public ByteBuffer buffer;
    public int sessionId = 0;

    public Packet(ByteBuffer buffer,int sessionId) {
        this.type = TYPE.BUFFER;
        this.buffer = buffer;
        this.sessionId = sessionId;
    }

    public Packet(UWAPData data) {
        this.type = TYPE.UWAPDATA;
        this.data = data;
    }
}
