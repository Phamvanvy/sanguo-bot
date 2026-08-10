package peony.net;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class DispatchUAEncoder extends ProtocolEncoderAdapter {

	public void encode(IoSession session, Object obj, ProtocolEncoderOutput out)
			throws Exception {
		if (obj instanceof DispatchPacket) {
			DispatchPacket dp = (DispatchPacket) obj;
			Packet packet = dp.packet;
			ByteBuffer data = packet.getData();
			data.flip();
			int len = 18 + data.remaining();
			ByteBuffer buf = ByteBuffer.allocate(len);
			buf.put(DispatchPacket.HEAD);
			buf.putInt(len);
			buf.putInt(dp.id);
			buf.put(Packet.HEAD);
			buf.putInt(len - 10);
			buf.putShort(packet.getOpCode());
			buf.put(data);
			buf.flip();
			out.write(buf);
		}
	}
}
