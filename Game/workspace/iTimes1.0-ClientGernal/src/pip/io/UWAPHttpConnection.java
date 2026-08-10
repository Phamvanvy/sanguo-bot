package pip.io;


import java.io.*;
import java.util.*;

import javax.microedition.io.*;

import pip.ASyncRequestThread;
import pip.GameState;
import pip.World;


public class UWAPHttpConnection implements Runnable, UWAPConnection{
    private boolean cut;
    
    /**
     * UWAP package header
     * 协议版本	byte[5]	UWAP协议版本。当前固定取值为“UWAP1”。
     * 保留	byte[4]	为不同通讯实现扩展
     * 包序列号	四字节整数（高字节在前）	包的顺序编号。一对请求与响应应该有相同的包序列号。
     * 包长度	四字节整数（高字节在前）	整个UWAP包的长度（包括头信息和数据段，但不包括校验信息）。
     * 数据段数量	双字节整数（高字节在前）	UWAP包中包含的数据段的数量。
     */
    protected final byte[] version = {
                    'U', 'W', 'A', 'P', '1'
    }; //版本

    int id = -1; // uwap session id
    public String url = null;

    private boolean closed = false; // 是否已关闭
    private boolean closeSignalSent = false;
    private static int serial = 1; // 当前包序列号
    private Vector _segments = new Vector(); // 等待发送的队列
    private byte checkSum = 0; // 存放临时CRC计算结果
    private long lastSendTime = System.currentTimeMillis();

    public Hashtable segmentsDoingQueue = new Hashtable();
    public static final Integer severSegmentSerial = new Integer(-1);

    public UWAPHttpConnection(String url) throws IOException{
        this.url = url;
        segmentsDoingQueue.clear();
        
        HttpConnection conn = UWAPSegment.getConnection(url, UWAPSegment.useProxyGlobal);
        conn.setRequestMethod(HttpConnection.GET);

        int code = conn.getResponseCode();

        if(code != 200){
            throw new IOException();
        }
    }

    public void start(){
        new Thread(this).start();
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
        0xf87c000, 0, 0x83ec0cc0, 0xffff9e7d, 0xc488
    };

