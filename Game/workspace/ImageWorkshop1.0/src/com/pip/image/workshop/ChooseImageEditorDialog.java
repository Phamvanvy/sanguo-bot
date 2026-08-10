package com.pip.image.workshop;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ChooseImageEditorDialog extends Dialog {
	private Text tfArg;
	private Text tfCmd;
	private String cmd;
	private String arg;
	
	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ChooseImageEditorDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);

		final Label lblCmd = new Label(container, SWT.NONE);
		lblCmd.setText("程序(&P)：");

		tfCmd = new Text(container, SWT.BORDER);
		final GridData gd_tfCmd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tfCmd.setLayoutData(gd_tfCmd);
		tfCmd.setText(cmd);

		final Button browseBtn = new Button(container, SWT.NONE);
		browseBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onSelectProgram();
			}
		});
		browseBtn.setText("浏览(B)...");

		final Label lblArg = new Label(container, SWT.NONE);
		lblArg.setLayoutData(new GridData());
		lblArg.setText("参数(&A)：");

		tfArg = new Text(container, SWT.BORDER);
		final GridData gd_tfArg = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		tfArg.setLayoutData(gd_tfArg);
		tfArg.setText(arg);
		
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
	@Override
	protected Point getInitialSize() {
		return new Point(439, 132);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("设置图片编辑器");
	}
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			cmd = tfCmd.getText();
			arg = tfArg.getText();
		}
		super.buttonPressed(buttonId);
	}
	
	private void onSelectProgram() {
		FileDialog dlg = new FileDialog(this.getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.exe", "*.*" });
		dlg.setFilterNames(new String[] { "可执行文件(*.exe)", "所有文件(*.*)" });
		String cmdFile = dlg.open();
		if (cmdFile != null) {
			tfCmd.setText(cmdFile);
		}
	}
}
