package com.pip.mapeditor;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.pip.util.AutoSelectAll;

public class NewMapFileDialog extends Dialog {
	private Combo comboType;
	private Text textFileName;
	private Combo comboTileSize;
	private int[] tileSizeList = new int[] { 16, 20, 24, 28, 32, 40, 48, 56, 64 };
	private File dir;
	
	public String fileName;
	public boolean libMode;
	public int tileSize;
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public NewMapFileDialog(Shell parentShell, File dir) {
		super(parentShell);
		this.dir = dir;
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

		final Label lblFileName = new Label(container, SWT.NONE);
		lblFileName.setText("文件名：");

		textFileName = new Text(container, SWT.BORDER);
		textFileName.setText("newmap");
		final GridData gd_textFileName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textFileName.setLayoutData(gd_textFileName);
		textFileName.addFocusListener(AutoSelectAll.instance);

		final Label lblType = new Label(container, SWT.NONE);
		final GridData gd_lblType = new GridData();
		lblType.setLayoutData(gd_lblType);
		lblType.setText("地图类型：");

		comboType = new Combo(container, SWT.READ_ONLY);
		comboType.setItems(new String[] {"库模式", "独立模式"});
		comboType.select(0);
		final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboType.setLayoutData(gd_comboType);

		final Label lblTileSize = new Label(container, SWT.NONE);
		lblTileSize.setText("贴图大小：");

		comboTileSize = new Combo(container, SWT.READ_ONLY);
		comboTileSize.setItems(new String[] {"16x16", "20x20", "24x24", "28x28", "32x32", "40x40", "48x48", "56x56", "64x64"});
		comboTileSize.select(0);
		final GridData gd_comboTileSize = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboTileSize.setLayoutData(gd_comboTileSize);
		
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
		return new Point(440, 224);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("新建地图文件");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			fileName = textFileName.getText().trim();
			if (fileName.length() == 0) {
				MessageDialog.openError(getShell(), "错误", "请输入文件名。");
				return;
			}
			if (!fileName.toLowerCase().endsWith(".map")) {
				fileName = fileName + ".map";
			}
			if (new File(dir, fileName).exists()) {
				MessageDialog.openError(getShell(), "错误", "文件已经存在了。");
				return;
			}
			libMode = comboType.getSelectionIndex() == 0;
			tileSize = tileSizeList[comboTileSize.getSelectionIndex()];
		}
		super.buttonPressed(buttonId);
	}
}
