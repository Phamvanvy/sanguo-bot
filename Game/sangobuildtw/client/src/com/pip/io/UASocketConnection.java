package com.pip.io;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.ui.VM;
import com.pip.ui.VMGame;


public class UASocketConnection implements Runnable{
    private boolean cut;
    protected DataInputStream _in;
    protected DataOutputStream _out;
    protected StreamConnection _connection;

    //#if ConnectionThread == dual
    protected UASocketConnection _reader;
    //#endif
    protected UASocketConnection _writer;

    protected boolean _isConnected;
    protected static Vector _segments = new Vector();

    private Thread _connectThread = null;

    public long lastWriteTime = System.currentTimeMillis();
    public long lastReadTime = System.currentTimeMillis();
    
    public static boolean offlineMode = false;

    protected final byte[] HEAD = {
                    'U', 'A'
    };
    
    protected final byte[] HEAD_RECV = {
                    'U'
    };

    //#ifdef buildtest
    public static final int TIME_SEND_TIMEOUT = 6000;
    //#else
    //# public static final int TIME_SEND_TIMEOUT = 60000;
    //#endif
    
    private UASocketConnection parent;
    private byte runType; // 0 - Reader, 1 - Writer
    
    //#if Revision == CMCC
    // 上次发送CMCC头信息的时间
    private boolean isUAProxy = false;		// 是否使用singlesocket协议
    private long lastSendCMCCPluseTime;
    //#endif
    
    //TODO delete
    /*
    public static final Hashtable sendStat = new Hashtable();
    public static final Hashtable recvStat = new Hashtable();
    */

    public UASocketConnection(String url) throws IOException{
        int l = url.indexOf("#");
        long serverID = 0;
        
        if(l != -1){
            String sid = url.substring(l + 1);
            url = url.substring(0, l);
            serverID = Long.parseLong(sid, 16);
        }
        
        _connection = (StreamConnection)Connector.open(url, Connector.READ_WRITE, true);

        //#if StreamMode == strict
        //# _in = new DataInputStream(_connection.openInputStream());
        //# _out = new DataOutputStream(_connection.openOutputStream());
        //#else
        _in = _connection.openDataInputStream();
        _out = _connection.openDataOutputStream();
        //#endif

        _isConnected = true;
        _segments.removeAllElements();
        
    	//#if Revision == CMCC
        // 卓望网关新协议需求，如果用户拥有卓望平台ID，则需要在连接建立时发送
        if (serverID != 0) {
        	isUAProxy = true;
        }
        trySendCMCCPluse(true);
    	//#endif

        if(serverID != 0){
            _out.writeInt((int)(serverID >> 16));
            _out.writeShort((short)serverID);
            _out.flush();
        }
    }
    
    //#if Revision == CMCC
    /*
     * 在输出流上尝试加入卓望网关要求的心跳包。连接开始时以及每60秒发送一次。
     */
    private void trySendCMCCPluse(boolean isFirst) {
    	if (!isUAProxy) {
    		return;
    	}
    	long now = System.currentTimeMillis();
    	if (!isFirst && now - lastSendCMCCPluseTime < 60000L) {
    		return;
    	}
    	lastSendCMCCPluseTime = now;
    	
    	// 如果没有取到卓望平台ID，则不发送
    	String cmccUserId = Tool.getGlobalString("cmccUserID");
    	if (cmccUserId.length() == 0) {
    		return;
    	}
    	
    	// 如果不是第一次发送，需要清空输出流并等待一会儿确保下一个发送的包是一个独立的TCP包
    	if (!isFirst) {
    		try {
    			_out.flush();
    			Thread.sleep(200);
    		} catch (Exception e) {
    		}
    	}
    	
    	// 拼接口包CMCCGAME_userId=xxxxxxxxxx，userid10位，不足的后面空格补齐
    	String s = cmccUserId;
    	if (s.length() > 10) {
    		s = s.substring(0, 10);
    	}
    	while (s.length() < 10) {
    		s += " ";
    	}
    	s = "CMCCGAME_userId=" + s;
    	try {
	    	_out.write(s.getBytes());
	    	_out.flush();
    	} catch (Exception e) {
    	}
    }
    //#endif

