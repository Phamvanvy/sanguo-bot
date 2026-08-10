package com.pip.mapeditor;

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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class NewCellMapDialog extends Dialog {
	private Text tfFileName;
	private Spinner spinDepth;
	private Spinner spinMapWidth, spinMapHeight;
	
	private String fileName = "newmap";
	private int cellDepth = 2, mapWidth = 256, mapHeight = 256;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getMapWidth() {
		return mapWidth;
	}

	public void setMapWidth(int mapWidth) {
		this.mapWidth = mapWidth;
	}

	public int getMapHeight() {
		return mapHeight;
	}

	public void setMapHeight(int mapHeight) {
		this.mapHeight = mapHeight;
	}

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public NewCellMapDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.NONE);
		label.setText("文件名：");

		tfFileName = new Text(container, SWT.BORDER);
		tfFileName.setText("newmap");
		final GridData gd_newmapText = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		tfFileName.setLayoutData(gd_newmapText);

		final Label lblColumn = new Label(container, SWT.NONE);
		lblColumn.setText("地图宽度：");

		spinMapWidth = new Spinner(container, SWT.BORDER);
		spinMapWidth.setMaximum(2000);
		spinMapWidth.setMinimum(1);
		spinMapWidth.setSelection(256);
		final GridData gd_spinMapWidth = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinMapWidth.setLayoutData(gd_spinMapWidth);

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("地图高度：");

		spinMapHeight = new Spinner(container, SWT.BORDER);
		spinMapHeight.setSelection(256);
		spinMapHeight.setMinimum(1);
		spinMapHeight.setMaximum(2000);
		final GridData gd_spinMapHeight = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinMapHeight.setLayoutData(gd_spinMapHeight);
		
		tfFileName.setText(fileName);
		spinMapWidth.setSelection(mapWidth);
		spinMapHeight.setSelection(mapHeight);

		final Label lblRow = new Label(container, SWT.NONE);
		lblRow.setLayoutData(new GridData());
		lblRow.setText("格点深度：");

		spinDepth = new Spinner(container, SWT.BORDER);
		spinDepth.setMinimum(2);
		spinDepth.setSelection(2);
		final GridData gd_spinDepth = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinDepth.setLayoutData(gd_spinDepth);
		spinDepth.setSelection(cellDepth);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
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
		return new Point(361, 156);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("新建网格地图");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			fileName = tfFileName.getText().trim();
			if (fileName.length() == 0) {
				MessageDialog.openError(getShell(), "错误", "必须输入文件名。");
				return;
			}
			cellDepth = spinDepth.getSelection();
			mapWidth = spinMapWidth.getSelection();
			mapHeight = spinMapHeight.getSelection();
		}
		super.buttonPressed(buttonId);
	}

    public int getCellDepth() {
        return cellDepth;
    }

    public void setCellDepth(int cellDepth) {
        this.cellDepth = cellDepth;
    }

}
