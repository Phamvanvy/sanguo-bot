package com.pip.itimes.server;


public class ITimesException extends Exception {

    private int serial;

    private int sessionId;

    private byte type;


    public ITimesException(String message, int serial, int sessionId, byte type) {

        super(message);

        this.serial = serial;

        this.sessionId = sessionId;

        this.type = type;

    }


    public ITimesException(String message, int serial, byte type) {

        this(message, serial, -1, type);

    }


    public int getSerial() {

        return serial;

    }


    public int getSessionId() {

        return sessionId;

    }


    public byte getAppType() {

        return type;

    }

}
