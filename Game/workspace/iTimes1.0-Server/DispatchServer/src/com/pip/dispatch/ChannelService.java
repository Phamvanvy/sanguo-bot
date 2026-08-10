package com.pip.dispatch;

import java.util.concurrent.*;

import org.apache.mina.common.*;

public class ChannelService {

    private ConcurrentHashMap<String,Channel> channels = new ConcurrentHashMap<String,Channel>();
    private Channel normal90Channel = new Channel("Normal90");
    private Channel fast90Channel = new Channel("Fast90");

    public ChannelService() {
        channels.put(normal90Channel.getName(),normal90Channel);
        channels.put(fast90Channel.getName(),fast90Channel);
    }

    public Channel getNormal90Channel(){
        return normal90Channel;
    }

    public Channel getFast90Channel(){
        return fast90Channel;
    }

    public  Channel getAndCreate(String name){
        Channel channel = new Channel(name);
        channels.putIfAbsent(name,channel);
        return channels.get(name);
    }

    public Channel getChannel(String name){
        return channels.get(name);
    }

    public void removeSessionFromAllChannel(IoSession session){
        for(Channel channel:channels.values()){
            channel.removeSession(session);
        }
    }

    public void clearChannels(IoSession session){
        for(Channel channel:channels.values()){
            if(channel!=normal90Channel&&channel!=fast90Channel)
                channel.removeSession(session);
        }
    }
}
