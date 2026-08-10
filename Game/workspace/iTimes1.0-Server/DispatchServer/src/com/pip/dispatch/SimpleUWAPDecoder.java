package com.pip.dispatch;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;
import java.io.IOException;

public class SimpleUWAPDecoder extends CumulativeProtocolDecoder{

    protected boolean doDecode(IoSession session, ByteBuffer in,
                               ProtocolDecoderOutput out) throws IOException{
        int start = in.position();
        if(in.hasRemaining()){
            int size = in.remaining();
            if(size>19){  //必须大于UWAP头
                byte[] head = new byte[5];
                in.get(head);
                if(!compareHead(head))  //todo:更好的处理方式应该是读到UWAP头，然后parse后面的，而不是直接抛出异常
                    throw new IOException("error protocol");
                in.skip(8);
                int len = in.getInt();  //第13个字节是UWAP包的长度,包括所有的UWAP头，但是不包括一个字节的crc
                if(len>102400)
                    throw new IOException("error protocol");
                if((len+1)<=size){
                    int limit = in.limit();
                    in.position(start);
                    in.limit(start+len+1);
                    out.write(in.slice());
                    in.position(start+len+1);
                    in.limit(limit);
                    return true;
                }else{
                    in.position(start);
                    return false;
                }
            }else{
                return false;
            }
        }
        return false;
    }

    protected boolean compareHead(byte[] value){
        for(int i=0;i<UWAPUtil.HEAD.length;i++){
            if(value[i]!=UWAPUtil.HEAD[i])
                return false;
        }
        return true;
    }
}
