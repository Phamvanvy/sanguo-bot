package com.pip.j0ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.j0ide.data.Model;

public class ModelInput implements IEditorInput {
	protected Model model;

	public ModelInput(Model m) {
		model = m;
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
		return model.toString();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return model.comments;
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ModelInput)) {
			return false;
		}
		return model.equals(((ModelInput)o).model);
	}
}
