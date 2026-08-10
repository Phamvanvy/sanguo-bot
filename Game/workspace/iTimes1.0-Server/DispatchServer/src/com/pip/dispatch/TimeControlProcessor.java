package com.pip.dispatch;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.ByteBuffer;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TimeControlProcessor
    implements ControlProcessor,Runnable {

    private static final byte SYNC_CHANNEL = (byte)185;
    private static final byte KICK = (byte)191;
    private static final byte FORCE_BROADCAST = (byte)183;
    private static final byte BROADCAST = (byte)184;
    private static final byte MAXPLAYER = (byte)197;
    private static final byte SHUTDOWN = (byte)192;
    public static final byte CLEAR_CHANNELS = (byte)214;
    
    public static final byte CHAT_NO_FILTER = (byte)3;
    
    public static final byte CHAT_NO_SEND = (byte)0;
    
    /**
     * 同步玩家的登录版本号协议
     */
    public static final byte  SYNC_PLAYER_DATAVESION= (byte)66;
    
    public static final String CAMP_CHANNEL = "CAMP";
    
    private ChannelService channelService;
    private Dispatcher dispatcher;
    private IpdService ipdService;
    private ChatService chatService;
    
    public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}

	private static final Logger log = Logger.getLogger(TimeControlProcessor.class);

    private BlockingQueue<UWAPData> datas = new LinkedBlockingQueue<UWAPData>();
    private Thread workingThread = null;
    private boolean stopped = false;

    public TimeControlProcessor(){
        workingThread = new Thread(this);
        workingThread.start();
    }

    public void shutdown() {
        stopped = true;
        workingThread.interrupt();
    }
    
    public void setChannelService(ChannelService channelService){
        this.channelService = channelService;
    }

    public void setDispatcher(Dispatcher dispatcher){
        this.dispatcher = dispatcher;
    }

    public void setIpdService(IpdService ipdService){
        this.ipdService = ipdService;
    }

    public void process(UWAPData data) {
        try {
            datas.put(data);
        }
        catch (InterruptedException ex1) {
        }
    }

    protected void process0(UWAPData data){
        byte type = data.getAppType();
        try {
            switch (type) {
                case SYNC_CHANNEL:
                    syncChannel(data);
                    break;
                case KICK:
                    kick(data);
                    break;
                case FORCE_BROADCAST:
                    forceBroadcast(data);
                    break;
                case BROADCAST:
                    broadcast(data);
                    break;
                case MAXPLAYER:
                    maxPlayer(data);
                    break;
                case SHUTDOWN:
                    shutdown(data);
                    break;
                case CLEAR_CHANNELS:
                    clearChannels(data);
                    break;
                case SYNC_PLAYER_DATAVESION :
                	syscPlayerDataVesion(data);
                	break;
            }
        }
        catch (Exception ex) {
            log.error(ex,ex);
        }
    }

    public void run(){
        while(!stopped){
            try {
                UWAPData data = datas.take();
                process0(data);
            }
            catch (InterruptedException ex) {
            }
        }
    }

    private void clearChannels(UWAPData data) throws Exception{
        int sessionId = data.readInt();
        IoSession session = dispatcher.getSession(sessionId);
        if(session!=null){
            channelService.clearChannels(session);
        }
    }

    private void shutdown(UWAPData data){
        dispatcher.shutdown();
    }

    private void forceBroadcast(UWAPData data) throws Exception{
        byte[] bytes = data.readBytes();
        dispatcher.broadcast(ByteBuffer.wrap(bytes));
    }

    private void broadcast(UWAPData data) throws Exception{
        Channel channel = channelService.getChannel(data.readString());
        boolean campMsg = channel.getName().startsWith(CAMP_CHANNEL);
        byte[] chat = data.readBytes();
    	byte[] filtChat = data.readBytes();
        //获取该频道的session并根据频道session查找玩家的客户端的dataversion..
        Set<IoSession> sessions = channel.getSessions();
        for(IoSession session:sessions){
        	int dataVesion = chatService.getPlayerDataVersion(session);
    		if(dataVesion == CHAT_NO_SEND){
    			if(campMsg){//如果是阵营消息的话，不下发
    				continue;
    			}
    		}
        	if(dataVesion >= CHAT_NO_FILTER){
        		session.write((ByteBuffer.wrap(chat)).duplicate());
        	}else{
        		session.write((ByteBuffer.wrap(filtChat)).duplicate());
        	}
        }
       /* if(channel!=null){
            channel.broadcast(ByteBuffer.wrap(data.readBytes()));
        }*/
    }


    private void maxPlayer(UWAPData data) throws Exception{
        int current = data.readInt();
        int maxPlayer = data.readInt();
        long c = data.readLong();
        log.info("SyncTime["+(System.currentTimeMillis()-c)+"]");
        if(ipdService!=null)
            ipdService.connect(current,maxPlayer);
    }
    
    /**
     * @param data
     * @throws Exception
     * 同步玩家的dataVersion
     */
    private void syscPlayerDataVesion(UWAPData data)throws Exception{
    	 int sessionId = data.readInt();
    	 int dataVersion = data.readInt();
    	 IoSession session = dispatcher.getSession(sessionId);
         if(session!=null){
        	 chatService.addChatPlayerDataVersion(session, dataVersion);
         }
    }
    
    
    private void syncChannel(UWAPData data) throws Exception{
        int sessionId = data.readInt();
        IoSession session = dispatcher.getSession(sessionId);
        if(session!=null){
            String[] aChannels = data.readStrings();
            String[] rChannels = data.readStrings();
            for (int i = 0; i < aChannels.length; i++) {
                Channel channel = channelService.getAndCreate(aChannels[i]);
                if (channel != null) {
                    channel.join(session);
                }
            }
            for(int i=0;i<rChannels.length;i++){
                Channel channel = channelService.getChannel(rChannels[i]);
                if(channel!=null){
                    channel.removeSession(session);
                }
            }
        }
    }

    private void kick(UWAPData data) throws Exception{
        int sessionId = data.readInt();
        dispatcher.unRegisterClient(sessionId);
    }
}
