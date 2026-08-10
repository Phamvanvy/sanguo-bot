package com.pip.j0ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.j0ide.data.Model;
import com.pip.j0ide.data.ProjectData;

public class GlobalVarInput implements IEditorInput {
	protected ProjectData project;

	public GlobalVarInput(ProjectData m) {
		project = m;
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageRegistry().getDescriptor("model");
	}

	public String getName() {
		return "全局变量";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "全局变量编辑器";
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof GlobalVarInput)) {
			return false;
		}
		return project.equals(((GlobalVarInput)o).project);
	}
}
