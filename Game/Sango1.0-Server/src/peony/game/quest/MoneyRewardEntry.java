package peony.game.quest;

import peony.game.Gain;
import peony.game.Player;
import peony.game.Server;
import peony.game.nation.Nation;
import peony.game.nation.NationSkill7;

public class MoneyRewardEntry implements QuestRewardEntry {
	
	protected int money;
	
	public MoneyRewardEntry(int money){
		this.money = money;
	}
	
	public void gain(Gain gain) {
		Player player = gain.getPlayer();
		float moneyRatio = 1.0f;
		if(player!=null)
			moneyRatio = player.moneyRatio;
		int addMoney = (int)(money * gain.getPlayer().tirePercent * moneyRatio * 1.0f);
		gain.addMoney(addMoney);
		processSkillNationMoney(player, addMoney);
	}
	
	protected void processSkillNationMoney(Player player, int addMoney){
		if(player!=null){
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
	        NationSkill7 skill = (NationSkill7) nation.skills.get(7);
	        float ratio = skill.getRatio();
	        int gainNationMoney = Math.round(addMoney * ratio);
	        nation.addMoney(gainNationMoney);
		}
	}

}
