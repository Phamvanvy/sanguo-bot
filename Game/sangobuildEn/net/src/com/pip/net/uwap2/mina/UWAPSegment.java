package com.pip.net.uwap2.mina;

import org.apache.commons.collections.primitives.*;

public class UWAPSegment {
    private ByteList buffer;
    private byte numOfParameter = (byte) 0;
    private short type;
    private int serial = -1;
    private int sessionId = -1;


    public UWAPSegment(short type, int serial) {
        this(type, serial, -1);
    }

    public UWAPSegment(short type, int serial, int sessionId) {
        this.type = type;
        this.serial = serial;
        this.sessionId = sessionId;
        buffer = new ArrayByteList(128);
        ByteListUtil.addShort(buffer, type);
        ByteListUtil.addInt(buffer, 7);
        ByteListUtil.addByte(buffer, (byte) 0);
    }

    public UWAPSegment(short type) {
        this(type, -1);
    }

    public UWAPSegment(short type, byte[] data, int serial, int sessionId) {
        this.type = type;
        this.serial = serial;
        this.sessionId = sessionId;
        buffer = new ArrayByteList(data.length);
        ByteListUtil.addBytes(buffer, data);
    }

    public UWAPSegment(UWAPData data, int sessionId, int playerId) {
        this.type = data.getAppType();
        this.serial = data.getSerial();
        this.sessionId = sessionId;
        byte[] bytes = data.toBytes();
        buffer = new ArrayByteList(bytes.length + 5);
        ByteListUtil.addShort(buffer, type);
        ByteListUtil.addInt(buffer, bytes.length + 5);
        ByteListUtil.addByte(buffer, (byte) (data.getNumOfParameter() + 1));
        ByteListUtil.addByte(buffer, (byte) 0x04);
        ByteListUtil.addInt(buffer, playerId);
        ByteListUtil.addBytes(buffer, bytes, 7, bytes.length - 7);
    }

    public UWAPSegment(UWAPData data, int sessionId) {
        this.type = data.getAppType();
        this.serial = data.getSerial();
        this.sessionId = sessionId;
        byte[] bytes = data.toBytes();
        buffer = new ArrayByteList(bytes.length);
        ByteListUtil.addBytes(buffer, bytes);
    }





    public short getType() {
        return type;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public byte getNumOfParameter() {
        return numOfParameter;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    protected void setSize() {
        ByteListUtil.setInt(buffer, 2, buffer.size());
    }

    protected void setNumOfParameter() {
        buffer.set(6, numOfParameter);
    }

    public void write(byte value) {
        ByteListUtil.addByte(buffer, (byte) 0x02);
        ByteListUtil.addByte(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void write(byte[] value) {
        ByteListUtil.addByte(buffer, (byte) 0x12);
        ByteListUtil.addShort(buffer, (short) value.length);
        ByteListUtil.addBytes(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeBoolean(boolean value) {
        ByteListUtil.addByte(buffer, (byte) 0x01);
        ByteListUtil.addByte(buffer, value ? (byte) 1 : (byte) 0);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeBooleans(boolean[] value) {
        ByteListUtil.addByte(buffer, (byte) 0x11);
        ByteListUtil.addShort(buffer, (short) value.length);
        ByteListUtil.addBooleans(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeShort(short value) {
        ByteListUtil.addByte(buffer, (byte) 0x06);
        ByteListUtil.addShort(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeShorts(short[] value) {
        ByteListUtil.addByte(buffer, (byte) 0x16);
        ByteListUtil.addShort(buffer, (short) value.length);
        ByteListUtil.addShorts(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeInt(int value) {
        ByteListUtil.addByte(buffer, (byte) 0x04);
        ByteListUtil.addInt(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeInts(int[] value) {
        ByteListUtil.addByte(buffer, (byte) 0x14);
        ByteListUtil.addShort(buffer, (short) value.length);
        ByteListUtil.addInts(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeLong(long value) {
        ByteListUtil.addByte(buffer, (byte) 0x05);
        ByteListUtil.addLong(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeLongs(long[] value) {
        ByteListUtil.addByte(buffer, (byte) 0x15);
        ByteListUtil.addShort(buffer, (short) value.length);
        ByteListUtil.addLongs(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeString(String value) {
        ByteListUtil.addByte(buffer, (byte) 0x07);
        ByteListUtil.addString(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeStrings(String[] value) {
        ByteListUtil.addByte(buffer, (byte) 0x17);
        ByteListUtil.addShort(buffer, (short) value.length);
        ByteListUtil.addStrings(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public int size() {
		return buffer.size();
	}

	public byte[] getByteArray() {
		return buffer.toArray();
	}


    public static void setNumber(int num, byte[] buf, int off, int len) {
        for (int i = len - 1; i >= 0; i--) {
            buf[off + i] = (byte) (num & 0xff);
            num >>= 8;
        }
    }

}
