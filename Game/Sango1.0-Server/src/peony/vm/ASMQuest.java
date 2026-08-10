package peony.vm;

import peony.game.GameQuest;

public interface ASMQuest {
	public int getId();
	public void execute(ASMGameVM vm);
	public int finishCondition(ASMGameVM vm);
	public int preCondition(ASMGameVM vm);
	public GameQuest getGameQuest();
	public String getDesc(ASMGameVM vm);
	public String getPreDesc(ASMGameVM vm);
	public String getPostDesc(ASMGameVM vm);
	public String getUnFinishDesc(ASMGameVM vm);
}
