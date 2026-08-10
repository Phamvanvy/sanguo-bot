package peony.db;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.player.PlayerService;
import peony.util.StringUtil;

public class PlayerCreateCall extends ClientSessionAsyncCall {
	protected static final Logger log = Logger.getLogger(PlayerCreateCall.class); 
	protected Player player;
	protected int serial;
	protected PlayerService playerService;
	protected ClientSession session;
	
	public PlayerCreateCall(PlayerService playerService,ClientSession session,Player player,int serial){
		super(session);
		this.playerService = playerService;
		this.player = player;
		player.session = session;
		this.serial = serial;
		this.session = session;
	}
	
	public void callFinish() throws Exception{
		if(success){
			Packet pt = new Packet(OpCode.ACTOR_CREATE_SERVER);
			pt.putInt(serial);
			pt.putInt(player.id);
			pt.putString(player.name);
			pt.put(player.sex);
			pt.put(player.level);
			pt.put(player.clazz);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACTOR_CREATE_CLIENT,errorMessage);
		}
	}

	public void run() {
		try {
			if (StringUtil.isValidName(player.name.toLowerCase()) != 0) {
				error(null,"用戶名錯誤");
			} else {
				int existCount = Server.server.getServiceRegistry().getDbService().playerDAO.getActorCount(session.getIdentity().getId());
				if (existCount >= 4) {
					error(null, "已經達到最大角色數");
				}
				Actor actor = Server.server.getServiceRegistry()
						.getActorCacheService().find(player.name);
				if (actor != null && actor.exist == 1) {
					error(null, "已經有同名角色存在");
				} else {
					Account account = (Account)session.getIdentity();
					playerService.setDefault(player, account.getJvmCode());
					if (!playerService.createPlayer(player)) {
						error(null, "創建角色失敗");
					}
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			error(null, "創建角色失敗");
		}
		addToClientSession();
	}

}
