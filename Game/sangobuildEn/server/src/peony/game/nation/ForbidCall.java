package peony.game.nation;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.util.StringUtil;

public class ForbidCall extends ClientSessionAsyncCall {
	
	private static final Logger log = Logger.getLogger(ForbidCall.class);
	int serial;
	String targetName;
	String cause;
	int flag;
	
	public ForbidCall(Packet packet, ClientSession session){
		super(session);
		serial = packet.getInt();
		targetName = packet.getString();
		cause = packet.getString();
		this.flag = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.NATION_FORBID_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_FORBID_CLINET, errorMessage);
		}
	}

	public void run() {
		Player p = (Player)session.getClient();
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		if(p!=null){
			log.info("[FORBID]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]TRY");
			if(flag==1){
				if(nationService.getNationByFaction(p.faction).getKingId()!=p.id){
					log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
					error(null, "你不是一國之君,沒有權利使用此項功能");
					addToClientSession();
					return;
				}
			}else if(flag==0){
				if(nationService.getNationByFaction(p.faction).getOfficerByPlayerId(p.id)==null
						 || nationService.getNationByFaction(p.faction).getKingId()==p.id){
					log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
					error(null, "你不是該國大臣,沒有權利使用此項功能");
					addToClientSession();
					return;
				}
			}
			if(cause.length()==0){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"沒有理由,不能禁言");
				addToClientSession();
				return;
			}
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			Officer officer = nation.getOfficerByPlayerId(p.id);
			if(officer==null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"你不是官員不能禁言");
				addToClientSession();
				return;
			}
			if(officer.level==Officer.LEVEL2 || officer.level==Officer.LEVEL4){
				error(null, "你暫時不能使用該項權利");
				addToClientSession();
				return;
			}
			if(officer.getMaxForbidTimes()==0){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"你的官員等級不能禁言");
				addToClientSession();
				return;
			}
			if(officer.getForbidTimes()>=officer.getMaxForbidTimes()){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"禁言次數已用完");
				addToClientSession();
				return;
			}
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(targetName);
			if(actor==null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"未找到指定角色");
				addToClientSession();
				return;
			}
			if(actor.faction!=p.faction){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"不能對其他國家的角色禁言");
				addToClientSession();
				return;
			}
			Officer targetOfficer = nation.getOfficerByPlayerId(actor.id);
			if(targetOfficer!=null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"不能對官員禁言");
				addToClientSession();
				return;
			}
			try {
				Server.server.getServiceRegistry().getNationService().forbid(officer, actor.id);
			} catch (NationVoteException e) {
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(e, e.getMessage());
				addToClientSession();
				return;
			}
			cause = StringUtil.filterBadWords(cause);
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction,"官員"
					, MessageFormat.format("{0}因{1}被禁言", actor.name,cause));
			Player target = ObjectAccessor.getPlayer(actor.id);
			if(target!=null){
				target.message(-1, MessageFormat.format("你已被{0}{1}禁言", officer.getName(),p.name), -1, -1);
			}else{
				String forbids = nation.pool.getString(p.faction+"FORBID");
				StringBuffer strb = new StringBuffer(forbids);
				nation.pool.setString(p.faction+"FORBID", strb.append(",")
						.append(((Integer)(actor.id)).toString()).toString());
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
			log.info("[FORBIDSUCCESS]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
			addToClientSession();
		}
	}

}
