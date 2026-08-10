package com.pip.wulin.server.io;

import java.io.*;
import java.util.*;

/**
 * The transfering data generator for UWAP protocol.
 */
public class UWAPWritter {
    /** the buffer of header of the whole UWAP package */
    protected byte headBuf[] = {
        'U', 'W', 'A', 'P', '1', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /** the buffer of header of onde UWAP segment */

    /** the serial number of this connection */
    int serNum = 1;
    protected boolean writting = false;

    /** number of segments in one UWAP package */



    /**
     * For Internal usage only:
     * Can be used only if the sub class not implemented the writeImpl method,
     * then the sub class can get the tranfering data from this buffer after
     * calling the flush method.
     */

    public void setHeaderReserved(int val) {
        setNumber(val, headBuf, 5, 4);
    }

    /**
     * The implementation of the write method.
     * it will send the real data to the outputs.
     */
    protected void writeImpl(byte[] buf) throws IOException {

    }

    protected void writeImpl(byte buf) throws IOException {

    }

    protected void writeImpl() throws IOException {
    }

    /**
     * Flushes this output stream.
     * This forces any buffered output bytes to be written out to the stream.
     */

    public void setSer(int ser) {
        serNum = ser;
    }

//    protected int flush(boolean caughtPkg) throws IOException {
//        synchronized (lock) {
//            Integer pk = null;
//            try {
//                // close the current segment
//
//                if (cash != null) {
//                    byte chkSum = 0;
//                    int ser = serNum++;
//
//                    // roll back
//                    if (serNum < 0) {
//                        serNum = 0;
//                    }
//                    byte[] buf = cash.toByteArray();
//                    byte[] tmp = new byte[buf.length + headBuf.length + 1];
//                    setNumber(ser, headBuf, 9, 4);
//                    setNumber(buf.length + headBuf.length, headBuf, 13, 4);
//                    setNumber(pkgNum, headBuf, 17, 2);
//                    pkgNum = 0;
//                    // fill the header
//                    System.arraycopy(headBuf, 0, tmp, 0, headBuf.length);
//                    // fill the content
//                    System.arraycopy(buf, 0, tmp, headBuf.length, buf.length);
//                    // calculate the chksum and
//                    for (int i = 0; i < headBuf.length; i++) {
//                        chkSum ^= headBuf[i];
//                    }
//                    for (int i = 0; i < buf.length; i++) {
//                        chkSum ^= buf[i];
//                    }
//                    tmp[tmp.length - 1] = chkSum;
//                    writeImpl(tmp);
//                    return ser;
//                }
//            } catch (IOException e) {
//
//
//                throw e;
//            } finally {
//                dout = null;
//                if (cash != null) {
//                    try {
//                        cash.close();
//                    } catch (Exception e) {
//                    }
//                    cash = null;
//                }
//            }
//            return -1;
//        }
//    }

    /** get next serial number. */
    public int getNextSerial() {
        synchronized (this) {
            int ser = serNum++;
            // roll back
            if (serNum < 0) {
                serNum = 0;
            }
            return ser;
        }
    }

    /** to write one dimention boolean array into the UWAP package's segment */

    public int write(UWAPSegment data, int ser) throws
        IOException {
        /**@todo
         *
         * 为了保证正确性，牺牲了
         * 效率，如果正确性验证后，则可以优化速度
         */

        return write(new UWAPSegment[] {data}, ser);

    }

    public int write(UWAPSegment[] data, int ser) throws
        IOException {
        try {
            writting = true;
            if (data != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                byte chkSum = 0;
                if (ser == -1) {
                    ser = getNextSerial();
                }

                int buflen = 0;
                for (int i = 0; i < data.length; i++) {
                    data[i].flush();
                    buflen += data[i].data.length;
                }

                setNumber(ser, headBuf, 9, 4);
                setNumber(buflen + headBuf.length, headBuf, 13, 4);
                setNumber(data.length, headBuf, 17, 2);

                // fill the header
                dos.write(headBuf);
                for (int i = 0; i < headBuf.length; i++) {
                    chkSum ^= headBuf[i];
                }
                for (int i = 0; i < data.length; i++) {
                    dos.write(data[i].data);

                    for (int j = 0; j < data[i].data.length; j++) {
                        chkSum ^= data[i].data[j];
                    }

                }
                dos.write(chkSum);
                writeImpl(bos.toByteArray());
                writeImpl();
            }
        } finally {
            writting = false;
            synchronized (this) {
                notifyAll();
            }
        }
        return ser;
    }

    public void syncWrite(UWAPSegment data, long timeout, int ser) throws IOException {
        if (writting) {
            synchronized (this) {
                try {
                    wait(timeout);
                } catch (Exception e) {
                }
            }
        }
        if (writting) {
            return;
        }
        write(new UWAPSegment[] { data }, ser);
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
    public static void setNumber(int num, byte[] buf, int off, int len) {
        for (int i = len - 1; i >= 0; i--) {
            buf[off + i] = (byte)(num & 0xff);
            num >>= 8;
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
