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
				if(nationService.getNationByFaction(p.faction).getKingId()!=p.id 
						&& nationService.getNationByFaction(p.faction).getOfficerByPlayerId(p.id)==null){
					log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
					error(null, peony.Messages.STRING_01910);
					addToClientSession();
					return;
				}
			}else if(flag==0){
				if(nationService.getNationByFaction(p.faction).getOfficerByPlayerId(p.id)==null
						 || nationService.getNationByFaction(p.faction).getKingId()==p.id){
					log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
					error(null, peony.Messages.STRING_01910);
					addToClientSession();
					return;
				}
			}
			if(cause.length()==0){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01911);
				addToClientSession();
				return;
			}
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			Officer officer = nation.getOfficerByPlayerId(p.id);
			if(officer==null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01912);
				addToClientSession();
				return;
			}
			if(officer.level==Officer.LEVEL2 || officer.level==Officer.LEVEL4){
				error(null, peony.Messages.STRING_01528);
				addToClientSession();
				return;
			}
			if(officer.getMaxForbidTimes()==0){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01913);
				addToClientSession();
				return;
			}
			if(officer.getForbidTimes()>=officer.getMaxForbidTimes()){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01914);
				addToClientSession();
				return;
			}
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(targetName);
			if(actor==null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01531);
				addToClientSession();
				return;
			}
			if(actor.faction!=p.faction){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01915);
				addToClientSession();
				return;
			}
			Officer targetOfficer = nation.getOfficerByPlayerId(actor.id);
			if(targetOfficer!=null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,peony.Messages.STRING_01916);
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
			String forbider = peony.Messages.STRING_01537;                   //禁言的实施者
			if(nationService.getNationByFaction(p.faction).getKingId()==p.id ){
			   forbider = peony.Messages.STRING_00704;
			}
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction,forbider
					, MessageFormat.format(peony.Messages.STRING_01917, actor.name,cause));
			Player target = ObjectAccessor.getPlayer(actor.id);
			if(target!=null){
				target.message(-1, MessageFormat.format(peony.Messages.STRING_01918, officer.getName(),p.name), -1, -1);
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
