package peony.game;

import java.util.HashMap;
import java.util.Map;

import ch.javasoft.util.intcoll.IntHashMap;

public class PersistenceManager {

	protected static IntHashMap<Marshaller> id2marshaller = new IntHashMap<Marshaller>();
	protected static Map<Class<? extends Marshaller>, Marshaller> class2marshaller = new HashMap<Class<? extends Marshaller>, Marshaller>();

	protected static IntHashMap<Serializer> id2serializer = new IntHashMap<Serializer>();
	protected static Map<Class<? extends Serializer>, Serializer> class2serializer = new HashMap<Class<? extends Serializer>, Serializer>();

	public static void registerMarshaller(Marshaller marshaller) {
		id2marshaller.put(marshaller.getId(), marshaller);
		class2marshaller.put(marshaller.getClass(), marshaller);
	}
	
	public static void registerSerializer(Serializer serializer){
		id2serializer.put(serializer.getId(), serializer);
		class2serializer.put(serializer.getClass(), serializer);
	}
	
	public static Marshaller marshaller(Class<? extends Marshaller> clazz){
		return class2marshaller.get(clazz);
	}
	
	public static Serializer serializer(Class<? extends Serializer> clazz){
		return class2serializer.get(clazz);
	}
	
	public static Marshaller marshaller(int id){
		return id2marshaller.get(id);
	}
	
	public static Serializer serializer(int id){
		return id2serializer.get(id);
	}
}
