package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.uieditor.model.AnimateData;

public class AnimatePropertyDescriptor extends PropertyDescriptor {
	public AnimatePropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		setLabelProvider(new AnimateLabelProvider());
	}
	
	
	
	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		AnimateCellEditor editor = new AnimateCellEditor(parent);
		return editor;
	}



	static class AnimateLabelProvider extends LabelProvider{

		@Override
		public String getText(Object element) {
			if(element == null)
				return "";
			AnimateData data = (AnimateData)element;
			return data.getFile()+ "," + data.getIndex();
		}
		
	}
}
