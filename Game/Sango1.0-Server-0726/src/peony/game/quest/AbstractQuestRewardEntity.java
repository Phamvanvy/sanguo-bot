package peony.game.quest;

import peony.game.Gain;
import peony.game.Server;
import peony.game.nation.Nation;
import peony.game.nation.NationSkill9;

public abstract class AbstractQuestRewardEntity implements QuestRewardEntry {

	protected float extraRewardRatio;
	
	public void gain(Gain gain) {
		initExtraRewardRatio(gain);
	}
	
	protected void initExtraRewardRatio(Gain gain){
		int ownerFaction = gain.getPlayer().faction;
		Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(ownerFaction);
		try {
			NationSkill9 nationSkill = (NationSkill9) nation.skills.get(9);
			extraRewardRatio = nationSkill.getRatio();
		} catch (Exception e) {
			
		}
	}

}
