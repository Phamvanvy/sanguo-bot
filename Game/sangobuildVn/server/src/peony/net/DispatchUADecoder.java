package peony.net;

import java.io.IOException;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class DispatchUADecoder extends ProtocolDecoderAdapter {

	
	private static final String BUFFER = ".UABuffer";
	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	

	public void decode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		boolean useSessionBuffer = false;
		ByteBuffer buf = (ByteBuffer)session.getAttribute(BUFFER);
		if(buf!=null){
			if (in.remaining() > Packet.MAX_PACKET_SIZE + 10 - buf.position()) {
				session.setAttribute(BUFFER, null);
				throw new IOException("DA packet exceeds maximum size.");
			}
			buf.put(in);
			buf.flip();
			useSessionBuffer = true;
		}else{
			buf = in;
		}
		for(;;){
			if(buf.remaining()>10){
				int pos = buf.position();
				if(buf.get()==68&&buf.get()==65){ //'D'&'A'
					int len = buf.getInt();
					if (len < 18 || len > Packet.MAX_PACKET_SIZE + 10) {
						session.setAttribute(BUFFER, null);
						throw new IOException("Invalid DA packet length: " + len);
					}
					int sessionId = buf.getInt();
					if(buf.remaining()>=(len-10)){  //去掉head以及len一共10个字节
						if (buf.get() == 85 && buf.get() == 65) {
							int packetLen = buf.getInt();
							if (packetLen < 8 || packetLen > Packet.MAX_PACKET_SIZE || packetLen != len - 10) {
								session.setAttribute(BUFFER, null);
								throw new IOException("Invalid nested UA packet length: " + packetLen);
							}
							short opCode = buf.getShort();
							ByteBuffer data = EMPTY;
							byte[] bytes = new byte[packetLen - 8];
							buf.get(bytes);
							data = ByteBuffer.wrap(bytes);
							Packet packet = new Packet(opCode, data);
							DispatchPacket dp = new DispatchPacket(sessionId,packet);
							out.write(dp);
						}else{
							session.setAttribute(BUFFER,null);
							throw new IOException("UA head error");
						}
					}else{
						buf.position(pos);
						break;
//						buf.compact();
//						if(!useSessionBuffer){
//							session.setAttribute(BUFFER,buf);
//							break;
//						}

					}
				}else{
					session.setAttribute(BUFFER,null);
					throw new IOException("DA head error.");
					//session.setAttribute(BUFFER,null);
					//throw new IOException("DA head error.");
				}
			}else{
//				if(buf.hasRemaining()){
//					buf.compact();
//					if(!useSessionBuffer)
//						session.setAttribute(BUFFER,buf);
//				}
				break;
			}
		}
		if (buf.hasRemaining()) {
				storeRemainingInSession(buf,session);

		}else{
			if(useSessionBuffer)
				session.setAttribute(BUFFER,null);
		}
	}
	
    private void storeRemainingInSession(ByteBuffer buf, IoSession session) {
		if (buf.remaining() > Packet.MAX_PACKET_SIZE + 10) {
			session.setAttribute(BUFFER, null);
			throw new IllegalArgumentException("DA packet exceeds maximum size.");
		}
        ByteBuffer remainingBuf = ByteBuffer.allocate(Math.max(18, buf.remaining()));
        remainingBuf.setAutoExpand(true);
        remainingBuf.order(buf.order());
        remainingBuf.put(buf);
        session.setAttribute(BUFFER, remainingBuf);
    }
}
