package com.pip.uieditor.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.FontUtil;
import com.pip.uieditor.model.StringRegion;
import com.pip.uieditor.model.text.Document.Line;
import com.pip.uieditor.model.text.DrawContext;
import com.pip.uieditor.model.text.View;

public class StringRegionFigure extends RegionFigure {

	
	public StringRegionFigure(StringRegion region) {
		super(region);
	}
	
	@Override
	protected void paintFigure(Graphics gc) {
		StringRegion ta = (StringRegion)getRegion();
		if(!ta.isValid()) {
			ta.validate();
		}
		Line currentLine = null;
		DrawContext context = new DrawContext(gc);
		context.textColor = new Color(Display.getCurrent(), ta.getColor().getRGB());
		context.linkColor = new Color(Display.getCurrent(), ta.getLinkColor().getRGB());
		context.shadowColor = new Color(Display.getCurrent(), ta.getShadowColor().getRGB());
		context.shadow = ta.isShadow();
		context.font = FontUtil.getFontData(ta.getFontName());
		context.x = ta.getBounds().x;
		context.y = ta.getBounds().y;
		gc.pushState();
//		gc.translate(getBounds().x + getInsets().left, getBounds().y
//				+ getInsets().top);
		for(int i = 0; i < ta.views.size(); i++) {
			View view = (View)ta.views.elementAt(i);
			Line line = view.getLine();
			if(line != currentLine) {
				currentLine = line;
				context.y += line.height;
				if (i > 0) {
					context.y += ta.getLineGap();
				}
				context.x = ta.getBounds().x;
			}
			view.draw(context);
			context.x += view.getWidth();
		}
		gc.popState();
		gc.restoreState();
//		StringRegion region = (StringRegion) getRegion();
//		if (region.isAvaliable()  && region.IsInParentState()) {
//			 int backgroundAlpha = region.getBackgroundColor().alpha;
//			//
//			 gc.setAlpha(backgroundAlpha);
//			Color backgroundColor = new Color(Display.getCurrent(),
//					region.getBackgroundColor().red,
//					region.getBackgroundColor().green,
//					region.getBackgroundColor().blue);
//			gc.setBackgroundColor(backgroundColor);
//			gc.fillRectangle(region.getLocation().x, region.getLocation().y,
//					region.getSize().width, region.getSize().height);
//
//			Color foregroundColor = new Color(Display.getCurrent(),
//					region.getColor().red, region.getColor().green,
//					region.getColor().blue);
//			Font font = new Font(Display.getCurrent(), region.getFontData());
//			gc.setFont(font);
//			if(region.isShadow()) {
//				Color shadowColor = new Color(Display.getCurrent(),
//						region.getShadowColor().red, region.getShadowColor().green,
//						region.getShadowColor().blue);
//				gc.setForegroundColor(shadowColor);
//				gc.setAlpha(region.getShadowColor().alpha);
//				gc.drawString(region.getText(), region.getLocation().translate(2, 0));
//				gc.drawString(region.getText(), region.getLocation().translate(0, 2));
//				gc.drawString(region.getText(), region.getLocation().translate(2, 2));
//				gc.drawString(region.getText(), region.getLocation().translate(1, 0));
//				gc.drawString(region.getText(), region.getLocation().translate(1, 2));
//				gc.drawString(region.getText(), region.getLocation().translate(0, 1));
//				gc.drawString(region.getText(), region.getLocation().translate(2, 1));
//				
//				gc.setAlpha(region.getColor().alpha);
//				gc.setForegroundColor(foregroundColor);
//				gc.drawString(region.getText(), region.getLocation().translate(1, 1));
//			} else {
//				gc.setAlpha(region.getColor().alpha);
//				gc.setForegroundColor(foregroundColor);
//				gc.drawString(region.getText(), region.getLocation());				
//			}
//			
//			
//			if(region.isLink()) {
//				gc.setLineWidth(2);
//				Rectangle rect = region.getBounds();
//				gc.drawLine(rect.getBottomLeft().translate(0, -1), rect.getBottomRight().translate(0, -1));
//			}
//			backgroundColor.dispose();
//			foregroundColor.dispose();
//			font.dispose();
//		}
	}	
}	
