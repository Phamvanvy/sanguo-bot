package com.pip.j0ide;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.j0ide.data.Variable;

public class VariableDialog extends Dialog {

	private Text tfValue;
	private Text tfName;
	private String name = "";
	private String value = "";
	
	private boolean fornew;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public VariableDialog(Shell parentShell, boolean fornew) {
		super(parentShell);
		this.fornew = fornew;
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

		final Label lblName = new Label(container, SWT.NONE);
		lblName.setText("名称：");

		tfName = new Text(container, SWT.BORDER);
		final GridData gd_tfName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tfName.setLayoutData(gd_tfName);
		tfName.addFocusListener(AutoSelectAll.instance);
		tfName.setText(name);

		final Label lblValue = new Label(container, SWT.NONE);
		lblValue.setText("值：");

		tfValue = new Text(container, SWT.BORDER);
		final GridData gd_tfValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tfValue.setLayoutData(gd_tfValue);
		tfValue.addFocusListener(AutoSelectAll.instance);
		tfValue.setText(value);
		
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

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(341, 145);
	}

	public void setValue(Variable var) {
		name = var.name;
		value = var.value;
	}
	
	public Variable getValue() {
		Variable var = new Variable();
		var.name = name;
		var.value = value;
		return var;
	}
	
	private void detectInput() throws Exception {
		if (tfName.getText().length() == 0) {
			tfName.setFocus();
			throw new Exception("必须输入名称。");
		}
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				detectInput();
			} catch (Exception e) {
				MessageDialog.openError(this.getParentShell(), "输入错误", e.getMessage());
				return;
			}
			name = tfName.getText();
			value = tfValue.getText();
		}
		super.buttonPressed(buttonId);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (fornew) {
			newShell.setText("添加变量");
		} else {
			newShell.setText("编辑变量");
		}
	}
}
