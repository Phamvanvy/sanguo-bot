package peony.game;

public interface Serializer {
	public int getId();
	public byte[] serialize(GameItemObject o) throws PersistenceException;
}
