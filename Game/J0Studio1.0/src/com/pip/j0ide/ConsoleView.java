package com.pip.j0ide;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class ConsoleView extends ViewPart {
	private Text console;
	public static final String ID = "com.pip.j0ide.ConsoleView"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));

		console = new Text(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		console.setLayoutData(BorderLayout.CENTER);

		createActions();
		initializeToolBar();
		initializeMenu();
		
		Application.getInstance().setConsole(this);
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void setFocus() {
	}

	public void println(String msg) {
		console.append(msg + "\n");
	}
	
	public void clear() {
		console.setText("");
	}
	
	public void asyncPrintln(String msg) {
		getSite().getShell().getDisplay().asyncExec(new PrintJob(msg));
	}

	public void syncPrintln(String msg) {
		getSite().getShell().getDisplay().syncExec(new PrintJob(msg));
	}

	class PrintJob implements Runnable {
		private String msg;
		
		public PrintJob(String m) {
			msg = m;
		}
		
		public void run() {
			println(msg);
		}
	}
}
