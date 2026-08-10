package peony.game.chat;

import java.text.MessageFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;

import peony.channel.Channel;
import peony.channel.ChannelFilter;
import peony.channel.ChannelService;
import peony.game.Actor;
import peony.game.Admin;
import peony.game.ChatOption;
import peony.game.ChatOptions;
import peony.game.Client;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.nation.Nation;
import peony.game.nation.NationService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.PlayerRelation;
import peony.service.tong.Tong;
import peony.util.StringUtil;
import ch.javasoft.util.intcoll.IntHashMap;

public class ChatService implements Service, ServiceEventListener, Runnable {

	private static final Logger log = Logger.getLogger(ChatService.class);

	protected static final String CHAT_CHANNEL_WORLD = "chat_world";
	protected static final String CHAT_CHANNEL_WEI = "chat_wei";
	protected static final String CHAT_CHANNEL_SHU = "chat_shu";
	protected static final String CHAT_CHANNEL_WU = "chat_wu";
	protected static final String CHAT_CHANNEL_GUILD = "chat_guild";
	protected static final String CHAT_CHANNEL_PARTY = "chat_party";
	protected static final String CHAT_CHANNEL_AREA = "chat_area";
	protected static final String CHAT_CHANNEL_SYSTEM = "chat_system";
	protected static final String CHAT_CHANNEL_NATIVE = "chat_native";

	protected Channel systemChannel;
	protected Channel weiChannel;
	protected Channel shuChannel;
	protected Channel wuChannel;
	protected Channel worldChannel;
	protected BlockingQueue<ChatMessage> messages = new LinkedBlockingQueue<ChatMessage>();
	protected IntHashMap<ChatForbid> forbids = new IntHashMap<ChatForbid>();

	protected ChannelFilter filter = new BlackListChannelFilter();
	protected ChannelFilter worldShoutFilter = new WorldShoutFilter();
	
	protected static Pattern urlPattern = Pattern.compile("(\\w+\\.)+[a-zA-Z]+");
	protected static String sgurl = "sg.pipgame.cn";

	protected static final String[] NATIVES = { "遼宁", "吉林", "黑龍江", "北京", "天津",
			"河北", "山西", "內蒙古", "湖北", "湖南", "河南", "江西", "上海", "江蘇", "浙江", "山東",
			"安徽", "廣東", "廣西", "海南", "福建", "四川", "重慶", "貴州", "云南", "西藏", "陝西",
			"甘肅", "新疆", "青海", "宁夏", "香港", "澳門", "台灣",

	};
	
	protected BlockingQueue<ChatMessage> worldShoutQueue = new LinkedBlockingQueue<ChatMessage>();
	
	// 特殊地图channel
	protected static int[] specialMap = new int[]{785, 737, 801};

	public ChatService() {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		worldChannel = channelService.createChannel(CHAT_CHANNEL_WORLD, false);
		weiChannel = channelService.createChannel(CHAT_CHANNEL_WEI, false);
		shuChannel = channelService.createChannel(CHAT_CHANNEL_SHU, false);
		wuChannel = channelService.createChannel(CHAT_CHANNEL_WU, false);
		systemChannel = channelService
				.createChannel(CHAT_CHANNEL_SYSTEM, false);
		for (int i = 0; i < NATIVES.length; i++) {
			channelService.createChannel(CHAT_CHANNEL_NATIVE + NATIVES[i],
					false);
		}
	}

