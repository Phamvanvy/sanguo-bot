package com.pip.uieditor.model;

public class Dialog extends Container {
	
	public final static Dialog PROTOTYPE = new Dialog();
	
	public Dialog() {
		super("Dialog");
	}
	
	@Override
	public String getDefaultName() {
		return "dlg";
	}
	
	public Dialog clone() {
		Dialog ret = new Dialog();
		fillCloneContainer(ret);
		return ret;
	}
}
