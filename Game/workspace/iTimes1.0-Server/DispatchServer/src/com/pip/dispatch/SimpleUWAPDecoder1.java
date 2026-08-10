package com.pip.dispatch;



import java.io.IOException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.*;

public class SimpleUWAPDecoder1 extends ProtocolDecoderAdapter {


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
                for (int i = 0; i < UWAPUtil.HEAD.length; i++) {
                    if (UWAPUtil.HEAD[i] != head[i]){
                        session.setAttachment(null);
                        throw new IOException("error protocol");
                    }
                }
                buffer.skip(8); //SessionId,Serial
                int len = buffer.getInt();
                int segNum = buffer.getShort();
                if(segNum!=1){
                    session.setAttachment(null);
                    throw new IOException("error protocol");
                }
//                buffer.skip(2); //uwapsegment的数量，现在总是1
                if(len>102400||len<19){
                    session.setAttachment(null);
                    throw new IOException("error protocol");
                }
                if ((len + 1) <= size) {
                    if(buffer.remaining()<5){  //必须保证大于5个字节，不然读取dataType以及dataLen就会出错
                        session.setAttachment(null);
                        throw new IOException("error protocol");
                    }else{
                        buffer.skip(1); //dataType;
                        int dataLen = buffer.getInt();
                        if(dataLen<0||(dataLen-5)>=buffer.remaining()){  //dataLen包括前面5个字节
                            session.setAttachment(null);
                            throw new IOException("error protocol");
                        }
                        byte[] data = new byte[len + 1];
                        buffer.reset();
                        buffer.get(data);
                        out.write(ByteBuffer.wrap(data));
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


}

