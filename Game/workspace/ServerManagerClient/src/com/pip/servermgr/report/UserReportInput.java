package com.pip.servermgr.report;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.servermgr.client.ClientPlugin;
import com.pip.servermgr.data.Server;
import com.pip.servermgr.data.ServerGroup;

public class UserReportInput implements IEditorInput {
	protected File dataFile;

	public UserReportInput(File f) {
		dataFile = f;
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return ClientPlugin.getDefault().getImageRegistry().getDescriptor("servergroup");
	}

	public String getName() {
		return dataFile.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return dataFile.getAbsolutePath();
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof UserReportInput)) {
			return false;
		}
		return this == o;
	}

}
