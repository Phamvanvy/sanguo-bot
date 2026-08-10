package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;


public class StatePropertyDescriptor extends PropertyDescriptor {
	
	public StatePropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		WidgetStateCellEditor editor = new WidgetStateCellEditor(parent);
		return editor;
	}
	
	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider(){
			@Override
			public String getText(Object element) {
				Integer state = (Integer)element;
				if(state != null) {
					return Integer.toBinaryString(state);
				}
				return null;
			}
		};
	}

}


