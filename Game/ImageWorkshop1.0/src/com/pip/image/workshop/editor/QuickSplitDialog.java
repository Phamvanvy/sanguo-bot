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

public class QuickSplitDialog extends Dialog {
	private int row;
	private int col;
	private Spinner spinColumn, spinRow;
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public QuickSplitDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label lblRow = new Label(container, SWT.NONE);
		lblRow.setText("行数：");

		spinRow = new Spinner(container, SWT.BORDER);
		spinRow.setMinimum(1);
		spinRow.setSelection(1);
		spinRow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label lblColumn = new Label(container, SWT.NONE);
		lblColumn.setText("列数：");

		spinColumn = new Spinner(container, SWT.BORDER);
		spinColumn.setMinimum(1);
		spinColumn.setSelection(1);
		spinColumn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		//
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
	}

//	/**
//	 * Return the initial size of the dialog
//	 */
//	@Override
//	protected Point getInitialSize() {
//		return new Point(224, 132);
//	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("快速切分");
	}
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			row = spinRow.getSelection();
			col = spinColumn.getSelection();
		}
		super.buttonPressed(buttonId);
	}

}
