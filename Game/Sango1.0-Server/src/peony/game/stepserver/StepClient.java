package peony.game.stepserver;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

import peony.common.ClientSessionAsyncCall;
import peony.game.NoInstanceVMapManager;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.net.AbstractClientSession;
import peony.net.ClientSession;
import peony.net.DispatchClientSession;
import peony.net.DispatchPacket;
import peony.service.Service;

public class StepClient implements Service {

	public static String stepServerIp = "221.179.216.58"; //跨服服务器IP地址
	public static int stepServerPortOfPacket = 7002; //跨服服务器监听端口
	
	protected SocketConnector connectorOfPacket;
	protected ConnectFuture futureOfPacket;
	
	/** 允许向跨服服务器发送的协议 */
	public static short[] stepOpCodes = {OpCode.SYNC_TIME_CLIENT,OpCode.MOVE_CLIENT,OpCode.RIDE_CLIENT,
		OpCode.UNRIDE_CLIENT,OpCode.TOUCHNPC_CLIENT,OpCode.LOADING_FINISHED_CLIENT,OpCode.ITEMINFO_CLIENT,
		OpCode.USEITEM_CLIENT,OpCode.BAG_CLIENT,OpCode.SKILL_LIST_CLIENT,
		OpCode.SKILL_REFRESH_CLIENT,OpCode.SKILL_NAMELIST_CLIENT,OpCode.ITEM_DESC_CLIENT,
		OpCode.EQUIP_CLIENT,OpCode.UNEQUIP_CLIENT,OpCode.PROPERTYPOINT_ADD_CLIENT,OpCode.SKILL_DESC_CLIENT,
		OpCode.SKILL_ATTACK_CLIENT,OpCode.UNIT_INFO_CLIENT,OpCode.PLAYER_INFO_CLIENT,
		OpCode.RELIVE_CLIENT,OpCode.CANCEL_AUTOATTACK_CLIENT,OpCode.CANCEL_ATTACK_CLIENT,OpCode.CANCEL_USEITEM_CLIENT,
		OpCode.REPAIR_CLIENT,OpCode.OUT_PRISON_CLIENT,OpCode.GRID_EXCHANGE_CLIENT,OpCode.GET_MOVE_CLIENT,
		OpCode.FINDPATH_CLIENT,OpCode.TITLES_GET_CLIENT,OpCode.TITLE_SET_CLIENT,OpCode.TITLE_REMOVE_CLIENT,
		OpCode.TITLE_LIST_CLIENT,OpCode.TITLE_BUY_CLIENT,OpCode.FORGET_SKILL_CLIENT,OpCode.CONFIG_CLIENT,
		OpCode.CONFIG_SAVE_CLIENT,OpCode.HORSE_EQUIP_CLIENT,OpCode.HORSE_RIDE_CLIENT,OpCode.HORSE_FEED_CLIENT,
		OpCode.HORSE_FOOD_CLIENT,OpCode.HORSE_CHANGE_SKILL_CLIENT,OpCode.HORSE_BAG_CLIENT,OpCode.HORSE_PACK_CLIENT,
		OpCode.HORSE_UNEQU_CLIENT,OpCode.HORSE_UNRIDE_CLIENT,OpCode.GATHER_CANCEL_CLIENT,OpCode.SKILL_REFRESH_MONEY_CLIENT,
		OpCode.BAG_ARRANGE_CLIENT,OpCode.NATION_INFO_CLIENT,OpCode.TELEPORT_CLIENT,OpCode.NPC_DESC_CLIENT,
		OpCode.WORLD_TELEPORT_CLIENT,OpCode.GET_FILE_CLIENT,OpCode.GLOBAL_NPC_LIST_CLIENT,OpCode.GLOBAL_NPC_DESC_CLIENT,
		OpCode.ATTENDANT_BAG_CLIENT,OpCode.ATTENDANT_FOLLOW_CLIENT,OpCode.ATTENDANT_CANCELFOLLOW_CLIENT,
		OpCode.ATTENDANT_EQUIP_CLIENT,OpCode.ATTENDANT_UNEQUIP_CLIENT,OpCode.ATTENDANT_ADDLOYAL_CLIENT,
		OpCode.PLAYER_SET_FIND_PATH_CLIENT,OpCode.START_7_BUFF_DESC_CLIENT,OpCode.EFFECT_JEWEL_GET_CLIENT,
		OpCode.NEW_GETFILE_CLIENT,OpCode.USE_KINGITEM_CLIENT,OpCode.GETFILE_CLIENT,OpCode.SUITE_CLIENT,
		OpCode.DECORATE_GET_CONFIG_CLIENT,OpCode.CHARGE_INFO_CLIENT,OpCode.BUFF_DESC_CLIENT,OpCode.CMCC_ISHOP_LIST_CLIENT,
		OpCode.BUFF_DESC_BYID_CLIENT,OpCode.PHONE_NOTIFY_CLIENT,OpCode.LOADING_FINISHED1_CLIENT,OpCode.MAP_NPC_CLINT};
	
	/** 客户端掉线 */
	public static int DISCONNECTED = 1;
	/** 退出战场传送位置 */
	public static int[][] outs = {{272,486,752},{240,583,673},{352,507,695}};
	
