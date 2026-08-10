package peony.game.stepserver;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import peony.common.ClientSessionAsyncCall;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.attendant.Attendant;
import peony.game.buff.Buff;
import peony.net.ClientSession;
import peony.net.DispatchClientSession;
import peony.net.DispatchClientSessionService;
import peony.net.PacketHandler;

public class StepBattelPacketPlayerCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(StepBattelPacketPlayerCall.class);
	protected Player player;
	protected IoSession ioSession;
	
	public StepBattelPacketPlayerCall(ClientSession session, Player player, IoSession ioSession) {
		super(session);
		this.player = player;
		this.ioSession = ioSession;
	}

	public void callFinish() throws Exception {
		initPlayerPacket(player, ioSession);
	}

	public void run() {
		addToClientSession();
	}
	
	public void initPlayerPacket(Player player, IoSession session){
		//进入报名缓冲区
		DispatchClientSessionService service = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
		DispatchClientSession ds = service.getSession(StepServer.getStepBattleSessionId(player.accountId, player.id));
		if(ds==null){
			PacketHandler handler = Server.server.getServiceRegistry().getPacketHandlerService().getPlayerHandler();
			ds = new DispatchClientSession(StepServer.getStepBattleSessionId(player.accountId, player.id), service, session, handler);
		}
		ds.setClient(player);
		ds.id = player.stepSessionId;
		player.session = ds;
		initStepPlayer(player);
		if(player!=null&&player.stepType!=StepServer.STEPBATTLE_TYPE_TOURNAMENT){
			Server.server.getServiceRegistry().getDbService().schedule(new LoadStepBattleScoreCall(ds, player));
		}
	}
	
	public void initStepPlayer(Player player){
		try {
			if(ObjectAccessor.getPlayer(player.id)!=null){
				ObjectAccessor.removeGameObject(player);
				log.info("[INITPLAYERWHENDISCONNET]"+LogUtil.getPlayerLogString(player));
			}
			ObjectAccessor.addGameObject(player);
			Server.server.getWorld().addPlayerToMap(player, player.tempMapId, player.x, player.y, true);
			player.setWarState(Player.PVPSTATE);
			StepBattleService battleService = Server.server.getServiceRegistry().getStepBattleService();
			battleService.signUp(player.id);
			int attendantInstanceId = player.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
			if(attendantInstanceId!=0){
				Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
				if(attendant!=null){
					attendant.follow();
				}
			}
			player.buffs.clear();
			player.initBuffs();
			int lastHorseInstId = player.pool.getInt(Player.PROPERTY_LAST_HORSE_INSTANCEID);
			if(lastHorseInstId != 0){
				player.horseRide(lastHorseInstId, 0,-1);
			}
			player.refreshProperties(false);
			log.info("[INITPLAYER]"+LogUtil.getPlayerLogString(player));
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
	}

}
