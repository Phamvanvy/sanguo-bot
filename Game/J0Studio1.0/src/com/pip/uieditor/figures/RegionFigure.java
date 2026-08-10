package com.pip.uieditor.figures;

import org.eclipse.draw2d.Figure;

import com.pip.uieditor.model.Region;

public class RegionFigure extends Figure {
	
	private Region region;
	
	
	public RegionFigure(Region region) {
		this.region = region;
	}
	
	public Region getRegion() {
		return region;
	}
	
	
	protected boolean useLocalCoordinates() {
		return true;
	}
	
	@Override
	protected void layout() {
		if(region.isAvaliable()) {
//			if(!region.isValid()) {
				region.validate();
//			}
		}
	}
	
	@Override
	protected boolean isMouseEventTarget() {
		return false;
	}
}
