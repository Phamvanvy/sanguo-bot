package com.pip.dispatch;

import org.apache.mina.common.ByteBuffer;

public class Packet1 {

    public enum TYPE{BUFFER,CONTROL};

    public TYPE type = TYPE.BUFFER;
    public ByteBuffer buffer;
    public int sessionId = 0;

    public Packet1(ByteBuffer buffer,int sessionId) {
        this.type = TYPE.BUFFER;
        this.buffer = buffer;
        this.sessionId = sessionId;
    }

    public Packet1(int sessionId) {
        this.type = TYPE.CONTROL;
        this.sessionId = sessionId;
    }
}
