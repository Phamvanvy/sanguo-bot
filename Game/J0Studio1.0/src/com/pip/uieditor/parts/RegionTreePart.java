package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;

import com.pip.uieditor.model.Region;

public class RegionTreePart extends UIObjectTreePart {

	public RegionTreePart(Region region) {
		super(region);
	}
	
	public Region getModel() {
		return (Region)super.getModel();
	}
	
	
	
	@Override
	protected String getText() {
		return "(" + getModel().getLayerString() + ")" + getModel().getId();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refreshVisuals();
	}

}
