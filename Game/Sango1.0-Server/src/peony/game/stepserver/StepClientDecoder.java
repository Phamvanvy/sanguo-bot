package peony.game.stepserver;

import java.io.IOException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import peony.net.DispatchPacket;
import peony.net.Packet;

public class StepClientDecoder extends ProtocolDecoderAdapter {

	private static final String BUFFER = ".UABuffer";
	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	

	public void decode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		boolean useSessionBuffer = false;
		ByteBuffer buf = (ByteBuffer)session.getAttribute(BUFFER);
		if(buf!=null){
			buf.put(in);
			buf.flip();
			useSessionBuffer = true;
		}else{
			buf = in;
		}
		for(;;){
			if(buf.remaining()>19){
				int pos = buf.position();
				if(buf.get()==68&&buf.get()==65){ //'D'&'A'
					int len = buf.getInt();
					byte type = buf.get();
					if(type==StepClientEncoder.type0fPacket){
						int accountId = 0;
						try{accountId = buf.getInt();}catch(Exception e){}
						int playerId = 0;
						try{playerId = buf.getInt();}catch(Exception e){}
						int sessionId = 0;
						try {
							sessionId = buf.getInt();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if(buf.remaining()>=(len-19)){  //去掉head以及len一共10个字节
							if (buf.get() == 85 && buf.get() == 65) {
								int packetLen = buf.getInt();
								short opCode = buf.getShort();
								ByteBuffer data = EMPTY;
								byte[] bytes = new byte[packetLen - 8];
								buf.get(bytes);
								data = ByteBuffer.wrap(bytes);
								data.position(bytes.length);
								Packet packet = new Packet(opCode, data);
								DispatchPacket dp = new DispatchPacket(sessionId,packet);
								dp.accountId = accountId;
								dp.playerId = playerId;
								out.write(dp);
							}else{
								session.setAttribute(BUFFER,null);
								throw new IOException("UA head error");
							}
						}else{
							buf.position(pos);
							break;
						}
					}
				}else{
					buf.position(pos + 1);
					continue;
				}
			}else{
//				buf.clear();
//				buf.flip();
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
    	ByteBuffer remainingBuf = ByteBuffer.allocate(buf.capacity());
        remainingBuf.setAutoExpand(true);
        remainingBuf.order(buf.order());
        remainingBuf.put(buf);
        session.setAttribute(BUFFER, remainingBuf);
    }

}
