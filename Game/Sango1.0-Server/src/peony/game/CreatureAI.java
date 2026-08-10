package peony.game;

public interface CreatureAI {
	public void update();
	public void init();
	public boolean canOutOfBattle();
	public void backState();
}
