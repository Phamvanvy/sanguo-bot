package com.pip.itimes.server.world;

import java.io.*;
import java.util.*;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.BasicItemTemplate;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.ChatOption;
import com.pip.itimes.server.stage.DynamicEquipmentTemplate;
import com.pip.itimes.server.stage.ExtendedItemTemplate;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.NormalEquipmentTemplate;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.TaskItemTemplate;
import com.pip.itimes.server.stage.TongUser;
import com.pip.itimes.server.util.KeywordsUtil;
import org.apache.log4j.Logger;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.world.game.InstanceService;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.server.world.game.HouseInstanceModel;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.Instance;
import com.pip.itimes.server.world.game.HouseInstance;
import com.pip.itimes.server.world.lyrics.LoveLyricsConfig;
import com.pip.itimes.server.world.lyrics.LyricsConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig2;
/**
 * @author Jeffrey
 * @version 1.0
 */
public class ChatService implements Runnable{
    private Logger log = Logger.getLogger(ChatService.class);

//    private Map favoriteChannels = new TreeMap();

    private StageService stageService = null;
    private ConnectService connectService = null;
    private TongService tongService = null;
    private PlayerService playerService = null;
    private InstanceService instanceService = null;
    private HouseInstanceModel houseModel = null;
    protected AdminService adminService;
    private ShoutService shoutService;
    
    private Map players = new HashMap();
    private Map forbiden = new TreeMap();

    public static final String WORLD_CHANNEL = "WORLD";
    public static final String MAP_CHANNEL = "MAP";
    public static final String FAVORITE_CHANNEL = "FAVORITE";
    public static final String CAMP_CHANNEL = "CAMP";
    
    /*保存玩家发送的物品的基本信息*/
    public static HashMap player2Items = new HashMap();
    public static long lastMakeTime = Utils.getTodayStart(); //默认要这天的起始时间 最好一次更新
    public static final int period = 86400000;
    
    public ChatService() {
        this.stageService = stageService;
    }


    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void setTongService(TongService tongService){
        this.tongService = tongService;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setInstanceService(InstanceService instanceService){
        this.instanceService = instanceService;
    }

    public void setHouseModel(HouseInstanceModel houseModel){
        this.houseModel = houseModel;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }
    
    public void setShoutService (ShoutService shoutService) {
    	this.shoutService = shoutService;
    }

    public void run(){
        try{
        	while(true){
        		Thread.sleep(300 * 1000L);
                synchronized (forbiden) {
                    long currentTime = System.currentTimeMillis();
                    Iterator ite = forbiden.entrySet().iterator();
                    while (ite.hasNext()) {
                        Map.Entry entry = (Map.Entry) ite.next();
                        Forbiden f = (Forbiden)entry.getValue();
                        if (f.validTime <= currentTime)
                            ite.remove();
                    }
                }
                if(Utils.getTodayStart() >= lastMakeTime + period){
                	ChatService.reSet();
        		}
        	}
            
            
        }catch(Throwable ex){
            log.error(ex,ex);
        }
    }

    public void forbiden(int id,int mask,int time){
        synchronized(forbiden){
            Forbiden f = new Forbiden();
            f.id = id;
            f.mask = mask;
            if(time==0){
                forbiden.remove(new Integer(id));
            }else{
                f.validTime = System.currentTimeMillis() + time * 1000L;
                forbiden.put(new Integer(id), f);
            }
        }
    }

    public boolean removeForbiden(int id){
        synchronized(forbiden){
            Object o = forbiden.remove(new Integer(id));
            return o != null;
        }
    }

    public boolean isForbiden(int id,int channel){
        if(!forbiden.containsKey(new Integer(id)))
            return false;
        else{
            Forbiden f = (Forbiden)forbiden.get(new Integer(id));
            return !f.isValid(channel);
        }
    }

    public void start(){
        new Thread(this).start();
    }


    public UWAPSegment adminMessage(int srcId,String srcName,int destId,int value,String msg){
        if(destId==ISendMessage.WORLD){
            sendWorldMessage(srcId,srcName,msg);
        }
        else if(destId==ISendMessage.MAP){
            sendMapMessage((short)value,srcId,srcName,msg);
        }
        else if(destId==ISendMessage.GUILD){
            sendTongMessage(value,srcId,srcName,msg);
        }
        else if(destId==ISendMessage.FAVORITE){
            sendFavoriteMessage(value,srcId,srcName,msg);
        }
        else if(destId==ISendMessage.SYSTEM){
            sendSystemMessage(msg);
        }
        else if(destId>0){ //私聊
            srcName = "[GM"+srcName+"]";
            return sendPrivateMessage(srcId,srcName,destId,msg);
        }
        return null;
    }

    public void sendSystemMessage(String msg){
    	saveItmeID(-1,msg);		// 系统发送的物品ID保存
        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT,-1);
        chatSeg.writeInt(-1);
        chatSeg.writeString("系统");
        chatSeg.writeInt(ISendMessage.SYSTEM);
        chatSeg.writeString(msg);
        
        UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT,-1);
        chatSeg2.writeInt(-1);
        chatSeg2.writeString("系统");
        chatSeg2.writeInt(ISendMessage.SYSTEM);
        chatSeg2.writeString(Utils.filterChatString(msg));
        
