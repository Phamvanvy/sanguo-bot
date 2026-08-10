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

import com.pip.util.AutoSelectAll;

public class DupMapDialog extends Dialog {
	private Spinner spinX, spinY;
	private Spinner spinWidth;
	private Spinner spinHeight;
	
	public int x;
	public int y;
	public int width;
	public int height;
	public int mapWidth;
	public int mapHeight;
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public DupMapDialog(Shell parentShell) {
		super(parentShell);
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

		final Label lblX = new Label(container, SWT.NONE);
		lblX.setText("起始X：");

		spinX = new Spinner(container, SWT.BORDER);
		spinX.setMinimum(-1000);
		spinX.setIncrement(16);
		spinX.setMaximum(10000);
		final GridData gd_spinX = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinX.setLayoutData(gd_spinX);
		spinX.addFocusListener(AutoSelectAll.instance);

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setLayoutData(new GridData());
		label_2.setText("起始Y(：");

		spinY = new Spinner(container, SWT.BORDER);
		spinY.setMinimum(-1000);
		spinY.setIncrement(16);
		spinY.setMaximum(10000);
		final GridData gd_spinY = new GridData(SWT.FILL, SWT.CENTER, true, false);
		spinY.setLayoutData(gd_spinY);
		spinX.addFocusListener(AutoSelectAll.instance);

		final Label label = new Label(container, SWT.NONE);
		label.setText("宽度：");

		spinWidth = new Spinner(container, SWT.BORDER);
		spinWidth.setIncrement(16);
		spinWidth.setMaximum(10000);
		spinWidth.setSelection(mapWidth);
		final GridData gd_spinWidth = new GridData(SWT.FILL, SWT.CENTER, false, false);
		spinWidth.setLayoutData(gd_spinWidth);

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("高度：");

		spinHeight = new Spinner(container, SWT.BORDER);
		spinHeight.setIncrement(16);
		spinHeight.setMaximum(10000);
		spinHeight.setSelection(mapHeight);
		final GridData gd_spinHeight = new GridData(SWT.FILL, SWT.CENTER, false, false);
		spinHeight.setLayoutData(gd_spinHeight);
		
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
		return new Point(422, 240);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("复制地图");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			x = spinX.getSelection();
			y = spinY.getSelection();
			width = spinWidth.getSelection();
			height = spinHeight.getSelection();
			if ((x % 16) > 0 || (y % 16) > 0 || (width % 16) > 0 || (height % 16) > 0) {
				MessageDialog.openError(getShell(), "错误", "必须是16的整倍数。");
				return;
			}
//			if (x < 0 || y < 0 || width <= 0 || height <= 0 || x + width > mapWidth || x + height > mapHeight) {
//				MessageDialog.openError(getShell(), "错误", "数据越界。");
//				return;
//			}
		}
		super.buttonPressed(buttonId);
	}
}
