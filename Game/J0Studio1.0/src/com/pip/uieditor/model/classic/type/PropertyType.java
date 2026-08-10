package com.pip.uieditor.model.classic.type;

/**
 * 字段类型,并且负责对字段进行字符串转化
 * @author Jeffrey
 *
 */
public interface PropertyType {
	
	public String getId();
	
	public String to(Object value);
	
	public Object from(String s);
	
}
