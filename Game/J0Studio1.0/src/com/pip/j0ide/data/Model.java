package com.pip.j0ide.data;

import java.util.ArrayList;

/**
 * 本类表示一个游戏机型定义。这里一个机型表示一个系列，可以包含多款手机。
 * @author lighthu
 */
public class Model {
	/** 乐园平台内部使用的机型ID */
	public String id;
	/** 机型标题 */
	public String title;
	/** 本机型对应的J2ME-Polish里的机型 */
	public String device;
	/** 说明 */
	public String comments;
	/** 为本机型定义的特殊Polish编译参数 */
	public ArrayList<Variable> variables;
	
	public String toString() {
		return title;
	}
}
