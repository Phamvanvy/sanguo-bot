package com.pip.dispatch;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;

public class ServerUWAPEncoder
    implements ProtocolEncoder {

    public void dispose(IoSession ioSession) throws Exception {
    }


    public void encode(IoSession session, Object object,
                       ProtocolEncoderOutput out) throws
        Exception {
        out.write((ByteBuffer)object);
    }
}
