package com.pip.uieditor.model.text;

import java.util.Vector;


public class Paragraph {
	protected Vector elements;
	
	public Paragraph() {
		this.elements = new Vector(5);
	}
	
	public Element getElement(int index) {
		return (Element)elements.elementAt(index);
	}
	
	public void addElement(Element element) {
		elements.addElement(element);
	}
	
	public int getElementCount() {
		return this.elements.size();
	}
	
}
