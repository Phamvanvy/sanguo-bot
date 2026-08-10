package com.pip.uieditor.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;

public class ResizeFigure extends RectangleFigure{

	@Override
	public void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		graphics.drawText("(" + getSize().width + ","
				+ getSize().height + ")", this.getLocation().x,
				this.getLocation().y);
	}
	
	
}
