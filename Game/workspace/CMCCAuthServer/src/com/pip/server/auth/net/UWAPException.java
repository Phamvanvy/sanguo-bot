package com.pip.server.auth.net;


public class UWAPException extends Exception {

    private int serial;

    private int sessionId;

    private byte type;


    public UWAPException(String message, int serial, int sessionId, byte type) {

        super(message);

        this.serial = serial;

        this.sessionId = sessionId;

        this.type = type;

    }


    public UWAPException(String message, int serial, byte type) {

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
