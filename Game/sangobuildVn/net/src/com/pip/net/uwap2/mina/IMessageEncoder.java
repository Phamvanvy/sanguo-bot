package com.pip.net.uwap2.mina;

import com.pip.net.IMessage;

public interface IMessageEncoder {
	public UWAPSegment encode(IMessage message) throws Exception;
}