        UWAPSegment seg = new UWAPSegment(ServerConstants.FORCE_BROADCAST);
        seg.write(chatSeg.getPacketByteArray());
        seg.write(chatSeg2.getPacketByteArray());
        connectService.broadcast(seg);
        adminService.onChatMessage(-ISendMessage.SYSTEM, -1, -1, chatSeg);
    }

    public void sendTongMessage(int tongId,int srcId,String srcName,String msg){
    	saveItmeID(srcId,msg);		// 系统发送的物品ID保存
        TongUser[] members = tongService.getTongMembers(tongId,
                1);
        for (int i = 0; i < members.length; i++) {
            WorldPlayer player = playerService.getWorldPlayer(members[i].id);

            if(player!=null&&player.online()&&player.getChatOptions()[ChatOption.GUILD].pri!=0){
                UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT,
                                                  -1);
                seg.writeInt(srcId);
                seg.writeString(srcName);
                seg.writeInt(ISendMessage.GUILD);
                WorldPlayer member = playerService.getWorldPlayer(members[i].id);
                if(member != null){
                	if(member.getClientDataVersion() < 3){
                		seg.writeString(Utils.filterChatString(msg));
                	}else{
                		seg.writeString(msg);
                	}
                }else{
                	seg.writeString(msg);
                }
          
                seg.writeString(msg);
                connectService.writeTo(seg, members[i].id);
            }
        }
		UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT, -1);
		seg.writeInt(srcId);
        seg.writeString(srcName);
        seg.writeInt(ISendMessage.GUILD);
        seg.writeString(msg);
        adminService.onChatMessage(-ISendMessage.GUILD, srcId, tongId, seg);
    }

    public void sendFavoriteMessage(int favoriteId,int srcId,String srcName,String msg){
    	saveItmeID(srcId,msg);		// 系统发送的物品ID保存
        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT,-1);
        chatSeg.writeInt(srcId);
        chatSeg.writeString(srcName);
        chatSeg.writeInt(ISendMessage.FAVORITE);
        chatSeg.writeString(msg);
        
        UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT,-1);
        chatSeg2.writeInt(srcId);
        chatSeg2.writeString(srcName);
        chatSeg2.writeInt(ISendMessage.FAVORITE);
        chatSeg2.writeString(Utils.filterChatString(msg));
        
        UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
        seg.writeString(FAVORITE_CHANNEL+favoriteId);
        seg.write(chatSeg.getPacketByteArray());
        seg.write(chatSeg2.getPacketByteArray());
        connectService.broadcast(seg);
        adminService.onChatMessage(-ISendMessage.FAVORITE, srcId, favoriteId, seg);
    }

    public void sendMapMessage(short mapId,int srcId,String srcName,String msg){
    	saveItmeID(srcId,msg);		// 系统发送的物品ID保存
        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
        chatSeg.writeInt(srcId);
        chatSeg.writeString(srcName);
        chatSeg.writeInt(ISendMessage.MAP);
        chatSeg.writeString(msg);
        
        
        //生成第二份拷贝协议，过滤掉新聊天协议中的字符
        UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT);
        chatSeg2.writeInt(srcId);
        chatSeg2.writeString(srcName);
        chatSeg2.writeInt(ISendMessage.MAP);
        chatSeg2.writeString(Utils.filterChatString(msg));
        
        InstanceDefinition idf = instanceService.getInstanceDefinitionByEntrance(mapId);
        if(idf==null){
            idf = instanceService.getInstanceDefintionByMap(mapId);
        }
        if(idf==null){
            UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
            seg.writeString(MAP_CHANNEL+mapId);
            seg.write(chatSeg.getPacketByteArray());
            seg.write(chatSeg2.getPacketByteArray());
            connectService.broadcast(seg);
        }else{
            short[] maps = idf.getMaps();
            for(int i=0;i<maps.length;i++){
                UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
                seg.writeString(MAP_CHANNEL+maps[i]);
                seg.write(chatSeg.getPacketByteArray());
                seg.write(chatSeg2.getPacketByteArray());
                connectService.broadcast(seg);
            }
            UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
            seg.writeString(MAP_CHANNEL+idf.getEntrance());
            seg.write(chatSeg.getPacketByteArray());
            seg.write(chatSeg2.getPacketByteArray());
            connectService.broadcast(seg);
        }
        adminService.onChatMessage(-ISendMessage.MAP, srcId, mapId, chatSeg);
    }

    public void sendTeamMessage(int srcId,String srcName,int destId,String msg){
    	saveItmeID(srcId,msg);		// 系统发送的物品ID保存
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                CHAT,-1);
        seg.writeInt(srcId);
        seg.writeString(srcName);
        seg.writeInt(ISendMessage.TEAM);
        WorldPlayer dest = playerService.getWorldPlayer(destId);
        if(dest != null){
        	if(dest.getClientDataVersion() >= 3){
        		seg.writeString(msg);
        	}else{
        		seg.writeString(Utils.filterChatString(msg));
        	}
        }else{
        	seg.writeString(msg);
        }
        connectService.writeTo(seg, destId);
        adminService.onChatMessage(-ISendMessage.TEAM, srcId, destId, seg);
    }

    public void sendWorldMessage(int srcId, String srcName, String msg){
    	sendWorldMessage(srcId, srcName, msg, null);
    }
    
    public void sendWorldMessage(int srcId,String srcName,String msg, IItem item){
    	
    	saveItmeID(srcId,msg, item);		// 系统发送的物品ID保存
        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
        chatSeg.writeInt(srcId);
        chatSeg.writeString(srcName);
        chatSeg.writeInt(ISendMessage.WORLD);
        chatSeg.writeString(msg);
        
        UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT);
        chatSeg2.writeInt(srcId);
        chatSeg2.writeString(srcName);
        chatSeg2.writeInt(ISendMessage.WORLD);
        chatSeg2.writeString(Utils.filterChatString(msg));

        UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
        seg.writeString(WORLD_CHANNEL);
        seg.write(chatSeg.getPacketByteArray());
        seg.write(chatSeg2.getPacketByteArray());
        connectService.broadcast(seg);
        adminService.onChatMessage(-ISendMessage.WORLD, srcId, 0, chatSeg);
    }
    
    // 发送狮子吼
    // boolean judge TRUE 是狮子吼，  FALSE 普通说话
    /**
     * 参数int srcId, String srcName,String msg等同于sendWorldMessage
     * judge 如果为false则是普通世界聊，如果是true则是狮子吼世界聊
     * x,y 当信息是狮子吼的时候为在屏幕上绘制的位置用法：x%,y%
     * color 当前狮子吼的颜色
     * srcChannle 当前狮子吼的频道用法：MAP(地图聊),WORLD(世界聊)
     * mapId 如果是地图聊的时候所配置的地图ID
    */
    public void sendRoarMessage(int srcId, String srcName,String msg, boolean judge, int x, int y, int color, String srcChannle, short mapId){
    	
    	if (judge) {
    		if (srcChannle.equals("WORLD")) {
    			saveItmeID(srcId,msg);		// 系统发送的物品ID保存
                UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
            	chatSeg.writeInt(-10);
                chatSeg.writeString(srcName);
                chatSeg.writeInt(ISendMessage.ROAR);
                chatSeg.writeString(msg);
            	chatSeg.writeInt(color);
            	chatSeg.writeInt(x);
            	chatSeg.writeInt(y);
                
                UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT);
            	chatSeg2.writeInt(-10);
            	chatSeg2.writeString(srcName);
            	chatSeg2.writeInt(ISendMessage.ROAR);
            	chatSeg2.writeString(msg);
            	chatSeg2.writeInt(color);
            	chatSeg2.writeInt(x);
            	chatSeg2.writeInt(y);

                UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
                seg.writeString(WORLD_CHANNEL);
                seg.write(chatSeg.getPacketByteArray());
                seg.write(chatSeg2.getPacketByteArray());
                connectService.broadcast(seg);
                adminService.onChatMessage(-ISendMessage.WORLD, srcId, 0, chatSeg);
    		} else if (srcChannle.equals("MAP")) {
    			saveItmeID(srcId,msg);		// 系统发送的物品ID保存
    	        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
    	        chatSeg.writeInt(-11);
    	        chatSeg.writeString(srcName);
    	        chatSeg.writeInt(ISendMessage.ROAR);
    	        chatSeg.writeString(msg);
    	        chatSeg.writeInt(color);
            	chatSeg.writeInt(x);
            	chatSeg.writeInt(y);
    	        
    	        
    	        //生成第二份拷贝协议，过滤掉新聊天协议中的字符
    	        UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT);
    	        chatSeg2.writeInt(-11);
    	        chatSeg2.writeString(srcName);
    	        chatSeg2.writeInt(ISendMessage.ROAR);
    	        chatSeg2.writeString(Utils.filterChatString(msg));
    	        chatSeg.writeInt(color);
            	chatSeg.writeInt(x);
            	chatSeg.writeInt(y);
    	        
    	        InstanceDefinition idf = instanceService.getInstanceDefinitionByEntrance(mapId);
    	        if(idf==null){
    	            idf = instanceService.getInstanceDefintionByMap(mapId);
    	        }
    	        if(idf==null){
    	            UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
    	            seg.writeString(MAP_CHANNEL+mapId);
    	            seg.write(chatSeg.getPacketByteArray());
    	            seg.write(chatSeg2.getPacketByteArray());
    	            connectService.broadcast(seg);
    	        }else{
    	            short[] maps = idf.getMaps();
    	            for(int i=0;i<maps.length;i++){
    	                UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
    	                seg.writeString(MAP_CHANNEL+maps[i]);
    	                seg.write(chatSeg.getPacketByteArray());
    	                seg.write(chatSeg2.getPacketByteArray());
    	                connectService.broadcast(seg);
    	            }
    	            UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
    	            seg.writeString(MAP_CHANNEL+idf.getEntrance());
    	            seg.write(chatSeg.getPacketByteArray());
    	            seg.write(chatSeg2.getPacketByteArray());
    	            connectService.broadcast(seg);
    	        }
    	        adminService.onChatMessage(-ISendMessage.MAP, srcId, mapId, chatSeg);
    		}
    	} else {
    		sendWorldMessage(srcId, srcName, msg);
    	}
    	
    }
    
    public void sendPrivateRoarMessage(int srcId, String srcName, String msg, int x, int y, int color, String srcChannle, short mapId, int destId, String bossName){	
		saveItmeID(srcId,msg);		// 系统发送的物品ID保存
        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
    	chatSeg.writeInt(-12);
    	chatSeg.writeString(srcName);
    	chatSeg.writeInt(ISendMessage.ROAR);		//频道号
    	WorldPlayer dest = playerService.getWorldPlayer(destId);
    	if(dest != null){
	     	if(dest.getClientDataVersion() < 3){
	     		chatSeg.writeString(Utils.filterChatString(msg));
	     		chatSeg.writeString(bossName);
	     	}else{
	     		chatSeg.writeString(msg);
	     		chatSeg.writeString(bossName);
	     	}
    	}else{
    		chatSeg.writeString(msg);
    		chatSeg.writeString(bossName);
    	}
    	chatSeg.writeInt(color);
    	chatSeg.writeInt(x);
    	chatSeg.writeInt(y);
		connectService.writeTo(chatSeg,destId);
    	adminService.onChatMessage(0, srcId, destId, chatSeg);
    }
    
    public void sendNewMessage(String content, int destId){
    	saveItmeID(-1,content);		// 系统发送的物品ID保存
    	 UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CONN_NEWS);
    	 chatSeg.writeInt(-1);
    	 chatSeg.writeString("系统");
    	 chatSeg.writeInt(ISendMessage.NEW);
    	 WorldPlayer dest = playerService.getWorldPlayer(destId);
         if(dest != null){
         	if(dest.getClientDataVersion() < 3){
         		chatSeg.writeString(Utils.filterChatString(content));
         	}else{
         		chatSeg.writeString(content);
         	}
         }else{
        	 chatSeg.writeString(content);
         }
         //chatSeg.writeString(content);
         connectService.writeTo(chatSeg, destId);
    	
    }
    public void receiveMessage(WorldPlayer player,WorldPlayer destPlayer,String msg){
        if(destPlayer==null){
            sendPrivateMessage(-1,"系统",player.getId(),"对方不在线");
        }else{
            log.info("ID["+player.getId()+"]NAME["+player.getPlayerName()+"]DESTID["+destPlayer.getId()+"]MESSAGE["+msg+"]");
            if(!isForbiden(player.getId(),destPlayer.getId()))
                sendPrivateMessage(player.getId(),player.getPlayerName(),destPlayer.getId(),msg);
        }
    }

    public void receiveMessage(WorldPlayer player, int srcId, String srcName,
                               int destId, String msg) {
        log.info("ID["+player.getId()+"]NAME["+player.getPlayerName()+"]DESTID["+destId+"]MESSAGE["+msg+"]");
        String message = KeywordsUtil.filterKeywords(msg);
        message = message.replace('\n',' ');
        if(Utils.filterClientChatString(message).endsWith("XX")){
        	sendPrivateMessage(-1, "系统", srcId, "你的输入不合法");
        	return;
        }
        String checkMessage = message.toLowerCase();
        int index = checkMessage.indexOf("<c");
        if(checkMessage.length() >= 4 && index >= 0 && checkMessage.indexOf("<c", index + 1) >= 0){
        	sendPrivateMessage(-1, "系统", srcId, "你的输入不合法");
        	return;
        }
        if(player.getLevel()<=10&&destId==ISendMessage.WORLD)
            destId = ISendMessage.MAP;
        if (destId == ISendMessage.GUILD&&!isForbiden(player.getId(),ISendMessage.GUILD)) {  //公会
            if(player!=null){
                if(player.getTongId()==-1){
                    sendPrivateMessage(-1,"系统",player.getId(),"没有所属公会");
                    return;
                }
                if(player.getTongDuty()==Tong.MUTE_MEMBER){
                    sendPrivateMessage(-1,"系统",player.getId(),"你已经被公会禁言");
                    return;
                }
                sendTongMessage(player.getTongId(),srcId, srcName, message);
            }
        }
        else if (destId == ISendMessage.TEAM&&!isForbiden(player.getId(),ISendMessage.TEAM)) { //队伍消息
            if (player != null) {
                Team team = player.getTeam();
                if (team != null) {
                	PositionSprite[] ps = team.getPlayers();
                	ArrayList<WorldPlayer> lstPlayer = new ArrayList<WorldPlayer>();
                	for(int i=0; i<ps.length; i++){
                		if(ps[i] instanceof WorldPlayer){
                			lstPlayer.add((WorldPlayer)ps[i]);
                		}
                	}
                	WorldPlayer[] players = new WorldPlayer[lstPlayer.size()];
                	lstPlayer.toArray(players);
                    for (int i = 0; i < players.length; i++) {
                        if(players[i].getChatOptions()[ChatOption.TEAM].pri!=0){
                            sendTeamMessage(srcId,srcName,players[i].getId(),message);
                        }
                    }
                }
            }
        }
        else if(destId==ISendMessage.FAVORITE&&!isForbiden(player.getId(),ISendMessage.FAVORITE)){
            if(player.getChatFavoriteId()!=-1){
                sendFavoriteMessage(player.getChatFavoriteId(),srcId,srcName,message);
            }
        }
        else if(destId==ISendMessage.MAP&&!isForbiden(player.getId(),ISendMessage.MAP)){
        	long currentTime = System.currentTimeMillis();
        	if (currentTime - player.lstareaChatTime < 10 * 1000) {
        		sendPrivateMessage(-1, "系统", srcId, "公共场所说话不能太快");
        		return;
        	}
            if(Utils.getStringLength(message)>40){
                sendPrivateMessage(-1,"系统",srcId,"您所发布的信息过长");
                return;
            }
            GameMap map = player.getMap();
            if(map!=null){
                Instance instance = map.getInstance();
                if(instance!=null&&instance instanceof HouseInstance){
                    WorldPlayer[] players = map.getPlayers();
                    for(int i=0;i<players.length;i++){
                        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
                        chatSeg.writeInt(srcId);
                        chatSeg.writeString(srcName);
                        chatSeg.writeInt(ISendMessage.MAP);
                        chatSeg.writeString(msg);
                        connectService.writeTo(chatSeg,players[i].getId());
                        player.lstareaChatTime = currentTime;
                    }
                    return;
                }else{
                	sendMapMessage(player.getMapId(),srcId,srcName,message);
                	player.lstareaChatTime = currentTime;
                }
            }
        }
        else if(destId==ISendMessage.WORLD&&!isForbiden(player.getId(),ISendMessage.WORLD)){
        	long currentTime = System.currentTimeMillis();
        	if (currentTime - player.lstChatTime < 10 * 1000) {
        		sendPrivateMessage(-1, "系统", srcId, "公共场所说话不能太快");
        	} else if(Utils.getStringLength(message) > 40){
                sendPrivateMessage(-1,"系统",srcId,"您所发布的信息过长");
            } else if(canSendWorldMessage(player)) {
            	int mapId = player.getMapId();
                sendWorldMessage(srcId,srcName,message);
                player.lstChatTime = currentTime;
                if (player != null) {
                	//经验包进行检测 
                	if(player.getLevel() < 20 && player.getCheckExpBag() == 0){
                		Changed changed = new Changed();
            			IItem item = Items.getTemplate(200500).newInstance();
            			player.completeAddItem(item, 1, changed, (byte)22);
            			connectService.sendGetItem(changed, player.getId(), (byte)22);
            			player.setCheckExpBag(1);
                	}
                	//歌词活动时 进行检测
                	if(LyricsConfig.state == LyricsConfig.ACTIONSTART){
                		LyricsConfig.playerChat(message, player);
                	}
                	//情歌对唱时 进行检测
                	if(LoveLyricsConfig.state == LoveLyricsConfig.ACTIONSTART){
                		LoveLyricsConfig.playerChat(message, player);
                	}
                	//猜灯谜活动时 进行检测
                	if(RiddlesConfig.state == RiddlesConfig.ACTIONSTART){
                		RiddlesConfig.playerChat(message, player);
                	}
                	//咏春诗歌活动时 进行检测
                	if(RiddlesConfig2.state == RiddlesConfig2.ACTIONSTART){
                		RiddlesConfig2.playerChat(message, player);
                	}
                	if (shoutService.checkStarted() && !shoutService.checkPlayerCompleted(player, ShoutService.WORLD)
                			&& shoutService.checkPosition(mapId, ShoutService.WORLD)
                			&& shoutService.checkMessageContent(message, ShoutService.WORLD)) {
                		player.setLastWorldCompleteTime(new Date());
                		shoutService.sendChatGift(player, ShoutService.WORLD);
                	}
                }
            } else {
                sendPrivateMessage(-1,"系统",srcId,"您今日的世界聊次数已达上限");
            }
        }
        else if(destId==ISendMessage.CAMP&&!isForbiden(player.getId(),ISendMessage.CAMP)){
        	long currentTime = System.currentTimeMillis();
        	if (currentTime - player.lstChatTime < 10 * 1000) {
        		sendPrivateMessage(-1, "系统", srcId, "公共场所说话不能太快");
        	} else if(Utils.getStringLength(message) > 40){
                sendPrivateMessage(-1,"系统",srcId,"您所发布的信息过长");
            } else {
            	if(player.getCamp() == Utils.NO_CAMP){
            		sendPrivateMessage(-1,"系统",srcId,"请你加入阵营后再发送阵营聊天");
            	}else if(canSendCampMessage(player)){
            		int mapId = player.getMapId();
	            	sendCampMessage(srcId,srcName,message, player.getCamp());
	            	player.lstChatTime = currentTime;
	            	if (player != null) {
	                	if (shoutService.checkStarted() && !shoutService.checkPlayerCompleted(player, ShoutService.CAMP)
	                			&& shoutService.checkPosition(mapId, ShoutService.CAMP)
	                			&& shoutService.checkMessageContent(message, ShoutService.CAMP)) {
	                		player.setLastCampCompleteTime(new Date());
	                		shoutService.sendChatGift(player, ShoutService.CAMP);
	                	}
	                }
            	}else{
            		sendPrivateMessage(-1,"系统",srcId,"您今日的阵营聊次数已达上限，提升等级可以增加每日阵营聊天的数量");
            	}
            }
        }
        else if(destId>0&&!isForbiden(player.getId(),destId)){
            WorldPlayer destPlayer = playerService.getWorldPlayer(destId);
            if(destPlayer==null||!destPlayer.online()){
                sendPrivateMessage(-1,"系统",player.getId(),"玩家不在线");
            }else{
                sendPrivateMessage(srcId,srcName,destId,message);
            }
        }
    }
    
    /**
     * @param srcId
     * @param srcName
     * @param msg
     * 发送阵营消息
     */
    public void sendCampMessage(int srcId,String srcName,String msg, int camp){
    	saveItmeID(srcId,msg);		// 系统发送的物品ID保存
    	
        UWAPSegment chatSeg = new UWAPSegment(ClientConstants.CHAT);
        chatSeg.writeInt(srcId);
        chatSeg.writeString(srcName);
        chatSeg.writeInt(ISendMessage.CAMP);
        chatSeg.writeString(msg);
        
        UWAPSegment chatSeg2 = new UWAPSegment(ClientConstants.CHAT);
        chatSeg2.writeInt(srcId);
        chatSeg2.writeString(srcName);
        chatSeg2.writeInt(ISendMessage.CAMP);
        chatSeg2.writeString(Utils.filterChatString(msg));

        UWAPSegment seg = new UWAPSegment(ServerConstants.BROADCAST);
        seg.writeString(CAMP_CHANNEL + camp);
        seg.write(chatSeg.getPacketByteArray());
        seg.write(chatSeg2.getPacketByteArray());
        connectService.broadcast(seg);
        adminService.onChatMessage(-ISendMessage.WORLD, srcId, 0, chatSeg);
    }
    
    public UWAPSegment sendPrivateMessage(int src,String srcName,int destId,String msg){
    	saveItmeID(src,msg);		// 系统发送的物品ID保存
    	
        UWAPSegment seg = new UWAPSegment(ClientConstants.CHAT);
        seg.writeInt(src);
        seg.writeString(srcName);
        seg.writeInt(destId);
        WorldPlayer dest = playerService.getWorldPlayer(destId);
        if(dest != null){
        	//如果屏蔽私聊
        	if (dest.getChatOptions()[ChatOption.PRIVATE].pri==0){
        		return null;
        	}
        	if(dest.getClientDataVersion() < 3){
        		seg.writeString(Utils.filterChatString(msg));
        		seg.writeString(srcName);
        	}else{
        		seg.writeString(msg);
        		seg.writeString(srcName);
        	}
        }else{
        	 seg.writeString(msg);
        	 seg.writeString(srcName);
        }
        connectService.writeTo(seg,destId);
        if(src>0){//发送给你自己的不用判断版本号
            UWAPSegment seg1 = new UWAPSegment(ClientConstants.CHAT);
            seg1.writeInt(src);
            seg1.writeString(srcName);
            seg1.writeInt(src);
            seg1.writeString(msg);
            seg1.writeString(dest.getPlayerName());
            connectService.writeTo(seg1,src);
        }
        adminService.onChatMessage(0, src, destId, seg);
        return seg;
    }

    public boolean canSendWorldMessage(WorldPlayer player){
        if(player.getPlayerName().startsWith("gm"))
            return true;
        long last = (player.getLastMessageTime().getTime()+28800000)/86400000;
        long curr = (System.currentTimeMillis()+28800000)/86400000;
        if(curr!=last){
            player.setLastMessageTime(new Date());
            player.setMessageCount(0);
        }
        if(player.getMessageCount(ISendMessage.WORLD)<10){
            player.increaseMessageCount(ISendMessage.WORLD);
            return true;
        }
        return false;
    }
    
    public boolean canSendCampMessage(WorldPlayer player){
    	if(player.getPlayerName().startsWith("gm"))
    		return true;
    	long last = (player.getLastMessageTime().getTime()+28800000)/86400000;
        long curr = (System.currentTimeMillis()+28800000)/86400000;
        if(curr!=last){
            player.setLastMessageTime(new Date());//阵营聊和世界聊共用一个时间记录
            player.setMessageCount(0);
        }
        if(player.getLevel()>50){
        	return true;
        }else{
        	if(player.getMessageCount(ISendMessage.CAMP)< player.getLevel()){
        		player.increaseMessageCount(ISendMessage.CAMP);
        		return true;
        	}
        }
    	return false;
    }
    //次方法专门给gm接口用
