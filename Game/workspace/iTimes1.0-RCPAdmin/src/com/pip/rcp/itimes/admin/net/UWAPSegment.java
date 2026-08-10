package com.pip.rcp.itimes.admin.net;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayByteList;
import org.apache.commons.collections.primitives.ByteList;


public class UWAPSegment{
    private ByteList buffer;
    private byte numOfParameter = (byte)0;
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

    public UWAPSegment(byte type, int serial){
        this(type, serial, -1);
    }

    public UWAPSegment(byte type, int serial, int sessionId){
        this.type = type;
        this.serial = serial;
        this.sessionId = sessionId;
        buffer = new ArrayByteList(128);
        ByteListUtil.addByte(buffer, type);
        ByteListUtil.addInt(buffer, 6);
        ByteListUtil.addByte(buffer, (byte)0);
    }

    public UWAPSegment(byte type){
        this(type, -1);
    }

    public UWAPSegment(byte type, byte[] data, int serial, int sessionId){
        this.type = type;
        this.serial = serial;
        this.sessionId = sessionId;
        buffer = new ArrayByteList(data.length);
        ByteListUtil.addBytes(buffer, data);
    }

    public UWAPSegment(UWAPData data, int sessionId, int playerId){
        this.type = data.getAppType();
        this.serial = data.getSerial();
        this.sessionId = sessionId;
        byte[] bytes = data.toBytes();
        buffer = new ArrayByteList(bytes.length + 5);
        ByteListUtil.addByte(buffer, type);
        ByteListUtil.addInt(buffer, bytes.length + 5);
        ByteListUtil.addByte(buffer, (byte)(data.getNumOfParameter() + 1));
        ByteListUtil.addByte(buffer, (byte)0x04);
        ByteListUtil.addInt(buffer, playerId);
        ByteListUtil.addBytes(buffer, bytes, 6, bytes.length - 6);
    }

    public UWAPSegment(UWAPData data, int sessionId){
        this.type = data.getAppType();
        this.serial = data.getSerial();
        this.sessionId = sessionId;
        byte[] bytes = data.toBytes();
        buffer = new ArrayByteList(bytes.length);
        ByteListUtil.addBytes(buffer, bytes);
    }

