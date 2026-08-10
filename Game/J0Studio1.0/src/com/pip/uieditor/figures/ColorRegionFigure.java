package com.pip.uieditor.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.pip.uieditor.model.ColorRegion;

public class ColorRegionFigure extends RegionFigure {

	
	public ColorRegionFigure(ColorRegion region) {
		super(region);
	}
	
	@Override
	protected void paintFigure(Graphics gc) {
		ColorRegion region = (ColorRegion) getRegion();
		if (region.isAvaliable() && region.IsInParentState()) {
			gc.setAlpha(region.getColor().alpha);
			Color color = new Color(Display.getCurrent(),
					region.getColor().red, region.getColor().green,
					region.getColor().blue);
			gc.setBackgroundColor(color);
			gc.fillRectangle(new Rectangle(region.getLocation(), region
					.getSize()));
			color.dispose();
		}
	}
	
}
