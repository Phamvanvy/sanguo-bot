package peony.game;

import java.util.ArrayList;
import java.util.List;

import peony.game.quest.QuestRewardEntry;

public class QuestRewardBranch {
	protected int id;
	protected List<QuestRewardEntry> rewards = new ArrayList<QuestRewardEntry>();
	
	public QuestRewardBranch(int id){
		this.id = id;
	}
	
	public void addQuestRewardEntry(QuestRewardEntry entry){
		rewards.add(entry);
	}
	
	public void gain(Gain gain){
		for(QuestRewardEntry entry:rewards){
			entry.gain(gain);
		}
	}
}
