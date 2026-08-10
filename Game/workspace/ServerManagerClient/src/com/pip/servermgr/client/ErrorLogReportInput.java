package com.pip.servermgr.client;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.servermgr.data.ServerGroup;

public class ErrorLogReportInput implements IEditorInput {
	public List<ExceptionRecord> exceptionRecords;
	public LongLogReport longReport;

	public ErrorLogReportInput(List<ExceptionRecord> exceptionRecords, LongLogReport longReport) {
		this.exceptionRecords = exceptionRecords;
		this.longReport = longReport;
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
		return "¥ÌŒÛ»’÷æ";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getName();
	}
	
	public boolean equals(Object o) {
		return this == o;
	}
}
