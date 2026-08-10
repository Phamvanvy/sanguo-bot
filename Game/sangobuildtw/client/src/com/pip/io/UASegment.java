package com.pip.io;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.pip.common.Tool;


public class UASegment{
    /** 协议类型 */
    public short type;
    /** 协议数据 */
    public byte[] data;
    public boolean handled;

    /** 协议串号标识，唯一 */
    public int serial = -1;
    public boolean needResponse;

    private int pos;
    private ByteArrayOutputStream segCache = null;
    private DataOutputStream outputCache = null;
    
    private static final Tool serialKey = new Tool();

    public UASegment(int type){
        this(type, false);
    }
    
    public UASegment(int type, boolean needSerial){
        this.type = (short)type;

        try{
            segCache = new ByteArrayOutputStream();
            outputCache = new DataOutputStream(segCache);
            outputCache.writeShort(type);
            
            if(needSerial) {
                serial = serialKey.nextKey();
                outputCache.writeInt(serial);        	
            }
        }catch(IOException ex){
        }
    }

    public UASegment(byte[] data){
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);

        type = (short)getNumber(this.data, 0, 2);
        pos = 2;
    }

    public void reset(){
        pos = 2;
    }

    public void flush(){
        if(segCache == null){
            return;
        }

        try{
            outputCache.flush();
            data = segCache.toByteArray();
        }catch(Exception e){
        }finally{
            try{
                outputCache.close();
            }catch(Exception e){
            }
        }

        segCache = null;
        outputCache = null;

        setNumber(type, data, 0, 2);
    }

    public boolean readBoolean(){
        return (data[pos++] == 1)? true: false;
    }

    public byte readByte(){
        return data[pos++];
    }
    
    public int readUnsignedByte(){
        return data[pos++] & 0xFF;
    }

    public short readShort(){
        pos += 2;

        return (short)getNumber(data, pos - 2, 2);
    }
    
    public int readUnsignedShort(){
        return readShort() & 0xFFFF;
    }

    public int readInt(){
        pos += 4;

        return (int)getNumber(data, pos - 4, 4);
    }
    
    public void setInt(int num){
        setNumber(num, data, pos, 4);
    }

    public long readLong(){
        pos += 8;

        return getNumber(data, pos - 8, 8);
    }

    public String readString(){
        DataInputStream dis = null;
        try {
            if ((data[pos] & 0x80) == 0) {
                dis = new DataInputStream(new ByteArrayInputStream(data, pos, data.length - pos));
                pos += getNumber(data, pos, 2) + 2;
                return Tool.readUTF(dis);
            } else {
                int len = (int)(getNumber(data, pos, 2) & 0x7FFF);
                pos += 2;
                char[] charr = new char[len / 2];
                for (int i = 0; i < charr.length; i++) {
                    charr[i] = (char)(((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF));
                    pos += 2;
                }
                return new String(charr);
            }
        } catch (Exception e) {
            e.printStackTrace();

            return "";
        }finally{
            try{
                dis.close();
            }catch(Exception e){
            }
        }
    }

    public boolean[] readBooleans(){
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        boolean[] result = new boolean[len];

        for(int i = 0; i < len; i++){
            result[i] = readBoolean();
        }

        return result;
    }

    public byte[] readBytes(){
        int len = (int)getNumber(data, pos, 4);
        pos += 4;
        byte[] result = new byte[len];

        for(int i = 0; i < len; i++){
            result[i] = readByte();
        }

        return result;
    }

    public short[] readShorts(){
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        short[] result = new short[len];

        for(int i = 0; i < len; i++){
            result[i] = readShort();
        }

        return result;
    }

    public int[] readInts(){
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        int[] result = new int[len];

        for(int i = 0; i < len; i++){
            result[i] = readInt();
        }

        return result;
    }

    public long[] readLongs(){
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        long[] result = new long[len];

        for(int i = 0; i < len; i++){
            result[i] = readLong();
        }

        return result;
    }

    public String[] readStrings(){
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        String[] result = new String[len];

        for(int i = 0; i < len; i++){
            result[i] = readString();
        }

        return result;
    }

    public void writeBoolean(boolean b) throws IOException{
        outputCache.writeBoolean(b);
    }

    public void writeByte(byte b) throws IOException{
        outputCache.writeByte(b);
    }

    public void writeShort(short s) throws IOException{
        outputCache.writeShort(s);
    }

    public void writeInt(int n) throws IOException{
        outputCache.writeInt(n);
    }

    public void writeLong(long l) throws IOException{
        outputCache.writeLong(l);
    }

    public void writeString(String s) throws IOException{
        outputCache.writeUTF((s == null)? "": s);
    }

    public void writeBooleans(boolean[] b) throws IOException{
        outputCache.writeShort(b.length);

        for(int i = 0; i < b.length; i++){
            outputCache.writeBoolean(b[i]);
        }
    }

    public void writeBytes(byte[] b) throws IOException{
        outputCache.writeInt(b.length);

        for(int i = 0; i < b.length; i++){
            outputCache.writeByte(b[i]);
        }
    }

    public void writeShorts(short[] s) throws IOException{
        outputCache.writeShort(s.length);

        for(int i = 0; i < s.length; i++){
            outputCache.writeShort(s[i]);
        }
    }

    public void writeInts(int[] n) throws IOException{
        outputCache.writeShort(n.length);

        for(int i = 0; i < n.length; i++){
            outputCache.writeInt(n[i]);
        }
    }

    public void writeLongs(long[] l) throws IOException{
        outputCache.writeShort(l.length);

        for(int i = 0; i < l.length; i++){
            outputCache.writeLong(l[i]);
        }
    }

    public void writeStrings(String[] s) throws IOException{
        outputCache.writeShort(s.length);

        for(int i = 0; i < s.length; i++){
            outputCache.writeUTF(s[i]);
        }
    }

    public static long getNumber(byte[] buf, int off, int len){
        long longVal = 0;

        for(int i = 0; i < len; i++){
            longVal <<= 8;
            longVal |= buf[off + i] & 0xff;
        }

        return longVal;
    }

    public static void setNumber(int num, byte[] buf, int off, int len){
        for(int i = len - 1; i >= 0; i--){
            buf[off + i] = (byte)(num & 0xff);
            num >>= 8;
        }
    }

    /** 打开一个HTTP连接。 */
    public static HttpConnection getConnection(String url, boolean proxyFlag) throws IOException{
        String proxyUrl = null;

        if(proxyFlag){
            proxyUrl = "10.0.0.172:80";
        }else{
            proxyUrl = null;
        }

        // 应用代理
        String requestUrl = url, realHost = null;

        if(proxyUrl != null){
            int ind = url.indexOf('/', 7);

            if(ind >= 0){
                requestUrl = url.substring(0, 7) + proxyUrl + url.substring(ind);
                realHost = url.substring(7, ind);
            }
        }

        HttpConnection conn;
        //#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == AndroidSmall
		if(realHost != null) {
		    conn = (HttpConnection)Connector.open("p" + url);
		} else {
			conn = (HttpConnection)Connector.open(requestUrl);			
		}
        //#else
        // 打开连接        
        conn = (HttpConnection)Connector.open(requestUrl);

        if(realHost != null){
            conn.setRequestProperty("X-Online-Host", realHost);
        }
        //#endif

        return conn;
    }
}