	public static boolean segmentHasResponse(UWAPSegment segment) {
		int tt = segment.type & 0xFF;
		int tt1 = tt / 32;
		int tt2 = tt % 32;
		if (tt1 >= SEGMENT_RESPONSE_SETTING.length) {
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
        //#mdebug debug
        if (segment.type != 90 && segment.type != 12)
        System.out.println("schedule send: " + segment.type);
        //#enddebug

        return serial;
    }

    public int writeSegment(UWAPSegment segment){
        return writeSegment(segment, true);
    }

    public void close(){
        closed = true;
    	UWAPSegment segment = new UWAPSegment((byte)91);
    	writeSegment(segment);
    }

    private void check(short shortVal){
        checkSum ^= (byte)(shortVal >> 8);
        checkSum ^= (byte)shortVal;
    }

    private void check(int i){
        checkSum ^= (byte)(i >> 24);
        checkSum ^= (byte)(i >> 16);
        checkSum ^= (byte)(i >> 8);
        checkSum ^= (byte)i;
    }

    private void check(byte[] byteArray){
        if(byteArray != null){
            for(int i = 0; i < byteArray.length; i++){
                checkSum ^= byteArray[i];
            }
        }
    }

    private void writeSegment(UWAPSegment seg, DataOutputStream out) throws Exception{
        // 计算长度
        seg.flush();
        
        //#if Revision == QQ
        //# int qqLength = 0;
        //# qqLength += 1; //STX值为 0x2 的一个字节
        //# qqLength += 2; //Version协议版本号,2个字节，网络序（暂定10）
        //# qqLength += 4; //Len整个包的长度
        //# qqLength += 1; //Cmd命令号；1个字节  0x03
        //# qqLength += 4; //Seq序列号；4个字节整数，网络序
        //# qqLength += 1; //ServerID“服务号”，1个字节    0
        //# qqLength += 8; //Reserved保留字段，8个字节
        //# qqLength += 19 + seg.data.length + 1;//qq potocal Body变长
        //# qqLength += 1; //ETX值为 0x3 的一个字节
        //# out.writeByte(0x02);
        //# out.writeShort(10);
        //# out.writeInt(qqLength);
        //# out.writeByte(0x03);
        //# out.writeInt(seg.serial);
        //# out.writeByte(1);
        //# out.write(new byte[8]);
        //#endif
        
        int length = 19;
        length += seg.data.length;
        checkSum = 0;
        out.write(version);
        check(version);
        out.writeInt(id);
        check(id);
        out.writeInt(seg.serial);
        check(serial);
        out.writeInt(length);
        check(length);
        out.writeShort(1);
        check((short)1);
        out.write(seg.data);
        check(seg.data);
        out.write(checkSum);
        
        //#if Revision == QQ
        //# out.writeByte(0x03); //ETX值为 0x3 的一个字节
        //#endif
        
        //#mdebug debug
        if (seg.type != 90 && seg.type != 12)
        	System.out.println("send: " + seg.type);
        //#enddebug
        
        if (seg.type == 91) {
          closeSignalSent = true;
        }
    }

    private UWAPSegment[] readSegment(DataInputStream in) throws Exception{
        in.skipBytes(5); //version
        int i = in.readInt(); // id

        if(id == -1){
            id = i;
        }

        int serial = in.readInt();
        in.skip(4); // length
        int segmentNum = in.readShort();
        UWAPSegment[] r = new UWAPSegment[segmentNum];

        for(i = 0; i < segmentNum; i++){
            r[i] = new UWAPSegment(in);
            r[i].serial = serial;
        }

        in.skip(1); // CRC

        return r;
    }

    /**
     * 连接主循环。这个循环不断地生成HTTP请求。同时只能有一个请求。两个请求之间的间隔至少
     * 是12秒。
     */
    public void run(){
        while(true){
            HttpConnection conn = null;
            DataInputStream in = null;
            DataOutputStream out = null;
            boolean connectionCreated = false;

            try{
                // 每12秒发送一次
                Thread.sleep(500);

                if (_segments.size() == 0 && System.currentTimeMillis() - lastSendTime < 3000){
                    continue;
                }

                lastSendTime = System.currentTimeMillis();

                connectionCreated = false;
                
                // 1. 连接
                conn = UWAPSegment.getConnection(url, UWAPSegment.useProxyGlobal);
                conn.setRequestMethod(HttpConnection.POST);
                
                connectionCreated = true;

                // 2. 发送请求
                out = new DataOutputStream(conn.openOutputStream());

                // 取pending的请求数据
                UWAPSegment[] requests;

                synchronized(_segments){
                    if(_segments.size() == 0){
                        UWAPSegment seg = new UWAPSegment((byte)90);
                        seg.writeInt((int)0);
                        writeSegment(seg);
                    }

                    requests = new UWAPSegment[_segments.size()];
                    _segments.copyInto(requests);
                    _segments.setSize(0);
                }
                
                for(int i = 0; i < requests.length; i++){
                    writeSegment(requests[i], out);
                }

                out.close();
                out = null;

                // 3. 获取返回
                int code = conn.getResponseCode();

                if(code != 200){
                    throw new IOException();
                }

                in = new DataInputStream(conn.openInputStream());

                while(true){ // 循环读取直到结束
                    try{
                        UWAPSegment[] responses = readSegment(in);

                        for(int i = 0; i < responses.length; i++){
                            try{
                            	if (responses[i].type == (byte)91) {
                                    GameState.closeConnection();
                            		tryReconnect();
                            		return;
                            	}
                                processSegment(responses[i]);
                            }catch(Exception e){
                            }
                        }
                    }catch(Exception e){
                        break;
                    }
                }
            }catch(Throwable e){
                //#debug debug
                e.printStackTrace();
                if (e instanceof IOException) {
	                try{
	                    if (closed == false){
	                        GameState.closeConnection();
	                        tryReconnect();
	                        return;
	                    }
	                }catch (Exception e1){
	                    //#debug
	                    e1.printStackTrace();
	                }
                }
            }finally{
                try{
                    if(out != null){
                        out.close();
                    }

                    if(in != null){
                        in.close();
                    }

                    if(conn != null){
                        conn.close();
                    }
                    
                    if (closed && (!connectionCreated || closeSignalSent)){
                        return;
                    }
                }catch(Throwable ex){
                }
            }
        }
    }

    public void processSegment(UWAPSegment segment) throws IOException{
        //#mdebug
    	if (segment.type != 90 && segment.type != 92 && segment.type != 13)
    		System.out.println("receive: " + segment.type);
    	//#enddebug
        if(processSegmentsDoingQueue(segment)){
            GameState.addSegment(segment);
        }else{
            //#debug
            System.out.println("丢弃 : " + segment.type + " , " + segment.serial);
        }
    }

    public void tryReconnect(){
        if(GameState.gameIsOk){
            GameState state = new GameState(GameState.STATE_RELOGIN);

            World.setGameState(state);
        }else{
            GameState.exitToGameMenu("与服务器失去联系", false);
        }
    }
    
    public void cut(boolean cut){
        this.cut = cut;
    }
}
