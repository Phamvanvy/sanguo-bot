package com.pip.uieditor.editor;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Text;

import com.pip.j0ide.Settings;

public class AttachScriptDialog extends Dialog {
	private Text text;
	
	private String script;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AttachScriptDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 3;
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lbScript = new Label(container, SWT.NONE);
		lbScript.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbScript.setText("\u811A\u672C:");
		
		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if(this.script != null) {
			text.setText(this.script);
		}
		
		Button btnFile = new Button(container, SWT.NONE);
		btnFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				dlg.setFilterExtensions(new String[]{"*.gtl"});
				dlg.setFilterNames(new String[] { "脚本文件(*.gtl)" });
				dlg.setFilterPath(Settings.workingDir.getAbsolutePath());
				String file = dlg.open();
				if(file != null) {
					if(file.startsWith(Settings.workingDir.getAbsolutePath())){
						text.setText(file.substring(Settings.workingDir.getAbsolutePath().length()));
					}
					else 
						MessageDialog.openError(getShell(), "错误", "文件必须在项目中");
				}
			}
		});
		btnFile.setText("...");
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	public String getScript() {
		return this.script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	
	@Override
	public void okPressed() {
		this.script = text.getText();
		if(this.script == null || this.script.length() == 0) {
			MessageDialog.openError(getShell(), "错误", "没有指定相关文件");
			return;
		}
		String filename = Settings.workingDir.getAbsolutePath() + this.script;
		File file = new File(filename);
		if(!file.exists()) {
			MessageDialog.openError(getShell(), "错误", "指定的文件不存在");
			return;
			
		}
		if(!file.isFile()) {
			MessageDialog.openError(getShell(), "错误", "必须指定相关文件");
			return;

		}
		super.okPressed();
	}
}
