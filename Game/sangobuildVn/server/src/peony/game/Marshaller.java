package peony.game;

import java.io.DataInputStream;

public interface Marshaller {
	public int getId();
	public GameItemObject marshaller(DataInputStream stream,GameItem owner) throws PersistenceException;
}
