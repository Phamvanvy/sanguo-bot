package pip.io;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import pip.ASyncRequestThread;
import pip.GameState;
import pip.World;


public class UWAPSocketConnection implements Runnable, UWAPConnection{
    private boolean cut;
    
    protected DataInputStream _in;
    protected DataOutputStream _out;

    protected StreamConnection _connection;

    //#if Connection == Socket
    protected UWAPSocketConnection _reader;
    //#endif
    protected UWAPSocketConnection _writer;

    protected boolean _isConnected;

    protected Vector _segments;

    private int _checkSum;
    private int _id;

    private static int serial = 1;

    private Thread _connectThread = null;

    public long lastWriteTime = System.currentTimeMillis();
    public long lastReadTime = System.currentTimeMillis();

    public Hashtable segmentsDoingQueue = new Hashtable();
    public static final Integer severSegmentSerial = new Integer(-1);

    protected final byte[] VERSION = {
                    'U', 'W', 'A', 'P', '1'
    };

    // 下面两个变量用于把Reader和Writer都合并到这一个类里面来
    private UWAPSocketConnection parent;
    private byte runType; // 0 - Reader, 1 - Writer
    
    private boolean qqHeadNeedSend = true;
    private byte qqServerId = 1;

    public UWAPSocketConnection(String url) throws IOException{
        int l = url.indexOf("#");
        long serverID = 0;
        
        if(l != -1){
            String sid = url.substring(l + 1);
            url = url.substring(0, l);
            serverID = Long.parseLong(sid, 16);
        }
        
        _connection = (StreamConnection)Connector.open(url, Connector.READ_WRITE, true);

        //#if Directory == MT-V300
        //# _in = new DataInputStream(_connection.openInputStream());
        //# _out = new DataOutputStream(_connection.openOutputStream());
        //#else
        _in = _connection.openDataInputStream();
        _out = _connection.openDataOutputStream();
        //#endif
        _isConnected = true;
        _segments = new Vector();
        
        segmentsDoingQueue.clear();
        
        //#if Revision != QQ
        if(serverID != 0){
            _out.writeInt((int)(serverID >> 16));
            _out.writeShort((short)serverID);
            _out.flush();
        }
        //#else
        //# if(serverID != 0){
        //#    qqServerId = (byte)(serverID >> 48);
        //# }
        //#endif
    }

    public UWAPSocketConnection(UWAPSocketConnection par, byte rt){
        parent = par;
        runType = rt;
    }

    public void run(){
        if(parent == null){
            //#if Connection == Socket
            _reader = new UWAPSocketConnection(this, (byte)0);
            new Thread(_reader).start();
            //#endif

            _writer = new UWAPSocketConnection(this, (byte)1);
            new Thread(_writer).start();
        }
        //#if Connection == Socket
        else if(runType == (byte)0){
            runReader();
        }
        //#endif
        else{
            runWriter();
        }
    }

    public void start(){
        _connectThread = new Thread(this);
        _connectThread.start();
    }

    public void cycleSegmentsDoingQueue(){
        if(segmentsDoingQueue.size() > 0){
            Vector tmpArray = new Vector();
            Enumeration emu = segmentsDoingQueue.keys();
            int currTime = ASyncRequestThread.getTimeStamp();

            while(emu.hasMoreElements()){
                Integer serialSend = (Integer)emu.nextElement();
                int[] segmentData = (int[])segmentsDoingQueue.get(serialSend);

                if(currTime - segmentData[1] > ASyncRequestThread.TIME_SEND_TIMEOUT){
                    tmpArray.addElement(serialSend);
                }
            }

            for(int i = 0; i < tmpArray.size(); i++){
                Integer serialSend = (Integer)tmpArray.elementAt(i);
                int[] segmentData = (int[])segmentsDoingQueue.get(serialSend);
                segmentsDoingQueue.remove(serialSend);

                sendSegmentTimeOut(segmentData[0], serialSend.intValue());
            }
        }
    }

