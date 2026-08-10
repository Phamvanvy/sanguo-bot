package com.pip.server.auth.net;

import java.io.IOException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.*;
import org.apache.log4j.*;

public class UWAPDecoder extends ProtocolDecoderAdapter {

    private static final Logger log = Logger.getLogger(UWAPDecoder.class);

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
                int version = head[4] - '0';
                int sessionId = buffer.getInt();
                int ser = buffer.getInt();
                int len = buffer.getInt();
                int num = buffer.getShort();
                if(len>102400){
                    session.setAttachment(null);
                    throw new IOException("error protocol");
                }
                int minBytes = 5;
                if(version==2)
                    minBytes = 6;
                if ((len + 1) <= size) {
                    UWAPData[] datas = new UWAPData[num];
                    for (int i = 0; i < num; i++) {
                        if(buffer.remaining()<minBytes){
                            session.setAttachment(null);
                            throw new IOException("error protocol");
                        }
                        buffer.mark();
                        if(version==2){
                            buffer.getShort();
                        }else{
                            buffer.get();
                        }
                        int dataLen = buffer.getInt();
                        if(dataLen<0||(dataLen-minBytes)>=buffer.remaining()){
                            session.setAttachment(null);
                            throw new IOException("error protocol");
                        }
                        byte[] data = new byte[dataLen];
                        buffer.reset();
                        buffer.get(data);
                        datas[i] = new UWAPData(data, ser, sessionId, false,version);

                    }
                    buffer.get(); //crc
                    log.debug("UWAPData:" + datas[0].getAppType());
                    Packet packet = new Packet();
                    packet.datas = datas;
                    session.setAttachment(null);
                    out.write(packet);
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
}
