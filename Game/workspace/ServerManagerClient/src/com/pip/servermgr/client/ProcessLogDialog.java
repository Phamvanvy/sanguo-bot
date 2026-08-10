package com.pip.servermgr.client;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProcessLogDialog extends Dialog {
	private Text textLogPath;
	private Text textEditor;
	
	public String logPath;
	public String editorApp;
	public int processType;   // 0 - 使用外部程序打开，1 - 使用内置分析器打开
	private Button buttonUseEditor;
	private Button buttonUseInternal;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ProcessLogDialog(Shell parentShell) {
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
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.NONE);
		label.setText("日志文件：");

		textLogPath = new Text(container, SWT.BORDER);
		textLogPath.setEditable(false);
		final GridData gd_textLogPath = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		textLogPath.setLayoutData(gd_textLogPath);

		buttonUseEditor = new Button(container, SWT.RADIO);
		buttonUseEditor.setSelection(true);
		buttonUseEditor.setText("用系统编辑器打开：");

		textEditor = new Text(container, SWT.BORDER);
		textEditor.setText("C:\\Program Files\\IDM Computer Solutions\\UltraEdit\\Uedit32.exe");
		final GridData gd_textEditor = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textEditor.setLayoutData(gd_textEditor);

		final Button buttonBrowseEditor = new Button(container, SWT.NONE);
		buttonBrowseEditor.setText("浏览...");

		buttonUseInternal = new Button(container, SWT.RADIO);
		buttonUseInternal.setText("内置日志分析工具");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		setDefaults();
		
		return container;
	}
	
	private void setDefaults() {
		if (logPath != null) {
			textLogPath.setText(logPath);
		}
		if (editorApp != null) {
			textEditor.setText(editorApp);
		}
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
				"退出", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(877, 239);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("处理日志");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (buttonUseEditor.getSelection()) {
				processType = 0;
				editorApp = textEditor.getText();
				if (!new File(editorApp).exists()) {
					MessageDialog.openError(getShell(), "错误", "选择的程序不存在。");
					return;
				}
			} else if (buttonUseInternal.getSelection()) {
				processType = 1;
			}
		}
		super.buttonPressed(buttonId);
	}
}
