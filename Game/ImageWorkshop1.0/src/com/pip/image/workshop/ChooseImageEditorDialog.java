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
	private Text tfETCTool;
	private Text tfPvrTexTool;
	private Text tfArg;
	private Text tfCmd;
	private String cmd;
	private String arg;
	private String pvrTexTool;
	private String etcTool;
	
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
	
	public String getPvrTexTool() {
		return pvrTexTool;
	}
	
	public void setPvrTexTool(String arg) {
		pvrTexTool = arg;
	}
	
	public String getETCTool() {
		return etcTool;
	}
	
	public void setETCTool(String arg) {
		etcTool = arg;
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
		lblCmd.setText("图片编辑器(&P)：");

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
		browseBtn.setText("浏览(&B)...");

		final Label lblArg = new Label(container, SWT.NONE);
		lblArg.setLayoutData(new GridData());
		lblArg.setText("图片编辑器参数(&A)：");

		tfArg = new Text(container, SWT.BORDER);
		final GridData gd_tfArg = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		tfArg.setLayoutData(gd_tfArg);
		tfArg.setText(arg);

		final Label pvrTexToolLabel = new Label(container, SWT.NONE);
		pvrTexToolLabel.setText("PVR压缩工具(&V)：");

		tfPvrTexTool = new Text(container, SWT.BORDER);
		final GridData gd_tfPvrTexTool = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tfPvrTexTool.setLayoutData(gd_tfPvrTexTool);
		tfPvrTexTool.setText(pvrTexTool);

		final Button browseBtn2 = new Button(container, SWT.NONE);
		browseBtn2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onSelectPvrTexTool();
			}
		});
		final GridData gd_browseBtn2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
		browseBtn2.setLayoutData(gd_browseBtn2);
		browseBtn2.setText("浏览(&R)..");

		final Label label = new Label(container, SWT.NONE);
		label.setText("ETC压缩工具(&E)：");

		tfETCTool = new Text(container, SWT.BORDER);
		final GridData gd_tfETCTool = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tfETCTool.setLayoutData(gd_tfETCTool);
		tfETCTool.setText(etcTool);

		final Button browseBtn3 = new Button(container, SWT.NONE);
		browseBtn3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onSelectETCTool();
			}
		});
		final GridData gd_browseBtn3 = new GridData(SWT.FILL, SWT.CENTER, false, false);
		browseBtn3.setLayoutData(gd_browseBtn3);
		browseBtn3.setText("浏览(&T)...");
		
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
		return new Point(588, 244);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("工具设置");
	}
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			cmd = tfCmd.getText();
			arg = tfArg.getText();
			pvrTexTool = tfPvrTexTool.getText();
			etcTool = tfETCTool.getText();
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
	
	private void onSelectPvrTexTool() {
		FileDialog dlg = new FileDialog(this.getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.exe", "*.*" });
		dlg.setFilterNames(new String[] { "可执行文件(*.exe)", "所有文件(*.*)" });
		String cmdFile = dlg.open();
		if (cmdFile != null) {
			tfPvrTexTool.setText(cmdFile);
		}
	}
	
	private void onSelectETCTool() {
		FileDialog dlg = new FileDialog(this.getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.exe", "*.*" });
		dlg.setFilterNames(new String[] { "可执行文件(*.exe)", "所有文件(*.*)" });
		String cmdFile = dlg.open();
		if (cmdFile != null) {
			tfETCTool.setText(cmdFile);
		}
	}
}
