package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public interface IItem {

    public static final byte TYPE_BASIC = 0;
    public static final byte TYPE_TASK = 1;
    public static final byte TYPE_EXTENDED = 2;
    public static final byte TYPE_EQU = 3;
    public static final byte TYPE_PET = 4;

    public static final byte BIND_NO = 0;
    public static final byte BIND_USE = 1;
    public static final byte BIND_GET = 2;

    public int getItemId();
    public int getId();
    public String getName();
    public byte getType();
    public boolean isBinded();
    public void setBinded(boolean binded);
    public byte getBindType();
    public byte getQuality();
    public String getDesc();
    public byte[] toClientBytesWithLevel(int level);
    public byte[] toDbBytes();
    
	public byte getItemShowType();
	public void setItemShowType(byte itemShowType);
	public byte[] toClientBytes(int dataVersion);
}
