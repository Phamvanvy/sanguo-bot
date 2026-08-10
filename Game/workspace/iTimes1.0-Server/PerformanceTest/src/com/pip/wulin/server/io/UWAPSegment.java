package com.pip.wulin.server.io;

import java.io.*;

/**
 * <p>Title: UWAPSegment类</p>
 * <p>Description: 封装一条记录</p>
 * <p>Note:本类既可以读，也可以写，但不能混合操作。<br>
 * 在写的状态，可以通过Flush后转换到读的状态，但读的状态不可转换到写的状态。
 * 为减少Size, 程序不检查状态，由应用自己保证。
 * 由于读和写的函数很多，程序不检查内存不足和越界等，以减少size</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: PiP</p>
 * @author Chad Zhu
 * @version 1.0
 */
public class UWAPSegment {
    /** 数据类型 */
    private byte type = -1;

    /** 数据段内参数个数 */
    private byte paramNum = 0;

    /**
     * 数据段头信息
     * 数据段类型	单字节整数	      具体的应用定义的请求或响应类型编码。
     * 数据段长度	四字节整数(高字节在前)   整个数据段的长度（包括数据段头信息）。
     * 参数个数	单字节整数	      数据段内参数的个数。
     */
    private static final byte dataHeadBuf[] = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0};

    private ByteArrayOutputStream segCash = null;
    private DataOutputStream segOut = null;

    /**
     * 构造一个用于写的UWAPSegment
     */
    public UWAPSegment() {
        try {
            segCash = new ByteArrayOutputStream(10);
            segOut = new DataOutputStream(segCash);
            segOut.write(dataHeadBuf);
        } catch (IOException ex) {
        }
    }

    public UWAPSegment(UWAPData data) {
        flushed = true;
        this.data = data.data;
    }

    public UWAPSegment(int type) {
        this();
        this.type = (byte)type;
    }

    /** 使用一段已有的Buffer创建，只读, 该Buffer是一个完整的UWAPSegment, 一般是从服务器端或RMS中读取的 */
    public UWAPSegment(byte buf[], int startPos, int len) {
        data = new byte[len];
        System.arraycopy(buf, startPos, data, 0, len);
        type = data[0];
        paramNum = data[5];
        flushed = true;
    }

    // 克隆
    public UWAPSegment cloneSegment() {
        if (this.flushed) {
            return new UWAPSegment(data, 0, data.length);
        } else {
            UWAPSegment seg = new UWAPSegment(type);
            seg.paramNum = this.paramNum;
            seg.segCash = new ByteArrayOutputStream();
            seg.segOut = new DataOutputStream(seg.segCash);
            seg.flushed = false;
            try {
                seg.segOut.write(segCash.toByteArray());
            } catch (IOException e) {
            }
            return seg;
        }
    }

    /** 添加一个Boolean参数 */
    public void writeBoolean(boolean b) throws IOException {
        segOut.writeByte(0x01);
        segOut.writeByte(b ? 0x01 : 0x0);
        paramNum++;
    }

    /** 添加一个byte参数 */
    public void writeByte(byte b) throws IOException {
        segOut.writeByte(0x02);
        segOut.writeByte(b);
        paramNum++;
    }

    /** 添加一个char参数 */
    public void writeChar(char c) throws IOException {
        segOut.writeByte(0x03);
        segOut.writeByte((byte)(c >> 8));
        segOut.writeByte((byte)c);
        paramNum++;
    }

    /** 添加一个int参数 */
    public void writeInt(int n) throws IOException {
        segOut.writeByte(0x04);
        segOut.writeByte((byte)(n >> 24));
        segOut.writeByte((byte)(n >> 16));
        segOut.writeByte((byte)(n >> 8));
        segOut.writeByte((byte)n);
        paramNum++;
    }

    /** 添加一个short参数 */
    public void writeShort(short shortValue) throws IOException {
        segOut.writeByte(0x06);
        segOut.writeByte((byte)(shortValue >> 8));
        segOut.writeByte((byte)shortValue);
        paramNum++;
    }

    /** 添加一个long参数 */
    public void writeLong(long longValue) throws IOException {
        segOut.writeByte(0x05);
        segOut.writeByte((byte)(longValue >> 56));
        segOut.writeByte((byte)(longValue >> 48));
        segOut.writeByte((byte)(longValue >> 40));
        segOut.writeByte((byte)(longValue >> 32));
        segOut.writeByte((byte)(longValue >> 24));
        segOut.writeByte((byte)(longValue >> 16));
        segOut.writeByte((byte)(longValue >> 8));
        segOut.writeByte((byte)longValue);
        paramNum++;
    }

    /** 添加一个String参数, UTF8编码 */
    public void writeString(String s) throws IOException {
        segOut.writeByte(0x07);
        segOut.writeUTF((s == null) ? "" : s);
        paramNum++;
    }

    /** 添加一个String参数, UTF16编码 */
    public void writeUTF16(String s) throws IOException {
        segOut.writeByte(8);
        writeAnUTF16(s);
        paramNum++;
    }

    void writeAnUTF16(String s) throws IOException {
        if (s == null) {
            s = "";
        }
        int len = s.length() + 1;
        len *= 2;
        segOut.writeByte((byte)(len >> 8));
        segOut.writeByte((byte)len);
        segOut.writeByte((byte)0xff);
        segOut.writeByte((byte)0xfe);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            segOut.writeByte((byte)c);
            segOut.writeByte((byte)(c >> 8));
        }
    }

    /** 添加一个boolean[]参数 */
    public void writeBooleanArr(boolean b[]) throws IOException {
        segOut.writeByte(0x11);
        if (b == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(b.length >> 8));
            segOut.writeByte((byte)b.length);
            for (int i = 0; i < b.length; i++) {
                segOut.writeByte(b[i] ? 0x1 : 0x0);
            }
        }
        paramNum++;
    }

    /** 添加一个byte[]参数 */
    public void writeByteArr(byte b[]) throws IOException {
        segOut.writeByte(0x12);
        if (b == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(b.length >> 8));
            segOut.writeByte((byte)b.length);
            for (int i = 0; i < b.length; i++) {
                segOut.writeByte(b[i]);
            }
        }
        paramNum++;
    }

    /** 添加一个char[]参数 */
    public void writeCharArr(char c[]) throws IOException {
        segOut.writeByte(0x13);
        if (c == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(c.length >> 8));
            segOut.writeByte((byte)c.length);
            for (int i = 0; i < c.length; i++) {
                segOut.writeByte((byte)(c[i] >> 8));
                segOut.writeByte((byte)c[i]);
            }
        }
        paramNum++;
    }

    /** 添加一个int[]参数 */
    public void writeIntArr(int n[]) throws IOException {
        segOut.writeByte(0x14);
        if (n == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(n.length >> 8));
            segOut.writeByte((byte)n.length);
            for (int i = 0; i < n.length; i++) {
                segOut.writeByte((byte)(n[i] >> 24));
                segOut.writeByte((byte)(n[i] >> 16));
                segOut.writeByte((byte)(n[i] >> 8));
                segOut.writeByte((byte)n[i]);
            }
        }
        paramNum++;
    }

    /** 添加一个long[]参数 */
    public void writeLongArr(long longArray[]) throws IOException {
        segOut.writeByte(0x15);
        if (longArray == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(longArray.length >> 8));
            segOut.writeByte((byte)longArray.length);
            for (int i = 0; i < longArray.length; i++) {
                segOut.writeByte((byte)(longArray[i] >> 56));
                segOut.writeByte((byte)(longArray[i] >> 48));
                segOut.writeByte((byte)(longArray[i] >> 40));
                segOut.writeByte((byte)(longArray[i] >> 32));
                segOut.writeByte((byte)(longArray[i] >> 24));
                segOut.writeByte((byte)(longArray[i] >> 16));
                segOut.writeByte((byte)(longArray[i] >> 8));
                segOut.writeByte((byte)(longArray[i]));
            }
        }
        paramNum++;
    }

    /** 添加一个short[]参数 */
    public void writeShortArr(short shortArray[]) throws IOException {
        segOut.writeByte(0x16);
        if (shortArray == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(shortArray.length >> 8));
            segOut.writeByte((byte)shortArray.length);
            for (int i = 0; i < shortArray.length; i++) {
                segOut.writeByte((byte)(shortArray[i] >> 8));
                segOut.writeByte((byte)shortArray[i]);
            }
        }
        paramNum++;
    }

    /** 添加一个String[]参数, UTF8 */
    public void writeStringArr(String s[]) throws IOException {
        segOut.writeByte(0x17);
        if (s == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(s.length >> 8));
            segOut.writeByte((byte)s.length);
            for (int i = 0; i < s.length; i++) {
                segOut.writeUTF((s[i] == null) ? "" : s[i]);
            }
        }
        paramNum++;
    }

    /** 添加一个String[]参数, UTF16 */
    public void writeUTF16(String s[]) throws IOException {
        segOut.writeByte(0x18);
        if (s == null) {
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        } else {
            segOut.writeByte((byte)(s.length >> 8));
            segOut.writeByte((byte)s.length);
            for (int i = 0; i < s.length; i++) {
                writeAnUTF16(s[i]);
            }
        }
        paramNum++;
    }

    /**
     * 置写结束状态
     */
    private boolean flushed = false;
    public void flush() {
        if (flushed)return;
        flushed = true;
        try {
            segOut.flush();
        } catch (Exception ex) {
        }
        data = segCash.toByteArray();
        try {
            segOut.close();
        } catch (IOException ex1) {
        }
        segCash = null;
        segOut = null;

        data[0] = type;
        data[5] = paramNum;
        //长度
        setNumber(data.length, data, 1, 4);
    }

    //下面是有关读的操作

    /** byte流 */
    public byte[] data;

    /**
     * Public helper method to save one little-endn number to a byte array.
     * @param num the number to saved
     * @param buf the byte array buffer
     * @param off the offset to save the number
     * @param len the num of bytes the number will take
     * Note: it is the client application's responsibility to make sure
     *       not to reach out of the array boundries.
     */
    public static void setNumber(int num, byte[] buf, int off, int len) {
        for (int i = len - 1; i >= 0; i--) {
            buf[off + i] = (byte)(num & 0xff);
            num >>= 8;
        }
    }

    /**
     * 立即释放内存
     */
    public void release() {
        data = null;
        try {
            if (segOut != null) {
                segOut.close();
            }
        } catch (Exception ex) {
        }
        segCash = null;
        segOut = null;
    }

    public void write(boolean b[]) throws IOException {
        writeBooleanArr(b);
    }

    public void write(boolean b) throws IOException {
        writeBoolean(b);
    }

    public void write(byte b) throws IOException {
        writeByte(b);
    }

    public void write(byte b[]) throws IOException {
        writeByteArr(b);
    }

    public void write(char c[]) throws IOException {
        writeCharArr(c);
    }

    public void write(char c) throws IOException {
        writeChar(c);
    }

    public void write(int n[]) throws IOException {
        writeIntArr(n);
    }

    public void write(int n) throws IOException {
        writeInt(n);
    }

    public void write(long longArray[]) throws IOException {
        writeLongArr(longArray);
    }

    public void write(long longValue) throws IOException {
        writeLong(longValue);
    }

    public void write(short shortArray[]) throws IOException {
        writeShortArr(shortArray);
    }

    public void write(short shortValue) throws IOException {
        writeShort(shortValue);
    }

    public void write(String s[]) throws IOException {
        writeStringArr(s);
    }

    public void write(String s) throws IOException {
        writeString(s);
    }
}
