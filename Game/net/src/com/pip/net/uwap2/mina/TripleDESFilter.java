package com.pip.net.uwap2.mina;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

public class TripleDESFilter extends IoFilterAdapter {

	protected String keyString;
	protected Cipher de_cipher = null;
	protected Cipher en_cipher = null;

	private static final Logger log = Logger.getLogger(TripleDESFilter.class);

	public TripleDESFilter(String keyString) throws Exception {
		this.keyString = keyString;
		DESedeKeySpec dks = new DESedeKeySpec(keyString.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
		SecretKey securekey = keyFactory.generateSecret(dks);
		de_cipher = Cipher.getInstance("DESEDE/ECB/NoPadding");
		de_cipher.init(Cipher.DECRYPT_MODE, securekey);
		en_cipher = Cipher.getInstance("DESEDE/ECB/NoPadding");
		en_cipher.init(Cipher.ENCRYPT_MODE, securekey);
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		ByteBuffer in = (ByteBuffer) message;
		ByteBuffer buf = (ByteBuffer) session.getAttachment();
		log.info("Triple receive:"+in);
		if (buf != null) {
			buf.put(in);
			buf.flip();
		} else {
			buf = in;
		}
		while (buf.hasRemaining()) {
			int oldPos = buf.position();
			log.info("pos:"+oldPos);
			log.info("buf:"+buf);
			int len = buf.getInt();
			if (buf.remaining() >= (len-4)) { // 如果剩下的内容的长度超过len，那么就可以进行解析
				log.info("len:"+len);
				byte[] bytes = new byte[len-4];
				buf.get(bytes);
//				byte[] dec_bytes = decode(bytes);
				byte[] dec_bytes = de_cipher.doFinal(bytes);
				log.info("dec_bytes_length:"+dec_bytes.length);
				ByteBuffer dec_buf = ByteBuffer.wrap(dec_bytes);
				log.info("dec_buf:"+dec_buf);
				nextFilter.messageReceived(session, dec_buf);
			} else {
				buf.position(oldPos);
				buf.compact();
				session.setAttachment(buf);
				break;
			}
		}
	}

	@Override
	public void filterWrite(NextFilter nextFilter, IoSession session,
			WriteRequest writeRequest) throws Exception {
		ByteBuffer out = (ByteBuffer) writeRequest.getMessage();
		if(out.remaining()==0){
			log.info("empty buffer");
			return;
		}
		int len = out.remaining();
		int v = len%8;
		if(v!=0){
			len += (8-v);
		}
		log.info("write:"+out);
		byte[] bytes = new byte[len];
//		bytes[0] = (byte)((len>>24)&0xFF);
//		bytes[1] = (byte)((len>>16)&0xFF);
//		bytes[2] = (byte)((len>>8)&0xFF);
//		bytes[3] = (byte)(len&0xFF);
		
		Arrays.fill(bytes, (byte)0x20);
		out.get(bytes,0,out.remaining());
		log.info("write:"+out);
		byte[] en_bytes = en_cipher.doFinal(bytes);
		ByteBuffer buf = ByteBuffer.allocate(en_bytes.length+4);
		buf.putInt(en_bytes.length);
		buf.put(en_bytes);
		buf.flip();
		log.info("encrypt:"+buf);
		nextFilter.filterWrite(session, new WriteRequest(buf, writeRequest
				.getFuture()));
	}

}
