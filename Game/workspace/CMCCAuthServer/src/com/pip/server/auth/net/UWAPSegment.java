package com.pip.server.auth.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.collections.primitives.*;

public class UWAPSegment {
    private ByteList buffer;
    private byte numOfParameter = (byte) 0;
    private byte type;
    private int serial = -1;
    private int sessionId = -1;

    //For compress option
    public static final int COMPRESS_NONE = 0;
    public static final int COMPRESS_ALL = 1;
    public static final int COMPRESS_OPTION = 2;

    private byte[] compressedData = null;
    private boolean isCompressed = false;
    private boolean forceCompressed = false;
    private static final int compressType = COMPRESS_NONE;

    public UWAPSegment(byte type, int serial) {
        this(type, serial, -1);
    }

    public UWAPSegment(byte type, int serial, int sessionId) {
        this.type = type;
        this.serial = serial;
        this.sessionId = sessionId;
        buffer = new ArrayByteList(128);
        ByteListUtil.addByte(buffer, type);
        ByteListUtil.addInt(buffer, 6);
        ByteListUtil.addByte(buffer, (byte) 0);
    }

    public UWAPSegment(byte type) {
        this(type, -1);
    }

    public UWAPSegment(byte type, byte[] data, int serial, int sessionId) {
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
        ByteListUtil.addByte(buffer, type);
        ByteListUtil.addInt(buffer, bytes.length + 5);
        ByteListUtil.addByte(buffer, (byte) (data.getNumOfParameter() + 1));
        ByteListUtil.addByte(buffer, (byte) 0x04);
        ByteListUtil.addInt(buffer, playerId);
        ByteListUtil.addBytes(buffer, bytes, 6, bytes.length - 6);
    }

    public UWAPSegment(UWAPData data, int sessionId) {
        this.type = data.getAppType();
        this.serial = data.getSerial();
        this.sessionId = sessionId;
        byte[] bytes = data.toBytes();
        buffer = new ArrayByteList(bytes.length);
        ByteListUtil.addBytes(buffer, bytes);
    }

    public UWAPSegment(byte type, int serial, boolean forceCompressed) {
        this(type, serial, -1);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(byte type, int serial, int sessionId,
                       boolean forceCompressed) {
        this(type, serial, sessionId);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(byte type, boolean forceCompressed) {
        this(type, -1);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(byte type, byte[] data, int serial, int sessionId,
                       boolean forceCompressed) {
        this(type, data, serial, sessionId);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(UWAPData data, int sessionId, int playerId,
                       boolean forceCompressed) {
        this(data, sessionId, playerId);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(UWAPData data, int sessionId, boolean forceCompressed) {
        this(data, sessionId);
        this.forceCompressed = forceCompressed;
    }

    public byte getType() {
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
        ByteListUtil.setInt(buffer, 1, buffer.size());
    }

    protected void setNumOfParameter() {
        buffer.set(5, numOfParameter);
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
        if (isCompressed) {
            return compressedData.length;
        } else {
            return buffer.size();
        }
    }

    public byte[] getByteArray() {
        if (isCompressed) {
            return compressedData;
        } else {
            return buffer.toArray();
        }
    }

    public byte[] getPacketByteArray(){
        byte[] bytes = getByteArray();
        ByteList l = new ArrayByteList(bytes.length+20);
        ByteListUtil.addBytes(l,UWAPUtil.HEAD);
        ByteListUtil.addInt(l,sessionId);
        ByteListUtil.addInt(l,serial);
        ByteListUtil.addInt(l,19+bytes.length);
        ByteListUtil.addShort(l,(short)1);
        ByteListUtil.addBytes(l,bytes);
        ByteListUtil.addByte(l,(byte)0);
        return l.toArray();
    }

    public void forceCompress() {
        this.forceCompressed = true;
    }

    public void processCompress() {
        if (needCompress() && !isCompressed) {
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toArray());
            bis.skip(5);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try {
                dos.writeByte(type);
                dos.writeInt(0);

                UWAPCompress compress = new UWAPCompress(bis, dos);
                compress.compress();

                compressedData = bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    dos.close();
                } catch (IOException e) {
                }

                try {
                    bis.close();
                } catch (IOException e) {
                }
            }

            int len = compressedData.length;
            len &= 0x00FFFFFF;
            len |= 0x01000000;
            setNumber(len, compressedData, 1, 4);

            isCompressed = true;
            buffer = null;
        }
    }

    public static void setNumber(int num, byte[] buf, int off, int len) {
        for (int i = len - 1; i >= 0; i--) {
            buf[off + i] = (byte) (num & 0xff);
            num >>= 8;
        }
    }

    private boolean needCompress() {
        if(true)
            return false;
        if (this.size() < 2048) {
            forceCompressed = false;
        }

        if (forceCompressed) {
            return true;
        }

        if (compressType == COMPRESS_NONE) {
            return false;
        }

        if (compressType == COMPRESS_ALL) {
            return true;
        }

        return false;
    }
}
