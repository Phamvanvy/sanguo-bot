package peony.db;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ActorListActor;
import peony.game.Equipments;
import peony.game.ErrorHandler;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.PlayerService;

import com.pip.net.message.gameaccount.LegacyQuickRegResultMessage;

public class PlayerQuickCreateCall extends ClientSessionAsyncCall {

	protected static final Logger log = Logger.getLogger(PlayerCreateCall.class); 
	protected static final Random RND = new Random();
	
	protected LegacyQuickRegResultMessage message;
	protected int playerID;
	protected String jvmCode;
	protected ClientSession session;
	
	public PlayerQuickCreateCall(ClientSession session,LegacyQuickRegResultMessage message,String jvmCode){
		super(session);
		this.session = session;
		this.message = message;
		this.jvmCode = jvmCode;
	}
	
	public void callFinish() throws Exception{
		if(success){
			Packet pt = new Packet(OpCode.ACCOUNT_REG_SERVER);
			pt.putInt(message.getSerial());
			pt.putString(message.getName());
			pt.putInt(message.getAccountId());
			pt.putString(message.getPassword());
			pt.putInt(playerID);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, message.getSerial(), OpCode.ACCOUNT_QUICK_REG_CLIENT,errorMessage);
		}
	}

	public void run() {
		try {
			if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
				// CMCC版本可能存在找回帐号的情况，取第一个角色返回
				DBService dbService = Server.server.getServiceRegistry().getDbService();
				List<ActorListActor> actorList = dbService.playerDAO.getActorList(message.getAccountId());
				if (actorList.size() > 0) {
					playerID = actorList.get(0).id;
					addToClientSession();
					return;
				}
			}
			Actor actor = Server.server.getServiceRegistry()
					.getActorCacheService().find(message.getName());
			if (actor != null && actor.exist == 1) {
				error(null, "Nick name đã tồn tại");
			} else {
				PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
				int sex = RND.nextInt(2);
				int clazz = RND.nextInt(4);
				int faction = RND.nextInt(3) + 1;
				Player player = PlayerUtil.createPlayer(message.getName(), sex, clazz, faction, message.getAccountId());
				playerService.setDefault(player, jvmCode);
				player.session = this.session;
				if (!playerService.createPlayer(player)) {
					error(null, "Tạo nhân vật thất bại");
				}
				playerID = player.id;
			}
		} catch (Exception e) {
			log.error(e, e);
			error(null, "Tạo nhân vật thất bại");
		}
		addToClientSession();
	}

}
