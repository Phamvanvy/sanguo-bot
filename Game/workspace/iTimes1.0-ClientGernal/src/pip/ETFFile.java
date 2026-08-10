package pip;


import java.io.*;


/**
 * ETF文件类。为了使此类能够用于J2ME客户端，这个类把成员变量都设计成了公有的，使用时要
 * 小心。
 */
public class ETFFile{
    /** 全局线索 */
    public static final int THREAD_GLOBAL = 0;
    /** 场景线索 */
    public static final int THREAD_MAP = 1;

    /** 头信息：语言版本号。固定为0。 */
    public byte languageVersion = 0;
    /** 头信息：任务版本号。 */
    public short fileVersion;
    /** 头信息：任务ID。 */
    public short taskID;
    /** 头信息：文件修改时间。 */
    public int modifyTime;
    /** 头信息：状态机内存大小。 */
    public short stateMemSize;
    /** 头信息：状态机字符串数量。*/
    public short stateStrCount;
    /** 头信息：任务属性。 */
    public short taskAttr;
    /** 头信息：任务名称。 */
    public String taskName;
    /** 头信息：任务描述。 */
    public String taskDesc = "";

    /** 字符串表。 */
    public String[] stringTable;

    /** 线索属性。每个线索用32位表示，高8位为线索ID，接着8位是线索类型，最低16位是场景ID */
    public int[] threadAttr;
    /** 线索代码。每个线索的代码是一个byte[]对象。 */
    public Object[] threadCode;
    /**
     * 线索代码分析结果。每个对象是一个byte[]数组，对应于线索的代码。不过这里面保存的是每
     * 个字节码在指令中的相对位置。
     */
    public Object[] threadCodeAttr;

    public ETFFile(){
    }

    /**
     * 从流中读取一个ETF文件。
     */
    public static ETFFile load(InputStream is) throws IOException{
        return load(new DataInputStream(is));
    }

    /**
     * 从流中读取一个ETF文件。
     */
    public static ETFFile load(DataInputStream is) throws IOException{
        ETFFile etf = new ETFFile();

        // 读取头信息
        int head = is.readInt();
        if(head != 0x45544600){ // ETF
            throw new IOException("Invalid ETF file!");
        }
        etf.fileVersion = is.readShort();
        etf.taskID = is.readShort();
        etf.modifyTime = is.readInt();
        etf.stateMemSize = is.readShort();
        etf.stateStrCount = is.readShort();
        etf.taskAttr = is.readShort();
        etf.taskName = readUTF16(is);
        etf.taskDesc = readUTF16(is);
        is.readShort(); // 跳过文件长度

        // 读取字符串表
        short tk = is.readShort();

        if(tk == 0x5354){ // ST
            short count = is.readShort();

            if(count <= 0){
                throw new IOException("Invalid ETF file!");
            }

            etf.stringTable = new String[count];
            short len = is.readShort();
            count = 0;

            while(len > 0){
                String s = readUTF16(is);

                if(s.length() < 128){
                    len -= 1 + 2 * s.length();
                }else{
                    len -= 2 + 2 * s.length();
                }

                etf.stringTable[count++] = s;
            }

            if(len != 0 || count != etf.stringTable.length){
                throw new IOException("Invalid ETF file!");
            }

            tk = is.readShort(); // 读取下一个段标识
        }else{
            etf.stringTable = new String[0];
        }

        // 读取线索
        if(tk == 0x4354){ // CT
            short count = is.readShort();

            if(count <= 0){
                throw new IOException("Invalid ETF file!");
            }

            etf.threadAttr = new int[count];
            etf.threadCode = new Object[count];
            short len = is.readShort();
            count = 0;

            while(len > 0){
                etf.threadAttr[count] = is.readInt();
                len -= 4;
                short clen = is.readShort();

                if(clen < 0){
                    throw new IOException("Invalid ETF file!");
                }

                len -= 2 + clen;
                byte[] code = new byte[clen];
                is.read(code);
                etf.threadCode[count++] = code;
            }

            if(len != 0 || count != etf.threadAttr.length){
                throw new IOException("Invalid ETF file!");
            }
        }else{
            throw new IOException("Invalid ETF file!");
        }

        return etf;
    }

    /**
     * 从流中读取一个UTF-16BE字符串。
     */
    public static String readUTF16(DataInputStream is) throws IOException{
        int slen = (int)is.readByte();

        if((slen & 0x80) != 0){ // 字符串长度大于128
            int slen2 = (int)is.readByte();
            slen = ((slen & 0x7F) << 8) + (slen2 & 0xFF);
        }

        char[] buf = new char[slen];

        for(int i = 0; i < slen; i++){
            buf[i] = is.readChar();
        }

        return new String(buf);
    }
}
