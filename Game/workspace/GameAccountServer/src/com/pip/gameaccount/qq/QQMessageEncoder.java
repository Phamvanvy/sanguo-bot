package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.pip.net.IMessage;

public class QQMessageEncoder extends ProtocolEncoderAdapter {

	private static final Logger log = Logger.getLogger(QQMessageEncoder.class);
	
	public void encode(IoSession session, Object obj, ProtocolEncoderOutput out)
			throws Exception {
		IMessage message = (IMessage)obj;
		if (message instanceof QQLoginResultMessage) {
			QQLoginResultMessage msg = (QQLoginResultMessage)message;
			ByteBuffer buf = ByteBuffer.allocate(24,false);
			byte[] bytes = msg.getResult().getBytes();
			buf.putInt(bytes.length);
			buf.put(bytes);
			if(bytes.length<4){
				buf.put(new byte[]{0x00,0x00});
			}
			buf.flip();
			log.info("login result:"+buf);
			out.write(buf);
		} else if (message instanceof QQBuyResultMessage) {
			log.info("buy result");
			QQBuyResultMessage msg = (QQBuyResultMessage)message;
			ByteBuffer buf = ByteBuffer.allocate(512,false);
			buf.setAutoExpand(true);
			buf.putShort(msg.getCmd());
			putString(buf,msg.getLinkId());
			putString(buf,msg.getBId());
			putString(buf,msg.getUin());
			putString(buf,msg.getObjectId());
			buf.putShort(msg.getCount());
			buf.putShort(msg.getResult());
			buf.flip();
			ByteBuffer buf1 = ByteBuffer.allocate(buf.remaining()+4,false);
			buf1.putInt(buf.remaining()+4);
			buf1.put(buf);
			buf1.flip();
			out.write(buf1);
		} else if (message instanceof QQLogin2ResultMessage) {
			QQLogin2ResultMessage msg = (QQLogin2ResultMessage)message;
			ByteBuffer buf = ByteBuffer.allocate(24,false);
			buf.putInt(11);		// ×Ü³¤¶È=11
			buf.put(msg.getVersion());
			buf.putInt(msg.getSeqNo());
			buf.put((byte)msg.getCmd());
			buf.put(msg.getResult() ? (byte)0 : (byte)1);
			buf.flip();
			log.info("login result:"+buf);
			out.write(buf);
		} else if (message instanceof QQBuy2ResultMessage) {
			log.info("buy result");
			QQBuy2ResultMessage msg = (QQBuy2ResultMessage)message;
			ByteBuffer buf = ByteBuffer.allocate(512,false);
			buf.setAutoExpand(true);
			buf.put(msg.getVersion());
			buf.putInt(msg.getSeqNo());
			buf.put((byte)msg.getCmd());
			putString(buf,msg.getUin());
			putString(buf,msg.getLinkId());
			buf.putInt(msg.getBId());
			buf.putShort(msg.getObjectId());
			buf.putInt(msg.getCount());
			buf.put(msg.getResult());
			buf.flip();
			ByteBuffer buf1 = ByteBuffer.allocate(buf.remaining()+4,false);
			buf1.putInt(buf.remaining()+4);
			buf1.put(buf);
			buf1.flip();
			out.write(buf1);
		}
	}

	protected void putString(ByteBuffer buf,String value){
		byte[] bytes = value.getBytes();
		buf.put((byte)bytes.length);
		buf.put(bytes);
	}
}
