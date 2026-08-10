package peony.vm;

import peony.game.GameQuest;

public abstract class AbstractASMQuest implements ASMQuest{
	
	protected GameQuest quest;
	protected int id;
	protected boolean isFail;
	
	public AbstractASMQuest(GameQuest quest){
		this.quest = quest;
		this.id = quest.getId();
	}
	

	
	public GameQuest getGameQuest(){
		return quest;
	}
	
	public int getId(){
		return id;
	}
	
	public boolean isFail(){
		return isFail;
	}
	
	public void setFail(){
		isFail = true;
	}
}
