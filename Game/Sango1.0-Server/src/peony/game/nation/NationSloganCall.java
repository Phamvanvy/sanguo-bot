package peony.game.nation;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.db.NationDAO;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.util.StringUtil;

public class NationSloganCall extends ClientSessionAsyncCall {

	protected int serial;
	protected String slogan;
	protected int flag;
	protected Player p;
	protected ClientSession session;
	
	public NationSloganCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.slogan = packet.getString();
		this.flag = packet.getInt();
		this.p = (Player)session.getClient();
		this.session = session;
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.NATION_SLOGAN_SERVER);
			pt.putInt(serial);
			if(p!=null)
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_SLOGAN_CLIENT, errorMessage);
		}
	}

	public void run() {
		if (p != null) {
			Nation nation = Server.server.getServiceRegistry()
					.getNationService().getNationByFaction(p.faction);
			NationDAO nationDAO = Server.server.getServiceRegistry().getDbService().nationDAO;
			if(flag==1){
				if (nation.getKingId() == p.id) {
					if(nation.pool.getInt(Officer.PROPERTY_SLOGAN_TIMES, 0)
							>=nation.getOfficer(Officer.KING).getMaxSloganTimes()){
						error(null, peony.Messages.STRING_01751);
						addToClientSession();
						return;
					}
					if (slogan.length() > 30) {
						error(null, peony.Messages.STRING_01752);
						addToClientSession();
						return;
					} else if(slogan.length() == 0){
						error(null, peony.Messages.STRING_01753);
						addToClientSession();
						return;
					}
					slogan = StringUtil.filterBadWords(slogan);
					nation.slogan = slogan;
					Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction, peony.Messages.STRING_01754);
					nation.pool.setInt(Officer.PROPERTY_SLOGAN_TIMES, nation.pool.getInt(Officer.PROPERTY_SLOGAN_TIMES)+1);
				}else{
					error(null, peony.Messages.STRING_01755);
				}
			}else if(flag==0){
				Officer officer = nation.getOfficerByPlayerId(p.id);
				if (officer!=null && nation.getKingId()!=p.id) {
					if(officer.level==Officer.LEVEL1){
						error(null, peony.Messages.STRING_01528);
						addToClientSession();
						return;
					}
					if(officer.level==Officer.LEVEL3 || officer.level==Officer.LEVEL4){
						error(null, peony.Messages.STRING_01528);
						addToClientSession();
						return;
					}
					if(nation.pool.getInt(officer.getName(), 0)
							>=officer.getMaxSloganTimes()){
						error(null, peony.Messages.STRING_01751);
						addToClientSession();
						return;
					}
					if (slogan.length() > 30) {
						error(null, peony.Messages.STRING_01752);
						addToClientSession();
						return;
					} else if(slogan.length() == 0){
						error(null, peony.Messages.STRING_01753);
						addToClientSession();
						return;
					}
					slogan = StringUtil.filterBadWords(slogan);
					nation.slogan = slogan;
					Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction, 
							MessageFormat.format(peony.Messages.STRING_01756, 
									nation.getOfficerByPlayerId(p.id).getName(),nation.getOfficerByPlayerId(p.id).actor.name));
					nation.pool.setInt(officer.getName(), nation.pool.getInt(officer.getName())+1);
				}else{
					error(null, peony.Messages.STRING_01757);
				}
			}
			nationDAO.updateEntity(nation);
		}
		addToClientSession();
	}

}
