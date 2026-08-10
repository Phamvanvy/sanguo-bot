package com.pip.j0ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.j0ide.data.ProjectData;

public class RevisionInput implements IEditorInput {
	protected ProjectData project;

	public RevisionInput(ProjectData m) {
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
		return "目标";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "目标编辑器";
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RevisionInput)) {
			return false;
		}
		return project.equals(((RevisionInput)o).project);
	}
}
