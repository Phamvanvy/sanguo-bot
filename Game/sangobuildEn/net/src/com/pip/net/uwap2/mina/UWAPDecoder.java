package com.pip.net.uwap2.mina;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class UWAPDecoder extends ProtocolDecoderAdapter {

    private static final Logger log = Logger.getLogger(UWAPDecoder.class);

    public void decode(IoSession session, ByteBuffer buffer,
                       ProtocolDecoderOutput out) throws Exception {
        byte[] remains = (byte[]) session.getAttachment();
        ByteBuffer oldBuffer = null;

        if (remains != null) {
            oldBuffer = ByteBuffer.wrap(remains);
            oldBuffer.setAutoExpand(true);
            oldBuffer.position(remains.length);
            oldBuffer.put(buffer);
            oldBuffer.flip();
        } else {
            oldBuffer = buffer;
        } while (oldBuffer.hasRemaining()) {
            oldBuffer.mark();
            int size = oldBuffer.remaining();
            if (size > 19) {
                byte[] head = new byte[5];
                oldBuffer.get(head);
                for (int i = 0; i < UWAPUtil.HEAD.length; i++) {
                    if (UWAPUtil.HEAD[i] != head[i]){
                        throw new IOException("error protocol");
                    }
                }
                int version = head[4] - 48;
                if(version != 1 && version!= 2)
                	throw new IOException("error protocol version");
                int sessionId = oldBuffer.getInt();
                int ser = oldBuffer.getInt();
                int len = oldBuffer.getInt();
                int num = oldBuffer.getShort();
                if(len>102400)
                    throw new IOException("error protocol");
                if ((len + 1) <= size) {
                    UWAPData[] datas = new UWAPData[num];
                    for (int i = 0; i < num; i++) {
                        oldBuffer.mark();
                        if(version == 1)
                        	oldBuffer.get();
                        else if(version == 2)
                        	oldBuffer.getShort();
                        int dataLen = oldBuffer.getInt();
                        boolean isCompressed = false;
                        if (dataLen >>> 24 != 0) {
                            isCompressed = true;
                        }
                        dataLen &= 0x00FFFFFF;
                        byte[] data = new byte[dataLen];
                        oldBuffer.reset();
                        oldBuffer.get(data);
                        datas[i] = new UWAPData(data, ser, sessionId, isCompressed, version);
                    }
                    oldBuffer.get(); //crc
                    Packet packet = new Packet();
                    packet.datas = datas;
                    session.setAttachment(null);
                    out.write(packet);
                } else {
                    oldBuffer.reset();
                    byte[] bytes = new byte[size];
                    oldBuffer.get(bytes);
                    session.setAttachment(bytes);
                }
            } else {
                oldBuffer.reset();
                byte[] bytes = new byte[size];
                oldBuffer.get(bytes);
                session.setAttachment(bytes);
            }
        }
    }
}
