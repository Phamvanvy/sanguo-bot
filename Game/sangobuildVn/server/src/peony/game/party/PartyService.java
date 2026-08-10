package peony.game.party;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import ch.javasoft.util.intcoll.IntHashMap;



public class PartyService implements Service, ServiceEventListener {
	
	protected IntHashMap<Party> pid2party = new IntHashMap<Party>();
	
	protected HashMap<Integer, Party> partyId2Party = new HashMap<Integer, Party>();
	
	protected AtomicInteger ids = new AtomicInteger(0);
	
	protected IntHashMap<PartyRequest> id2request = new IntHashMap<PartyRequest>();
	
	public PartyRequest newPartyRequest(Player player,Player target){
		PartyRequest request = new PartyRequest(ids.incrementAndGet(),Time.currTime,player.ref(),target.ref());
		id2request.put(request.id, request);
		return request;
	}
	
	public void startup() {
		Server.server.getEventManager().registerListener(this);
	}
	
	public void shutdown() {
		
	}
	
	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			playerLoaded((Player) event.param1);
			break;
		}
	}
	
	protected void playerLoaded(Player p){
		if(p.party != null){
			p.party.sendPartyInfo(p);
		}
	}

	public Party getParty(int id){
		return pid2party.get(id);
	}
	
	public Party getPartyById(int id){
		return partyId2Party.get(id);
	}
	
	public PartyRequest getAndRemoveRequest(int id){
		return id2request.remove(id);
	}
	
	void notifyPartyCreated(Party party){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PARTY_CREATED, party));
	}
	
	void notifyPartyDestroied(Party party){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PARTY_DESTROIED, party));
	}
	
	void notifyMemberAdded(Party party,PartyMember member){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_MEMBER_ADDED, party, member));
	}
	
	void notifyMemberLeaved(Party party,PartyMember member){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_MEMBER_LEAVED, party, member));
	}
	
	
	void partyCreated(Party party){
		notifyPartyCreated(party);
		pid2party.put(party.leader.getId(), party);
		synchronized (party) {
			partyId2Party.put(party.id, party);
		}
	}
	
	void partyDestroied(Party party){
		notifyPartyDestroied(party);
		synchronized (party) {
			for(PartyMember member:party.members){
				pid2party.remove(member.getId());
			}
			partyId2Party.remove(party.id);
		}
	}
	
	void memberAdded(Party party,PartyMember member){
		notifyMemberAdded(party, member);
		pid2party.put(member.getId(), party);
	}
	
	void memberLeaved(Party party,PartyMember member){
		notifyMemberLeaved(party, member);
		pid2party.remove(member.getId());
	}
}
