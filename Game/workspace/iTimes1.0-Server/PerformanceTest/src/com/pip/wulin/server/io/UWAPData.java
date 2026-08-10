package com.pip.wulin.server.io;

import java.io.*;

/**
 * The transfering data of the UWAP.
 */
public class UWAPData {
    /** internal use variable: if the system support UTF16 */
    private static boolean supportUTF16 = false;

    /** the raw data container */
    byte[] data;

    /** the number of parameters of the data */
    int num;

    /** the current position of the reading data */
    int pos;

    /** is 40 version? */
    boolean is40;

    /** to construct the data instance from parts of the buffer */
    public UWAPData(byte buf[], int pos, int len) {
        data = new byte[len];
        System.arraycopy(buf, pos, data, 0, len);
        num = (int)UWAPWritter.getNumber(data, 5, 1);
        this.pos = 6;
    }

    public UWAPData(byte buf[], int pos, int len, boolean is40) {
        this(buf, pos, len);
        this.is40 = is40;
    }

    UWAPData(byte buf[]) {
        data = buf;
        num = (int)UWAPWritter.getNumber(data, 5, 1);
        this.pos = 6;

    }

    public boolean is40Format() {
        return is40;
    }

    public int getNumOfParameters() {
        return ((int)num) & 0xff;
    }

    public byte[] getData() {
        return data;
    }

    /** to get the application type of the data */
    public byte getAppType() {
        if (data != null && data.length >= 6) {
            return data[0];
        }
        return -1;
    }

    /**
     * To get the next parameter type
     */
    public byte getNextParaType() {
        if (is40) {
            throw new RuntimeException("40版本数据不允许此操作。");
        } else {
            if (pos >= data.length) {
                return -1;
            }
            return data[pos];
        }
    }

    /** to read the next parameter as boolean */
    public boolean readBoolean() throws IllegalAccessException {
        if (is40) {
            pos++;
        } else {
            if (pos >= data.length - 1 || data[pos] != 1) {
                throw new IllegalAccessException();
            }
            pos += 2;
        }
        return ((data[pos - 1] & 1) == 1);
    }

    /** to read the next parameter as byte */
    public byte readByte() throws IllegalAccessException {
        if (is40) {
            pos++;
        } else {
            if (pos >= data.length - 1 || data[pos] != 2) {
                throw new IllegalAccessException();
            }
            pos += 2;
        }
        return data[pos - 1];
    }

    /** to read the next parameter as character */
    public char readChar() throws IllegalAccessException {
        if (is40) {
            pos += 2;
        } else {
            if (pos >= data.length - 2 || data[pos] != 3) {
                throw new IllegalAccessException();
            }
            pos += 3;
        }
        return (char)UWAPWritter.getNumber(data, pos - 2, 2);
    }

    /** to read the next parameter as integer */
    public int readInt() throws IllegalAccessException {
        if (is40) {
            pos += 4;
        } else {
            if (pos >= data.length - 4 || data[pos] != 4) {
                throw new IllegalAccessException();
            }
            pos += 5;
        }
        return (int)UWAPWritter.getNumber(data, pos - 4, 4);
    }

    /** to read the next parameter as long */
    public long readLong() throws IllegalAccessException {
        if (is40) {
            pos += 8;
        } else {
            if (pos >= data.length - 8 || data[pos] != 5) {
                throw new IllegalAccessException();
            }
            pos += 9;
        }
        return UWAPWritter.getNumber(data, pos - 8, 8);
    }

    /** to read the next parameter as short */
    public short readShort() throws IllegalAccessException {
        if (is40) {
            pos += 2;
        } else {
            if (pos >= data.length - 2 || data[pos] != 6) {
                throw new IllegalAccessException();
            }
            pos += 3;
        }
        return (short)UWAPWritter.getNumber(data, pos - 2, 2);
    }

    /** to read the next parameter as String */
    public String readString() throws IOException, IllegalAccessException {
        if (is40) {
            return readUTFString((byte)7);
        } else {
            byte md = data[pos];
            if (pos >= data.length - 1 || (md != 7 && md != 8)) {
                throw new IllegalAccessException();
            }
            pos++;
            return readUTFString(md);
        }
    }

