package com.pip.uieditor.figures;

import java.util.Vector;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.FontUtil;
import com.pip.uieditor.model.TextArea;
import com.pip.uieditor.model.text.Document;
import com.pip.uieditor.model.text.Document.FormatContext;
import com.pip.uieditor.model.text.Document.Line;
import com.pip.uieditor.model.text.DrawContext;
import com.pip.uieditor.model.text.Element;
import com.pip.uieditor.model.text.Paragraph;
import com.pip.uieditor.model.text.View;

public class TextAreaFigure extends WidgetFigure{
	
	Vector views = new Vector();
	
	public TextAreaFigure(TextArea widget) {
		super(widget);
	}
	
	@Override
	public void paintFigure(Graphics gc) {
		TextArea ta = (TextArea)widget;
		views.removeAllElements();
		Document doc = new Document();
		doc.setText(ta.getContent());
		GC gcc = new GC(Display.getCurrent());
		formatContent(doc, gcc, ta);
		Line currentLine = null;
		DrawContext context = new DrawContext(gc);
		context.textColor = new Color(Display.getCurrent(), ta.getTextColor().getRGB());
		context.linkColor = new Color(Display.getCurrent(), ta.getLinkColor().getRGB());
		context.shadowColor = new Color(Display.getCurrent(), ta.getShadowColor().getRGB());
		context.shadow = ta.isShadow();
		context.font = FontUtil.getFontData(ta.getFontName());
		context.x = ta.getClientAreaX();
		context.y = ta.getClientAreaY();
		gc.pushState();
		gc.translate(getBounds().x + getInsets().left, getBounds().y
				+ getInsets().top);
		for(int i = 0; i < views.size(); i++) {
			View view = (View)views.elementAt(i);
			Line line = view.getLine();
			if(line != currentLine) {
				currentLine = line;
				context.y += line.height;
				if (i > 0) {
					context.y += ta.getLineGap();
				}
				context.x = ta.getClientAreaX();
			}
			view.draw(context);
			context.x += view.getWidth();
		}
		gc.popState();
		gc.restoreState();
		gcc.dispose();
	}
	
	protected void formatContent(Document doc, GC gc, TextArea ta) {
		FormatContext context = new FormatContext(gc, ta.getClientAreaWidth(), FontUtil.getFontData(ta.getFontName()));
		for(int i = 0; i < doc.getParagraphCount(); i++) {
			Paragraph paragraph = doc.getParagraph(i);
			for(int j = 0; j < paragraph.getElementCount(); j++) {
				Element element = paragraph.getElement(j);
				element.format(context);
			}
			context.newLine();
		}
		views = context.views;
	}
	
}