//    public ISendMessage getSendMessage(int destId, int value, String msg) {
//        if (destId > 0) {
//            PlayerChatInfo destPlayer = getChatPlayer(destId);
//            if (destPlayer != null) {
//                ISendMessage ret = new PrivateSendMessage( -1,
//                        "系统",
//                        destId, msg);
//                return ret;
//            }
//        } else {
//            IChatChannel channel = null;
//            if (destId == -1) { //世界
//                channel = worldChannel;
//            } else if (destId == -2) { //地图
//                channel = getMapChannel(value);
//            } else if (destId == -7) {
//                channel = getFavoriteChannel(value);
//            }
//            if (channel != null) {
//                ISendMessage ret = new SendMessage(-1,
//                        "系统", channel.getPlayers(), msg);
//                return ret;
//            }
//        }
//        return null;
//    }




//    private IChatChannel getMapChannel(int id){
//        return (IChatChannel)mapChannels.get(new Integer(id));
//    }

//    private IChatChannel getFavoriteChannel(int id){
//        return (IChatChannel)favoriteChannels.get(new Integer(id));
//    }

//    private PlayerChatInfo getChatPlayer(int id){
//        return (PlayerChatInfo)players.get(new Integer(id));
//    }



    public void registry(Player player) {
//        PlayerChatInfo chatPlayer = (PlayerChatInfo)players.get(new Integer(player.getId()));
//        if(chatPlayer!=null){
////            log.debug("player:"+player.getPlayerName()+" already in chatservice");
//            return;
//        }
//        chatPlayer = new PlayerChatInfo(player);
//        players.put(new Integer(player.getId()),chatPlayer);
//        registry(chatPlayer,systemChannel);
//        ChatOption[] options = chatPlayer.getChatOptions();
//        if(options[ChatOption.MAP].pri!=0){ //地图
//            short mapId = player.getMapId();
//            IChatChannel mapChannel = getMapChannel(mapId);
//            if (mapChannel != null) {
//                registry(chatPlayer, mapChannel);
//            }
//        }
//        if(options[ChatOption.WORLD].pri!=0){
//            registry(chatPlayer,worldChannel);
//        }
//        if(options[ChatOption.FAVORITE].pri!=0){
//            IChatChannel favoriteChannel = getFavoriteChannel(chatPlayer.getFavoriteId());
//            if(favoriteChannel!=null){
//                registry(chatPlayer,favoriteChannel);
//            }
//        }
    }

    private void registry(PlayerChatInfo player,IChatChannel channel){
        channel.registry(player.getPlayer());
        player.addChannel(channel);
    }

    private void unRegistry(PlayerChatInfo player,IChatChannel channel){
        channel.unRegistry(player.getPlayer());
        player.removeChannel(channel);
    }

    public void unRegistry(Player player) {
        PlayerChatInfo chatPlayer = (PlayerChatInfo)players.get(new Integer(player.getId()));
        if(chatPlayer==null){
//            log.debug("player:"+player.getPlayerName()+" not in chatservice[unregistry]");
            return;
        }
        Iterator ite = chatPlayer.getChannels().iterator();
        while(ite.hasNext()){
            ChatChannel chatChannel = (ChatChannel)ite.next();
            unRegistry(chatPlayer,chatChannel);
        }
    }
    
    public void saveItmeID(int playID, String msg){
    	saveItmeID(playID, msg, null);
    }
    
    public void saveItmeID(int playID, String msg, IItem srcItem){
    	try{
    		String[] idStr = Utils.getItemID(msg);		//物品ID
    		int idType = 0;
    		int id = 0;
    		if(idStr != null && idStr[0]!= null && idStr[1]!= null ){
    			idType = Integer.parseInt(idStr[0].trim());
    			id = Integer.parseInt(idStr[1].trim());
    		}else{
    			return;
    		}
    		Player2Item player2Item = null;
    		if(player2Items.containsKey(playID)){
    			player2Item = (Player2Item) player2Items.get(playID);		// 查看当前玩家是否添加过消息
    		}else{
    			player2Item = new Player2Item();
    		}
    		
    		if(playID == -1){
    			if(srcItem != null){
					player2Item.setItem(srcItem.getId(), srcItem, -1);
				}else{
	    			IItemTemplate tempItemTemplate = Items.getTemplate(id);
	    			if(tempItemTemplate == null){
	    				return;
	    			}else{
    					IItem item = null;
	        			Class t = tempItemTemplate.getClass();
	    	    		if(tempItemTemplate instanceof NormalEquipmentTemplate){
	    	    			NormalEquipmentTemplate dynamicEquipmentTemplate = (NormalEquipmentTemplate) tempItemTemplate;
	    	    			item = dynamicEquipmentTemplate.newInstance(0, id);
	    	    		}else if(tempItemTemplate instanceof DynamicEquipmentTemplate){
	    	    			DynamicEquipmentTemplate dynamicEquipmentTemplate = (DynamicEquipmentTemplate) tempItemTemplate;
	    	    			item = dynamicEquipmentTemplate.newInstance(0, id);
	    	    		}else if(tempItemTemplate instanceof TaskItemTemplate || tempItemTemplate instanceof BasicItemTemplate || tempItemTemplate instanceof ExtendedItemTemplate){
	    	    			item = tempItemTemplate.newInstance();
	    	    		}
	    	    		if(item != null){
	    	    			player2Item.setItem(id, item, -1);
	    				}
	    			}
				}
    		}else{
//    			WorldPlayer playerTarget = playerService.loadWorldPlayer(playID);
    			WorldPlayer playerTarget = playerService.getWorldPlayerAndCatch(playID);
    			IItem item = null;
    			Grid grid = null;
    			if(idType == 1){
    				grid = playerTarget.getItem(id, 0);
    			}else if(idType == 2){
    				grid = playerTarget.getEquipmentByInstanceid(id);
    			}else if(idType == 3){
    				Pet pet = playerTarget.getPet(id);
    				player2Item.setPet(id, pet);
    			}
    			if(grid != null){
    				item = grid.item;
    				if(item != null){
    					player2Item.setItem(id, item, idType == 2 ? playerTarget.getLevel() : -1);
    				}
    			}
    			playerService.releasePlayer(playerTarget);
    		}
    		player2Items.put(playID, player2Item);
    	}catch(Exception e){
    	}
    }
    
    /**
	 * 已经是第二天了，需要重置
	 */
	public static void reSet(){
		lastMakeTime = Utils.getTodayStart();
		player2Items.clear();
	}

