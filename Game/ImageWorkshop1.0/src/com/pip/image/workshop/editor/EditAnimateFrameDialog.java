package com.pip.image.workshop.editor;

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

import com.pipimage.image.PipAnimateFrameRef;

public class EditAnimateFrameDialog extends Dialog {
	private Text textSpeedFactor;
	private Spinner spinInput;
	private PipAnimateFrameRef editObject;
	private Button checkEnableTransform;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public EditAnimateFrameDialog(Shell parentShell, PipAnimateFrameRef editObject) {
		super(parentShell);
		this.editObject = editObject;
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

		final Label lblValue = new Label(container, SWT.NONE);
		lblValue.setText("持续帧数：");

		spinInput = new Spinner(container, SWT.BORDER);
		spinInput.setMinimum(1);
		spinInput.setMaximum(1000000);
		spinInput.setSelection(editObject.getDelay());
		spinInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		checkEnableTransform = new Button(container, SWT.CHECK);
		final GridData gd_checkEnableTransform = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		checkEnableTransform.setLayoutData(gd_checkEnableTransform);
		checkEnableTransform.setText("开启插值效果");
		checkEnableTransform.setSelection(editObject.enableTransform);

		final Label label = new Label(container, SWT.NONE);
		label.setText("加速因子：");

		textSpeedFactor = new Text(container, SWT.BORDER);
		final GridData gd_textSpeedFactor = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textSpeedFactor.setLayoutData(gd_textSpeedFactor);
		textSpeedFactor.setText(String.valueOf(editObject.speedFactor));

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label_1.setText("加速因子是最后一帧的速度和第一帧的速度的比值，单位是1%。");

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
		return new Point(400, 222);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("动画帧属性");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			int sf = 0;
			try {
				sf = Integer.parseInt(textSpeedFactor.getText());
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "错误", "请正确输入速度因子。");
				return;
			}
			if (sf < 1 || sf > 10000) {
				MessageDialog.openError(getShell(), "错误", "请正确输入速度因子(1-10000)。");
				return;
			}
			editObject.setDelay(spinInput.getSelection());
			editObject.enableTransform = checkEnableTransform.getSelection();
			editObject.speedFactor = sf;
		}
		super.buttonPressed(buttonId);
	}
}
