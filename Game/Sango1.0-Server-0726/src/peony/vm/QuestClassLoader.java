package peony.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import peony.game.GameQuest;

public class QuestClassLoader extends ClassLoader {
    private static AtomicInteger idGen = new AtomicInteger();
	
	/**
	 * 存放类名到类的对应，类名是以.分割的
	 */
    private Map<String,Class<?>> loadedClasses = new HashMap<String,Class<?>>();
    
    /**
     * 存放类名到类字节码的对应，类名是以.分割的
     */
    private static final Map<String,byte[]> className2bytes = new HashMap<String,byte[]>();
    
    public static void addQuest(GameQuest quest){
    	String className = "peony.vm.Quest"+quest.getId() + "_" + idGen.getAndIncrement();
    	quest.setClassName(className);
    	ByteCodeProducer producer = new ByteCodeProducer(quest);
    	byte[] bytes = producer.produce();
    	className2bytes.put(className, bytes);
    	
    }

    public QuestClassLoader() {
    }

    @Override
	public synchronized Class<?> loadClass(String className, boolean resolve)
			throws ClassNotFoundException {
		Class newClass;
		byte[] classData;
		newClass = loadedClasses.get(className);
		if (newClass != null) {
			if (resolve)
				resolveClass(newClass);
			return newClass;
		}
		try {
			newClass = findSystemClass(className);
			return newClass;
		} catch (ClassNotFoundException e) {
		}
		classData = className2bytes.get(className);
		if(classData==null)
			throw new ClassNotFoundException(className);
		try {
			newClass = defineClass(className, classData, 0, classData.length);
			if (newClass == null)
				throw new ClassNotFoundException(className);
		} catch (Exception e) {
			throw new ClassNotFoundException(className);
		}
		loadedClasses.put(className, newClass);
		if (resolve) {
			resolveClass(newClass);
		}
		return newClass;
	}
}