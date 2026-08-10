package com.pip.uieditor.editor.clipboard;

public class Clipboard {
	
	private static Clipboard instance = new Clipboard();
	
	private Object object;
	
	public Clipboard() {
		
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
	
	public Object getObject() {
		return this.object;
	}
	
	public static Clipboard instance() {
		return instance;
	}
}
