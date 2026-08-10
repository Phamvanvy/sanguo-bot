package com.pip.dispatch;

import java.io.OutputStream;
import java.io.DataOutputStream;
import org.apache.mina.common.ByteBuffer;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HttpUWAPEncoder {
    public void encode(OutputStream out, ByteBuffer[] buffer) throws Exception {
        for(int i=0;i<buffer.length;i++){
            DataOutputStream dos = new DataOutputStream(out);
            int len = buffer[i].remaining();
            byte[] bytes = new byte[len];
            buffer[i].get(bytes);
            dos.write(bytes);
        }
    }
}
