package com.pip.sanguo.performancetest.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.pip.sanguo.performancetest.client.iTimesClient;

public class UWAPSocketConnection implements Runnable{
    private boolean cut;

    protected DataInputStream _in;
    protected DataOutputStream _out;
    protected Socket _socket;

    protected UWAPSocketConnection _reader;
    protected UWAPSocketConnection _writer;

    protected boolean _isConnected;

    protected List<UWAPSegment> _segments = new ArrayList<UWAPSegment>();

    private int _checkSum;
    private int _id;

    private static int serial = 1;

    private Thread _connectThread = null;

    public long lastWriteTime = System.currentTimeMillis();
    public long lastReadTime = System.currentTimeMillis();

    protected final byte[] VERSION = {
                    'U', 'W', 'A', 'P', '1'
    };

    // 下面两个变量用于把Reader和Writer都合并到这一个类里面来
    private UWAPSocketConnection parent;
    private iTimesClient owner;
    private byte runType; // 0 - Reader, 1 - Writer

    public UWAPSocketConnection(String url, iTimesClient owner) throws IOException{
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
        _segments.clear();

        if(serverID != 0){
            _out.writeInt((int) (serverID >> 16));
            _out.writeShort((short) serverID);
            _out.flush();
        }
    }

    public UWAPSocketConnection(UWAPSocketConnection par, byte rt){
        parent = par;
        runType = rt;
    }

    public void run(){
        if(parent == null){
            _reader = new UWAPSocketConnection(this, (byte) 0);
            new Thread(_reader).start();

            _writer = new UWAPSocketConnection(this, (byte) 1);
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

        _segments.add(segment);

        return serial;
    }

    public int writeSegment(UWAPSegment segment){
        return writeSegment(segment, true);
    }

    void check(short shortVal){
        _checkSum ^= (byte) (shortVal >> 8);
        _checkSum ^= (byte) shortVal;
    }

    void check(int i){
        _checkSum ^= (byte) (i >> 24);
        _checkSum ^= (byte) (i >> 16);
        _checkSum ^= (byte) (i >> 8);
        _checkSum ^= (byte) i;
    }

    void check(byte[] byteArray){
        if(byteArray != null){
            for(int i = 0; i < byteArray.length; i++){
                _checkSum ^= byteArray[i];
            }
        }
    }

    protected void write(UWAPSegment segment) throws IOException{
        segment.flush();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

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
        check((short) 1);
        dos.write(segment.data);
        check(segment.data);
        dos.write(_checkSum);

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
            l += ((int) buf[off + i]) & 0xff;
        }

        return l;
    }

    public UWAPSegment[] readSegments() throws IOException{
        int chkSum = 0;
        byte[] head = new byte[19];

        byte[] hd = {
                        'U', 'W', 'A', 'P', '1'
        };

        if(readFull(_in, head) != head.length){
            return null;
        }

        for(int i = 0; i < hd.length; i++){
            if(hd[i] != head[i]){
                throw new IOException("Wrong protocol");
            }
        }

        int len = (int) getNumber(head, 13, 4);
        int num = (int) getNumber(head, 17, 2);
        int serial = (int) getNumber(head, 9, 4);
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

        UWAPSegment data[] = new UWAPSegment[num];
        int off = 0;

        for(int i = 0; i < num; i++){
            int dataLen = (int) getNumber(buf, off + 1, 4);
            data[i] = new UWAPSegment(buf, off, dataLen);
            data[i].serial = serial;
            off += dataLen & 0x00FFFFFF;
        }

        return data;
    }

    public void processSegment(UWAPSegment segment) throws IOException{
        owner.processSegment(segment);
    }

    public void close(){
        _isConnected = false;

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
                    }
                }catch(Exception e){
                    //#debug
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
                    throw new Exception("断网测试");
                }
                if(parent._segments.size() != 0){
                    UWAPSegment segment = (UWAPSegment) parent._segments.get(0);
                    parent._segments.remove(0);

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

    public void cut(boolean cut){
        this.cut = cut;
    }
}