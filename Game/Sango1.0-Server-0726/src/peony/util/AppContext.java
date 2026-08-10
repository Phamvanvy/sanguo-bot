package peony.util;

import java.util.HashMap;
import java.util.Map;

public class AppContext {
	public static final Map<Class,Object> map = new HashMap<Class,Object>();
	
	public static final void reg(Class clazz,Object o){
		map.put(clazz, o);
	}
	
	public static final Object get(Class clazz){
		return map.get(clazz);
	}
}