    public static void sendSegmentTimeOut(int type, int serialNo){
        try{
            UWAPSegment errorSegment = new UWAPSegment(GameState.CONN_ERROR);

            errorSegment.writeByte((byte)0);
            errorSegment.writeString("服务器超时: " + type + " , " + serialNo);
            errorSegment.flush();
            errorSegment.serial = serialNo;

            GameState.addSegment(errorSegment);
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
    }

    public boolean processSegmentsDoingQueue(UWAPSegment segment){
        boolean flag = true;

        Integer serialRecv = new Integer(segment.serial);

        if(!segmentHasResponse(segment)){
            flag = true;
        }else{
            if(serialRecv.intValue() != severSegmentSerial.intValue()){
                if(segmentsDoingQueue.containsKey(serialRecv)){
                    segmentsDoingQueue.remove(serialRecv);
                    flag = true;
                }else{
                    flag = false;
                }
            }else{
                flag = true;
            }
        }

        return flag;
    }

    public static final int[] SEGMENT_RESPONSE_SETTING = new int[]{
                    0xf87c000, 0, 0x83e80cc0, 0xffff9e7d, 0xc488
    };

    public static boolean segmentHasResponse(UWAPSegment segment){
        int tt = segment.type & 0xFF;
        int tt1 = tt / 32;
        int tt2 = tt % 32;
        if(tt1 >= SEGMENT_RESPONSE_SETTING.length){
            return false;
        }
        return ((SEGMENT_RESPONSE_SETTING[tt1] & (1 << tt2)) != 0);
    }

    public void addSegmentsDoingQueue(UWAPSegment segment){
        if(segmentHasResponse(segment)){
            Integer serialSend = new Integer(segment.serial);
            int[] segmentData = new int[]{
                            segment.type & 0xFF, ASyncRequestThread.getTimeStamp()
            };

            segmentsDoingQueue.put(serialSend, segmentData);
        }
    }

    public int writeSegment(UWAPSegment segment, boolean createSerial){
        if(createSerial){
            synchronized(this){
                serial++;

                if(serial < 0){
                    serial = 0;
                }

                segment.serial = serial;
            }
        }

        addSegmentsDoingQueue(segment);

        _segments.addElement(segment);

        return serial;
    }

    public int writeSegment(UWAPSegment segment){
        return writeSegment(segment, true);
    }

    void check(short shortVal){
        _checkSum ^= (byte)(shortVal >> 8);
        _checkSum ^= (byte)shortVal;
    }

    void check(int i){
        _checkSum ^= (byte)(i >> 24);
        _checkSum ^= (byte)(i >> 16);
        _checkSum ^= (byte)(i >> 8);
        _checkSum ^= (byte)i;
    }

    void check(byte[] byteArray){
        if(byteArray != null){
            for(int i = 0; i < byteArray.length; i++){
                _checkSum ^= byteArray[i];
            }
        }
    }

    protected void write(UWAPSegment segment) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        
        if(qqHeadNeedSend){
            //#if Revision == QQ
            //# int qqLength = 0;
            //# qqLength += 1; //STX值为 0x2 的一个字节
            //# qqLength += 2; //Version协议版本号,2个字节，网络序（暂定10）
            //# qqLength += 4; //Len整个包的长度
            //# qqLength += 1; //Cmd命令号；1个字节  0x03
            //# qqLength += 4; //Seq序列号；4个字节整数，网络序
            //# qqLength += 1; //ServerID“服务号”，1个字节    0
            //# qqLength += 8; //Reserved保留字段，8个字节
            //# qqLength += 19 + segment.data.length + 1;//qq potocal Body变长
            //# qqLength += 1; //ETX值为 0x3 的一个字节
            //# dos.writeByte(0x02);
            //# dos.writeShort(10);
            //# dos.writeInt(qqLength);
            //# dos.writeByte(0x03);
            //# dos.writeInt(segment.serial);
            //# dos.writeByte(qqServerId);
            //# dos.write(new byte[8]);
            //#endif
        }
        
        int length = 19;
        _checkSum = 0;

        dos.write(VERSION);
        check(VERSION);
        dos.writeInt(_id);
        check(_id);
        dos.writeInt(segment.serial);
        check(segment.serial);
        length += segment.data.length;
        dos.writeInt(length);
        check(length);
        dos.writeShort(1);
        check((short)1);
        dos.write(segment.data);
        check(segment.data);
        dos.write(_checkSum);
        
        if(qqHeadNeedSend){
            //#if Revision == QQ
            //# dos.writeByte(0x03); //ETX值为 0x3 的一个字节
            //#endif
        }
        
        qqHeadNeedSend = false;
        
        dos.flush();
        _out.write(bos.toByteArray());
        _out.flush();
        
        dos.close();
    }

