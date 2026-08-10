package com.pip.net;

import java.util.IdentityHashMap;
import java.util.Map;


public class DemuxingMessageHandler implements IMessageHandler{
	protected Map<Class<? extends IMessage>,IMessageHandler> handlers = new IdentityHashMap<Class<? extends IMessage>, IMessageHandler>();

	
	
	public void addHandler(Class<? extends IMessage> clazz,
			IMessageHandler handler) {
		handlers.put(clazz, handler);
	}

	public void handle(IMessage message) throws Exception{
		IMessageHandler handler = getHandler(message.getClass());
		if(handler!=null){
			handler.handle(message);
		}else{
			throw new UnknownMessageTypeException("UnknownExceptionType "+message.getCmd());
		}
	}
	
	IMessageHandler getHandler(Class<? extends IMessage> clazz){
		return handlers.get(clazz);
	}
}
