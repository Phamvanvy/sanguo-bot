package com.pip.uieditor.model.text;


public interface Element {
	
	public static final String IMAGE = "image";
	public static final String ANIMATE = "animate";
	public static final String TEXT = "text";
	
	String getName();
	Document getDocument();
	
	Link getLink();
	
	void format(Document.FormatContext context);
}
