package com.pip.itimes.net;

import java.io.InputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HttpUWAPDecoder {

    public HttpUWAPDecoder() {
    }

    public Packet[] decode(InputStream in) throws Exception {
        List l = new LinkedList();

        try {
            while (true) {
                int chkSum = 0;
                byte[] head = new byte[19];
                if (readFull(in, head) != head.length) {
                    return null;
                }
                for (int i = 0; i < UWAPUtil.HEAD.length-1; i++) {
                    if (head[i] != UWAPUtil.HEAD[i]) {
                        throw new IOException("Wrong protocol");
                    }
                }
                int version = head[4] - '0';
                int sessionId = (int) getNumber(head, 5, 4);
                int ser = (int) getNumber(head, 9, 4);
                int len = (int) getNumber(head, 13, 4);
                int num = (int) getNumber(head, 17, 2);
                len -= head.length;
                byte buf[] = null;
                if (len > 0) {
                    buf = new byte[len];
                    if (readFull(in, buf) != len) {
                        throw new IOException("No enouth input");
                    }
                    for (int i = 0; i < buf.length; i++) {
                        chkSum ^= buf[i];
                    }
                }
                for (int i = 0; i < head.length; i++) {
                    chkSum ^= head[i];
                }
                chkSum &= 0xff;
                in.read();

                UWAPData data[] = new UWAPData[num];
                int off = 0;
                for (int i = 0; i < num; i++) {
                    int dataLen = (int) getNumber(buf, off + 1, 4);
                    byte[] dd = new byte[dataLen];
                    System.arraycopy(buf, off, dd, 0, dataLen);
                    data[i] = new UWAPData(dd, ser, sessionId, false,version);
                    off += dataLen;
                }
                Packet packet = new Packet();
                packet.datas = data;

//                System.out.println("Data Type:"+data[0].getAppType());
                l.add(packet);
            }
        } catch (IOException ex) {
        }
        Packet[] ret = new Packet[l.size()];
        l.toArray(ret);
        return ret;
    }

    public static long getNumber(byte[] buf, int off, int len) {
        long l = 0;
        for (int i = 0; i < len; i++) {
            l <<= 8;
            l += ((int)buf[off + i]) & 0xff;
        }
        return l;
    }

    /**
     * Internal using method to fill the buffer from inputstream.
     *
     * @throws IOException if no enough data from the inputstream or read error
     */
    protected static int readFull(InputStream in, byte[] buf) throws
        IOException {
        int len = 0;
        try {
            while (len < buf.length) {
                int l = in.read(buf, len, buf.length - len);
                if (l < 0) {
                    throw new EOFException();
                }
                len += l;
            }
        } finally {
            for (int i = len; i < buf.length; i++) {
                buf[i] = 0;
            }
        }
        return len;
    }
}
