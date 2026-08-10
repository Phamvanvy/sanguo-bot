package com.pip.image.workshop;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import java.io.*;

public class FileInput implements IEditorInput {
	protected File file;

	public FileInput(File f) {
		file = f;
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkshopPlugin.getDefault().getImageRegistry().getDescriptor("image");
	}

	public String getName() {
		return file.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return file.getAbsolutePath();
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FileInput)) {
			return false;
		}
		return file.equals(((FileInput)o).file);
	}

	public File getFile() {
		return file;
	}
}
