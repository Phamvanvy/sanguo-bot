package com.pip.image.workshop;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.pip.util.SWTUtils;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "com.pip.image.workshop.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	public boolean preShutdown() {
		try {
			getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow().getActivePage().savePerspective();
		} catch (Exception e) {
		}
		return super.preShutdown();
	}

	@Override
	public void eventLoopException(Throwable exception) {
		if(exception instanceof AssertionFailedException){
			super.eventLoopException(exception);
		}else{
			SWTUtils.showError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "´íÎó", exception);
		}
	}
}