//    public void positionChanged(Player player,
//                                short oldMapId, short x, short y) {
//        PlayerChatInfo chatPlayer = (PlayerChatInfo)players.get(new Integer(player.getId()));
//        if(chatPlayer==null){
////            log.debug("player:"+player.getPlayerName()+" not in chatservice[position]");
//            return;
//        }
//        short mapId = player.getMapId();
//        if(mapId!=oldMapId&&chatPlayer.inMap()){
//            IChatChannel channel = getMapChannel(oldMapId);
//            if(channel!=null){
//                unRegistry(chatPlayer,channel);
//            }
//            channel = getMapChannel(mapId);
//            if(channel!=null){
//                registry(chatPlayer,channel);
//            }
//        }
//    }

//    public void changeChatFavorite(int playerId,byte index){
//        try {
//            PlayerChatInfo chatPlayer = (PlayerChatInfo) players.get(new
//                    Integer(
//                            playerId));
//            if (chatPlayer != null) {
//                int oldFavorite = chatPlayer.getFavoriteId();
//                if(index<0){
//                    IChatChannel channel = getFavoriteChannel(oldFavorite);
//                    if(channel!=null)
//                        unRegistry(chatPlayer,channel);
//                    chatPlayer.setFavoriteId(-1);
//                }
//                ChatFavorite[] cfs = ChatFavorites.getChatFavorites();
//                int favorite = cfs[index].id;
//                if (oldFavorite != favorite) {
//                    IChatChannel channel = getFavoriteChannel(oldFavorite);
//                    if(channel!=null)
//                        unRegistry(chatPlayer,channel);
//                    channel = getFavoriteChannel(favorite);
//                    if(channel!=null)
//                        registry(chatPlayer,channel);
//                    chatPlayer.setFavoriteId(favorite);
//                }
//            }
//        } catch (Exception ex) {
//        }
//    }

