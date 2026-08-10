package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class QQMessageDecoder extends ProtocolDecoderAdapter {

	private static final Logger log = Logger.getLogger(QQMessageDecoder.class);
	
	
	public void decode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		log.info("Decoder receive:"+in);
		int length = in.getInt();
		log.info("length:"+length);
		int oldPosition = in.position();
		short cmd = in.getShort();
		if(cmd==QQMessageType.QQ_LOGIN){
			log.info("login message");
			String uin = getString(in);
			log.info("uin:"+uin);
			String sessionKey = getString(in);
			log.info("key:"+sessionKey);
			int time = in.getInt();
			QQLoginMessage msg = new QQLoginMessage(uin,sessionKey,time);
			log.info("msg out:"+msg.hashCode());
			out.write(msg);
		}
		else if(cmd==QQMessageType.QQ_BUY||cmd==QQMessageType.QQ_BUY_CHINARUN){
			log.info("buy type:"+cmd);
			log.info("buy message");
			String linkId = getString(in);
			log.info("linkId:"+linkId);
			String bId = getString(in);
			log.info("bId:"+bId);
			String uin = getString(in);
			log.info("uin:"+uin);
			String objectId = getString(in);
			log.info("objectId:"+objectId);
			int count = in.getShort()&0xFFFF;
			log.info("count:"+count);
			int time = in.getInt();
			QQBuyMessage msg = new QQBuyMessage(cmd,linkId,bId,uin,objectId,count,time);
			log.info("msg out:"+msg.hashCode());
			out.write(msg);
		}
		else{
			// 新版本协议
			in.position(oldPosition);
			byte version = in.get();
			int seqNo = in.getInt();
			cmd = (short)(in.get() & 0xFFFF);
			if (cmd == QQMessageType.QQ_LOGIN2) {
				log.info("login message");
				String uin = getString(in);
				log.info("uin:"+uin);
				String sessionKey = getString(in);
				log.info("key:"+sessionKey);
				int time = in.getInt();
				QQLogin2Message msg = new QQLogin2Message(version, seqNo, uin, sessionKey, time);
				log.info("msg out:"+msg.hashCode());
				out.write(msg);
			} else if (cmd == QQMessageType.QQ_BUY2 || cmd == QQMessageType.QQ_BUY_CHINARUN2) {
				log.info("buy type:"+cmd);
				log.info("buy message");
				String uin = getString(in);
				log.info("uin:"+uin);
				String linkId = getString(in);
				log.info("linkId:"+linkId);
				int bId = in.getInt();
				log.info("bId:"+bId);
				short objectId = in.getShort();
				log.info("objectId:"+objectId);
				int count = in.getInt();
				log.info("count:"+count);
				int time = in.getInt();
				QQBuy2Message msg = new QQBuy2Message(cmd, version, seqNo, uin, linkId, bId, objectId, count, time);
				log.info("msg out:"+msg.hashCode());
				out.write(msg);
			}
		}
	}

	
	public String getString(ByteBuffer in){
		short len = (short)(in.get()&0xFF);
		byte[] bytes = new byte[len];
		in.get(bytes);
		return new String(bytes);
	}
}
