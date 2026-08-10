package com.pip.uieditor.model.text;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;


public class DrawContext {
	public Graphics gc;
	public int x, y;
	public Color textColor, shadowColor, linkColor;
	public boolean shadow;
	public FontData font;
	
	public DrawContext(Graphics gc) {
		this.gc = gc;
	}
}
