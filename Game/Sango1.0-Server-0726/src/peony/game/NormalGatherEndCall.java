package peony.game;

import java.util.Random;

import peony.produce.ProduceService;

public class NormalGatherEndCall implements GatherEndCall {

	private static final Random rnd = new Random();

	public void gatherEnd(GatherUnit gu, Player p) {
		Gain gain = new Gain(p);
		Gain[] gains = new Gain[1];
		gains[0] = gain;
		gu.fall.gain(rnd, gains);
		gain.completeAddToPlayer(true, "GTR");
		int gainLevel = gu.template.collectParam; // 采集原材料的级别
		int producePractice = p.pool.getInt(Player.PROPERTY_GATHER_ABILITY, 1); // 采集技能熟练度
		int playerLevel = p.level;
		ProduceService produceService = Server.server.getServiceRegistry().getProduceService();
		int playerPracticeLevel = ProduceService.getPracticeLevel(playerLevel,producePractice);
		int enhancePractice = ProduceService.enhancePractice(p,gainLevel,playerPracticeLevel, 
				p.pool.getInt(Player.PROPERTY_GATHER_ABILITY), playerLevel);
		enhancePractice += p.pool.getInt(Player.PROPERTY_GATHER_ABILITY, 1);
		p.pool.setInt(Player.PROPERTY_GATHER_ABILITY, enhancePractice); // 增加采集技能熟练度
	}

}