package com.pip.uieditor.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewRegionDialog extends Dialog {
	private Text txtId;
	private CCombo cbLayer;
	
	private String regionId;
	private int layer;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public NewRegionDialog(Shell parentShell) {
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
		gridLayout.numColumns = 5;
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Id:");
		
		txtId = new Text(container, SWT.BORDER);
		GridData gd_txtId = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtId.widthHint = 255;
		txtId.setLayoutData(gd_txtId);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setText("Layer:");
		
		cbLayer = new CCombo(container, SWT.BORDER);
		cbLayer.setEditable(false);
		cbLayer.setItems(new String[] {"BACKGROUND", "BORDER", "ARTWORK", "OVERLAY"});
		cbLayer.select(0);

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
	
	
	public String getId() {
		return regionId;
	}
	
	public int getLayer() {
		return layer;
	}
	
	@Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	layer = cbLayer.getSelectionIndex();
        	regionId = txtId.getText();
        	if(regionId == null || regionId.length() == 0) {
            	MessageDialog.openError(getShell(), "´íÎó", "Id´íÎó");
            	return;
        	}
        }
        super.buttonPressed(buttonId);
	}
}
