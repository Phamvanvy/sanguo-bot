package peony.game.instance;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class BossTimeScoreCall extends ClientSessionAsyncCall {

	public Player p;
	public int serial;
	public int bossId;
	
	public BossTimeScoreCall(ClientSession session, Packet packet) {
		super(session);
		p = (Player)session.getClient();
		serial = packet.getInt();
		bossId = packet.getInt();
	}

	public void callFinish() throws Exception {
		
	}
	
	public void run() {
		if(p!=null){
			BossScoreService bossScoreService = Server.server.getServiceRegistry().getBossScoreService();
			Score score = bossScoreService.bossScores.get(bossId);
			BossTimeScore[] scores = score.timeScores;
			score.bubbleBossTimeScores();
			Packet pt = new Packet(OpCode.BOSS_TIMEBOARD_SERVER);
			pt.putInt(serial);
			pt.put(score.getBossTimeScoresSize());
			if(score.getBossScoresSize()>0){
				for(int i=0;i<10;i++){
					BossTimeScore bossTimeScore = scores[i];
					if(bossTimeScore!=null){
						pt.put(i+1);
						pt.putInt(bossTimeScore.time);
						pt.putInt(bossTimeScore.members.list.size());
						for(Member member : bossTimeScore.members.list){
							pt.putString(member.name);
							pt.put(member.faction);
							pt.put(member.level);
							pt.put(member.sex);
							int playerId = member.id;
							TongService service = Server.server.getServiceRegistry().getTongService();
							Tong tong = service.getPlayerTong(playerId);
							pt.putString(tong==null ? "" : tong.name);
							pt.put(member.clazz);
						}
					}
				}
			}
			p.send(pt);
		}
		addToClientSession();
	}

}