    protected static int readFull(InputStream in, byte[] buf) throws IOException{
        int len = 0;

        try{
            while(len < buf.length){
                int l = in.read(buf, len, buf.length - len);

                if(l < 0){
                    throw new IOException("Wrong protocol");
                }

                len += l;
            }
        }finally{
            for(int i = len; i < buf.length; i++){
                buf[i] = 0;
            }
        }

        return len;
    }

    public static long getNumber(byte[] buf, int off, int len){
        long l = 0;

        for(int i = 0; i < len; i++){
            l <<= 8;
            l += ((int)buf[off + i]) & 0xff;
        }

        return l;
    }

    public UWAPSegment[] readSegments() throws IOException{
        int chkSum = 0;
        byte[] head = new byte[19];

        byte[] hd = {
                        'U', 'W', 'A', 'P', '1'
        };

        //#if Directory == MT-V300
        //# while(true){
        //# _in.readFully(head, 0, 1);
        //# if(head[0] == 'U'){
        //#     break;
        //# }
        //# }
        //# _in.readFully(head, 1, head.length - 1);
        //#else
        if(readFull(_in, head) != head.length){
            return null;
        }
        //#endif

        for(int i = 0; i < hd.length; i++){
            if(hd[i] != head[i]){
                throw new IOException("Wrong protocol");
            }
        }

        int len = (int)getNumber(head, 13, 4);
        int num = (int)getNumber(head, 17, 2);
        int serial = (int)getNumber(head, 9, 4);
        len -= head.length;
        byte buf[] = null;

        if(len > 0){
            buf = new byte[len];

            if(readFull(_in, buf) != len){
                throw new IOException("Not enough input");
            }

            for(int i = 0; i < buf.length; i++){
                chkSum ^= buf[i];
            }
        }else if(num != 0){
        }

        for(int i = 0; i < head.length; i++){
            chkSum ^= head[i];
        }

        chkSum &= 0xff;

        _in.readByte(); //CRC byte

        //        if(chkSum != _in.read()){
        //            throw new IOException("CheckSum error");
        //        }else{
        UWAPSegment data[] = new UWAPSegment[num];
        int off = 0;

        for(int i = 0; i < num; i++){
            int dataLen = (int)getNumber(buf, off + 1, 4);
            data[i] = new UWAPSegment(buf, off, dataLen);
            data[i].serial = serial;
            off += dataLen & 0x00FFFFFF;
        }

        return data;
        //        }
    }

    public void processSegment(UWAPSegment segment) throws IOException{
        if(processSegmentsDoingQueue(segment)){
            GameState.addSegment(segment);
            //#debug
        }else{
            //#debug
            System.out.println("丢弃 : " + segment.type + " , " + segment.serial);
        }
    }

    public void tryReconnect(){
        if(GameState.logouting){
            return;
        }
        
        if(GameState.gameIsOk){
            GameState state = new GameState(GameState.STATE_RELOGIN);

            World.setGameState(state);
        }else{
            GameState.exitToGameMenu("与服务器失去联系", false);
        }
    }

