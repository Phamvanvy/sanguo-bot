package com.pip.net.uwap2.mina;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import com.pip.net.IMessage;

public class UWAP2MessageFilter extends IoFilterAdapter {

	protected IMessageDecoder decoder;
	protected IMessageEncoder encoder;

	public UWAP2MessageFilter(IMessageDecoder decoder,IMessageEncoder encoder){
		this.decoder = decoder;
		this.encoder = encoder;
	}

	@Override
	public void filterWrite(NextFilter nextFilter, IoSession session,
			WriteRequest writeRequest) throws Exception {
		IMessage msg = (IMessage)writeRequest.getMessage();
		UWAPSegment seg = encoder.encode(msg);
                if(seg!=null)
                    nextFilter.filterWrite(session, new WriteRequest(seg,writeRequest.getFuture()));
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		Packet packet = (Packet)message;
		IMessage msg = decoder.decode(packet.datas[0]);
		nextFilter.messageReceived(session, msg);
	}


}
