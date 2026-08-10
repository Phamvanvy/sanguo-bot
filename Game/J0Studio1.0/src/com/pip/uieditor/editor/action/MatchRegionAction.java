package com.pip.uieditor.editor.action;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.internal.InternalImages;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.Widget;
import com.pip.uieditor.parts.ScreenPart;
import com.pip.uieditor.parts.WidgetPart;

public class MatchRegionAction extends SelectionAction {

	public static final String ID = "com.pip.uieditor.action.matchregion";

	public MatchRegionAction(IWorkbenchPart part) {
		super(part);
		initUI();
	}
	
	protected void initUI() {
		setId(ID);
		setText("Match");
		setToolTipText("");
		setImageDescriptor(InternalImages.DESC_MATCH_SIZE);
		setDisabledImageDescriptor(InternalImages.DESC_MATCH_SIZE_DIS);
	}

	@Override
	protected boolean calculateEnabled() {
		List l = getSelectedObjects();
		Object o = null;
		if(l.size() == 1) {
			 o = getSelectedObjects().get(0);
			 return o instanceof WidgetPart && !(o instanceof ScreenPart);
		}
		return false;
//		List l = getSelectedObjects();
//		if(l.size() != 1)
//			return false;
//		Object o = l.get(0);
//		if(o instanceof ContainerPart && !(o instanceof ScreenPart)) {
//			return true;
//		}
//		return false;
	}
	
	@Override
	public void run() {
		Rectangle rect = new Rectangle();
		Widget widget = null;
		if(getSelectedObjects().size() > 0) {
			Object o = getSelectedObjects().get(0);
			if(o instanceof WidgetPart) {
				widget = ((WidgetPart)o).getModel();
			}
			else {
				return;
			}
		}
		List l = widget.getRegions();
		for(int i = 0; i < l.size(); i++) {
			Region region = (Region)l.get(i);
			if(region.isAvaliable() && region.isValid()) {
				rect.union(new Rectangle(region.getLocation(), region.getSize()));
			}
		}
		if(!rect.equals(0, 0, 0, 0)) {
			widget.setSize(new Dimension(rect.width, rect.height));
		}
	}
}
