package peony.game.quest;

import peony.game.Gain;
import peony.game.GameQuest;
import peony.game.Player;

public class ExpRewardEntry extends AbstractQuestRewardEntity {
    protected GameQuest quest;
	protected int exp;
	
	public ExpRewardEntry(GameQuest quest, int exp){
	    this.quest = quest;
		this.exp = exp;
	}

	/**
	 * 计算一个玩家完成任务获得的经验值。
	 * @param quest 任务对象 
	 * @param exp 原始配置经验值
	 * @param player 玩家
	 * @return 返回实际获得经验值
	 */
	public static int calcExp(GameQuest quest, int exp, Player player) {
	    // 如果是循环或每日任务，经验加上级别惩罚
        int plevel = player.level;
        int realExp = exp;
        boolean needDeduce = quest.getRepeatType() != 0;   // 循环任务有经验惩罚
        if (realExp < 0) {
        	// 如果经验值是负数，则根据级别计算经验，并且跳过经验惩罚。
        	realExp = (-realExp) * plevel;
        	needDeduce = false;
        }
	    if (needDeduce) {
	        int qlevel = quest.getLevel();
	        if (plevel >= qlevel + 14) {
	        	return realExp / 10;
	        } else if (plevel >= qlevel + 6) {
	        	return (realExp * (15 + qlevel - plevel)) / 10;
	        } else {
	        	return realExp;
	        }
	    } else {
	    	return realExp;
	    }
	}
	
	public void gain(Gain gain) {
		super.gain(gain);
		gain.addExp((int)(calcExp(quest, exp, gain.getPlayer()) * gain.getPlayer().tirePercent * (1 + extraRewardRatio)));
	}
}
