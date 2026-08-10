package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class IntegerInputDialog extends Dialog {
	private int value;
	private Spinner spinInput;
	private int min, max;
	private String title;
	private String hint;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public IntegerInputDialog(Shell parentShell, int min, int max, int value, String title, String hint) {
		super(parentShell);
		this.min = min;
		this.max = max;
		this.value = value;
		this.title = title;
		this.hint = hint;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label lblValue = new Label(container, SWT.NONE);
		lblValue.setText(hint + "：");

		spinInput = new Spinner(container, SWT.BORDER);
		spinInput.setMinimum(min);
		spinInput.setMaximum(max);
		spinInput.setSelection(value);
		spinInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(269, 153);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			value = spinInput.getSelection();
		}
		super.buttonPressed(buttonId);
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
