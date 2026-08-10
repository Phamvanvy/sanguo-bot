package com.pip.uieditor.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import com.pip.uieditor.model.Widget;

public class WidgetStateDialog extends Dialog {

	private int state;
	
	
	Button chkCustom1;
	Button chkCustom2;
	Button chkCustom3;
	Button chkCustom4;
	Button chkDisable;
	Button chkPushed;
	Button chkSelected;
	Button chkFocused;
	Button chkHighlight;
	Button chkChecked;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public WidgetStateDialog(Shell parentShell) {
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
		gridLayout.numColumns = 6;
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		chkDisable = new Button(container, SWT.CHECK);
		chkDisable.setText("DISABLE");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.DISABLED) != 0) {
			chkDisable.setSelection(true);
		}
		
		chkCustom1 = new Button(container, SWT.CHECK);
		chkCustom1.setText("CUSTOM1");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.STATE_CUSTOM1) != 0) {
			chkCustom1.setSelection(true);
		}

		
		chkPushed = new Button(container, SWT.CHECK);
		chkPushed.setText("PUSHED");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.PUSHED) != 0) {
			chkPushed.setSelection(true);
		}
		
		chkCustom2 = new Button(container, SWT.CHECK);
		chkCustom2.setText("CUSTOM2");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.STATE_CUSTOM2) != 0) {
			chkCustom2.setSelection(true);
		}
		
		
		chkHighlight = new Button(container, SWT.CHECK);
		chkHighlight.setText("HIGHLIGHT");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.HIGHLIGHT) != 0) {
			chkHighlight.setSelection(true);
		}
		
		chkCustom3 = new Button(container, SWT.CHECK);
		chkCustom3.setText("CUSTOM3");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.STATE_CUSTOM3) != 0) {
			chkCustom3.setSelection(true);
		}
		
		chkSelected = new Button(container, SWT.CHECK);
		chkSelected.setText("SELECTED");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.SELECTED) != 0) {
			chkSelected.setSelection(true);
		}
		
		chkCustom4 = new Button(container, SWT.CHECK);
		chkCustom4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		chkCustom4.setText("CUSTOM4");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.STATE_CUSTOM4) != 0) {
			chkCustom4.setSelection(true);
		}
		
		chkFocused = new Button(container, SWT.CHECK);
		chkFocused.setText("FOCUSED");
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
		if((this.state & Widget.FOCUSED) != 0) {
			chkFocused.setSelection(true);
		}
		
		chkChecked = new Button(container, SWT.CHECK);
		chkChecked.setText("CHECKED");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		if((this.state & Widget.CHECKED) != 0) {
			chkChecked.setSelection(true);
		}
		
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
		return new Point(545, 426);
	}
	
	
	public  void setState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return this.state;
	}
	
	
	
	@Override
	protected void okPressed() {
		buildState();
		super.okPressed();
	}

	private void buildState() {
		int state = 0;
		if(chkDisable.getSelection()) {
			state |= Widget.DISABLED;
		}
		if(chkPushed.getSelection()) {
			state |= Widget.PUSHED;
		}
		if(chkSelected.getSelection()) {
			state |= Widget.SELECTED;
		}
		if(chkFocused.getSelection()) {
			state |= Widget.FOCUSED;
		}
		if(chkHighlight.getSelection()) {
			state |= Widget.SELECTED;
		}
		if(chkChecked.getSelection()) {
			state |= Widget.CHECKED;
		}
		if(chkCustom1.getSelection()) {
			state |= Widget.STATE_CUSTOM1;
		}
		if(chkCustom2.getSelection()) {
			state |= Widget.STATE_CUSTOM2;
		}
		if(chkCustom3.getSelection()) {
			state |= Widget.STATE_CUSTOM3;
		}
		if(chkCustom4.getSelection()) {
			state |= Widget.STATE_CUSTOM4;
		}
		this.state = state;
	}
}
