package com.pip.uieditor.policies;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

import com.pip.uieditor.figures.ResizeFigure;

public class ResizeEditPolicy extends ResizableEditPolicy {

	
	protected IFigure createDragSourceFeedbackFigure() {
		ResizeFigure r = new ResizeFigure();
		FigureUtilities.makeGhostShape(r);
		r.setLineStyle(Graphics.LINE_DOT);
		r.setForegroundColor(ColorConstants.white);
		r.setBounds(getInitialFeedbackBounds());
		r.validate();
		addFeedback(r);
		return r;
	}
}
