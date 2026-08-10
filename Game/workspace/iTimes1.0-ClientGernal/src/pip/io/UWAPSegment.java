package pip.io;


import java.io.*;

import javax.microedition.io.*;


public class UWAPSegment{
    /** 全局代理设置 */
    //#if UseProxy == false;
    public static boolean useProxyGlobal = false;
    //#else
    //# public static boolean useProxyGlobal = true;
    //#endif
        
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

        // 打开连接
        HttpConnection conn = (HttpConnection)Connector.open(requestUrl);

        if(realHost != null){
            conn.setRequestProperty("X-Online-Host", realHost);
        }

        return conn;
    }

    /**
     * 异步请求标志，用于判断是否是相同的请求
     */
    public long asyncSign = 0;

    /** 数据类型 */
    public byte type = -1;

    /** 数据段内参数个数 */
    public byte paramNum = 0;

    public int serial;

    /**
     * 数据发出时的时间戳
     */
    public int timeStamp;

    /**
     * 数据段头信息
     * 数据段类型	单字节整数	      具体的应用定义的请求或响应类型编码。
     * 数据段长度	四字节整数(高字节在前)   整个数据段的长度（包括数据段头信息）。
     * 参数个数	单字节整数	      数据段内参数的个数。
     */
    byte dataHeadBuf[] = {
                    0x0, 0x0, 0x0, 0x0, 0x0, 0x0
    };

    ByteArrayOutputStream segCash = null;
    DataOutputStream segOut = null;
    
    /**
     * 构造一个用于写的UWAPSegment
     */
    public UWAPSegment(){
        try{
            segCash = new ByteArrayOutputStream(10);
            segOut = new DataOutputStream(segCash);
            segOut.write(dataHeadBuf);
        }catch(IOException ex){
        }
    }

    public UWAPSegment(byte type){
        this();
        this.type = type;
    }

    /** 使用一段已有的Buffer创建，只读, 该Buffer是一个完整的UWAPSegment, 一般是从服务器端或RMS中读取的 */
    public UWAPSegment(byte buf[], int startPos, int len){
        boolean isCompressed = false;

        if(len >>> 24 != 0){
            isCompressed = true;
        }

        len &= 0x00FFFFFF;
        
        data = new byte[len];
        
        System.arraycopy(buf, startPos, data, 0, len);
        type = data[0];
        
        if(isCompressed){
            long t1 = System.currentTimeMillis();
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try{
                bis.skip(5);
                dos.writeByte(type);
                dos.writeInt(0);

                UWAPUncompress ucom = new UWAPUncompress(bis, bos);
                ucom.unCompress();

                data = bos.toByteArray();
            }catch(IOException e){
                //#debug
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
            
            data[0] = type;
            setNumber(data.length, data, 1, 4);
            
            t1 = System.currentTimeMillis() - t1;
            //#debug
            System.out.println("Uncompress segment : " + type + " , " + len + " , " + data.length + " , " + (len * 100 / data.length) + "% , spend : " + t1 + "ms");
        }
        
        paramNum = data[5];
    }

    /** 从服务器端接收的数据中创建 */
    public UWAPSegment(DataInputStream in) throws Exception{
        type = in.readByte();
        int len = in.readInt();

        boolean isCompressed = false;

        if(len >>> 24 != 0){
            isCompressed = true;
        }

        len &= 0x00FFFFFF;

        if(len < 0 || len > 100000){
            throw new Exception("Invalid packet: too large segment");
        }

        if(isCompressed){
            long t1 = System.currentTimeMillis();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            try{
                dos.writeByte(type);
                dos.writeInt(0);

                UWAPUncompress ucom = new UWAPUncompress(in, bos);
                ucom.unCompress();

                data = bos.toByteArray();
            }catch(IOException e){
                //#debug
                e.printStackTrace();
            }finally{
                try{
                    dos.close();
                }catch(IOException e){
                }
            }
            
            paramNum = data[5];
            data[0] = type;
            setNumber(data.length, data, 1, 4);
            
            t1 = System.currentTimeMillis() - t1;
            //#debug
            System.out.println("Uncompress segment : " + type + " , " + len + " , " + data.length + " , " + (len * 100 / data.length) + "% , spend : " + t1 + "ms");
        }else{

            data = new byte[len];

            try{
                in.readFully(data, 5, len - 5);
            }catch(EOFException e){
                throw new Exception("数据验证错误!");
            }

            paramNum = data[5];
            data[0] = type;
            setNumber(len, data, 1, 4);
        }
    }

    /** 添加一个Boolean参数 */
    public void writeBoolean(boolean b) throws IOException{
        segOut.writeByte(0x01);
        segOut.writeByte(b? 0x01: 0x0);
        paramNum++;
    }

    /** 添加一个byte参数 */
    public void writeByte(byte b) throws IOException{
        segOut.writeByte(0x02);
        segOut.writeByte(b);
        paramNum++;
    }

    /** 添加一个char参数 */
    public void writeChar(char c) throws IOException{
        segOut.writeByte(0x03);
        segOut.writeByte((byte)(c >> 8));
        segOut.writeByte((byte)c);
        paramNum++;
    }

    /** 添加一个int参数 */
    public void writeInt(int n) throws IOException{
        segOut.writeByte(0x04);
        segOut.writeByte((byte)(n >> 24));
        segOut.writeByte((byte)(n >> 16));
        segOut.writeByte((byte)(n >> 8));
        segOut.writeByte((byte)n);
        paramNum++;
    }

    /** 添加一个short参数 */
    public void writeShort(short shortValue) throws IOException{
        segOut.writeByte(0x06);
        segOut.writeByte((byte)(shortValue >> 8));
        segOut.writeByte((byte)shortValue);
        paramNum++;
    }

    /** 添加一个long参数 */
    public void writeLong(long longValue) throws IOException{
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
    public void writeString(String s) throws IOException{
        segOut.writeByte(0x07);
        segOut.writeUTF((s == null)? "": s);
        paramNum++;
    }

    /** 添加一个String参数, UTF16编码 */
    public void writeUTF16(String s) throws IOException{
        segOut.writeByte(8);
        writeAnUTF16(s);
        paramNum++;
    }

    void writeAnUTF16(String s) throws IOException{
        if(s == null){
            s = "";
        }

        int len = s.length() + 1;
        len *= 2;
        segOut.writeByte((byte)(len >> 8));
        segOut.writeByte((byte)len);
        segOut.writeByte((byte)0xff);
        segOut.writeByte((byte)0xfe);

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            segOut.writeByte((byte)c);
            segOut.writeByte((byte)(c >> 8));
        }
    }

    /** 添加一个boolean[]参数 */
    public void writeBooleans(boolean b[]) throws IOException{
        segOut.writeByte(0x11);

        if(b == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(b.length >> 8));
            segOut.writeByte((byte)b.length);

            for(int i = 0; i < b.length; i++){
                segOut.writeByte(b[i]? 0x1: 0x0);
            }
        }

        paramNum++;
    }

    /** 添加一个byte[]参数 */
    public void writeBytes(byte b[]) throws IOException{
        segOut.writeByte(0x12);

        if(b == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(b.length >> 8));
            segOut.writeByte((byte)b.length);

            for(int i = 0; i < b.length; i++){
                segOut.writeByte(b[i]);
            }
        }

        paramNum++;
    }

    /** 添加一个char[]参数 */
    public void writeChars(char c[]) throws IOException{
        segOut.writeByte(0x13);

        if(c == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(c.length >> 8));
            segOut.writeByte((byte)c.length);

            for(int i = 0; i < c.length; i++){
                segOut.writeByte((byte)(c[i] >> 8));
                segOut.writeByte((byte)c[i]);
            }
        }

        paramNum++;
    }

    /** 添加一个int[]参数 */
    public void writeInts(int n[]) throws IOException{
        segOut.writeByte(0x14);

        if(n == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(n.length >> 8));
            segOut.writeByte((byte)n.length);

            for(int i = 0; i < n.length; i++){
                segOut.writeByte((byte)(n[i] >> 24));
                segOut.writeByte((byte)(n[i] >> 16));
                segOut.writeByte((byte)(n[i] >> 8));
                segOut.writeByte((byte)n[i]);
            }
        }

        paramNum++;
    }

    /** 添加一个long[]参数 */
    public void writeLongs(long longArray[]) throws IOException{
        segOut.writeByte(0x15);

        if(longArray == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(longArray.length >> 8));
            segOut.writeByte((byte)longArray.length);

            for(int i = 0; i < longArray.length; i++){
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
    public void writeShorts(short shortArray[]) throws IOException{
        segOut.writeByte(0x16);

        if(shortArray == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(shortArray.length >> 8));
            segOut.writeByte((byte)shortArray.length);

            for(int i = 0; i < shortArray.length; i++){
                segOut.writeByte((byte)(shortArray[i] >> 8));
                segOut.writeByte((byte)shortArray[i]);
            }
        }

        paramNum++;
    }

    /** 添加一个String[]参数, UTF8 */
    public void writeStrings(String s[]) throws IOException{
        segOut.writeByte(0x17);

        if(s == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(s.length >> 8));
            segOut.writeByte((byte)s.length);

            for(int i = 0; i < s.length; i++){
                segOut.writeUTF((s[i] == null)? "": s[i]);
            }
        }

        paramNum++;
    }

    /** 添加一个String[]参数, UTF16 */
    public void writeUTF16s(String s[]) throws IOException{
        segOut.writeByte(0x18);

        if(s == null){
            segOut.writeByte(0x0);
            segOut.writeByte(0x0);
        }else{
            segOut.writeByte((byte)(s.length >> 8));
            segOut.writeByte((byte)s.length);

            for(int i = 0; i < s.length; i++){
                writeAnUTF16(s[i]);
            }
        }

        paramNum++;
    }

    /**
     * 置写结束状态
     */
    boolean flushed = false;

    public void flush(){
        if(flushed){
            return;
        }

        flushed = true;

        try{
            segOut.flush();
        }catch(Exception ex){
        }

        data = segCash.toByteArray();

        try{
            segOut.close();
        }catch(IOException ex1){
        }

        segCash = null;
        segOut = null;

        data[0] = type;
        data[5] = paramNum;

        //长度
        setNumber(data.length, data, 1, 4);
    }

    //下面是有关读的操作

    /** 读指针 */
    int pos = 6;

    public void reset(){
        pos = 6;
    }

    /** byte流 */
    public byte[] data;

    /** 获取下一个参数的类型, 兼做EOF检测 */
    public byte getNextParaType(){
        if(pos >= data.length - 1){
            return -1;
        }

        return data[pos];
    }

    public boolean readBoolean(){
        pos += 2;

        return ((data[pos - 1] & 0x01) == 0x01);
    }

    public byte readByte(){
        pos += 2;

        return data[pos - 1];
    }

    public char readChar(){
        pos += 3;

        return (char)getNumber(data, pos - 2, 2);
    }

    public int readInt(){
        pos += 5;

        return (int)getNumber(data, pos - 4, 4);
    }

    public long readLong(){
        pos += 9;

        return getNumber(data, pos - 8, 8);
    }

    public short readShort(){
        pos += 3;

        return (short)getNumber(data, pos - 2, 2);
    }

    public String readString(){
        byte md = data[pos];
        pos++;

        return readUTFString(md);
    }

    //IOException被Catch了, 返回""
    //不再代替手机生成UTFString, 既然手机用UTF16编码，就应该支持
    String readUTFString(byte md){
        try{
            int len = (int)getNumber(data, pos, 2) + 2;
            int start = pos;
            pos += len;

            if(md == 7){
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(data, start, len));

                return in.readUTF();
            }else{ // if (md == 8) {
                pos += 2;
                int chars = (len - 4) / 2;

                if(chars == 0){
                    return "";
                }

                char[] carr = new char[chars];
                int cindex = 0;

                for(int i = start + 4; cindex < chars; i += 2){
                    // data[i]是低位，data[i + 1]是高位
                    carr[cindex] = (char)((data[i] & 0xFF) + ((data[i + 1] & 0xFF) << 8));
                    cindex++;
                }

                return new String(carr);
            }
        }catch(Exception e){
            return "";
        }
    }

    public boolean[] readBooleans(){
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        boolean[] ret = new boolean[len];

        for(int i = 0; i < len; i++){
            ret[i] = (data[pos++] & 0x01) == 0x01;
        }

        return ret;
    }

    public byte[] readBytes(){
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        byte[] ret = new byte[len];
        System.arraycopy(data, pos, ret, 0, len);
        pos += len;

        return ret;
    }

    public char[] readChars(){
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        char[] ret = new char[len];

        for(int i = 0; i < len; i++, pos += 2){
            ret[i] = (char)(((data[pos] & 0xff) << 8) | (data[pos + 1] & 0xFF));
        }

        return ret;
    }

    public int[] readInts(){
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        int[] ret = new int[len];

        for(int i = 0; i < len; i++, pos += 4){
            ret[i] = (data[pos] & 0xFF) << 24 | (data[pos + 1] & 0xFF) << 16 | (data[pos + 2] & 0xFF) << 8 | (data[pos + 3] & 0xFF);
        }

        return ret;
    }

    public long[] readLongs(){
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        long[] ret = new long[len];

        for(int i = 0; i < len; i++, pos += 8){
            ret[i] = (data[pos] & 0xFF) << 56 | (data[pos + 1] & 0xFF) << 48 | (data[pos + 2] & 0xFF) << 40 | (data[pos + 3] & 0xFF) << 32 | (data[pos + 4] & 0xFF) << 24
                            | (data[pos + 5] & 0xFF) << 16 | (data[pos + 6] & 0xFF) << 8 | (data[pos + 7] & 0xFF);
        }

        return ret;
    }

    public short[] readShorts(){
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;
        short[] ret = new short[len];

        for(int i = 0; i < len; i++, pos += 2){
            ret[i] = (short)(((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF));
        }

        return ret;
    }

    public String[] readStrings(){
        byte md = data[pos];
        md -= 0x10;
        pos++;
        int len = (int)getNumber(data, pos, 2);
        pos += 2;

        String[] ret = new String[len];

        for(int i = 0; i < len; i++){
            ret[i] = readUTFString(md);
        }

        return ret;
    }

    /** 跳过几个参数阅读 */
    public void skip(int i){
        int len;

        for(; i > 0; i--){
            switch(data[pos++]){
                case 1: //boolean
                case 2: //byte
                    pos++;

                    break;
                case 3: //char
                    pos += 2;

                    break;
                case 4: //int
                    pos += 4;

                    break;
                case 5: //long
                    pos += 8;

                    break;
                case 6: //short
                    pos += 2;

                    break;
                case 7: //UTF8
                case 8: //UTF16
                case 0x11: //boolean[]
                case 0x12: //byte[]
                    len = (int)getNumber(data, pos, 2);
                    pos += 2 + len;

                    break;
                case 0x13: //char[]
                    len = (int)getNumber(data, pos, 2);
                    pos += 2 + len * 2;

                    break;
                case 0x14: //int[]
                    len = (int)getNumber(data, pos, 2);
                    pos += 2 + len * 4;

                    break;
                case 0x15: //long[]
                    len = (int)getNumber(data, pos, 2);
                    pos += 2 + len * 8;

                    break;
                case 0x16: //short[]
                    len = (int)getNumber(data, pos, 2);
                    pos += 2 + len * 2;

                    break;
                case 0x17: //UTF8 String[]
                case 0x18: //UTF16 String[]
                    len = (int)getNumber(data, pos, 2);

                    for(int j = 0; j < len; j++){
                        int sLen = (int)getNumber(data, pos, 2);
                        pos += 2 + sLen;
                    }

                    break;
            }
        }
    }

    /**
     * Public helper method to get one little-endn number from a byte array.
     * @param buf the byte array buffer
     * @param off the offset to read the number
     * @param len the num of bytes the number will take
     * Note: it is the client application's responsibility to make sure
     *       not to reach out of the array boundries.
     */
    public static long getNumber(byte[] buf, int off, int len){
        long longVal = 0;

        for(int i = 0; i < len; i++){
            longVal <<= 8;
            longVal |= buf[off + i] & 0xff;
        }

        return longVal;
    }

    /**
     * Public helper method to save one little-endn number to a byte array.
     * @param num the number to saved
     * @param buf the byte array buffer
     * @param off the offset to save the number
     * @param len the num of bytes the number will take
     * Note: it is the client application's responsibility to make sure
     *       not to reach out of the array boundries.
     */
    public static void setNumber(int num, byte[] buf, int off, int len){
        for(int i = len - 1; i >= 0; i--){
            buf[off + i] = (byte)(num & 0xff);
            num >>= 8;
        }
    }

    /**
     * 立即释放内存
     */
    public void release(){
        data = null;

        try{
            if(segOut != null){
                segOut.close();
            }
        }catch(Exception ex){
        }

        segCash = null;
        segOut = null;
    }

    public boolean equals(Object obj){
        if(obj == null || !(obj instanceof UWAPSegment)){
            return false;
        }

        UWAPSegment s = (UWAPSegment)obj;

        if(s.asyncSign == asyncSign){
            return true;
        }else{
            return false;
        }
    }
}
