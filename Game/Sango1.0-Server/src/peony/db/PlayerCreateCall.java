package peony.db;

import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ActorListActor;
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
	protected int type;
	
	public static int TYPE_RANDOM_FACTION_SEX_NAME = 1; //随机生成名字、阵营、性别的类型
	
	public PlayerCreateCall(PlayerService playerService,ClientSession session,Player player,int serial,int type){
		super(session);
		this.playerService = playerService;
		this.player = player;
		player.session = session;
		this.serial = serial;
		this.session = session;
		this.type = type;
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
			if (type!=TYPE_RANDOM_FACTION_SEX_NAME && StringUtil.isValidName(player.name.toLowerCase()) != 0) {
				error(null,peony.Messages.STRING_00493);
			} else {
				int existCount = Server.server.getServiceRegistry().getDbService().playerDAO.getActorCount(session.getIdentity().getId());
				if (existCount >= 4) {
					error(null, peony.Messages.STRING_00494);
				}
				Actor actor = null;
				try{actor = Server.server.getServiceRegistry().getActorCacheService().find(player.name);}catch(Exception e){}
				if (type!=TYPE_RANDOM_FACTION_SEX_NAME && actor != null && actor.exist == 1) {
					error(null, peony.Messages.STRING_00495);
				} else {
					Account account = (Account)session.getIdentity();
					playerService.setDefault(player, account.getJvmCode());
					if (!playerService.createPlayer(player)) {
						error(null, peony.Messages.STRING_00496);
					}else{
						if(type==TYPE_RANDOM_FACTION_SEX_NAME 
								&& Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW) 
								&& player.name.equals("")){
							//台湾每个账号第一次创建角色的时候自动生成角色名、阵营、职业、性别
							player.name = "遊客" + player.id;
						}
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
		                        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		                        
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
			error(null, peony.Messages.STRING_00496);
		}
		addToClientSession();
	}

}
