package com.pip.uieditor.figures.classic;

import org.eclipse.draw2d.RectangleFigure;

import com.pip.uieditor.model.classic.GWidget;

public class GWidgetFigure extends RectangleFigure{
	
	private GWidget widget;
	
	public GWidgetFigure(GWidget widget) {
		this.widget = widget;
	}

	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
	
}
