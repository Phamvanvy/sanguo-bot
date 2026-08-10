package com.pip.servermgr.client;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.servermgr.data.ServerGroup;

public class ServerGroupInput implements IEditorInput {
	protected ServerGroup group;

	public ServerGroupInput(ServerGroup g) {
		group = g;
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
		return group.parent.toString() + " : " + group.toString();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getName();
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ServerGroupInput)) {
			return false;
		}
		return group.equals(((ServerGroupInput)o).group);
	}

}
