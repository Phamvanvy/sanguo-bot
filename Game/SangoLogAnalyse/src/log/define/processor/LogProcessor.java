package log.define.processor;

import java.lang.reflect.Constructor;
import org.apache.commons.configuration.SubnodeConfiguration;
import log.define.LogDefine;

public abstract class LogProcessor {
	protected String id;
	public LogProcessor(String id){
		this.id = id;
	}
	public String getId(){
		return id;
	}
	public abstract String process(String data);
	
	@SuppressWarnings("unchecked")
	public static final LogProcessor loadProcessor(LogDefine define,SubnodeConfiguration node){
		try {
			String id = node.getString("id");
			String clazz = node.getString("class");
		    return ((Constructor<LogProcessor>) Class.forName(clazz).getConstructors()[0]).newInstance(id);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
