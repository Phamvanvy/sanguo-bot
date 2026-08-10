package com.pip.uieditor.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import com.pip.uieditor.model.Container;
import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.TableColumn;
import com.pip.uieditor.model.Widget;

public class PartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof Screen) {
			return new ScreenPart((Screen)model);
		} else if(model instanceof Container) {
			return new ContainerPart((Container)model);
		} else if(model instanceof Widget) {
			return new WidgetPart((Widget)model);
		} else if(model instanceof Region) {
			return new RegionPart((Region)model);
		} else if(model instanceof TableColumn) {
			return new TableColumnPart((TableColumn)model);
		} else
			throw new IllegalArgumentException();
	}

}
