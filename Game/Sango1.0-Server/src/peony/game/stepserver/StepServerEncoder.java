package peony.game.stepserver;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import peony.net.DispatchPacket;
import peony.net.Packet;

public class StepServerEncoder extends ProtocolEncoderAdapter {

	public void encode(IoSession session, Object obj, ProtocolEncoderOutput out)
			throws Exception {
		if (obj instanceof DispatchPacket) {
			DispatchPacket dp = (DispatchPacket) obj;
			Packet packet = dp.packet;
			ByteBuffer data = packet.getData();
			data.flip();
			int len = 18 + data.remaining() + 4 + 1 + 4;
			ByteBuffer buf = ByteBuffer.allocate(len);
			buf.put(DispatchPacket.HEAD);
			buf.putInt(len);
			buf.put(StepClientEncoder.type0fPacket);
			buf.putInt(dp.accountId);
			buf.putInt(dp.playerId);
			buf.putInt(dp.id);
			buf.put(Packet.HEAD);
			buf.putInt(len - 10 - 4 - 1 - 4);
			buf.putShort(packet.getOpCode());
			buf.put(data);
			buf.flip();
			out.write(buf);
		}
	}

}
