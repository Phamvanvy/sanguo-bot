package peony.game;

import java.io.DataInputStream;

public class IMoneyCardPersistence implements Marshaller, Serializer {

	public int getId() {
		return 3;
	}

    public IMoneyCard marshaller(DataInputStream stream,GameItem owner) {
        return IMoneyCard.fromDBBytes(stream,owner);
    }

    public byte[] serialize(GameItemObject o) throws PersistenceException {
    	IMoneyCard h = (IMoneyCard)o;
        return h.toDBBytes();
    }

}
