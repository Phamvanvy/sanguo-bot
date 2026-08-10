package com.pip.uieditor.model;

public class ModelUtil {
	
	/**
	 * 查看一个Widget是否在另外一个wdiget的层次中。
	 * 查找child的parent，直到parent为null或者parent等于w。如果有一个parent等于w，那么就说child在w的层次中
	 * @param child
	 * @param w
	 * @return
	 */
	public static boolean isInHierarchy(Widget child, Widget w) {
		Widget parent = null;
		while((parent = child.getParent()) != null) {
			if(w == parent)
				return true;
		}
		return false;
	}
}
