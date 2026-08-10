package com.pip.uieditor.parts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.EditPolicy;

import com.pip.uieditor.model.Screen;
import com.pip.uieditor.policies.ContainerModelEditPolicy;

public class ScreenPart extends ContainerPart {

	public ScreenPart(Screen screen) {
		super(screen);
	}
	
	@Override
	public Screen getModel() {
		return (Screen)super.getModel();
	}
	
	@Override
	public List getModelChildren() {
		return getModel().getChildren();
	}
	
	@Override
	protected IFigure createFigure() {
		Screen screen = (Screen)getModel();
		RectangleFigure figure = new RectangleFigure() {
			protected boolean useLocalCoordinates() {
				return true;
			}
		};
		figure.setAlpha(125);  //设置成透明，让底下的格点能够显示出来
        figure.setBounds(screen.getBounds());
        figure.setLayoutManager(new XYLayout());
        return figure;
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ContainerModelEditPolicy());
	}
}
