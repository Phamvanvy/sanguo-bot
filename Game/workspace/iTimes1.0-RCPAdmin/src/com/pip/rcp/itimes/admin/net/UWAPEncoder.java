package com.pip.rcp.itimes.admin.net;


import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;


public class UWAPEncoder extends ProtocolEncoderAdapter{
    public void encode(IoSession session, Object data, ProtocolEncoderOutput out) throws Exception{
        UWAPSegment segment = (UWAPSegment)data;
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.setAutoExpand(true);
        buffer.put(UWAPUtil.HEAD);
        buffer.putInt(segment.getSessionId());
        buffer.putInt(segment.getSerial());

        segment.processCompress();

        buffer.putInt(19 + segment.size());
        buffer.putShort((short)1);
        buffer.put(segment.getByteArray());
        buffer.put((byte)0);
        buffer.flip();

        out.write(buffer);
    }

}
