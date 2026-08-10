package com.pip.dispatch;

import java.io.IOException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.*;
import org.apache.log4j.Logger;

public class SimpleUWAPDecoder2 extends ProtocolDecoderAdapter {

    private static final Logger log = Logger.getLogger(SimpleUWAPDecoder2.class);

    public void decode(IoSession session, ByteBuffer in,
                       ProtocolDecoderOutput out) throws Exception {
        byte[] remains = (byte[]) session.getAttachment();
        ByteBuffer buffer = null;

        if (remains != null) {
            buffer = ByteBuffer.wrap(remains);
            buffer.setAutoExpand(true);
            buffer.position(remains.length);
            buffer.put(in);
            buffer.flip();
        } else {
            buffer = in;
        } while (buffer.hasRemaining()) {
            buffer.mark();
            int size = buffer.remaining();
            if (size > 19) {
                byte[] head = new byte[5];
                buffer.get(head);
                for (int i = 0; i < UWAPUtil.HEAD.length-1; i++) {
                    if (UWAPUtil.HEAD[i] != head[i]){
                        session.setAttachment(null);
                        throw new IOException("error protocol");
                    }
                }
                int sessionId = buffer.getInt();
                buffer.skip(4); //Serial
                int len = buffer.getInt();
                short num = buffer.getShort(); //segment数量，如果为控制包那么数量应该是0
                if ((len + 1) <= size) {
                    byte[] data = new byte[len + 1];
                    buffer.reset();
                    buffer.get(data);
                    if(num==0){
                        Packet1 packet = new Packet1(sessionId);
                        out.write(packet);
                        session.setAttachment(null);
                    }else{
                        ByteBuffer ubuffer = ByteBuffer.wrap(data);
                        if(check(ubuffer)){
                            Packet1 pakcet = new Packet1(ubuffer, sessionId);
                            out.write(pakcet);
                        }
                        session.setAttachment(null);
                    }
                } else {
                    buffer.reset();
                    byte[] bytes = new byte[size];
                    buffer.get(bytes);
                    session.setAttachment(bytes);
                }
            } else {
                buffer.reset();
                byte[] bytes = new byte[size];
                buffer.get(bytes);
                session.setAttachment(bytes);
            }
        }
    }


    protected boolean check(ByteBuffer buffer){
        byte c = buffer.get(4);
        if(c!=UWAPUtil.HEAD[4]){
            return false;
        }
        try {
            int dataLen = buffer.getInt(20);
            if (dataLen != buffer.remaining() - 20) {
                return false;
            }
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
}
