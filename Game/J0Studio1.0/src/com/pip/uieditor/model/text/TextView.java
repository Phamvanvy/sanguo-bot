package com.pip.uieditor.model.text;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import com.pip.uieditor.model.text.Document.Line;


public class TextView extends View{

	private int startPosition, endPosition;
	
	public TextView(Element element, Line line, int width, int height, int startPosition, int endPosition) {
		super(element, line, width, height);
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public int getStartPosition() {
		return this.startPosition;
	}
	
	public int getEndPositiion() {
		return this.endPosition;
	}
	
	@Override
	public void draw(DrawContext context) {
		TextElement el = (TextElement) getElement();
		Font font = new Font(Display.getCurrent(), el.getFont());
		context.gc.setFont(font);
		if (getShadow(context)) {
			boolean isLink = el.getLink() != null;
			Color color = isLink ? getLinkColor(context) : getTextColor(context);
			Color shadowColor = getShadowColor(context);
			String str = el.getContent(this.startPosition, this.endPosition);
			context.gc.setForegroundColor(shadowColor);
			Point point = new Point(context.x, context.y - getHeight());
			context.gc.drawString(str, point.getTranslated(2, 0));
			context.gc.drawString(str, point.getTranslated(0, 2));
			context.gc.drawString(str, point.getTranslated(2, 2));
			context.gc.drawString(str, point.getTranslated(1, 0));
			context.gc.drawString(str, point.getTranslated(1, 2));
			context.gc.drawString(str, point.getTranslated(0, 1));
			context.gc.drawString(str, point.getTranslated(2, 1));
			context.gc.setForegroundColor(color);
			context.gc.drawString(str, point.getTranslated(1, 1));
			if(isLink) {
				context.gc.drawLine(context.x, context.y, context.x + getWidth(), context.y);
				context.gc.drawLine(context.x, context.y - 1, context.x + getWidth(), context.y - 1);
			}
//			shadowColor.dispose();
		} else {
			boolean isLink = el.getLink() != null;
			Color color = isLink ? getLinkColor(context) : getTextColor(context);
			context.gc.setForegroundColor(color);
			context.gc.drawString(el.getContent(this.startPosition, this.endPosition), context.x, context.y - getHeight());
			if(isLink) {
				context.gc.drawLine(context.x, context.y, context.x + getWidth(), context.y);	
			}
		}
		font.dispose();
	}
	
	protected Color getShadowColor(DrawContext context) {
		TextElement el = (TextElement)getElement();
		if(el.getShadowColor() != null)
			return el.getShadowColor();
		return context.shadowColor;
	}
	
	protected Color getLinkColor(DrawContext context) {
		TextElement el = (TextElement)getElement();
		if(el.getTextColor() != null)
			return el.getTextColor();
		return context.linkColor;
	}
	
	protected boolean getShadow(DrawContext context) {
		TextElement el = (TextElement)getElement();
		if(el.getShadow() == -1)
			return context.shadow;
		return el.getShadow() == 0 ? false : true;
	}
	
	protected Color getTextColor(DrawContext context) {
		TextElement el = (TextElement)getElement();
		if(el.getTextColor() != null)
			return el.getTextColor();
		return context.textColor;
	}
	
}
