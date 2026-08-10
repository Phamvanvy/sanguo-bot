package com.pip.uieditor.editor;

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
import org.eclipse.swt.widgets.Text;

import com.pip.uieditor.model.Screen;

public class NewUIDialog extends Dialog {
	private Text tfName;
	private Combo cbScreen;
	
	
	private String uiName;
	
	private int[] screenSize;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public NewUIDialog(Shell parentShell) {
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
		gridLayout.numColumns = 4;
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("请输入文件名:");
		
		tfName = new Text(container, SWT.BORDER);
		GridData gd_tfName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_tfName.widthHint = 212;
		tfName.setLayoutData(gd_tfName);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label label_1 = new Label(container, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("屏幕尺寸:");
		
		cbScreen = new Combo(container, SWT.READ_ONLY);
		cbScreen.setItems(Screen.SCREEN_SIZE_STRING);
		cbScreen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbScreen.select(0);

		return container;
	}
	
	public String getUIName() {
		return this.uiName;
	}
	
	public int[] getScreenSize() {
		return screenSize;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
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
	
	@Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            if(cbScreen.getSelectionIndex() != -1) {
            	screenSize = Screen.SCREEN_SIZE[cbScreen.getSelectionIndex()];
            } else {
            	MessageDialog.openError(getShell(), "错误", "请选择一个屏幕尺寸。");
            	return;
            }
            if(tfName.getText()!=null||tfName.getText().length()>0) {
            	uiName = tfName.getText();
            } else {
            	MessageDialog.openError(getShell(), "错误", "文件名不能为空。");
            	return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
