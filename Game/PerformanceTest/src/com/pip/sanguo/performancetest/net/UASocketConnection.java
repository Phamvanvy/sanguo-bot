package com.pip.sanguo.performancetest.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Vector;

import com.pip.sanguo.performancetest.client.SanguoClient;

public class UASocketConnection implements Runnable{
    private boolean cut;
    protected DataInputStream _in;
    protected DataOutputStream _out;
    protected Socket _socket;

    protected UASocketConnection _reader;
    protected UASocketConnection _writer;

    protected boolean _isConnected;
    protected Vector _segments = new Vector();

    private Thread _connectThread = null;

    public long lastWriteTime = System.currentTimeMillis();
    public long lastReadTime = System.currentTimeMillis();

    protected final byte[] HEAD = {
                    'U', 'A'
    };

    public static final int TIME_SEND_TIMEOUT = 6000;

    private UASocketConnection parent;
    private SanguoClient owner;
    private byte runType; // 0 - Reader, 1 - Writer

    public UASocketConnection(String url, SanguoClient owner) throws IOException{
        this.owner = owner;
        
        int l = url.indexOf("#");
        long serverID = 0;

        if(l != -1){
            String sid = url.substring(l + 1);
            url = url.substring(0, l);
            serverID = Long.parseLong(sid, 16);
        }

        if(url.indexOf("socket://") >= 0){
            url = url.substring("socket://".length());
        }
        
        String host = url.substring(0, url.indexOf(':'));
        int port = Integer.parseInt(url.substring(url.indexOf(':') + 1));
        _socket = new Socket(host, port);

        _in = new DataInputStream(_socket.getInputStream());
        _out = new DataOutputStream(_socket.getOutputStream());

        _isConnected = true;
        _segments.removeAllElements();

        if(serverID != 0){
            _out.writeInt((int) (serverID >> 16));
            _out.writeShort((short) serverID);
            _out.flush();
        }
    }

    public UASocketConnection(UASocketConnection par, byte rt){
        parent = par;
        runType = rt;
    }

    public void run(){
        if(parent == null){
            _reader = new UASocketConnection(this, (byte) 0);
            new Thread(_reader).start();

            _writer = new UASocketConnection(this, (byte) 1);
            new Thread(_writer).start();
        }else if(runType == (byte) 0){
            runReader();
        }else{
            runWriter();
        }
    }

    public void start(){
        _connectThread = new Thread(this);
        _connectThread.start();
    }

    public void writeSegment(UASegment segment){
        _segments.addElement(segment);
    }

    protected void write(UASegment segment) throws IOException{
        segment.flush();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.write(HEAD);
        dos.writeInt(segment.data.length + HEAD.length + 4);
        dos.write(segment.data);
        dos.flush();
        byte[] _sendBuffer = bos.toByteArray();
        dos.close();
        
        _out.write(_sendBuffer);
        _out.flush();

        owner.addSendBytes(_sendBuffer.length);
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
    	if (readFull(_in, head) != head.length){
            return null;
        }
    	if (head[0] != 'U') {
    		throw new IOException("Wrong protocol");
    	}
    	int len;
    	if (head[1] == 'A') {
    		len = _in.readInt() - 6;
    	} else if (head[1] == 'B') {
    		len = (_in.readShort() & 0xFFFF) - 4;
    	} else if (head[1] == 'C') {
    		len = (_in.readByte() & 0xFF) - 3;
    	} else {
    		throw new IOException("Wrong protocol");
    	}
    	
        byte buf[] = null;

        if(len > 0){
            buf = new byte[len];

            if(readFull(_in, buf) != len){
                throw new IOException("Not enough input");
            }
        }

        UASegment data = new UASegment(buf);

        owner.addRecvBytes(head.length + buf.length);
        
        return data;
    }

    public void processSegment(UASegment segment) throws IOException{
        owner.processSegment(segment);
    }

    public void testClose(){
        _in = null;
    }

    public void close(){
        _isConnected = false;

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

        if(_socket != null){
            try{
                _socket.close();
            }catch(IOException ex4){
            }
        }

        _in = null;
        _out = null;
        _socket = null;
    }

    public void runReader(){
        while(parent._isConnected){
            try{
                if(parent.cut){
                    throw new Exception("∂œÕ¯≤‚ ‘");
                }

                UASegment segment = parent.readSegment();

                if(segment == null){
                    continue;
                }

                parent.lastReadTime = System.currentTimeMillis();
                parent.processSegment(segment);
            }catch(Exception ex){
                ex.printStackTrace();

                try{
                    if(parent._isConnected){
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public void runWriter(){
        while(parent._isConnected){
            try{
                if(parent.cut){
                    throw new Exception("∂œÕ¯≤‚ ‘");
                }

                while(parent._segments.size() != 0){
                    UASegment segment = (UASegment)parent._segments.elementAt(0);
                    parent._segments.removeElementAt(0);

                    parent.write(segment);
                    parent.lastWriteTime = System.currentTimeMillis();
                }
            }catch(Exception ex){
                ex.printStackTrace();

                try{
                    if(parent._isConnected){
                    }
                }catch(Exception e){
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

    public void cut(boolean cut){
        this.cut = cut;
    }
}