package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.GameQuest;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerPacketHandler;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.net.Packet;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class QuestItemEffect implements ItemEffect {
	
	private static final String SCRIPT = "ui_push_quest_game";
	
	protected int questIdWei;
	protected int questIdShu;
	protected int questIdWu;
	
	public QuestItemEffect(int questIdWei, int questIdShu, int questIdWu){
		this.questIdWei = questIdWei;
		if (questIdShu != 0) {
		    this.questIdShu = questIdShu;
		} else {
		    this.questIdShu = questIdWei;
		}
        if (questIdWu != 0) {
            this.questIdWu = questIdWu;
        } else {
            this.questIdWu = questIdWei;
        }
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		int questId = questIdWei;
		if (p.faction == GameObject.FACTION_SHU) {
		    questId = questIdShu;
		} else if (p.faction == GameObject.FACTION_WU) {
		    questId = questIdWu;
		}
		GameQuest quest = ASMQuestUtil.getGameQuest(questId);
		if(quest.questInfo.owner.repeatType==0){
			if(p.asmVm.hasTask(questId)==1||p.asmVm.taskFinished(questId)==1)
				throw new UseItemException(peony.Messages.STRING_01740);
		}else if(quest.questInfo.owner.repeatType==4){
			if(p.asmVm.hasTask(questId)==1){
				throw new UseItemException(peony.Messages.STRING_01741);
			}
		}
		Packet pt = new Packet(OpCode.QUEST_START_ADDED_SERVER);
		pt.putInt(quest.getStartNpc());
		pt.putInt(quest.getFinishNpc());
		pt.putInt(quest.getId());
		pt.put(quest.getLevel());
		pt.putString(quest.getName());
		pt.put(1);	//¿É¼û
		
//		if(p.isNewUI()){
			ASMQuest asmquest = ASMQuestUtil.getQuest(questId);
			String preDesc = asmquest.getPreDesc(p.asmVm);
			pt.putString(preDesc);
			String unDesc = asmquest.getUnFinishDesc(p.asmVm);
			pt.putString(unDesc);
			String postDesc = asmquest.getPostDesc(p.asmVm);
			pt.putString(postDesc);
			String desc = asmquest.getDesc(p.asmVm);
			pt.putString(desc);
			PlayerPacketHandler.writeQuestRewardSets(pt,asmquest.getGameQuest(),p);
//		}
		
		
		p.send(pt);

		pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(SCRIPT);
		pt.putString(String.valueOf(questId));
		p.send(pt);
	}

	public boolean isAsync(){
		return false;
	}
	
	public int getQuestID(int faction) {
	    int questId = questIdWei;
        if (faction == GameObject.FACTION_SHU) {
            questId = questIdShu;
        } else if (faction == GameObject.FACTION_WU) {
            questId = questIdWu;
        }
        return questId;
	}
	
	public boolean needRemove() {
		return false;
	}
}
