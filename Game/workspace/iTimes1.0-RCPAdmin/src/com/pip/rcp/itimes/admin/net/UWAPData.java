package com.pip.rcp.itimes.admin.net;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class UWAPData{

    private byte[] data;
    private byte appType;
    private int numOfParameter;
    private int serial;
    private int pos = 0;
    private int sessionId;

    private boolean sourceCompressed = false;

    public UWAPData(byte[] data, int serial, int sessionId, boolean needUncompress, int version){
        if(needUncompress){
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            bis.skip(5);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try{
                dos.writeByte(data[0]);
                dos.writeInt(0);

                UWAPUncompress ucom = new UWAPUncompress(bis, dos);
                ucom.unCompress();

                this.data = bos.toByteArray();
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

            UWAPSegment.setNumber(this.data.length, this.data, 1, 4);
        }else{
            this.data = data;
        }
        if(version == 2){
            appType = this.data[1];
            this.numOfParameter = this.data[6];
            pos = 7;
        }else{
            appType = this.data[0];
            this.numOfParameter = this.data[5];
            pos = 6;
        }

        this.sessionId = sessionId;

        this.serial = serial;

        sourceCompressed = needUncompress;
    }

    public boolean needCompress(){
        return sourceCompressed;
    }

    public int getSessionId(){
        return sessionId;
    }

    public int getSerial(){
        return serial;
    }

    public byte getAppType(){
        return appType;
    }

    public int getNumOfParameter(){
        return numOfParameter;
    }

    public boolean readBoolean() throws IllegalAccessException{
        if(pos >= data.length - 1 || data[pos] != 1){
            throw new IllegalAccessException();
        }
        pos += 2;

        return ((data[pos - 1] & 1) == 1);
    }

    public boolean[] readBooleans() throws IllegalAccessException{
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        if(pos + len > data.length){
            throw new IllegalAccessException();
        }
        boolean[] ret = new boolean[len];
        for(int i = 0; i < len; i++){
            ret[i] = (data[pos++] & 1) == 1;
        }
        return ret;
    }

    public byte readByte() throws IllegalAccessException{
        if(pos >= data.length - 1 || data[pos] != 2){
            throw new IllegalAccessException();
        }
        pos += 2;
        return data[pos - 1];
    }

    public byte[] readBytes() throws IllegalAccessException{
        if(data[pos] != 0x12)
            throw new IllegalAccessException();
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        if(pos + len > data.length){
            throw new IllegalAccessException();
        }
        byte[] ret = new byte[len];
        System.arraycopy(data, pos, ret, 0, len);
        pos += len;
        return ret;
    }

    public short readShort() throws IllegalAccessException{
        if(pos >= data.length - 2 || data[pos] != 6){
            throw new IllegalAccessException();
        }
        pos += 3;

        return (short)getNumber(data, pos - 2, 2);
    }

    public short[] readShorts() throws IllegalAccessException{
        if(data[pos] != 0x16)
            throw new IllegalAccessException();
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        if(pos + len * 2 > data.length){
            throw new IllegalAccessException();
        }
        short[] ret = new short[len];
        for(int i = 0; i < len; i++){
            short c = (short)(((int)data[pos++]) & 0xff);
            c = (short)((c << 8) + (((int)data[pos++]) & 0xff));
            ret[i] = c;
        }
        return ret;
    }

    public int readInt() throws IllegalAccessException{
        if(pos >= data.length - 4 || data[pos] != 4){
            throw new IllegalAccessException();
        }
        pos += 5;
        return (int)getNumber(data, pos - 4, 4);
    }

    public int[] readInts() throws IllegalAccessException{
        if(pos >= data.length - 1 || data[pos] != ((1 << 4) + 4)){
            throw new IllegalAccessException();
        }
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        if(pos + len * 4 > data.length){
            throw new IllegalAccessException();
        }
        int[] ret = new int[len];
        for(int i = 0; i < len; i++){
            int c = ((char)data[pos++]) & 0xff;
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            ret[i] = c;
        }
        return ret;
    }

    public long readLong() throws IllegalAccessException{
        if(pos >= data.length - 8 || data[pos] != 5){
            throw new IllegalAccessException();
        }
        pos += 9;
        return getNumber(data, pos - 8, 8);
    }

    public long[] readLongs() throws IllegalAccessException{
        if(data[pos] != 0x15)
            throw new IllegalAccessException();
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        if(pos + len * 8 > data.length){
            throw new IllegalAccessException();
        }
        long[] ret = new long[len];
        for(int i = 0; i < len; i++){
            long c = ((char)data[pos++]) & 0xff;
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            ret[i] = c;
        }
        return ret;
    }

    public String readString() throws IllegalAccessException{
        byte md = data[pos];
        if(pos >= data.length - 1 || (md != 7 && md != 8)){
            throw new IllegalAccessException();
        }
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        if(pos + len > data.length){
            throw new IllegalAccessException();
        }
        StringBuffer str = new StringBuffer(len);
        byte bytearr[] = new byte[len];
        int c, char2, char3;
        int count = 0;
        System.arraycopy(data, pos, bytearr, 0, len);
        pos += len;

        while(count < len){
            c = (int)bytearr[count] & 0xff;
            switch(c >> 4){
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    count++;
                    str.append((char)c);
                    break;
                case 12:
                case 13:
                    count += 2;
                    if(count > len)
                        throw new IllegalAccessException();
                    char2 = (int)bytearr[count - 1];
                    if((char2 & 0xC0) != 0x80)
                        throw new IllegalAccessException();
                    str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));
                    break;
                case 14:
                    count += 3;
                    if(count > len)
                        throw new IllegalAccessException();
                    char2 = (int)bytearr[count - 2];
                    char3 = (int)bytearr[count - 1];
                    if(((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new IllegalAccessException();
                    str.append((char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
                    break;
                default:
                    throw new IllegalAccessException();
            }
        }

        return new String(str);
    }

    public String[] readStrings() throws IllegalAccessException{
        if(data[pos] != 0x17)
            throw new IllegalAccessException();
        pos++;
        int len1 = (int)getNumber(data, pos, 2);
        pos += 2;
        String[] ret = new String[len1];
        for(int i = 0; i < len1; i++){
            int len = (int)getNumber(data, pos, 2);
            pos += 2;
            if(pos + len > data.length){
                throw new IllegalAccessException();
            }
            StringBuffer str = new StringBuffer(len);
            byte bytearr[] = new byte[len];
            int c, char2, char3;
            int count = 0;
            System.arraycopy(data, pos, bytearr, 0, len);
            pos += len;

            while(count < len){
                c = (int)bytearr[count] & 0xff;
                switch(c >> 4){
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        count++;
                        str.append((char)c);
                        break;
                    case 12:
                    case 13:
                        count += 2;
                        if(count > len)
                            throw new IllegalAccessException();
                        char2 = (int)bytearr[count - 1];
                        if((char2 & 0xC0) != 0x80)
                            throw new IllegalAccessException();
                        str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));
                        break;
                    case 14:
                        count += 3;
                        if(count > len)
                            throw new IllegalAccessException();
                        char2 = (int)bytearr[count - 2];
                        char3 = (int)bytearr[count - 1];
                        if(((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                            throw new IllegalAccessException();
                        str.append((char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
                        break;
                    default:
                        throw new IllegalAccessException();
                }
            }
            ret[i] = new String(str);
        }

        //        for (int i = 0; i < len; i++) {
        //            ret[i] = this.readString();
        //        }
        return ret;
    }

    public static long getNumber(byte[] buf, int off, int len){
        long l = 0;
        for(int i = 0; i < len; i++){
            l <<= 8;
            l += ((int)buf[off + i]) & 0xff;
        }
        return l;
    }

    public byte[] toBytes(){
        return data;
    }

    private static final String sep = ", ";

    public String toString(){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("Type:").append(appType);
        int tmppos = pos;
        pos = 6;

        for(int i = 0; i < numOfParameter; i++){
            try{
                switch(data[pos]){
                    case 1: //	布尔类型。
                        sbuf.append(sep).append("boolean:").append(readBoolean());
                        break;
                    case 2: //	单字节有符号整数
                        sbuf.append(sep).append("byte:").append(this.readByte());
                        break;
                    //				case 3: //	Unicode字符
                    //					sbuf.append(sep).append("Unicode char:").append(
                    //							this.readChar());
                    //					break;
                    case 4: //	四字节有符号整数
                        sbuf.append(sep).append("int:").append(this.readInt());
                        break;
                    case 5: //	八字节有符号整数
                        sbuf.append(sep).append("long:").append(this.readLong());
                        break;
                    case 6: //	双字节有符号整数
                        sbuf.append(sep).append("Short:").append(this.readShort());
                        break;
                    case 7: //	UTF-8字符串
                        sbuf.append(sep).append("UTF-8:").append(this.readString());
                        break;
                    case 8: //	UTF-16字符串
                        sbuf.append(sep).append("UTF-16:").append(this.readString());
                        break;
                    case 17: {
                        boolean[] barr = readBooleans();
                        sbuf.append(sep).append("boolean array num:").append(barr.length).append(" data:");
                        for(int j = 0; j < barr.length; j++){
                            sbuf.append(" ").append(barr[j]);
                        }
                    }
                        break;
                    case 18: {
                        byte[] barr = readBytes();
                        sbuf.append(sep).append("byte array num:").append(barr.length).append(" data:");
                        if(barr.length < 40){
                            for(int j = 0; j < barr.length; j++){
                                sbuf.append(" ").append(barr[j]);
                            }
                        }else{
                            sbuf.append(" omitted");
                        }
                    }
                        break;
                    //				case 19: {
                    //					char[] barr = readChars();
                    //					sbuf.append(sep).append("char array num:").append(
                    //							barr.length).append(" data:");
                    //					for (int j = 0; j < barr.length; j++) {
                    //						sbuf.append(" ").append(barr[j]);
                    //					}
                    //				}

                    //					break;
                    case 20: {
                        int[] barr = readInts();
                        sbuf.append(sep).append("int array num:").append(barr.length).append(" data:");
                        for(int j = 0; j < barr.length; j++){
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                        break;
                    case 21: {
                        long[] barr = readLongs();
                        sbuf.append(sep).append("long array num:").append(barr.length).append(" data:");
                        for(int j = 0; j < barr.length; j++){
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                        break;
                    case 22: {
                        short[] barr = readShorts();
                        sbuf.append(sep).append("short array num:").append(barr.length).append(" data:");
                        for(int j = 0; j < barr.length; j++){
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                        break;
                    case 23: {
                        String[] barr = readStrings();
                        sbuf.append(sep).append("String array num:").append(barr.length).append(" data:");
                        for(int j = 0; j < barr.length; j++){
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                        break;
                    default:
                        throw new IllegalAccessException();

                }
            }catch(Exception ex){
                sbuf.append(sep).append("参数错误num:").append(i).append(" type:").append(data[pos]);
                break;

            }
        }

        pos = tmppos;
        return sbuf.toString();
    }
}
