package com.pip.gameaccount;

import java.net.SocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

import com.pip.net.Connector;
import com.pip.net.message.gameaccount.MessageDecoder;
import com.pip.net.message.gameaccount.MessageEncoder;
import com.pip.net.uwap2.mina.UWAP2MessageFilter;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;

public class AccountSkeleton extends Connector {

	public AccountSkeleton(String id,SocketAddress address){
		super(id,address,true);
	}
	
	@Override
	public void init() {
		config.getFilterChain().addLast("uwap2codec",
				new ProtocolCodecFilter(new UWAPEncoder(), new UWAPDecoder()));
		config.getFilterChain().addLast(
				"uwap2message",
				new UWAP2MessageFilter(new MessageDecoder(),
						new MessageEncoder()));
	}


}
