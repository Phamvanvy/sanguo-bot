package com.pip.mapeditor;

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
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ImportMapDialog extends Dialog {
	private Text tfFileName;
	private Label lblPercent;
	private Scale scale;
	private Button btnOptionDrop, btnOptionNew;
	
	private String fileName = "";
	private int tolerance;
	private boolean ignoreUnmatched;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getTolerance() {
		return tolerance;
	}

	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	public boolean isIgnoreUnmatched() {
		return ignoreUnmatched;
	}

	public void setIgnoreUnmatched(boolean ignoreUnmatched) {
		this.ignoreUnmatched = ignoreUnmatched;
	}

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ImportMapDialog(Shell parentShell) {
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

		final Label label = new Label(container, SWT.NONE);
		label.setText("文件：");

		tfFileName = new Text(container, SWT.BORDER);
		tfFileName.setText("newmap");
		final GridData gd_newmapText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tfFileName.setLayoutData(gd_newmapText);

		final Button browseButton = new Button(container, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.png", "*.gif", "*.*" });
				dlg.setFilterNames(new String[] { "PNG图片文件(*.png)", "GIF图片文件(*.gif)", "所有文件(*.*)" });
				String file = dlg.open();
				if (file != null) {
					tfFileName.setText(file);
				}
			}
		});
		browseButton.setText("浏览...");
		
		tfFileName.setText(fileName);

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("容错率：");

		scale = new Scale(container, SWT.NONE);
		scale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lblPercent.setText(scale.getSelection() + "%");
			}
		});
		scale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		scale.setSelection(tolerance);

		lblPercent = new Label(container, SWT.NONE);
		lblPercent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		lblPercent.setText(tolerance + "%");

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("不匹配图块：");

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		composite.setLayout(gridLayout_1);

		btnOptionNew = new Button(composite, SWT.RADIO);
		btnOptionNew.setSelection(!ignoreUnmatched);
		btnOptionNew.setText("新建贴图");

		btnOptionDrop = new Button(composite, SWT.RADIO);
		final GridData gd_btnOptionDrop = new GridData();
		btnOptionDrop.setLayoutData(gd_btnOptionDrop);
		btnOptionDrop.setText("丢弃");
		btnOptionDrop.setSelection(ignoreUnmatched);
		
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
		return new Point(380, 185);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("导入地图");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			fileName = tfFileName.getText().trim();
			if (fileName.length() == 0) {
				MessageDialog.openError(getShell(), "错误", "必须输入文件名。");
				return;
			}
			File file = new File(fileName);
			if (!file.exists()) {
				MessageDialog.openError(getShell(), "错误", "文件不存在。");
				return;
			}
			if (file.isDirectory()) {
				MessageDialog.openError(getShell(), "错误", "文件不存在。");
				return;
			}
			tolerance = scale.getSelection();
			ignoreUnmatched = btnOptionDrop.getSelection(); 
		}
		super.buttonPressed(buttonId);
	}
}