    public UASocketConnection(UASocketConnection par, byte rt){
        parent = par;
        runType = rt;
    }

    public void run(){
        if(parent == null){
            //#if ConnectionThread == dual
            _reader = new UASocketConnection(this, (byte)0);
            new Thread(_reader).start();
            //#endif

            _writer = new UASocketConnection(this, (byte)1);
            new Thread(_writer).start();
        }
        //#if ConnectionThread == dual
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

    public static void writeSegment(UASegment segment){
        _segments.addElement(segment);
    }

    protected void write(UASegment segment) throws IOException{
        segment.flush();

        if(offlineMode){
            return;
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.write(HEAD);
        dos.writeInt(segment.data.length + HEAD.length + 4);
        dos.write(segment.data);
        dos.flush();

        _out.write(bos.toByteArray());
        _out.flush();
        
        //TODO delete
        /*
        Integer sendKey = new Integer(segment.type);
        int[] sendSize = (int[])sendStat.get(sendKey);
        if(sendSize == null){
            sendSize = new int[]{
                            0, 0
            };
        }
        sendSize[0]++;
        sendSize[1] += bos.size();
        sendStat.put(sendKey, sendSize);*/

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
            l += (buf[off + i]) & 0xff;
        }

        return l;
    }

    public UASegment readSegment() throws IOException{
        byte[] head = new byte[2];

        //#if StreamMode == strict
        //# while(true){
        //# _in.readFully(head, 0, 1);
        //# if(head[0] == 'U'){
        //# break;
        //# }
        //# }
        //# _in.readFully(head, 1, head.length - 1);
        //#else
        if(readFull(_in, head) != head.length){
            return null;
        }
        //#endif

        for(int i = 0; i < HEAD_RECV.length; i++){
            if(HEAD[i] != head[i]){
                throw new IOException("Wrong protocol");
            }
        }
        
        byte[] lenInHead = null;
        
        switch(head[1]){
            case 'A':
                lenInHead = new byte[4];
                break;
            case 'B':
                lenInHead = new byte[2];
                break;
            case 'C':
                lenInHead = new byte[1];
                break;
            default:
                throw new IOException("Wrong protocol");
        }
        
        if(readFull(_in, lenInHead) != lenInHead.length){
            return null;
        }

        int len = 0;
        
        switch(head[1]){
            case 'A':
                len = (int)getNumber(lenInHead, 0, 4);
                break;
            case 'B':
                len = (int)(getNumber(lenInHead, 0, 2) & 0xFFFF);
                break;
            case 'C':
                len = (int)(getNumber(lenInHead, 0, 1) & 0xFF);
                break;
        }

        len -= head.length + lenInHead.length;
        byte buf[] = null;

        if(len > 0){
            buf = new byte[len];

            if(readFull(_in, buf) != len){
                throw new IOException("Not enough input");
            }
        }

        UASegment data = new UASegment(buf);
        
        //TODO delete
        /*
        Integer recvKey = new Integer(data.type);
        int[] recvSize = (int[])recvStat.get(recvKey);
        if(recvSize == null){
            recvSize = new int[]{
                            0, 0
            };
        }
        recvSize[0]++;
        recvSize[1] += buf.length + head.length;
        recvStat.put(recvKey, recvSize);*/

        return data;
    }

    public void processSegment(UASegment segment) throws IOException{
        Utilities.addSegment(segment);
    }

    public void tryReconnect(){
        // 试图重新登录
        Utilities.tryReconnect();
    }

    public void testClose(){
        _in = null;
    }

    public void close(){
        _isConnected = false;

        /*
         * try{ if(_connectThread != null){ _connectThread.join(); }
         * }catch(InterruptedException ex5){ }
         */

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

    //#if ConnectionThread == dual
    public void runReader(){
        while(parent._isConnected){
            try{
                if(parent.cut){
                    throw new Exception("断网测试");
                }

                UASegment segment = parent.readSegment();

                if(segment == null){
                    continue;
                }

                parent.lastReadTime = System.currentTimeMillis();

              //#ifdef buildtest
                System.out.println("recv: " + segment.type);
              //#endif
                
                parent.processSegment(segment);
            }catch(Exception ex){
            	//#ifdef buildtest
                ex.printStackTrace();
              //#endif

                try{
                    if(parent._isConnected){
                        Utilities.closeConnection();
                        parent.tryReconnect();
                        
                        VM vm = VMGame.getVMGameByVMKey(VMGame.gameWorldVMGameKey).getVM();
                        
                        synchronized(vm){
                            vm.callback(VMGame.CALLBACK_DIS_CONNECTED, null);
                        }
                    }
                }catch(Exception e){
                	//#ifdef buildtest
                    e.printStackTrace();
                  //#endif
                }

                break;
            }
        }
    }

    //#endif

    //#if ConnectionThread == dual
    public void runWriter(){
        while(parent._isConnected){
            try{
                if(parent.cut){
                    throw new Exception("断网测试");
                }
                //#if Revision == CMCC
                // 检查是否需要发送CMCC头信息
                parent.trySendCMCCPluse(false);
                //#endif
                
                while(UASocketConnection._segments.size() != 0){
                    UASegment segment = (UASegment)UASocketConnection._segments.elementAt(0);
                    UASocketConnection._segments.removeElementAt(0);

                    parent.write(segment);

                  //#ifdef buildtest
                    System.out.println("send: " + (segment.type & 0xFFFF));
                  //#endif
                    
                    parent.lastWriteTime = System.currentTimeMillis();
                }
            }catch(Exception ex){
            	//#ifdef buildtest
                ex.printStackTrace();
              //#endif
                
                try{
                    if(parent._isConnected){
                        Utilities.closeConnection();
                        parent.tryReconnect();
                        
                        VM vm = VMGame.getVMGameByVMKey(VMGame.gameWorldVMGameKey).getVM();
                        
                        synchronized(vm){
                            vm.callback(VMGame.CALLBACK_DIS_CONNECTED, null);
                        }
                    }
                }catch(Exception e){
                	//#ifdef buildtest
                    e.printStackTrace();
                  //#endif
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
    //# while(parent._isConnected){
    //# try{
    //#if Revision == CMCC
    //# // 检查是否需要发送CMCC头信息
    //#             parent.trySendCMCCPluse(false);
    //#endif
    //# while(parent._segments.size() != 0){
    //# UWAPSegment segment = (UWAPSegment)parent._segments.elementAt(0);
    //# parent._segments.removeElementAt(0);
    //# parent.write(segment);
    //# System.out.println("send: " + segment.type);
    //# parent.lastWriteTime = System.currentTimeMillis();
    //# }
    //# UWAPSegment[] segments = null;
    //# segments = parent.readSegments();
    //# if(segments == null){
    //# continue;
    //# }
    //# parent.lastReadTime = System.currentTimeMillis();
    //# for(int i = 0, size = segments.length; i < size; i++){
    //# //#ifdef buildtest
    //# System.out.println("type:" + segments[i].type);
    //# //#endif
    //# parent.processSegment(segments[i]);
    //# }
    //# }catch(Exception ex1){
    //# //if(_isConnected){
    //#if StreamMode == strict
    //# if (ex1.getMessage().startsWith("Wrong")) {
    //# continue;
    //# }
    //#endif
    //# Utilities.closeConnect();
    //# parent.tryReconnect();
    //# //}
    //# }finally{
    //# try{
    //# Thread.sleep(50);
    //# }catch(Exception e){
    //# }
    //# }
    //# }
    //# }
    //#endif

    public void cut(boolean cut){
        this.cut = cut;
    }
}