	/**16强和争霸赛退出战场的传送位置*/
//	public static int[][] outs_16 = {{272,540,712},{240,635,646},{352,458,717}};
	public static int[][] outs_16 = {{272,519,690},{240,734,481},{352,712,522}};
	
	public Map<Integer, DispatchPacket> uiPackets = new HashMap<Integer, DispatchPacket>(); //重复报名协议缓存
	
	public static boolean useNewStepClientCall = false;
	public static String stepClientCallName = null;
	
	public void startup() throws Exception {
		connectorOfPacket = new SocketConnector();
		SocketConnectorConfig cfg = new SocketConnectorConfig();
		cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(StepClientEncoder.class, StepClientDecoder.class));
		connectorOfPacket.setDefaultConfig(cfg);
		futureOfPacket = connectorOfPacket.connect(new InetSocketAddress(stepServerIp, stepServerPortOfPacket), new ClientHandler());
	}
	//向stepserver发送消息包
	public void send(Object o, int accountId, int playerId, ClientSession clientSession){
		try {
			futureOfPacket.getSession().write(o);
		} catch (Exception e) {
			futureOfPacket = connectorOfPacket.connect(new InetSocketAddress(stepServerIp, stepServerPortOfPacket), new ClientHandler());
			throw new RuntimeException("网络中断,请稍后再试");
		}
		
		//这个session是本地服务器的session，方便战场结束找回玩家自己的session
		Server.server.getServiceRegistry().getStepSessionService().addSession(accountId, playerId, ((AbstractClientSession)clientSession).session);
		Server.server.getServiceRegistry().getStepSessionService().addDispatchSession(accountId, playerId, (DispatchClientSession)clientSession);
	}
	
	public void shutdown() {
		
	}

	class ClientHandler extends IoHandlerAdapter {

		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			super.exceptionCaught(session, cause);
		}
		//收到stepserver信息包，并且处理后发给client
		public void messageReceived(IoSession session, Object message) throws Exception {
			if(message instanceof DispatchPacket){//如果是分配器类型的信息包（会发送给client报名成功,失败,奖励等消息)
				DispatchPacket dp = (DispatchPacket)message;
				int accountId = dp.accountId;
				int playerId = dp.playerId;
				StepSessionService service = Server.server.getServiceRegistry().getStepSessionService();
				IoSession ioSession = service.getSession(accountId, playerId);
				DispatchClientSession disSession = service.getDispatchClientSession(accountId, playerId);
				if(ioSession!=null && dp.packet.getOpCode()!=OpCode.OPENUI_SERVER && dp.packet.getOpCode()!=OpCode.STEPBATTLE_FINALS_BETANDWATCH_SERVER){
					ioSession.write(message);
				}
				if(useNewStepClientCall){
					Class<ClientSessionAsyncCall> clazz = (Class<ClientSessionAsyncCall>) Class.forName(stepClientCallName);
					ClientSessionAsyncCall call = clazz.getConstructor(ClientSession.class, DispatchClientSession.class, DispatchPacket.class).newInstance(null, disSession, dp);
					Server.server.getWorld().schedule(call);
				}else{
					Server.server.getWorld().schedule(new StepClientPacketCall(null, disSession, dp));
				}
			}else if(message instanceof Player){//如果是玩家的信息包(会发找到对应session，原封发送给client
				Player player = (Player)message;
				int accountId = player.accountId;
				StepSessionService service = Server.server.getServiceRegistry().getStepSessionService();
				IoSession ioSession = service.getSession(accountId, (int) ((DispatchClientSession)player.session).id);
				if(ioSession!=null)
					ioSession.write(message);
			}
		}
		
		public void messageSent(IoSession session, Object message) throws Exception {
			super.messageSent(session, message);
		}

		public void sessionClosed(IoSession session) throws Exception {
			for(DispatchClientSession dSession : Server.server.getServiceRegistry().getStepSessionService().dispatchSessions.values()){
				if(dSession!=null){
//					dSession.close();
					try {
						Player player = (Player)dSession.getClient();
						if(player!=null && player.isInStep){
							player.isInStep = false;
							int[] out = outs[player.faction-1];
							//当为常规跨服战和争霸战时的传出位置（有可能不一样）
							if(player.stepType==StepServer.STEPBATTLE_TYPE_16||player.stepType==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
								out=outs_16[player.faction-1];
							}
							VMap map = ((NoInstanceVMapManager)Server.server.getWorld().getVMapManager(out[0])).getVMaps(out[0])[0];
							player.goMap(out[0], out[1], out[2]);
							player.addToMap(map, out[1], out[2]);
							player.loadFinished();
							player.refreshProperties(false);
						}
					} catch (Exception e) {
					}
				}
			}
			Server.server.getServiceRegistry().getStepSessionService().clearAllCache();
			super.sessionClosed(session);
			futureOfPacket = connectorOfPacket.connect(new InetSocketAddress(stepServerIp, stepServerPortOfPacket), new ClientHandler());
		}

		public void sessionCreated(IoSession session) throws Exception {
			super.sessionCreated(session);
		}

		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			super.sessionIdle(session, status);
		}

		public void sessionOpened(IoSession session) throws Exception {
			super.sessionOpened(session);
		}

	}
	
	/** 是否允许向跨服服务器发送此协议 */
	public static boolean canSend(int opCode){
		for(int code : stepOpCodes){
			if(code==opCode)
				return true;
		}
		return false;
	}

}
