package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.uieditor.util.NumberUtil;

public class InsetsPropertyDescriptor extends TextPropertyDescriptor {
	
	public InsetsPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
	
	public CellEditor createPropertyEditor(Composite parent) {
		return new InsetsCellEditor(parent);
	}
	
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				Insets rect = (Insets)element;
				if(rect != null) {
					return rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom;
				}
				return null;
			}
			
		};
	}
}

class InsetsCellEditor extends TextCellEditor {
	public InsetsCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected Object doGetValue() {
		int[] vs = NumberUtil.parseInts((String)super.doGetValue());
		return new Insets(vs[1], vs[0], vs[3], vs[2]);
	}

	@Override
	protected void doSetValue(Object value) {
		Insets p = (Insets)value;
		super.doSetValue(p.left + "," + p.top + "," + p.right + "," + p.bottom);
	}
}