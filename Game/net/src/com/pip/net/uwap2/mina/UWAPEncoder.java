package com.pip.net.uwap2.mina;

import org.apache.log4j.Logger;
import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;

public class UWAPEncoder extends ProtocolEncoderAdapter {

    private final static Logger log = Logger.getLogger(UWAPEncoder.class);

    public void encode(IoSession session, Object data,
                       ProtocolEncoderOutput out) throws Exception {
        UWAPSegment segment = (UWAPSegment) data;
        
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.setAutoExpand(true);
        buffer.put(UWAPUtil.HEAD);
        buffer.put((byte)50);
        buffer.putInt(segment.getSessionId());
        buffer.putInt(segment.getSerial());

//        segment.processCompress();

        buffer.putInt(19 + segment.size());
        buffer.putShort( (short) 1);
        buffer.put(segment.getByteArray());
        buffer.put( (byte) 0);
        buffer.flip();
//        log.debug("send seg");
        out.write(buffer);
//        System.out.println("encode:"+segment.getType());
    }

}
