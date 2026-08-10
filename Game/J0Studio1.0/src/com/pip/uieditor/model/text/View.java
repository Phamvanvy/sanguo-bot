package com.pip.uieditor.model.text;

import com.pip.uieditor.model.text.Document.Line;

public abstract class View {
	
	private int x,y;
	private int width, height;
	private Element element;
	private Line line;
	
	public View(Element element,Line line, int width ,int height) {
		this.width = width;
		this.height = height;
		this.line = line;
		this.element = element;
	}
	
	public Element getElement() {
		return element;
	}
	
	public int getHeight() {
		return this.height;
	}
	public int getWidth() {
		return this.width;
	}
	
	public Line getLine() {
		return this.line;
	}
	
	public abstract void draw(DrawContext context);
}

