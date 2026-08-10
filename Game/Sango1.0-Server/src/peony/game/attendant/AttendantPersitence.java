package peony.game.attendant;

import java.io.DataInputStream;

import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.Marshaller;
import peony.game.PersistenceException;
import peony.game.Serializer;

public class AttendantPersitence implements Marshaller,Serializer {

	public int getId() {
		return 4;
	}

	public GameItemObject marshaller(DataInputStream stream, GameItem owner)
			throws PersistenceException {
		return Attendant.fromDBBytes(stream, null, null);
	}

	public byte[] serialize(GameItemObject o) throws PersistenceException {
		Attendant atd = (Attendant)o;
		return atd.toDBBytes();
	}

}