    public void close(){
        _isConnected = false;
        
        segmentsDoingQueue.clear();

        /*try{
            if(_connectThread != null){
                _connectThread.join();
            }
        }catch(InterruptedException ex5){
        }*/

        if(_in != null){
            try{
                _in.close();
            }catch(IOException ex2){
            }
        }

        if(_out != null){
            try{
                _out.close();
            }catch(IOException ex3){
            }
        }

        if(_connection != null){
            try{
                _connection.close();
            }catch(IOException ex4){
            }
        }

        _in = null;
        _out = null;
        _connection = null;
    }

    //#if Connection == Socket
    public void runReader(){
        while(parent._isConnected){
            try{
                if(parent.cut){
                    throw new Exception("断网测试");
                }
                
                UWAPSegment[] segments = parent.readSegments();

                if(segments == null){
                    continue;
                }

                parent.lastReadTime = System.currentTimeMillis();

                for(int i = 0, size = segments.length; i < size; i++){
                    ///#debug
                    System.out.println("recv: " + (segments[i].type & 0xFF) + " , " + segments[i].serial);
                    parent.processSegment(segments[i]);
                }
            }catch(Exception ex){
                //#debug
                ex.printStackTrace();

                try{
                    if(parent._isConnected){
                        GameState.closeConnection();
                        parent.tryReconnect();
                    }
                }catch(Exception e){
                    //#debug
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    //#endif

    //#if Connection == Socket
    public void runWriter(){
        while(parent._isConnected){
            try{
                if(parent.cut){
                    throw new Exception("断网测试");
                }
                if(parent._segments.size() != 0){
                    UWAPSegment segment = (UWAPSegment)parent._segments.elementAt(0);
                    parent._segments.removeElementAt(0);

                    parent.write(segment);
                    ///#debug
                    System.out.println("send: " + (segment.type & 0xFF) + " , " + segment.serial);
                    parent.lastWriteTime = System.currentTimeMillis();
                }
            }catch(Exception ex){
                //#debug
                ex.printStackTrace();
                
                try{
                    if(parent._isConnected){
                        GameState.closeConnection();
                        parent.tryReconnect();
                    }
                }catch(Exception e){
                    //#debug
                    e.printStackTrace();
                }

                break;
            }finally{
                try{
                    Thread.sleep(50);
                }catch(Exception e){
                }
            }
        }
    }
    //#else
    //# public void runWriter(){
    //#     while(parent._isConnected){
    //#         try{
    //#             while(parent._segments.size() != 0){
    //#                 UWAPSegment segment = (UWAPSegment)parent._segments.elementAt(0);
    //#                 parent._segments.removeElementAt(0);
    //#                 parent.write(segment);
    //#                 System.out.println("send: " + segment.type);
    //#                 parent.lastWriteTime = System.currentTimeMillis();
    //#             }
    //#             UWAPSegment[] segments = null;
    //#             segments = parent.readSegments();
    //#             if(segments == null){
    //#                 continue;
    //#             }
    //#             parent.lastReadTime = System.currentTimeMillis();
    //#             for(int i = 0, size = segments.length; i < size; i++){
    //#                 //#debug debug
    //#                 System.out.println("type:" + segments[i].type);
    //#                 parent.processSegment(segments[i]);
    //#             }
    //#         }catch(Exception ex1){
    //#             //if(_isConnected){
    //#if Directory == MT-V300
    //# if (ex1.getMessage().startsWith("Wrong")) {
    //#     continue;
    //# }
    //#endif
    //#                 GameState.closeConnection();
    //#                 parent.tryReconnect();
    //#             //}
    //#         }finally{
    //#             try{
    //#                 Thread.sleep(50);
    //#             }catch(Exception e){
    //#             }
    //#         }
    //#     }
    //# }
    //#endif
    
    public void cut(boolean cut){
        this.cut = cut;
    }
}