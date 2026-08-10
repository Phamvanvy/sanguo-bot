package peony.db;

import java.util.List;
import java.util.Random;

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
			
			// 向ipdservice同步
			Server.server.getServiceRegistry().getIpdService().updateRoleCount(message.getName(), 1);
		}else{
			ErrorHandler.sendErrorMessage(session, message.getSerial(), OpCode.ACCOUNT_QUICK_REG_CLIENT,errorMessage);
		}
	}

	public void run() {
		try {
			if (Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)|| Server.server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
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
				error(null, peony.Messages.STRING_00495);
			} else {
				PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
				int sex = RND.nextInt(2);
				int clazz = RND.nextInt(4);
				int faction = RND.nextInt(3) + 1;
				Player player = PlayerUtil.createPlayer(message.getName(), sex, clazz, faction, message.getAccountId());
				playerService.setDefault(player, jvmCode);
				player.session = this.session;
				if (!playerService.createPlayer(player)) {
					error(null, peony.Messages.STRING_00496);
				}
				playerID = player.id;
				
				//日文版创建第一个角色时通知billing服务器，由billing服务器判断是否发好友邀请奖励
				if(Server.REVISION_TYPE_JAPAN.equals(Server.server.revision)){
				    DBService dbService = Server.server.getServiceRegistry().getDbService();
	                List<ActorListActor> actorList = dbService.playerDAO.getActorList(message.getAccountId());
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
		} catch (Exception e) {
			log.error(e, e);
			error(null, peony.Messages.STRING_00496);
		}
		addToClientSession();
	}

}
