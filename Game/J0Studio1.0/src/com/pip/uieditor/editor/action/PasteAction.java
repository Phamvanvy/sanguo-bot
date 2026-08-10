package com.pip.uieditor.editor.action;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.pip.uieditor.editor.clipboard.Clipboard;
import com.pip.uieditor.model.Container;
import com.pip.uieditor.model.Widget;
import com.pip.uieditor.parts.ContainerPart;

public class PasteAction extends SelectionAction {

	public PasteAction(IWorkbenchPart part) {
		super(part);
		setId(ActionFactory.PASTE.getId());
		setText("Paste");
		setToolTipText("Paste");
	}
	
	@Override
	protected boolean calculateEnabled() {
		List l = getSelectedObjects();
		if(l.size() == 1 && Clipboard.instance().getObject() != null) {
			Object o = getSelectedObjects().get(0);
			if(o instanceof ContainerPart) {
				return true;
			}
			return false;
		} else {
			return false;
		}
	}
	
	@Override
	public void run() {
		Object o = getSelectedObjects().get(0);
		ContainerPart part = (ContainerPart)o;
		Container container = part.getModel();
		Widget widget = (Widget)Clipboard.instance().getObject();
		widget = widget.clone();
		
		// 扫描需要新增加的对象，名字不能和现有的名字重叠
		fixNames(widget, container.getScreen(), widget);

		container.addChild(widget);
	}

	protected void fixNames(Widget widget, Widget container1, Widget container2) {
		if (widget.getName() != null) {
			int id = 0;
			String name = widget.getName();
			while (true) {
				boolean dup = false;
				if (container1.findWidget(name) != null) {
					dup = true;
				}
				if (container2.findWidget(name) != null && container2.findWidget(name) != widget) {
					dup = true;
				}
				if (dup) {
					id++;
					name = widget.getName() + "_" + id;
				} else {
					widget.setName(name);
					break;
				}
			}
		}
		for (int i = 0; i < widget.getChildCount(); i++) {
			fixNames(widget.getChild(i), container1, container2);
		}
	}
}