    private String readUTFString(byte md) throws IOException,
        IllegalAccessException {
        int len = (int)UWAPWritter.getNumber(data, pos, 2) + 2;
        if (pos + len > data.length) {
            throw new IllegalAccessException();
        }
        byte[] tmp = new byte[len];
        System.arraycopy(data, pos, tmp, 0, len);
        pos += len;
        if (md == 7) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                tmp));
            return in.readUTF();
        } else if (md == 8) {
            if (len == 4) {
                return "";
            }
            if (supportUTF16) {
                try {
                    String s = new String(tmp, 2, len - 2, "UTF16");
                    return s;
                } catch (UnsupportedEncodingException e) {
                    supportUTF16 = false;
                }
            }
            int off = 2;
            len -= 2;
            int highByte = 1;
            int lowByte = 0;
            StringBuffer ret = new StringBuffer();
            if (tmp[off] == (byte)0xFF && tmp[off + 1] == (byte)0xFE) {
                off += 2;
                len -= 2;
            } else if (tmp[off] == (byte)0xFE && tmp[off + 1] == (byte)0xFF) {
                off += 2;
                len -= 2;
                highByte = 0;
                lowByte = 1;
            }
            for (int i = 0; i < len; i += 2) {
                ret.append((char)((((char)tmp[off + i + highByte] & 0xFF) <<
                                   8) |
                                  ((char)tmp[off + i + lowByte] & 0xFF)));
            }
            return ret.toString();
        }
        throw new IllegalAccessException();
    }

    /** to read the next parameter as one dimension of boolean array */
    public boolean[] readBooleans() throws IllegalAccessException {
        if (!is40) {
            if (pos >= data.length - 1 || data[pos] != ((1 << 4) + 1)) {
                throw new IllegalAccessException();
            }
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;
        if (pos + len > data.length) {
            throw new IllegalAccessException();
        }
        boolean[] ret = new boolean[len];
        for (int i = 0; i < len; i++) {
            ret[i] = (data[pos++] & 1) == 1;
        }
        return ret;
    }

    /** to read the next parameter as one dimension of byte array */
    public byte[] readBytes() throws IllegalAccessException {
        if (!is40) {
            if (pos >= data.length - 1 || data[pos] != ((1 << 4) + 2)) {
                throw new IllegalAccessException();
            }
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;
        if (pos + len > data.length) {
            throw new IllegalAccessException();
        }
        byte[] ret = new byte[len];
        System.arraycopy(data, pos, ret, 0, len);
        pos += len;
        return ret;
    }

    /** to read the next parameter as one dimension of character array */
    public char[] readChars() throws IllegalAccessException {
        if (!is40) {
            if (pos >= data.length - 1 || data[pos] != ((1 << 4) + 3)) {
                throw new IllegalAccessException();
            }
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;
        if (pos + len * 2 > data.length) {
            throw new IllegalAccessException();
        }
        char[] ret = new char[len];
        for (int i = 0; i < len; i++) {
            char c = (char)((int)data[pos++] & 0xff);
            c = (char)((c << 8) + ((int)data[pos++] & 0xff));
            ret[i] = c;
        }
        return ret;
    }

    /** to read the next parameter as one dimension of integer array */
    public int[] readInts() throws IllegalAccessException {
        if (!is40) {
            if (pos >= data.length - 1 || data[pos] != ((1 << 4) + 4)) {
                throw new IllegalAccessException();
            }
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;
        if (pos + len * 4 > data.length) {
            throw new IllegalAccessException();
        }
        int[] ret = new int[len];
        for (int i = 0; i < len; i++) {
            int c = ((char)data[pos++]) & 0xff;
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            c = (c << 8) + (((char)data[pos++]) & 0xff);
            ret[i] = c;
        }
        return ret;
    }

    /** to read the next parameter as one dimension of long array */
    public long[] readLongs() throws IllegalAccessException {
        if (!is40) {
            if (pos >= data.length - 1 || data[pos] != ((1 << 4) + 5)) {
                throw new IllegalAccessException();
            }
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;
        if (pos + len * 8 > data.length) {
            throw new IllegalAccessException();
        }
        long[] ret = new long[len];
        for (int i = 0; i < len; i++) {
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

    /** to read the next parameter as one dimension of short array */
    public short[] readShorts() throws IllegalAccessException {
        if (!is40) {
            if (pos >= data.length - 1 || data[pos] != ((1 << 4) + 6)) {
                throw new IllegalAccessException();
            }
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;
        if (pos + len * 2 > data.length) {
            throw new IllegalAccessException();
        }
        short[] ret = new short[len];
        for (int i = 0; i < len; i++) {
            short c = (short)(((int)data[pos++]) & 0xff);
            c = (short)((c << 8) + (((int)data[pos++]) & 0xff));
            ret[i] = c;
        }
        return ret;
    }

    /** to read the next parameter as one dimension of string array */
    public String[] readStrings() throws IOException, IllegalAccessException {
        byte md = 7;
        if (!is40) {
            md = data[pos];
            if (pos >= data.length - 1 ||
                (md != ((1 << 4) + 7) && md != ((1 << 4) + 8))) {
                throw new IllegalAccessException();
            }
            md -= (1 << 4);
            pos++;
        }
        int len = (int)UWAPWritter.getNumber(data, pos, 2);
        pos += 2;

        String[] ret = new String[len];
        for (int i = 0; i < len; i++) {
            ret[i] = this.readUTFString(md);
        }
        return ret;
    }

    /** to set the position to the first parameter of the data set */
    public void reset() {
        pos = 6;
    }

    /** to skip specified number of parameters */
    public void skip(int i) throws IOException, IllegalAccessException {
        if (is40) {
            throw new RuntimeException("40版本数据不允许此操作。");
        }
        int len;
        for (; i >= 0; i--) {
            switch (data[pos++]) {
            case 1:
            case 2:
                pos++;
                break;
            case 3:
                pos += 2;
                break;
            case 4:
                pos += 4;
                break;
            case 5:
                pos += 8;
                break;
            case 6:
                pos += 2;
                break;
            case 7:
            case 8:
            case (1 << 4) + 1:
            case (1 << 4) + 2:
                len = (int)UWAPWritter.getNumber(data, pos, 2);
                pos += 2 + len;
                break;
            case (1 << 4) + 3:
                len = (int)UWAPWritter.getNumber(data, pos, 2);
                pos += 2 + len * 2;
                break;
            case (1 << 4) + 4:
                len = (int)UWAPWritter.getNumber(data, pos, 2);
                pos += 2 + len * 4;
                break;
            case (1 << 4) + 5:
                len = (int)UWAPWritter.getNumber(data, pos, 2);
                pos += 2 + len * 8;
                break;
            case (1 << 4) + 6:
                len = (int)UWAPWritter.getNumber(data, pos, 2);
                pos += 2 + len * 2;
                break;
            case (1 << 4) + 7:
            case (1 << 4) + 8:
                len = (int)UWAPWritter.getNumber(data, pos, 2);
                for (int j = 0; j < len; j++) {
                    int sLen = (int)UWAPWritter.getNumber(data, pos, 2);
                    pos += 2 + sLen;
                }
                break;
            }
        }
    }

    static final String sep = ", ";
    public synchronized String toString() {
        return toString(true);
    }

    public synchronized String toString(boolean includeBinary) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("AppType:").append(getAppType());
        int tmppos = pos;
        reset();
        int num = getNumOfParameters();

        if (is40) {
            sbuf.append(", ParamNum:" + num + ", Data:");
            int max = data.length;
            if (!includeBinary && max > 150) {
                max = 150;
            }
            for (int j = 0; j < max; j++) {
                sbuf.append(" ").append(data[j]);
            }
        } else {
            for (int i = 0; i < num; i++) {
                try {
                    switch (getNextParaType()) {
                    case 1: //	布尔类型。
                        sbuf.append(sep).append("boolean:").append(this.
                            readBoolean());
                        break;
                    case 2: //	单字节有符号整数
                        sbuf.append(sep).append("byte:").append(this.readByte());
                        break;
                    case 3: //	Unicode字符
                        sbuf.append(sep).append("Unicode char:").append(this.
                            readChar());
                        break;
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
                        sbuf.append(sep).append("UTF-8:").append(this.
                            readString());
                        break;
                    case 8: //	UTF-16字符串
                        sbuf.append(sep).append("UTF-16:").append(this.
                            readString());
                        break;
                    case 17: {
                        boolean[] barr = readBooleans();
                        sbuf.append(sep).append("boolean array num:").append(
                            barr.
                            length).append(" data:");
                        for (int j = 0; j < barr.length; j++) {
                            sbuf.append(" ").append(barr[j]);
                        }
                    }
                    break;
                    case 18: {
                        byte[] barr = readBytes();
                        sbuf.append(sep).append("byte array num:").append(barr.
                            length).append(" data:");
                        if (includeBinary || barr.length < 40) {
                            for (int j = 0; j < barr.length; j++) {
                                sbuf.append(" ").append(barr[j]);
                            }
                        } else {
                            sbuf.append(" omitted");
                        }
                    }
                    break;
                    case 19: {
                        char[] barr = readChars();
                        sbuf.append(sep).append("char array num:").append(barr.
                            length).append(" data:");
                        for (int j = 0; j < barr.length; j++) {
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                    break;
                    case 20: {
                        int[] barr = readInts();
                        sbuf.append(sep).append("int array num:").append(barr.
                            length).append(" data:");
                        for (int j = 0; j < barr.length; j++) {
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                    break;
                    case 21: {
                        long[] barr = readLongs();
                        sbuf.append(sep).append("long array num:").append(barr.
                            length).append(" data:");
                        for (int j = 0; j < barr.length; j++) {
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                    break;
                    case 22: {
                        short[] barr = readShorts();
                        sbuf.append(sep).append("short array num:").append(barr.
                            length).append(" data:");
                        for (int j = 0; j < barr.length; j++) {
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                    break;
                    case 23: {
                        String[] barr = readStrings();
                        sbuf.append(sep).append("String array num:").append(
                            barr.
                            length).append(" data:");
                        for (int j = 0; j < barr.length; j++) {
                            sbuf.append(" ").append(barr[j]);
                        }
                    }

                    break;
                    default:
                        throw new IllegalAccessException();

                    }
                } catch (Exception ex) {
                    sbuf.append(sep).append("参数错误num:").append(i).append(
                        " type:").
                        append(getNextParaType());
                    break;

                }
            }
        }
        pos = tmppos;
        return sbuf.toString();
    }
}
