package com.pip.uieditor.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import com.pip.uieditor.model.ColorRegion;
import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.StringRegion;
import com.pip.uieditor.model.Widget;

public class WidgetFigure extends RectangleFigure {
	
	protected Widget widget;
	
	public WidgetFigure(Widget widget)  {
		this.widget = widget;
		this.setOpaque(false);
		this.setFill(false);
	}

	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
	
//	@Override
//	public void paintFigure(Graphics gc) {
//		for(int i = 0; i < widget.getRegionCount(); i++) {
//			Region region = widget.getRegion(i);
//			if(region.isAvaliable() && region.isShowInEditing()) {
//				paintRegion(gc, region);
//			}
//		}
//	}
	
	public void paint(Graphics gc) {
		super.paint(gc);
	}
	
	protected void paintStringRegion(Graphics gc, StringRegion region) {
		int backgroundAlpha = region.getBackgroundColor().alpha;
//		gc.setAlpha(backgroundAlpha);
//		Color backgroundColor = new Color(Display.getCurrent(),
//				region.getBackgroundColor().red,
//				region.getBackgroundColor().green,
//				region.getBackgroundColor().blue);
//		gc.setBackgroundColor(backgroundColor);
//		gc.fillRectangle(region.getLocation().x, region.getLocation().y,
//				region.getSize().width, region.getSize().height);

//		gc.setAlpha(region.getColor().alpha);
		Color foregroundColor = new Color(Display.getCurrent(),
				region.getColor().alpha, region.getColor().green,
				region.getColor().blue);
		gc.setForegroundColor(foregroundColor);
		Font font = new Font(Display.getCurrent(), region.getFontData());
		gc.setFont(font);
		gc.drawString(region.getText(), region.getLocation());
//		backgroundColor.dispose();
		foregroundColor.dispose();
		font.dispose();
	}
	

//	@Override
//	protected void paintClientArea(Graphics graphics) {
////		super.paintClientArea(graphics);
//	}
	
//	public Insets getInsets() {
//		return widget.getInset().getAdded(widget.getBorder());
//	}
	
//	public Rectangle getClientArea(Rectangle rect) {
//		rect.setBounds(getBounds());
//		rect.shrink(widget.getBorder());
//		rect.shrink(widget.getInset());
//		if (useLocalCoordinates())
//			rect.setLocation(0, 0);
//		return rect;
//	}
	
}
