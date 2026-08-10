/**
 * 
 */
package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.image.workshop.editor.PieceColorDialog;
import com.pip.uieditor.model.ARGB;
import com.pip.uieditor.model.text.RichTextParser;

/**
 * @author Jeffrey
 *
 */
public class RichTextPropertyDescriptor extends TextPropertyDescriptor{
	public RichTextPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
	
	public CellEditor createPropertyEditor(Composite parent) {
		return new RichTextCellEditor(parent);
	}
	
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				return RichTextParser.escapeString((String)element);
			}
		};
	}
}