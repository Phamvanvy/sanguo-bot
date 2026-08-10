package peony.game;

import java.io.DataInputStream;

public class HorsePersistence implements Marshaller,Serializer {

	
	public int getId() {
		return 1;
	}

	public Horse marshaller(DataInputStream stream,GameItem owner) {
		return Horse.fromDBBytes(stream);
	}

	public byte[] serialize(GameItemObject o) throws PersistenceException{
		Horse h = (Horse)o;
		return h.toDBBytes();
	}

}
