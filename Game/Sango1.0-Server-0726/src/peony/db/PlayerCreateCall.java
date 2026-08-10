package peony.db;

import org.apache.log4j.Logger;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import peony.game.ActorListActor;
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
				error(null,"用户名错误");
			} else {
				int existCount = Server.server.getServiceRegistry().getDbService().playerDAO.getActorCount(session.getIdentity().getId());
				if (existCount >= 4) {
					error(null, "已经达到最大角色数");
				}
				Actor actor = Server.server.getServiceRegistry()
						.getActorCacheService().find(player.name);
				if (actor != null && actor.exist == 1) {
					error(null, "已经有同名角色存在");
				} else {
					Account account = (Account)session.getIdentity();
					playerService.setDefault(player, account.getJvmCode());
					if (!playerService.createPlayer(player)) {
						error(null, "创建角色失败");
					}else{
						  //日文版创建第一个角色时通知billing服务器，由billing服务器判断是否发好友邀请奖励
			                if(Server.REVISION_TYPE_JAPAN.equals(Server.server.revision)){
			                    DBService dbService = Server.server.getServiceRegistry().getDbService();
			                    List<ActorListActor> actorList = dbService.playerDAO.getActorList(account.getId());
			                    //创建第一个角色时通知
			                    if (actorList.size() == 1) {
			                        log.info("[HANGAME_CHECK_INVITE]ACCOUNT[" + player.getAccount().getName() + "]PLAYERID[" + player.id + "]");
			                        
			                        String hangame_check_invite_url = Server.server.getConfig().configurationAt("hangame").getString("check_invite_url");
			                        PostMethod method;
			                        method = new PostMethod(hangame_check_invite_url);
			                        method.getParams().setContentCharset("utf-8");
			                        method.addRequestHeader("Connection", "close");
			                        method.setParameter("account", player.getAccount().getName());
			                        method.setParameter("gamecode", Server.server.gameCode);
			                        method.setParameter("player", String.valueOf(player.id));
			                        
			                        try {
			                            HttpClient httpclient = new HttpClient();
			                            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			                            httpclient.getParams().setSoTimeout(30000);
			                            int code = httpclient.executeMethod(method);
			                        } catch (Exception ex) {
			                            log.error(ex, ex);
			                        } finally {
			                            method.releaseConnection();
			                        }
			                    }
		                }
					}
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			error(null, "创建角色失败");
		}
		addToClientSession();
	}

}
