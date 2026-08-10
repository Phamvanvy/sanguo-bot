package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.pip.uieditor.figures.AnimateRegionFigure;
import com.pip.uieditor.figures.ColorRegionFigure;
import com.pip.uieditor.figures.CustomeRegionFigure;
import com.pip.uieditor.figures.ExtendedRegionFigure;
import com.pip.uieditor.figures.GameSpriteRegionFigure;
import com.pip.uieditor.figures.ImageRegionFigure;
import com.pip.uieditor.figures.ModelRegionFigure;
import com.pip.uieditor.figures.RegionFigure;
import com.pip.uieditor.figures.StringRegionFigure;
import com.pip.uieditor.model.AnimateRegion;
import com.pip.uieditor.model.ColorRegion;
import com.pip.uieditor.model.CustomeRegion;
import com.pip.uieditor.model.ExtendedRegion;
import com.pip.uieditor.model.GameSpriteRegion;
import com.pip.uieditor.model.ImageRegion;
import com.pip.uieditor.model.ModelRegion;
import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.StringRegion;

public class RegionPart extends UIObjectPart {

	public RegionPart(Region region) {
		super(region);
	}
	
	@Override
	public Region getModel() {
		return (Region)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		Region region = getModel();
		RegionFigure figure = null;
		if(region instanceof StringRegion) {
			figure = new StringRegionFigure((StringRegion)region);
		} else if(region instanceof ColorRegion) {
			figure = new ColorRegionFigure((ColorRegion)region);
		} else if(region instanceof ImageRegion) {
			figure =  new ImageRegionFigure((ImageRegion)region);
		} else if( region instanceof AnimateRegion) {
			figure = new AnimateRegionFigure((AnimateRegion)region);
		} else if(region instanceof ModelRegion) {
			figure = new ModelRegionFigure((ModelRegion)region);
		} else if(region instanceof GameSpriteRegion) {
			figure = new GameSpriteRegionFigure((GameSpriteRegion)region);
		} else if(region instanceof CustomeRegion) {
			figure = new CustomeRegionFigure((CustomeRegion)region);
		} else if(region instanceof ExtendedRegion) {
			figure = new ExtendedRegionFigure((ExtendedRegion)region);
		} else {
			throw new IllegalArgumentException();
		}
		if(!region.isVisible()) {
			figure.setVisible(false);
		}
		if(region.isAvaliable()) {
			figure.setLocation(region.getLocation());
			figure.setSize(region.getSize());
		}
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("size")) {
			getFigure().setSize((Dimension)evt.getNewValue());
		}
		if (evt.getPropertyName().equals("location")) {
			getFigure().setLocation((Point)evt.getNewValue());
		} else if(evt.getPropertyName().equals("size")) {
			getFigure().setSize((Dimension)evt.getNewValue());
		} else if(evt.getPropertyName().equals("showInEditing")) {
			getFigure().setVisible((Boolean)evt.getNewValue());
		} else if(evt.getPropertyName().equals("trans")) {
			getFigure().revalidate();
//			getFigure().repaint();
		} else if(evt.getPropertyName().equals("anchorPoints")) {
			getFigure().revalidate();
//			getFigure().repaint();
		} else if(evt.getPropertyName().equals("fill")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("font")) {
			getFigure().revalidate();
			getFigure().repaint();
//			getFigure().getParent().revalidate();
//			getFigure().repaint();
		} else if(evt.getPropertyName().equals("lineGap")) {
			getFigure().revalidate();
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("hookPoint")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("hookAnchor")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("shadow")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("link")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("color")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("shadowColor")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("backgroundColor")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("text")) {
			getFigure().revalidate();
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("linkColor")) {
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("imageData")) {
			getFigure().revalidate();
			getFigure().repaint();
		}
//		getFigure().revalidate();
//		getFigure().getParent().repaint();
	}
	
}