//    public void setOptions(int playerId, ChatOption[] options) {
//        PlayerChatInfo chatPlayer = (PlayerChatInfo) players.get(new Integer(
//                playerId));
//        if (chatPlayer != null) {
//            ChatOption[] oldOptions = chatPlayer.getChatOptions();
//            if (oldOptions[ChatOption.WORLD].pri !=
//                options[ChatOption.WORLD].pri) {
//                if(options[ChatOption.WORLD].pri==0){
//                    unRegistry(chatPlayer,worldChannel);
//                }else{
//                    if(options[ChatOption.WORLD].pri!=0&&oldOptions[ChatOption.WORLD].pri==0)
//                        registry(chatPlayer,worldChannel);
//                }
//            }
//            if (oldOptions[ChatOption.MAP].pri !=
//                options[ChatOption.MAP].pri) {
//                IChatChannel channel = getMapChannel(chatPlayer.getPlayer().getMapId());
//                if(channel!=null){
//                    if(options[ChatOption.MAP].pri==0){
//                        unRegistry(chatPlayer,channel);
//                    }else{
//                        if(options[ChatOption.MAP].pri!=0&&oldOptions[ChatOption.MAP].pri==0)
//                            registry(chatPlayer,channel);
//                    }
//                }
//            }
//            if (oldOptions[ChatOption.FAVORITE].pri !=
//                options[ChatOption.FAVORITE].pri) {
//                IChatChannel channel = getFavoriteChannel(chatPlayer.getFavoriteId());
//                if(channel!=null){
//                    if(options[ChatOption.FAVORITE].pri==0){
//                        unRegistry(chatPlayer,channel);
//                    }else{
//                        if(options[ChatOption.FAVORITE].pri!=0&&oldOptions[ChatOption.FAVORITE].pri==0)
//                            registry(chatPlayer,channel);
//                    }
//                }
//            }
//            chatPlayer.setChatOptions(options);
//        }
//    }
}

