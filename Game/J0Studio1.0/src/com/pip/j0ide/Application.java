package com.pip.j0ide;

import java.net.URL;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.pip.j0ide.data.*;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	protected static Application instance;
	protected ProjectData projectData;
	protected ConsoleView console;
	protected ErrorsView errorsView;
	protected String[] args;

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		instance = this;
		projectData = new ProjectData();

		Display display = PlatformUI.createDisplay();
		try {
			args = (String[])context.getArguments().get("application.args");
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	public static Application getInstance() {
		return instance;
	}
	
	public ProjectData getProjectData() {
		return projectData;
	}
	
	public ConsoleView getConsole() {
		return console;
	}

	public void setConsole(ConsoleView console) {
		this.console = console;
	}
	
	public ErrorsView getErrorsView() {
		return errorsView;
	}
	
	public void setErrorsView(ErrorsView view) {
		errorsView = view;
	}

	public String[] getArgs() {
		return args;
	}
}
