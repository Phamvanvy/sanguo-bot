package peony.game.instance;

import java.util.Date;

import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class BossScoreBoardCall extends ClientSessionAsyncCall {

	public Player p;
	public int serial;
	public int bossId;
	
	public BossScoreBoardCall(ClientSession session, Packet packet) {
		super(session);
		p = (Player)session.getClient();
		if(p!=null){
			serial = packet.getInt();
			bossId = packet.getInt();
		}
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(p!=null){
			BossScoreService bossScoreService = Server.server.getServiceRegistry().getBossScoreService();
			Score score = bossScoreService.bossScores.get(bossId);
			BossScore[] scores = score.bossScores;
			Packet pt = new Packet(OpCode.BOSS_SCOREBOARD_SERVER);
			pt.putInt(serial);
			pt.put(score.getBossScoresSize());
			if(score.getBossScoresSize()>0){
				for(int i=0;i<11;i++){
					BossScore bossScore = scores[i];
					if(bossScore!=null){
						pt.put(bossScore.score);
						pt.putString(CommonUtil.getDateString(bossScore.date));
						pt.putInt(bossScore.members.list.size());
						for(Member member : bossScore.members.list){
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
