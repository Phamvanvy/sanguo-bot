package com.pip.uieditor.model.propertydescriptor;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.uieditor.model.AnchorPoint;
import com.pip.uieditor.util.AnchorUtil;

public class AnchorsPropertyDescriptor extends PropertyDescriptor {

	public AnchorsPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		setLabelProvider(new AnchorLableProvider());
	}
	
	
	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		return new AnchorPointCellEditor(parent);
	}
	
	static class AnchorLableProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if(element == null)
				return "";
			List<AnchorPoint> anchors = (List<AnchorPoint>)element;
			if(anchors.size() == 0)
				return "";
			try {
				return AnchorUtil.anchorPointListToText(anchors);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}
	}
}
