package com.pip.uieditor.layout;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class ScalableLayout implements LayoutManager {

	private HashMap constraints = new HashMap();
	
	@Override
	public Object getConstraint(IFigure figure) {
		return constraints.get(figure);
	}

	@Override
	public Dimension getMinimumSize(IFigure figure, int wHint,
			int hHint) {
		return getPreferredSize(figure, wHint, hHint);
	}

	@Override
	public Dimension getPreferredSize(IFigure figure, int wHint,
			int hHint) {
		return null;
	}

	@Override
	public void invalidate() {
		
	}

	@Override
	public void layout(IFigure container) {
		Rectangle rect = container.getClientArea();
		Iterator ite = container.getChildren().iterator();
		while(ite.hasNext()) {
			IFigure figure =  (IFigure)ite.next();
			AlignData data = (AlignData)getConstraint(figure);
			if(data == null)
				continue;
			figure.setBounds(data.getBounds(rect.width, rect.height).translate(rect.getLocation()));
		}
	}

	@Override
	public void remove(IFigure figure) {
		constraints.remove(figure);
	}

	@Override
	public void setConstraint(IFigure figure, Object constraint) {
		constraints.put(figure, constraint);
	}

}
