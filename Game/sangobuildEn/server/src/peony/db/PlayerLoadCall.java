package peony.db;


import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Identity;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.World;
import peony.game.buff.BuffUtil;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.player.PlayerService;

public class PlayerLoadCall extends ClientSessionAsyncCall {
	protected static final Logger log = Logger.getLogger(PlayerLoadCall.class); 
	protected World world;
	protected int actorId;
	protected Player player;
	protected int serial;
	protected PlayerService playerService;
	protected String MIEI = "";
	
	public PlayerLoadCall(PlayerService playerService, ClientSession session,
			int actorId, World world, int serial, String MIEI) {
		super(session);
		this.playerService = playerService;
		this.actorId = actorId;
		this.world = world;
		this.serial = serial;
		this.MIEI = MIEI;
		LogUtil.logLoginTry((Account)session.getIdentity(), actorId, MIEI);
	}
	
	public void callFinish() throws Exception {
		if (success) {
			if (session.isConnected()) {
				session.setClient(player);
				if(player.systemState!=Player.SYSTEMSTATE_LOAD){
					player.removeFromWorld();
				}
				player.loginTime = System.currentTimeMillis();
				
				ObjectAccessor.addGameObject(player);
				VMap map = world.addPlayerToMap(player, player.map.id,
						player.x, player.y,true);
				player.logined();
				player.moveType = 0;
				player.moveExtended = 0;
				Packet pt = new Packet(OpCode.ACTOR_LOGIN_SERVER);
				pt.putInt(serial);
				pt.put(player.toClientBytes());
				session.send(pt);
				
				// 下发小提示
				String hint = Server.server.getServiceRegistry().getDataService().getHint(player);
				if (hint != null) {
				    Packet pt1 = new Packet(OpCode.PUSH_HINT_SERVER);
				    pt1.putString(hint);
				    session.send(pt1);
				}
				
				Packet pt1 = new Packet(OpCode.GOMAP_ALLOW_SERVER);
				pt1.putInt(player.map.id);
				pt1.putInt(player.getVMap().getInstanceId());
				pt1.putInt(player.x);
				pt1.putInt(player.y);
				pt1.put(player.getVMap().allowFollow()?1:0);
				session.send(pt1);
//				session.send(pt);
			}
		} else {
			if(errorMessage==null){
				errorMessage = "該用戶已下線";
			}
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACTOR_LOGIN_CLIENT,errorMessage);
		}

	}

	public void run() {
		Identity identity = session.getIdentity();
		if (identity == null) {
			error(null, "沒有登錄");
		} else {
			if (playerService.isMuted(actorId)) {
				error(null, "防作弊系統檢測到您的角色狀態异常,將臨時隔离,請稍候再嘗試登錄");
			} else {
				player = playerService.loadPlayer(identity.getId(), actorId);
				if (player != null) {
					player.checkActivePower();
					if (player.isKing() == 1) {
						player.buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
					} else {
						player.buffs.removeBuff(216);
					}
					// Server.server.getServiceRegistry().getNationService().checkOnKingMap(player);
					Server.server.getServiceRegistry().getRelationService()
							.removeAllRelation(player);
				}else{
					log.info("[LOADPLAYERCHEAT]ACCOUNT["+identity.getId()+"]ID["+actorId+"]");
					error(null, "沒有找到指定角色");
				}
			}
		}
		addToClientSession();
	}
	
}
