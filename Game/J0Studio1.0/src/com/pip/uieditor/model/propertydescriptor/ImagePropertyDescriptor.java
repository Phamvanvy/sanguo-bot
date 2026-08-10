package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.uieditor.model.ImageData;

public class ImagePropertyDescriptor extends PropertyDescriptor {

	public ImagePropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		setLabelProvider(new ImageLabelProvider());
	}
	
	
	
	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		ImageCellEditor editor = new ImageCellEditor(parent);
		return editor;
	}



	static class ImageLabelProvider extends LabelProvider{

		@Override
		public String getText(Object element) {
			if(element == null)
				return "";
			ImageData data = (ImageData)element;
			return data.getFile()+ "," + data.getFrame();
		}
		
	}
}
