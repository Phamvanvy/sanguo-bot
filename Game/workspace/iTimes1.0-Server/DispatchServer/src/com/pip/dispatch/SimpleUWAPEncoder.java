package com.pip.dispatch;

import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.common.ByteBuffer;

public class SimpleUWAPEncoder implements ProtocolEncoder{

    public void dispose(IoSession ioSession) throws Exception {
    }


    public void encode(IoSession session, Object object,
                       ProtocolEncoderOutput out) throws
        Exception {
//        System.out.println(object.toString());
        out.write((ByteBuffer)object);
    }

}
