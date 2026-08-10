package com.pip.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ExceptionDialog extends Dialog {
	private Text messageText;
	private String message;
	private String title;

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ExceptionDialog(Shell parentShell, String title, String message, Throwable exception) {
		super(parentShell);
		this.title = title;
		this.message = message;
		if (message == null) {
			this.message = "";
		}
		if (exception != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			pw.flush();
			if (this.message.length() > 0) {
				this.message += "\n" + sw.toString();
			} else {
				this.message = sw.toString();
			}
		}
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		messageText = new Text(container, SWT.WRAP | SWT.MULTI);
		messageText.setEditable(false);
		messageText.setText(message);
		
		final GridData gd_messageText = new GridData(SWT.FILL, SWT.FILL, true, true);
		messageText.setLayoutData(gd_messageText);
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(653, 472);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}
}
