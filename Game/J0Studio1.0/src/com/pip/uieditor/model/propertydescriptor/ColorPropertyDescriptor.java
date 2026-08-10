/**
 * 
 */
package com.pip.uieditor.model.propertydescriptor;

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

/**
 * @author Jeffrey
 *
 */
public class ColorPropertyDescriptor extends TextPropertyDescriptor{
	public ColorPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
	
	public CellEditor createPropertyEditor(Composite parent) {
		return new ColorCellEditor(parent);
	}
	
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				ARGB argb = (ARGB)element;
				if(argb != null) {
					return Integer.toHexString(argb.toInt());
//					return argb.alpha + "," + argb.red + "," + argb.green + "," +argb.blue;
				}
				return null;
			}
			
		};
	}
}

class ColorCellEditor extends DialogCellEditor {
	public ColorCellEditor(Composite parent) {
		super(parent);
//		super.setValidator(new ICellEditorValidator() {
//			
//			@Override
//			public String isValid(Object value) {
//				int[] ss = NumberUtil.parseInts((String)value);
//				if(ss.length != 4)
//					return "格式错误";
//				for(int i = 0; i < 4; i++) {
//					if(ss[i] < 0 || ss[i] > 255)
//						return "格式错误";
//				}
//				return null;
//			}
//		});
	}

	@Override
	protected Object doGetValue() {
		long c = Long.parseLong((String)super.doGetValue(), 16);
//		int c = Integer.parseInt((String)super.doGetValue(), 16);
		return new ARGB((int)(c&0xFFFFFFFF));
//		int[] vs = NumberUtil.parseInts((String)super.doGetValue());
//		return new ARGB(vs[0], vs[1], vs[2], vs[3]);
	}

	@Override
	protected void doSetValue(Object value) {
		ARGB argb = (ARGB)value;
		super.doSetValue(Integer.toHexString(argb.toInt()));
//		super.doSetValue(argb.alpha + "," + argb.red + "," + argb.green + "," + argb.blue);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		ARGB rgb = (ARGB)getValue();
		PieceColorDialog dlg = new PieceColorDialog(cellEditorWindow.getShell(), rgb.toInt(), null);
		if(dlg.open() == Window.OK) {
			return new ARGB(dlg.getColor());
		} else {
			return rgb;
		}
	}
}