	public void join(int channel, int targetId, String nativeString,
			ClientSession session) {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (channel == ChatOption.SYSTEM) {
			channelService.addSessionToChannel(CHAT_CHANNEL_SYSTEM, session);
		} else if (channel == ChatOption.WORLD) {
			channelService.addSessionToChannel(CHAT_CHANNEL_WORLD, session);
		} else if (channel == ChatOption.FACTION) {
			if (targetId == 1) {
				channelService.addSessionToChannel(CHAT_CHANNEL_WEI, session);
			} else if (targetId == 2) {
				channelService.addSessionToChannel(CHAT_CHANNEL_SHU, session);
			} else if (targetId == 3) {
				channelService.addSessionToChannel(CHAT_CHANNEL_WU, session);
			}
		} else if (channel == ChatOption.GUILD) {
			channelService.addSessionToChannel(CHAT_CHANNEL_GUILD + targetId,
					session);
		} else if (channel == ChatOption.NATIVE) {
			channelService.addSessionToChannel(CHAT_CHANNEL_NATIVE
					+ nativeString, session);
		} else if (channel == ChatOption.AREA) {
			channelService.addSessionToChannel(CHAT_CHANNEL_AREA + targetId,
					session);
		}
	}

	public void startup() {
		Server.server.getEventManager().registerListener(this);
		new Thread(this, "ChatService").start();
		new Thread(new CheckWorldShoutProcess()).start();
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	/**
	 * 返回此监听者关心的事件类型数组。
	 */
	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_MAP_ADDED,
				ServiceEvent.EVENT_MAP_REMOVED,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_MAP_PLAYER_LOADED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
				ServiceEvent.EVENT_PLAYER_CREATED,
				ServiceEvent.EVENT_PLAYER_LOADED,
				ServiceEvent.EVENT_PLAYER_LOGINED,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_TONG_LOADED,
				ServiceEvent.EVENT_PLAYER_CHANGETONG,
				ServiceEvent.EVENT_PLAYER_LEAVETONG,
				ServiceEvent.EVENT_PLAYER_CHANGE_FACTION };
	}

	/**
	 * 处理服务事件。
	 * 
	 * @param event
	 */
	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_MAP_ADDED:
			mapAdded((VMap) event.param1);
			break;
		case ServiceEvent.EVENT_MAP_REMOVED:
			mapRemoved((VMap) event.param1);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			mapPlayerAdded((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			mapPlayerLoadingFinish((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			mapPlayerRemoved((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_CREATED:
			playerCreated((Player) event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOADED:
			playerLoaded((Player) event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			playerLogined((Player) event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player) event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			playerFirstLoad((Player) event.param1);
			break;
		case ServiceEvent.EVENT_TONG_LOADED:
			tongLoaded((Tong) event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_CHANGETONG:
			changeTong((Actor) event.param1, (Tong) event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LEAVETONG:
			leaveTong((Actor) event.param1, (Tong) event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_CHANGE_FACTION:
			changeFaction((Player) event.param1, (Integer) event.param2);
			break;
		}
	}

	protected void changeFaction(Player p, Integer oldFaction) {
		String oldChannelName = null;
		if (oldFaction == GameObject.FACTION_WEI)
			oldChannelName = CHAT_CHANNEL_WEI;
		else if (oldFaction == GameObject.FACTION_SHU)
			oldChannelName = CHAT_CHANNEL_SHU;
		else if (oldFaction == GameObject.FACTION_WU)
			oldChannelName = CHAT_CHANNEL_WU;
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		channelService.removeSessionFromChannel(oldChannelName, p.session);
		String newChannelName = null;
		if (p.faction == GameObject.FACTION_WEI)
			newChannelName = CHAT_CHANNEL_WEI;
		else if (p.faction == GameObject.FACTION_SHU)
			newChannelName = CHAT_CHANNEL_SHU;
		else if (p.faction == GameObject.FACTION_WU)
			newChannelName = CHAT_CHANNEL_WU;
		channelService.addSessionToChannel(newChannelName, p.session);
	}

	protected void changeTong(Actor actor, Tong t) {
		Player p = ObjectAccessor.getPlayer(actor.id);
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (p != null && p.session != null
				&& p.chatOptions.options[ChatOption.GUILD].inChannel) {
			channelService.addSessionToChannel(CHAT_CHANNEL_GUILD + t.id,
					p.session);
		}
	}

	public void leaveTong(Actor actor, Tong t) {
		Player p = ObjectAccessor.getPlayer(actor.id);
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (p != null && p.session != null
				&& p.chatOptions.options[ChatOption.GUILD].inChannel) {
			channelService.removeSessionFromChannel(CHAT_CHANNEL_GUILD + t.id,
					p.session);
		}
	}

	public void tongLoaded(Tong tong) {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		channelService.createChannel(CHAT_CHANNEL_GUILD + tong.id, false);
	}

	public void mapAdded(VMap map) {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if(isSpecialMap(map.getId()) && map.instance!=null){
			channelService.createChannel(CHAT_CHANNEL_AREA + map.getId() + map.instance.getId(), false);
		}else{
			channelService.createChannel(CHAT_CHANNEL_AREA + map.getId(), false);
		}
	}

	public void mapRemoved(VMap map) {

	}

	public void playerCreated(Player player) {

	}

	public void playerFirstLoad(Player player) {
		// log.debug("add to chatService:" + player.name);
		ChatOptions options = player.chatOptions;
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		channelService.addSessionToChannel(CHAT_CHANNEL_SYSTEM, player.session);
		if (options.options[ChatOption.WORLD].inChannel) {
			channelService.addSessionToChannel(CHAT_CHANNEL_WORLD,
					player.session);
		}
		if (options.options[ChatOption.FACTION].inChannel) {
			if (player.faction == GameObject.FACTION_WEI) {
				channelService.addSessionToChannel(CHAT_CHANNEL_WEI,
						player.session);
			} else if (player.faction == GameObject.FACTION_SHU) {
				channelService.addSessionToChannel(CHAT_CHANNEL_SHU,
						player.session);
			} else if (player.faction == GameObject.FACTION_WU) {
				channelService.addSessionToChannel(CHAT_CHANNEL_WU,
						player.session);
			}
		}
		if (options.options[ChatOption.NATIVE].inChannel
				&& player.chatOptions.nativeName.length() > 0) {
			channelService.addSessionToChannel(CHAT_CHANNEL_NATIVE
					+ player.chatOptions.nativeName, player.session);
		}
		if (options.options[ChatOption.GUILD].inChannel) {
			Tong tong = Server.server.getServiceRegistry().getTongService()
					.getPlayerTong(player.id);
			if (tong != null) {
				channelService.addSessionToChannel(
						CHAT_CHANNEL_GUILD + tong.id, player.session);
			}
		}
	}

	public void playerLoaded(Player player) {

	}

	public void playerLogined(Player player) {
	}

	public void playerLogouted(Player player) {
		ChatOptions options = player.chatOptions;
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		channelService.removeSessionFromChannel(CHAT_CHANNEL_SYSTEM,
				player.session);
		if (options.options[ChatOption.WORLD].inChannel) {
			channelService.removeSessionFromChannel(CHAT_CHANNEL_WORLD,
					player.session);
		}
		if (options.options[ChatOption.FACTION].inChannel) {
			if (player.faction == GameObject.FACTION_WEI) {
				channelService.removeSessionFromChannel(CHAT_CHANNEL_WEI,
						player.session);
			} else if (player.faction == GameObject.FACTION_SHU) {
				channelService.removeSessionFromChannel(CHAT_CHANNEL_SHU,
						player.session);
			} else if (player.faction == GameObject.FACTION_WU) {
				channelService.removeSessionFromChannel(CHAT_CHANNEL_WU,
						player.session);
			}
		}
		if (options.options[ChatOption.NATIVE].inChannel) {
			channelService.removeSessionFromChannel(CHAT_CHANNEL_NATIVE
					+ player.chatOptions.nativeName, player.session);
		}
	}

	public void mapPlayerAdded(VMap map, Player player) {

	}

	public void mapPlayerLoadingFinish(VMap map, Player player) {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (player.chatOptions.options[ChatOption.AREA].inChannel) {
			if(isSpecialMap(map.getId()) && map.instance!=null){
				channelService.addSessionToChannel(CHAT_CHANNEL_AREA + map.getId() + map.instance.getId(),
						player.session);
			}else{
				channelService.addSessionToChannel(CHAT_CHANNEL_AREA + map.getId(),
						player.session);
			}
		}
	}

	public void mapPlayerRemoved(VMap map, Player player) {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (player.chatOptions.options[ChatOption.AREA].inChannel) {
			if(isSpecialMap(map.getId()) && map.instance!=null){
				channelService.removeSessionFromChannel(CHAT_CHANNEL_AREA
						+ map.getId() + map.instance.getId(), player.session);
			}else{
				channelService.removeSessionFromChannel(CHAT_CHANNEL_AREA
						+ map.getId(), player.session);
			}
		}
	}

	public void addChatMessage(ChatMessage cm) {
		messages.add(cm);
	}

	public void run() {
		while (true) {
			try {
				ChatMessage message = messages.take();
				send(message);
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}

	protected void send(ChatMessage cm) {
		if (!checkForbid(cm))
			return;
		if (cm.sourceId > 0) {
			String s = StringUtil.filterBadWords(cm.getMessage());
			cm.message = s.replaceAll("[\r\n]", "");
//			cm.message = s.replace('\n', ' ');
			cm.message = replaceUrl(cm.message);
		}
		switch (cm.channel) {
		case ChatOption.SYSTEM:
			systemChannel.broadcast(cm.getPacket(), null);
			break;
		case ChatOption.WORLD:
			worldChannel.broadcast(cm, filter);
			break;
		case ChatOption.AREA: {
			ChannelService channelService = Server.server.getServiceRegistry()
					.getChannelService();
			Channel ch = channelService.getChannel(CHAT_CHANNEL_AREA
					+ cm.destId);
			if (ch != null)
				ch.broadcast(cm, filter);
		}
			break;
		case ChatOption.FACTION: {
			Channel ch = null;
			if (cm.destId == GameObject.FACTION_WEI) {
				ch = weiChannel;
			} else if (cm.destId == GameObject.FACTION_SHU) {
				ch = shuChannel;
			} else if (cm.destId == GameObject.FACTION_WU) {
				ch = wuChannel;
			}
			if (ch != null)
				ch.broadcast(cm, filter);
		}
			break;
		case ChatOption.GUILD: {
			Tong tong = Server.server.getServiceRegistry().getTongService()
					.getTong(cm.destId);
			if (tong != null) {
				ChannelService channelService = Server.server
						.getServiceRegistry().getChannelService();
				Channel ch = channelService.getChannel(CHAT_CHANNEL_GUILD
						+ tong.id);
				if (ch != null)
					ch.broadcast(cm, filter);
			}
		}
			break;
		case ChatOption.PRIVATE: {
			if (cm.destId > 0) { // 如果是一般的玩家
				Player p = ObjectAccessor.getPlayer(cm.destId);
				if (p != null && p.session != null) {
					// Packet pt = getPacket(cm);
					filter.filter(p.session, cm, null);
					if (cm.sourceId > 0) {
						Player source = ObjectAccessor.getPlayer(cm.sourceId);
						if (source != null) {
							source.send(cm.getPacket());
						}
						
						// 发送聊天通知消息，更新临时关系表
						Server.server.getEventManager().addEvent(
								new ServiceEvent(ServiceEvent.EVENT_INTERACT, source,
										p, PlayerRelation.INTERACT_CHAT));
					}
				} else {
					if (cm.sourceId > 0) {
						Player source = ObjectAccessor.getPlayer(cm.sourceId);
						if (source != null) {
							source.send(new ChatMessage(ChatOption.PRIVATE, -1,
									-1, "系統", "對方不在線", null).getPacket());
						}
					}
				}
			} else {
				if (cm.destId != -1) {// 如果是Admin
					Admin admin = Server.server.getServiceRegistry()
							.getAdminService().getAdmin(-cm.destId);
					if (admin != null) {
						Packet pt = cm.getPacket();
						pt.putString("private");
						admin.send(pt);
					}
				}
			}
		}
			break;
		case ChatOption.NATIVE: {
			ChannelService channelService = Server.server.getServiceRegistry()
					.getChannelService();
			if (cm.destName != null && cm.destName.length() > 0) {
				Channel ch = channelService.getChannel(CHAT_CHANNEL_NATIVE
						+ cm.destName);
				if (ch != null)
					ch.broadcast(cm, filter);
			}
		}
			break;
		case ChatOption.PARTY: {
			if (cm.sessions != null && cm.sessions.length > 0) {
				for (ClientSession session : cm.sessions) {
					if (session != null)
						filter.filter(session, cm, null);
				}
			}
		}
			break;
		case ChatOption.FACTION_SHOUT: {
			Channel ch = null;
			if (cm.destId == GameObject.FACTION_WEI) {
				ch = weiChannel;
			} else if (cm.destId == GameObject.FACTION_SHU) {
				ch = shuChannel;
			} else if (cm.destId == GameObject.FACTION_WU) {
				ch = wuChannel;
			}
			if (ch != null)
				ch.broadcast(cm.getShoutPacket());
		}
			break;
		case ChatOption.WORLD_SHOUT: {
			Channel ch = worldChannel;
			if (ch != null)
				ch.broadcast(cm.getShoutPacket(), worldShoutFilter);
		}
		case ChatOption.PRIVATE_SHOUT:
			if (cm.destId > 0) {
				Player p = ObjectAccessor.getPlayer(cm.destId);
				if (p != null && p.session != null) {
					p.send(cm.getShoutPacket());
				}
			}
		break;
		}
	}
	
	protected static String replaceUrl(String s){
		return urlPattern.matcher(s).replaceAll(sgurl);
	}

	public void sendPrivateShout(int destId, int color, int duration, int faction, String message){
		ChatMessage cm = new ChatMessage(ChatOption.PRIVATE_SHOUT, destId, faction, "", message, null);
		cm.destId = destId;
		cm.shoutColor = color;
		cm.shoutDuration = duration;
		addChatMessage(cm);
	}
	
	public void sendSystemMessage(int ch, String name, String message) {
		ChatMessage cm = new ChatMessage(ch, -1, -1, "系統", message, null);
		addChatMessage(cm);
	}
	
	public void sendPrivateMessage(int destId,String message){
		ChatMessage cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, "系統",
				destId, message, null);
		addChatMessage(cm);
	}
	
	public void sendFactionSystemMessage(int faction, String message) {
		ChatMessage cm = new ChatMessage(ChatOption.FACTION, -1, faction, "系統", message, null);
		cm.destId = faction;
		addChatMessage(cm);
	}
	
	public void sendAreaSystemMessage(String message,int destId){
		ChatMessage cm = new ChatMessage(ChatOption.AREA,-1,-1,"系統",message,null);
		cm.destId = destId;
		addChatMessage(cm);
	}
	
	public void sendFactionShout(int faction,String message,int color,int duration){
		ChatMessage cm = new ChatMessage(ChatOption.FACTION_SHOUT, -1, faction, "系統", message, null);
		cm.destId = faction;
		cm.shoutColor = color;
		cm.shoutDuration = duration;
		addChatMessage(cm);
	}
	
	public void sendWorldShout(String name,int sourceId,int faction,String message,int color,int duration){
		ChatMessage cm = new ChatMessage(ChatOption.WORLD_SHOUT, sourceId, faction, "", message, null);
		cm.sourceName = name;
		cm.destId = faction;
		cm.shoutColor = color;
		cm.shoutDuration = duration;
		worldShoutQueue.add(cm);
	}
	
	public void sendFactionSystemMessage(int faction, String name, String message){
		ChatMessage cm = new ChatMessage(ChatOption.FACTION, -1, faction, name, message, null);
		cm.destId = faction;
		addChatMessage(cm);
	}
	
	public void sendWorldMessage(String message) {
		ChatMessage cm = new ChatMessage(ChatOption.WORLD,-1,-1,"系統",message,null);
		addChatMessage(cm);
	}
	
	public void sendGuildSystemMessage(String message, int destId){
		ChatMessage cm = new ChatMessage(ChatOption.GUILD,-1,-1,"系統",message,null);
		cm.destId = destId;
		addChatMessage(cm);
	}

	public void changeOption(int ch, boolean oldInChannel,
			boolean newInChannel, Player player) {
		String channelName = null;
		if (ch == ChatOption.WORLD) {
			channelName = CHAT_CHANNEL_WORLD;
		} else if (ch == ChatOption.FACTION) {
			if (player.faction == GameObject.FACTION_WEI) {
				channelName = CHAT_CHANNEL_WEI;
			} else if (player.faction == GameObject.FACTION_SHU) {
				channelName = CHAT_CHANNEL_SHU;
			} else if (player.faction == GameObject.FACTION_WU) {
				channelName = CHAT_CHANNEL_WU;
			}
		} else if (ch == ChatOption.AREA) {
			if(player.map.map.instance!=null){
				channelName = CHAT_CHANNEL_AREA + player.map.id + player.map.map.instance.getId();
			}else{
				channelName = CHAT_CHANNEL_AREA + player.map.id;
			}
		} else if (ch == ChatOption.NATIVE) {
			if (player.chatOptions.nativeName.length() > 0) {
				channelName = CHAT_CHANNEL_NATIVE
						+ player.chatOptions.nativeName;
			}
		}
		if (ch == ChatOption.GUILD) {
			Tong tong = Server.server.getServiceRegistry().getTongService()
					.getPlayerTong(player.id);

			if (tong != null) {
				channelName = CHAT_CHANNEL_GUILD+tong.id;
			}
		}
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (channelName != null) {
			if (oldInChannel) {
				channelService.removeSessionFromChannel(channelName,
						player.session);
			}
			if (newInChannel) {
				channelService.addSessionToChannel(channelName, player.session);
			}
		}
	}

	public void nativeChange(String oldNative, String newNative, Player player) {
		ChannelService channelService = Server.server.getServiceRegistry()
				.getChannelService();
		if (player.chatOptions.options[ChatOption.NATIVE].inChannel) {
			if (oldNative.length() > 0) {
				channelService.removeSessionFromChannel(CHAT_CHANNEL_NATIVE
						+ oldNative, player.session);
			}
			if (newNative.length() > 0) {
				channelService.addSessionToChannel(CHAT_CHANNEL_NATIVE
						+ newNative, player.session);
			}
		}
	}

	// protected Packet getPacket(ChatMessage msg) {
	// Packet pt = new Packet(OpCode.CHAT_SERVER);
	// pt.put(msg.channel);
	// pt.putInt(msg.sourceId);
	// pt.putString(msg.sourceName);
	// pt.putString(msg.message);
	// pt.put(msg.getBytes());
	// return pt;
	// }

	public void forbid(int playerId, int channel, long time) {
		if (time == 0 || time < System.currentTimeMillis()) {
			forbids.remove(playerId);
			return;
		}
		ChatForbid forbid = forbids.get(playerId);
		if (forbid == null) {
			forbid = new ChatForbid(playerId);
			forbids.put(playerId, forbid);
		}
		forbid.flag |= channel;
		forbid.time = time;
	}
	
	public boolean checkForbid(ChatMessage cm) {
		ChatForbid cf = forbids.get(cm.sourceId);
		if (cf == null)
			return true;
		if (cf.time < System.currentTimeMillis())
			return true;
		switch (cm.channel) {
		case ChatOption.WORLD:
			return (cf.flag & ChatForbid.FORBID_WORLD) == 0;
		case ChatOption.FACTION:
			return (cf.flag & ChatForbid.FORBID_FACTION) == 0;
		case ChatOption.AREA:
			return (cf.flag & ChatForbid.FORBID_AREA) == 0;
		case ChatOption.NATIVE:
			return (cf.flag & ChatForbid.FORBID_NATIVE) == 0;
		case ChatOption.PRIVATE:
			return (cf.flag & ChatForbid.FORBID_PRIVATE) == 0;
		}
		return true;
	}
	
	public static boolean isSpecialMap(int mapId){
		for(int id : specialMap){
			if(id==mapId)
				return true;
		}
		return false;
	}
	
	class CheckWorldShoutProcess implements Runnable{

		public void run() {
			while(true){
				try {
					ChatMessage cm = worldShoutQueue.take();
					String mess = cm.message;
					cm.message = MessageFormat.format("{0}:{1}", cm.sourceName,mess);
					addChatMessage(cm);
					ChatMessage cm1 = new ChatMessage(ChatOption.WORLD,cm.sourceId,cm.faction,cm.sourceName,mess,null);
					addChatMessage(cm1);
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}

class BlackListChannelFilter implements ChannelFilter {
	public void filter(ClientSession session, Object object, Channel channel) {
		ChatMessage cm = (ChatMessage) object;
		Client client = session.getClient();
		if (client != null) {
			if (client instanceof Player) {
				if (cm.sourceId < 0) {
					session.send(cm.getPacket());
					return;
				}
				Player p = (Player) client;
				ChatOption option = p.chatOptions.options[cm.channel];
				if (option.inChannel) {
					PlayerRelation r = Server.server.getServiceRegistry()
							.getRelationService().get(p.id);
					if (r != null) {
						if (r.blackList.exists(cm.sourceId))
							return;
					}
					if (cm.faction == -1 || p.faction == cm.faction || cm.channel==ChatOption.WORLD)//世界聊因为花钱了，所以都能看见
						session.send(cm.getPacket());
					else {
						if(cm.channel==ChatOption.PRIVATE&&p.faction!=cm.faction){
							NationService nationService = Server.server.getServiceRegistry().getNationService();
							Nation sourceNation = nationService.getNationByFaction(cm.faction);
							if(sourceNation.getKingId()==cm.sourceId){ //如果是两个国王之间的私聊就可以发送
								 Nation destNation = nationService.getNationByFaction(p.faction);
								 if(destNation.getKingId()==p.id){
									 session.send(cm.getPacket());
								 }
							}
						}
						if (cm.maskMessage != null
								&& cm.maskMessage.length() > 0)
							session.send(cm.getMaskPacket());
					}
				}
			} else if (client instanceof Admin) {
				Packet packet = cm.getPacket();
				Packet p = new Packet(packet.getOpCode());
				ByteBuffer buf = packet.getData();
				int n = buf.position();
				byte bbuf[] = new byte[n];
				buf.position(0);
				buf.get(bbuf);
				buf.position(n);
				p.getData().put(bbuf);
				p.putString(channel.getName());
				session.send(p);
			}
		}
		// ByteBuffer buff = packet.getData();
		// byte ch = buff.get(0);
		// int sourceId = buff.getInt(1);
		//
		// Client client = session.getClient();
		// if (client != null) {
		// if (client instanceof Player) {
		// if (sourceId < 0) {
		// session.send(packet);
		// return;
		// }
		// Player p = (Player) client;
		// // if (p.id == sourceId)
		// // return;
		// ChatOption option = p.chatOptions.options[ch];
		// if (option.inChannel) {
		// PlayerRelation r = Server.server.getServiceRegistry()
		// .getRelationService().get(p.id);
		// if (r != null) {
		// if (r.blackList.exists(sourceId))
		// return;
		// }
		// session.send(packet);
		// }
		// } else if (client instanceof Admin) {
		// Packet p = new Packet(packet.getOpCode());
		// ByteBuffer buf = packet.getData();
		// int n = buf.position();
		// byte bbuf[] = new byte[n];
		// buf.position(0);
		// buf.get(bbuf);
		// buf.position(n);
		// p.getData().put(bbuf);
		// p.putString(channel.getName());
		// session.send(p);
		// }
		// }
	}

}

class ChatForbid {
	public int playerId;
	public int flag;
	public long time;

	public static final int FORBID_WORLD = 1;
	public static final int FORBID_FACTION = 1 << 1;
	public static final int FORBID_AREA = 1 << 2;
	public static final int FORBID_NATIVE = 1 << 3;
	public static final int FORBID_PRIVATE = 1 << 4;

	public ChatForbid(int playerId) {
		this.playerId = playerId;
	}
}
