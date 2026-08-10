package peony.game.nation;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class CandidateListCall extends ClientSessionAsyncCall {

	protected int serial;
	protected Packet packet;

	public CandidateListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.packet = packet;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		int flag = 0;
		Player p = (Player)session.getClient();
		if(p!=null){
			CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
			Candidate c = candidateService.getCandidate(p.id, p.faction);
			List<Candidate> list = candidateService.getCandidates(p.faction);
			Packet pt = new Packet(OpCode.CANDIDATE_LIST_SERVER);
			pt.putInt(serial);
			Candidate[] candidates = null;
			if(list!=null && list.size()>0){
				if(!list.contains(c) && candidateService.hasCandidate(p.id, p.faction) 
						&& candidateService.canSignup(p.faction)){
//					candidates = new Candidate[list.size()+1];
					candidates = new Candidate[list.size()];
					flag = 1;
				}else{
					candidates = new Candidate[list.size()];
				}
				int i = 0;
				for(Candidate candidate : list){
					candidates[i] = candidate;
					i++;
				}
				for(int x=0; x<candidates.length; x++){
					for(int y=x+1; y<candidates.length; y++){
						if(candidates[x].getVotes()<candidates[y].getVotes()){ 
							Candidate temp;
							temp = candidates[x];
							candidates[x] = candidates[y];
							candidates[y] = temp;
						}
						if(candidates[x].getVotes()==candidates[y].getVotes() 
								&& candidates[x].credit<candidates[y].credit){
							Candidate temp;
							temp = candidates[x];
							candidates[x] = candidates[y];
							candidates[y] = temp;
						}
						if(candidates[x].getVotes()==candidates[y].getVotes() && 
								candidates[x].getCredit()==candidates[y].getCredit() 
								&& candidates[x].getCreateTime().after(candidates[y].getCreateTime())){
							Candidate temp;
							temp = candidates[x];
							candidates[x] = candidates[y];
							candidates[y] = temp;
						}
					}
				}
			}
			if(flag==1){
				pt.put(list==null ? 0 : list.size()+1);
			}
			else{
				pt.put(list==null ? 0 : list.size());
			}
			if(candidates!=null && candidates.length>0){
				for(int x=0; x<candidates.length; x++){
					TongService ts = Server.server.getServiceRegistry().getTongService();
					int playerId = candidates[x].getPlayerId();
					Tong tong = ts.getPlayerTong(playerId);
					pt.putInt(playerId);
					pt.putString(Server.server.getServiceRegistry().getActorCacheService().find(playerId).name);
					pt.putString(tong == null ? "" : tong.name);
					pt.putInt(candidates[x].getVotes());
					pt.putInt(x+1);
				}
				if(flag == 1){
					TongService ts = Server.server.getServiceRegistry().getTongService();
					int playerId = c.getPlayerId();
					Tong tong = ts.getPlayerTong(playerId);
					pt.putInt(playerId);
					pt.putString(Server.server.getServiceRegistry().getActorCacheService().find(playerId).name);
					pt.putString(tong == null ? "" : tong.name);
					pt.putInt(c.getVotes());
					pt.putInt(candidateService.getNumInCandidates(p));
				}
			}
			p.send(pt);
		}
		addToClientSession();
	}

}
