package com.pip.uieditor.model.text;

public interface RichTextVisitor {
	void text(String text);
	void braceTag(String content);
	void tagBegin(char type, String attribute);
	void tagEnded(char type);
	void ended();
}
