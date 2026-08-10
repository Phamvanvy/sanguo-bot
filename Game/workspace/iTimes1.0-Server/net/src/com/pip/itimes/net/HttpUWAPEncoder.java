package com.pip.itimes.net;

import java.io.OutputStream;
import java.io.DataOutputStream;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HttpUWAPEncoder {
    public void encode(OutputStream out, UWAPSegment[] segments) throws Exception {
        DataOutputStream dos = new DataOutputStream(out);
        for(int i=0;i<segments.length;i++){
            dos.write(UWAPUtil.HEAD);
            dos.writeInt(segments[i].getSessionId());
            dos.writeInt(segments[i].getSerial());
            segments[i].processCompress();
            dos.writeInt(19+segments[i].size());
            dos.writeShort((short)1);
            dos.write(segments[i].getByteArray());
            dos.writeByte(0);
        }
    }
}
