package com.pip.uieditor.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import com.pip.uieditor.model.ModelRegion;

public class ModelRegionFigure extends RegionFigure{
	public ModelRegionFigure(ModelRegion region) {
		super(region);
	}
	
	@Override
	protected void paintFigure(Graphics gc) {
		ModelRegion region = (ModelRegion) getRegion();
		if (region.isAvaliable() && region.IsInParentState()) {
			Color color = ColorConstants.gray;
			gc.setBackgroundColor(color);
			gc.fillRectangle(new Rectangle(region.getLocation(), region
					.getSize()));
		}
	}
}
