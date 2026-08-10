package com.pip.mapeditor;

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

import com.pip.mapeditor.data.MapFile;
import com.pip.util.AutoSelectAll;

public class NewMapDialog extends Dialog {
	private MapFile mapFile;
	private Combo cbType;
	private Spinner spinMapWidth, spinMapHeight;
	
	private int mapWidth = 256;
	private int mapHeight = 256;
	private int mapType = 0;

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

	public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }
    private boolean canChooseType = true;
    private int defaultChooseType;
    public int getDefaultChooseType() {
		return defaultChooseType;
	}

	public void setDefaultChooseType(int defaultChooseType) {
		this.defaultChooseType = defaultChooseType;
	}

	public void setEnableChooseType(boolean en){
    	canChooseType = en; 
    }
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public NewMapDialog(Shell parentShell, MapFile mapFile) {
		super(parentShell);
		this.mapFile = mapFile;
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

		final Label lblColumn = new Label(container, SWT.NONE);
		lblColumn.setText("地图宽度：");

		spinMapWidth = new Spinner(container, SWT.BORDER);
		spinMapWidth.setIncrement(16);
		spinMapWidth.setMaximum(10000);
		spinMapWidth.setMinimum(1);
		spinMapWidth.setSelection(256);
		final GridData gd_spinMapWidth = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinMapWidth.setLayoutData(gd_spinMapWidth);
		spinMapWidth.setSelection(mapWidth);
		spinMapWidth.addFocusListener(AutoSelectAll.instance);

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setLayoutData(new GridData());
		label_2.setText("地图高度：");

		spinMapHeight = new Spinner(container, SWT.BORDER);
		spinMapHeight.setIncrement(16);
		spinMapHeight.setSelection(256);
		spinMapHeight.setMinimum(1);
		spinMapHeight.setMaximum(10000);
		final GridData gd_spinMapHeight = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinMapHeight.setLayoutData(gd_spinMapHeight);
		spinMapHeight.setSelection(mapHeight);
		spinMapWidth.addFocusListener(AutoSelectAll.instance);

		final Label label = new Label(container, SWT.NONE);
		label.setText("贴图类型：");

		cbType = new Combo(container, SWT.READ_ONLY);
		cbType.setItems(new String[] {"精细", "模糊"});
        cbType.select(defaultChooseType);
        cbType.setEnabled(canChooseType);
		final GridData gd_cbType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		cbType.setLayoutData(gd_cbType);
		
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
//	protected Point getInitialSize() {
////		return new Point(223, 160);
//	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("新建地图");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			mapWidth = spinMapWidth.getSelection();
			mapHeight = spinMapHeight.getSelection();
			mapType = cbType.getSelectionIndex();
			if (mapType == -1) {
			    MessageDialog.openError(getShell(), "错误", "必须选择一个地图类型。");
                return;
			}
			if (mapType == 0) {
				// 精细
				if ((mapWidth % mapFile.getTileWidth()) != 0 || (mapHeight % mapFile.getTileHeight()) != 0) {
					MessageDialog.openError(getShell(), "错误", "地图大小和贴图大小设置不符，不能整除。");
					return;
				}
			} else {
				// 模糊
				if ((mapWidth % mapFile.getBlurTileWidth()) != 0 || (mapHeight % mapFile.getBlurTileHeight()) != 0) {
					MessageDialog.openError(getShell(), "错误", "地图大小和贴图大小设置不符，不能整除。");
					return;
				}
			}
		}
		super.buttonPressed(buttonId);
	}
}