    public UWAPSegment(byte type, int serial, boolean forceCompressed){
        this(type, serial, -1);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(byte type, int serial, int sessionId, boolean forceCompressed){
        this(type, serial, sessionId);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(byte type, boolean forceCompressed){
        this(type, -1);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(byte type, byte[] data, int serial, int sessionId, boolean forceCompressed){
        this(type, data, serial, sessionId);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(UWAPData data, int sessionId, int playerId, boolean forceCompressed){
        this(data, sessionId, playerId);
        this.forceCompressed = forceCompressed;
    }

    public UWAPSegment(UWAPData data, int sessionId, boolean forceCompressed){
        this(data, sessionId);
        this.forceCompressed = forceCompressed;
    }

    public byte getType(){
        return type;
    }

    public int getSessionId(){
        return sessionId;
    }

    public void setSessionId(int sessionId){
        this.sessionId = sessionId;
    }

    public byte getNumOfParameter(){
        return numOfParameter;
    }

    public int getSerial(){
        return serial;
    }

    public void setSerial(int serial){
        this.serial = serial;
    }

    protected void setSize(){
        ByteListUtil.setInt(buffer, 1, buffer.size());
    }

    protected void setNumOfParameter(){
        buffer.set(5, numOfParameter);
    }

    public void write(byte value){
        ByteListUtil.addByte(buffer, (byte)0x02);
        ByteListUtil.addByte(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void write(byte[] value){
        ByteListUtil.addByte(buffer, (byte)0x12);
        ByteListUtil.addShort(buffer, (short)value.length);
        ByteListUtil.addBytes(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeBoolean(boolean value){
        ByteListUtil.addByte(buffer, (byte)0x01);
        ByteListUtil.addByte(buffer, value? (byte)1: (byte)0);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeBooleans(boolean[] value){
        ByteListUtil.addByte(buffer, (byte)0x11);
        ByteListUtil.addShort(buffer, (short)value.length);
        ByteListUtil.addBooleans(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeShort(short value){
        ByteListUtil.addByte(buffer, (byte)0x06);
        ByteListUtil.addShort(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeShorts(short[] value){
        ByteListUtil.addByte(buffer, (byte)0x16);
        ByteListUtil.addShort(buffer, (short)value.length);
        ByteListUtil.addShorts(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeInt(int value){
        ByteListUtil.addByte(buffer, (byte)0x04);
        ByteListUtil.addInt(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeInts(int[] value){
        ByteListUtil.addByte(buffer, (byte)0x14);
        ByteListUtil.addShort(buffer, (short)value.length);
        ByteListUtil.addInts(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeLong(long value){
        ByteListUtil.addByte(buffer, (byte)0x05);
        ByteListUtil.addLong(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeLongs(long[] value){
        ByteListUtil.addByte(buffer, (byte)0x15);
        ByteListUtil.addShort(buffer, (short)value.length);
        ByteListUtil.addLongs(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeString(String value){
        ByteListUtil.addByte(buffer, (byte)0x07);
        ByteListUtil.addString(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public void writeStrings(String[] value){
        ByteListUtil.addByte(buffer, (byte)0x17);
        ByteListUtil.addShort(buffer, (short)value.length);
        ByteListUtil.addStrings(buffer, value);
        setSize();
        numOfParameter++;
        setNumOfParameter();
    }

    public int size(){
        if(isCompressed){
            return compressedData.length;
        }else{
            return buffer.size();
        }
    }

    public byte[] getByteArray(){
        if(isCompressed){
            return compressedData;
        }else{
            return buffer.toArray();
        }
    }

    public byte[] getPacketByteArray(){
        byte[] bytes = getByteArray();
        ByteList l = new ArrayByteList(bytes.length + 20);
        ByteListUtil.addBytes(l, UWAPUtil.HEAD);
        ByteListUtil.addInt(l, sessionId);
        ByteListUtil.addInt(l, serial);
        ByteListUtil.addInt(l, 19 + bytes.length);
        ByteListUtil.addShort(l, (short)1);
        ByteListUtil.addBytes(l, bytes);
        ByteListUtil.addByte(l, (byte)0);
        return l.toArray();
    }

    public void forceCompress(){
        this.forceCompressed = true;
    }

    public void processCompress(){
        if(needCompress() && !isCompressed){
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toArray());
            bis.skip(5);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try{
                dos.writeByte(type);
                dos.writeInt(0);

                UWAPCompress compress = new UWAPCompress(bis, dos);
                compress.compress();

                compressedData = bos.toByteArray();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                try{
                    dos.close();
                }catch(IOException e){
                }

                try{
                    bis.close();
                }catch(IOException e){
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

    public static void setNumber(int num, byte[] buf, int off, int len){
        for(int i = len - 1; i >= 0; i--){
            buf[off + i] = (byte)(num & 0xff);
            num >>= 8;
        }
    }

    private boolean needCompress(){
        if(true)
            return false;
        if(this.size() < 2048){
            forceCompressed = false;
        }

        if(forceCompressed){
            return true;
        }

        if(compressType == COMPRESS_NONE){
            return false;
        }

        if(compressType == COMPRESS_ALL){
            return true;
        }

        boolean result = false;

        switch(type){
            //Client Type
            case ClientConstants.ERROR:
            case ClientConstants.ACCOUNT_REG:
            case ClientConstants.ACCOUNT_REG_OK:
            case ClientConstants.ACTOR_CREATE:
            case ClientConstants.ACTOR_CREATE_OK:
            case ClientConstants.ACTOR_GET_LIST:
            case ClientConstants.ACTOR_LIST:
            case ClientConstants.PLAYER_LOGIN:
            case ClientConstants.PLAYER_LOGIN_OK:
            case ClientConstants.LOGIN:
            case ClientConstants.LOGIN_OK:
            case ClientConstants.GET_FILE_OK:
            case ClientConstants.SEND_POSITION:
            case ClientConstants.BBS_POST:
            case ClientConstants.BBS_POST_OK:
            case ClientConstants.BBS_LIST:
            case ClientConstants.BBS_GET_CONTENT:
            case ClientConstants.BBS_CONTENT:
            case ClientConstants.TOUCH_NPC:
            case ClientConstants.MAIL_POST_OK:
            case ClientConstants.MAIL_LIST:
            case ClientConstants.MAIL_CONTENT:
            case ClientConstants.MAIL_GET_ATTACHMENT_OK:
            case ClientConstants.MAIL_DELETE:
            case ClientConstants.MAIL_NEW:
            case ClientConstants.USE_ITEM:
            case ClientConstants.BATTLE_RESULT:
            case ClientConstants.GOT_TASKITEM:
            case ClientConstants.ADD_PROPERTY_POINT_OK:
            case ClientConstants.TEAM_CREATE:
            case ClientConstants.TEAM_CREATE_OK:
            case ClientConstants.TEAM_INVIT:
            case ClientConstants.TEAM_INVIT_RESULT:
            case ClientConstants.TEAM_JOIN_OK:
            case ClientConstants.TEAM_JOIN_FAIL:
            case ClientConstants.TEAM_LEAVE:
            case ClientConstants.BATTLE_REQUEST:
            case ClientConstants.BATTLE_INIT:
            case ClientConstants.BATTLE_JOIN:
            case ClientConstants.BATTLE_JOIN_RESULT:
            case ClientConstants.BATTLE_START:
            case ClientConstants.BATTLE_ABORT:
            case ClientConstants.BATTLE_FIGHT:
            case ClientConstants.BATTLE_ROUND_END:
            case ClientConstants.PLAYER_UPLOAD:
            case ClientConstants.PLAYRE_UPLOAD_OK:
            case ClientConstants.PK_REQUEST:
            case ClientConstants.PK_CREATED:
            case ClientConstants.PK_REFUSE:
            case ClientConstants.PK_OK:
            case ClientConstants.PK_START:
            case ClientConstants.PK_FIGHT:
            case ClientConstants.PK_ROUND_END:
            case ClientConstants.EQU_CHANGED:
            case ClientConstants.EQU_CHANGED_OK:
            case ClientConstants.CHAT:
            case ClientConstants.CHAT_OPTION:
            case ClientConstants.CHATFAVORITE_LIST:
            case ClientConstants.CHAT_FAVORITE_DESC:
            case ClientConstants.CHANGE_CHATFAVORITE:
            case ClientConstants.REFRESH:
            case ClientConstants.COMMAND:
            case ClientConstants.MESSAGE:
            case ClientConstants.LOOK_EQU_OK:
            case ClientConstants.REQUEST_ITEM_LINK:
            case ClientConstants.STORE_ITEM_LIST:
            case ClientConstants.STORE_TRADE_OK:
            case ClientConstants.ADD_FRIEND_OK:
            case ClientConstants.TASK_DESC:
            case ClientConstants.SYNC_TIME:
            case ClientConstants.GET_ITEM:
            case ClientConstants.GATHER:
            case ClientConstants.GATHER_OK:
            case ClientConstants.GATHER_RESULT:
            case ClientConstants.LEARN_SKILL:
            case ClientConstants.LEAR_SKILL_OK:
            case ClientConstants.SKILL_LIST:
            case ClientConstants.GET_DESC:
            case ClientConstants.DESC:
            case ClientConstants.PRODUCT:
            case ClientConstants.ABILITY_LIST:
            case ClientConstants.LEARN_ABILITY:
            case ClientConstants.LEAR_ABILITY_OK:
            case ClientConstants.TASK_COMPLETED:
            case ClientConstants.SHOP_CREATE_OK:
            case ClientConstants.SHOP_LIST:
            case ClientConstants.SHOP_ITEM_LIST:
            case ClientConstants.SHOP_ADD_ITEM_OK:
            case ClientConstants.SHOP_REMOVE_ITEM_OK:
            case ClientConstants.SHOP_MONEY_CHANGE_OK:
            case ClientConstants.SHOP_CHANGE_OK:
            case ClientConstants.AUCTION_TYPE_LIST:
            case ClientConstants.AUCTION_LIST:
            case ClientConstants.AUCTION_DESC:
            case ClientConstants.AUCTION_PRICE_OK:
            case ClientConstants.AUCTION_ITEM_OK:
            case ClientConstants.BUY_MAERIAL_TYPE_LIST:
            case ClientConstants.BUY_MATERIAL_LIST:
            case ClientConstants.SELL_MATERIAL_OK:
            case ClientConstants.OEM_TYPE_LIST:
            case ClientConstants.OEM_LIST:
            case ClientConstants.OEM_OK:
                result = false;

                break;

            //Server type
            case ServerConstants.SERVER_LOGIN:
            case ServerConstants.SERVER_LOGIN_OK:
            case ServerConstants.SERVER_LOGIN_FAIL:
            case ServerConstants.SERVER_FORWARD:

                //        case ServerConstants.RESOURCE_ADD:
            case ServerConstants.PLAYER_LOGOUT:
            case ServerConstants.GATHER_OK:
                result = false;

                break;
            default:
                result = false;
        }

        return result;
    }
}
