package peony.game.itemenhance;

import java.io.DataInputStream;

import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.Marshaller;
import peony.game.PersistenceException;
import peony.game.Serializer;

public class ItemEnhancePersistence implements Marshaller, Serializer {
    public int getId() {
        return 2;
    }

    public ItemEnhance marshaller(DataInputStream stream,GameItem owner) {
        return ItemEnhance.fromDBBytes(stream,owner);
    }

    public byte[] serialize(GameItemObject o) throws PersistenceException {
        ItemEnhance h = (ItemEnhance)o;
        return h.toDBBytes();
    }
}
