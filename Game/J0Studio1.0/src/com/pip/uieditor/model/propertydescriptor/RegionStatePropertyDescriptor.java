package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class RegionStatePropertyDescriptor extends PropertyDescriptor {
	public RegionStatePropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		RegionMaskCellEditor editor = new RegionMaskCellEditor(parent);
		return editor;
	}
	
	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider(){
			@Override
			public String getText(Object element) {
				Long state = (Long)element;
				if(state != null) {
					return Long.toBinaryString(state);
				}
				return null;
			}
		};
	}
}
