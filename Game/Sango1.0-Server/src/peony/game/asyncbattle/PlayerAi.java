package peony.game.asyncbattle;

public interface PlayerAi {

	public void update();
	public void init();
	public boolean canOutOfBattle();
	public void backState();
	public void processHpMp(int itemId);
}