class PlayerChatInfo{
    private Player player;
    private Set channels = new HashSet();
    private ChatOption[] chatOptions = new ChatOption[8];
    private int favoriteId;

    public PlayerChatInfo(Player player){
        this.player = player;
        byte[] bytes = player.getChatOptions();
        try {
            if (bytes != null && bytes.length > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bis);
                for (int i = 0; i < 8; i++) {
                    ChatOption option = new ChatOption();
                    option.pri = dis.readByte();
                    option.color = dis.readByte();
                    chatOptions[i] = option;
                }
                favoriteId = dis.readInt();
            }else{
                chatOptions = ChatOption.getDefaltChatOptions();
                favoriteId = -1;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ChatOption[] getChatOptions(){
        return chatOptions;
    }

    public void setChatOptions(ChatOption[] chatOptions){
        this.chatOptions = chatOptions;
    }

    public void addChannel(IChatChannel channel){
        channels.add(channel);
    }

    public void removeChannel(IChatChannel channel){
        channels.remove(channel);
    }

    public Set getChannels(){
        return channels;
    }

    public Player getPlayer(){
        return player;
    }

    public int getFavoriteId(){
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId){
        this.favoriteId = favoriteId;
    }

    public boolean inWorld(){
        return chatOptions[ChatOption.WORLD].pri!=0;
    }

    public boolean inMap(){
        return chatOptions[ChatOption.MAP].pri!=0;
    }

}
class Forbiden{
    public int id;
    public int mask;
    public long validTime;
    public boolean isValid(int channel){
        if(channel==ISendMessage.WORLD){
            return (mask&1)==0;
        }else if(channel==ISendMessage.MAP){
            return (mask&2)==0;
        }else if(channel==ISendMessage.GUILD){
            return (mask&4)==0;
        }else if(channel==ISendMessage.GROUP){
            return (mask&8)==0;
        }else if(channel==ISendMessage.TEAM){
            return (mask&16)==0;
        }else if(channel==ISendMessage.FAVORITE){
            return (mask&32)==0;
        }else if(channel==ISendMessage.SYSTEM){
            return (mask&64)==0;
        }else if(channel==ISendMessage.CAMP){
            return (mask&128)==0;
        }else if(channel>0){
            return (mask&128)==0;
        }
        return false;
    }
}