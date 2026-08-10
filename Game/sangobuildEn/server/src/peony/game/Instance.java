package peony.game;

public interface Instance {
	public int getId();
	public String getName();
	public void update(int diff);
	public VMap getMap(int mapId);
	public void addPlayer(Player player) throws VMapException;
	public void removePlayer(Player player);
	public void loadingFinished(Player player);
}
