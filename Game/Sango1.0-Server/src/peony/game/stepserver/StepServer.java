package peony.game.stepserver;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import peony.game.Player;
import peony.game.Server;
import peony.net.DispatchClientSession;
import peony.net.DispatchClientSessionService;
import peony.net.DispatchPacket;
import peony.net.SyncInteger;
import peony.service.Service;

public class StepServer implements Service {

	public static int localPortOfPacket = 7002; //跨服服务器监听端口
	
	/** 报名失败 */
	public static int PACKET_SIGN_0 = 0;
	/** 报名成功 */
	public static int PACKET_SIGN_1 = 1;
	/** 报名时间不到 */
	public static int PACKET_CAUSE_SIGN_1 = 1;
	/** 已经报过名了 */
	public static int PACKET_CAUSE_SIGN_2 = 2;
	/** 级别不足报名 */
	public static int PACKET_CAUSE_SIGN_3 = 3;
	/** 今天已经报过名了 */
	public static int PACKET_CAUSE_SIGN_4 = 4;
	
	/**今天报名越过3次*/
	public static int PACKET_CAUSE_SING_OVER3TIMES=5;
	
	/**不是常规赛周期的提示*/
	public static int PACKET_CAUSE_SING_NOTTOP16=6;
	
	/**不是争霸赛玩家不能报名提示*/
	public static int PACKET_CAUSE_SING_NOFINALS16=7;
	
	
	/** 跨服战场结束 */
	public static int TYPE_BATTLE_END = 1;
	/** 跨服战场开始 */
	public static int TYPE_BATTLE_START = 2;
	/** 跨服战场包格改变 */
	public static int TYPE_BATTLE_BAGCHANGE = 3;
	/** 跨服战场血量改变 */
	public static int TYPE_BATTLE_HP = 4;
	/** 跨服战场精力改变 */
	public static int TYPE_BATTLE_MP = 5;
	/** 跨服战场可以报名了 */
	public static int TYPE_BATTLE_CANSIGN = 6;
	
	/**跨服发送奖励*/
	public static int TYPE_BATTLE_SENDGIFT = 7;
	/**1v1跨服可以报名*/
	public static int TYPE_BATTLE_1V1_CANSIGN=8;
	
	/**争霸战结束时发送奖励（按押注百分比发金钱奖励）
	 * sourcePlayerId 		int			
	 * sourcePlayerGameCode String
	 * sourcePlayerBetCoins int			本金
	 * sorucePlayerWinCoins int			赢得金币
	 * 
	 * */
	public static int TYPE_FINALBATTLE_SENDREWARD=9;
	
	/**通知所有争霸赛玩家报名*/
	public static int TYPE_NOTIFYFINALPLAYERS=10;
	
	/**发送称号*/
	public static int TYPE_SENDTITLE=11;
	
	/**普通跨服战*/
	public static final int STEPBATTLE_TYPE_NORMAL=0;
	/**常规跨服战*/
	public static final int STEPBATTLE_TYPE_16=1;
	/**16强争霸赛*/
	public static final int STEPBATTLE_TYPE_TOURNAMENT=2;
	
	/**金钱不够*/
	public static final int STEPBATTLE_BETANDWATCH_LESSMONEY=0;
	/**金钱扣除成功*/
	public static final int STEPBATTLE_BETANDWATCH_DECMONEYOK=1;
	/**金钱不能超过100万*/
	public static final int STEPBATTLE_BETANDWATCH_MORETHEN100WAN=2;
	/**可以预约观战*/
	public static final int STEPBATTLE_BETANDWATCH_CANWATCH=3;
	/**预约观战对象是否更换*/
	public static final int STEPBATTLE_BETANDWATCH_WATCHCHANGE=4;
	/**已经预约，无需再次预约*/
	public static final int STEPBATTLE_BETANDWATCH_HADWATCH=5;
	
	/**不在押注时间内*/
	public static final int STEPBATTLE_BET_CANNOTBET=6;
	
	public static int[] canUseItem = {8,618,619,637,638,645,620,621,622,639,640,
		650,651,652,653,654,655,656,657,658,659,660};
	
	public List<IoSession> sessions = new ArrayList<IoSession>();
	
	public void startup() throws Exception {
		IoAcceptor acceptor = new SocketAcceptor();
		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(StepServerEncoder.class, StepServerDecoder.class));
		acceptor.bind(new InetSocketAddress(localPortOfPacket), new StepServerHandler(), cfg);
	}
	
	public void shutdown() {
		
	}
	
	public static boolean canUse(int itemId){
		for(int id : canUseItem){
			if(itemId==id)
				return true;
		}
		return false;
	}
	
	class StepServerHandler extends IoHandlerAdapter {
		
		public SyncInteger ids = new SyncInteger(0);
		
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			
		}

		public void messageReceived(IoSession session, Object message)
				throws Exception {
			if(message instanceof DispatchPacket){//收到stepclient消息如果是分配器消息包，处理后返回给stepclient，如：是否在报名时间内，报名失败，请求排行榜等
				DispatchPacket dPacket = (DispatchPacket)message;
				int accountId = dPacket.accountId;
				int playerId = dPacket.playerId;
				int sessionId = dPacket.id;
				Server.server.getWorld().schedule(new StepBattlePacketCall(null,
						accountId, playerId, sessionId, session, dPacket));
			}else if(message instanceof Player){//如果收到stepclient消息如果是player消息,????
				Player player = (Player)message;
				if(player!=null){
					Server.server.getWorld().schedule(new StepBattelPacketPlayerCall(player.session, player, session));
				}
			}
		}
		
		public void messageSent(IoSession session, Object message) throws Exception {
			
		}

		public void sessionClosed(IoSession session) throws Exception {
			DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
			List<Long> ids = new ArrayList<Long>();
			for(DispatchClientSession dSession : dispatchClientSessionService.sessions.values()){
				if(dSession!=null && dSession.session==session){
					Player player = (Player)dSession.getClient();
					if(player!=null){
						player.removeFromWorld();
						ids.add(StepServer.getStepBattleSessionId(player.accountId, player.id));
						StepBattleService service = Server.server.getServiceRegistry().getStepBattleService();
						service.removeFromQueue(player.id);
					}
				}
			}
			for(long id : ids){
				DispatchClientSession dSession = dispatchClientSessionService.removeClientSession(id);
				dSession.close();
			}
			sessions.remove(session);
		}

		public void sessionCreated(IoSession session) throws Exception {
			sessions.add(session);
		}

		public void sessionIdle(IoSession session, IdleStatus status)
				throws Exception {
			
		}

		public void sessionOpened(IoSession session) throws Exception {
			
		}
		
	}
	
	public static synchronized Long getStepBattleSessionId(int accountId, int playerId){
		return (Long)(((long)accountId<<32) | ((long)playerId));
	}

}
