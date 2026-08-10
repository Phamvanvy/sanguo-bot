package com.pip.uieditor.editor.action;

import java.util.List;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.Widget;
import com.pip.uieditor.parts.RegionTreePart;

public class DownRegionAction extends Action implements ISelectionChangedListener {
	
	public static final String ID = "com.pip.uieditor.editor.action.downregion";
	
	private EditPartViewer viewer;
	
	public DownRegionAction(EditPartViewer viewer) {
		this.viewer = viewer;
		initUI();
		viewer.addSelectionChangedListener(this);
	}
	
	protected void initUI() {
		setId(ID);
		setText("Down Region");
		setToolTipText("Down Region");
		setEnabled(false);
	}
	
	
	@Override
	public void run() {
		Object o = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
		RegionTreePart part = (RegionTreePart)o;
		Region region = part.getModel();
		Widget widget = region.getParent();
		widget.downRegion(region);
	}
	

	
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection select = (IStructuredSelection) selection;
			List l = select.toList();
			if(l.size() == 1) {
				Object o = l.get(0);
				if(o instanceof RegionTreePart) {
					RegionTreePart treePart = (RegionTreePart)o;
					if(!treePart.getModel().isRequire()) {
						setEnabled(true);
						return;
					}
				}
			}
		}
		setEnabled(false);
	}
}
