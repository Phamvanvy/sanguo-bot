package com.pip.j0ide.data;

/**
 * 本类表示一个机型的特殊Polish参数。
 * @author lighthu
 */
public class Variable implements Cloneable {
	/** 参数名称 */
	public String name = "";
	/** 参数值 */
	public String value = "";
	
	public Object clone() {
		try {
			return super.clone();
		} catch (Exception e) {
			return null;
		}
	}
}
