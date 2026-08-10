package com.pip.wulin.server.io;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * The server side implementation of UWAP over Socket.
 * TODO: Package Type verifing/Server side package definition
 */
public class UWAPSocketSConnection extends UWAPConnection
    implements Runnable {
    Socket socket;
    InputStream inStream;
    public OutputStream outStream;
    public Thread runningThread;

    /** the serial number of the requested package */
    private int ser = 0;
    private boolean closed = false;

    /** the construction with the session ID */
    public UWAPSocketSConnection(Socket socket, UWAPApp app) {
        try {
            this.socket = socket;
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();
            app.registerSession(this);
            runningThread = new Thread(this);
            runningThread.start();
        } catch (Exception e) {
            close();
        }
    }

    public void run() {
        while (inStream != null) {
            try {
                readAll(inStream);
            } catch (Throwable e) {
                break;

            }
        }
    }

    public byte[] readAll(InputStream in) {
        byte head[] = null;
        try {
//                if (in != null) {
                // get the returned data array
                byte[] tmpHd = new byte[19];
                UWAPData[] data = readData(in, tmpHd);
                if (data != null) {
                    head = tmpHd;
                    ser = (int)getNumber(head, 9, 4);
                    Enumeration emum = listeners.elements();
                    try {
                        while (emum.hasMoreElements()) {
                            UWAPDataListener l = (UWAPDataListener)emum.
                                nextElement();
                            if (l.onGotData(this, data, ser, 0)) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        writeErr(e.getMessage(), ser);

                    }
                }
//                }
        } catch (Exception e) {
            close();
            head = null;
        }
        return head;
    }

    public void writeErr(String err, int ser) throws IOException {
        UWAPSegment seg = new UWAPSegment((byte)0xff);
        seg.write((short)0); // system error
        seg.write(err);
        write(seg, ser);
    }

    /** to close the connection */
    public void closeConn() {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (Exception e) {
            }
            inStream = null;
        }
        if (outStream != null) {
            try {
                outStream.close();
            } catch (Exception e) {
            }
            outStream = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
            }
            socket = null;
        }
    }

    /**
     * Internal method to read whole package from the inputstream
     * @param head must fix-size(19) buffer for the UWAP head.
     */
    public static UWAPData[] readData(InputStream in, byte[] head) throws IOException {
        int chkSum = 0;
        byte[] hd = {'U', 'W', 'A', 'P', '1'};
        if (readFull(in, head) != head.length) {
            return null;
        }
        for (int i = 0; i < hd.length; i++) {
            if (hd[i] != head[i]) {
                throw new IOException("Wrong protocol");
            }
        }

        int ser = (int)getNumber(head, 9, 4);
        int len = (int)getNumber(head, 13, 4);
        int num = (int)getNumber(head, 17, 2);
        len -= head.length;
        byte buf[] = null;
        if (len > 0) {
            buf = new byte[len];
            if (readFull(in, buf) != len) {
                throw new IOException("No enouth inputs");
            }
            // com.pip.util.Debug.outHex("Pkg body:", buf);
            // check sum of the data
            for (int i = 0; i < buf.length; i++) {
                chkSum ^= buf[i];
            }
        }
        // check sum of the header
        for (int i = 0; i < head.length; i++) {
            chkSum ^= head[i];
        }
        chkSum &= 0xff;
        in.read();
//        if (chkSum != in.read()) {
//            throw new IOException("CheckSum error");
//        } else {
            UWAPData data[] = new UWAPData[num];
            int off = 0;
            for (int i = 0; i < num; i++) {
                int dataLen = (int)getNumber(buf, off + 1, 4);
                data[i] = new UWAPData(buf, off, dataLen);
                off += dataLen;
            }
            return data;
//        }
    }

    protected void writeImpl(byte[] buf) throws IOException {
        try {
            outStream.write(buf);
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    protected void writeImpl(byte buf) throws IOException {
        try {
            outStream.write(buf);
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    protected void writeImpl() throws IOException {
        try {
            outStream.flush();
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    public synchronized void close() {
        closeConn();
        if (!closed) {
            closed = true;
            Enumeration emum = listeners.elements();
            while (emum.hasMoreElements()) {
                UWAPDataListener l = (UWAPDataListener)emum.nextElement();
                l.onSignal(this, UWAPDataListener.SIG_CLOSING, "Closed");
            }
        }
    }

    public String getRemoteIP() {
        return socket.getRemoteSocketAddress().toString();
    }
}
