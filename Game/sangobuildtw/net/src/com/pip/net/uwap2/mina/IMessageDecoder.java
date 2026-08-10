package com.pip.net.uwap2.mina;

import com.pip.net.IMessage;

public interface IMessageDecoder {
	public IMessage decode(UWAPData data) throws Exception;
}
