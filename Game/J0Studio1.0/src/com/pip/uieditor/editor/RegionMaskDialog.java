package com.pip.uieditor.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import com.pip.uieditor.model.Widget;

public class RegionMaskDialog extends Dialog {
	
	private static String[] ITEMS = {"Œﬁ ”", "œ‘ æ", "“˛≤ÿ"};
	
	private Combo cbDisable;
	private Combo cbPushed;
	private Combo cbHighlight;
	private Combo cbSelected;
	private Combo cbFocused;
	private Combo cbChecked;
	private Combo cbCustom1;
	private Combo cbCustom2;
	private Combo cbCustom3;
	private Combo cbCustom4;
	
	private int mask;
	private int flag;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public RegionMaskDialog(Shell parentShell) {
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
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("DISABLE:");
		
		cbDisable = new Combo(container, SWT.READ_ONLY);
		cbDisable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		cbDisable.setItems(ITEMS);
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("CUSTOM1:");
		
		cbCustom1 = new Combo(container, SWT.READ_ONLY);
		cbCustom1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		cbCustom1.setItems(ITEMS);
		
		Label lblNewLabel_2 = new Label(container, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("PUSHED:");
		
		cbPushed = new Combo(container, SWT.READ_ONLY);
		cbPushed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		cbPushed.setItems(ITEMS);
		
		Label lblNewLabel_3 = new Label(container, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("CUSTOM2:");
		
		cbCustom2 = new Combo(container, SWT.READ_ONLY);
		cbCustom2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		cbCustom2.setItems(ITEMS);
		
		Label lblNewLabel_4 = new Label(container, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("HIGHLIGHT:");
		
		cbHighlight = new Combo(container, SWT.READ_ONLY);
		cbHighlight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		cbHighlight.setItems(ITEMS);
		
		Label lblNewLabel_8 = new Label(container, SWT.NONE);
		lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_8.setText("CUSTOME3:");
		
		cbCustom3 = new Combo(container, SWT.READ_ONLY);
		cbCustom3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		cbCustom3.setItems(ITEMS);
		
		Label lblNewLabel_5 = new Label(container, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_5.setText("SELECTED:");
		
		cbSelected = new Combo(container, SWT.READ_ONLY);
		cbSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		cbSelected.setItems(ITEMS);
		
		Label lblNewLabel_9 = new Label(container, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("CUSTOM4:");
		
		cbCustom4 = new Combo(container, SWT.READ_ONLY);
		cbCustom4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		cbCustom4.setItems(ITEMS);
		
		Label lblNewLabel_6 = new Label(container, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("FOCUSED:");
		
		cbFocused = new Combo(container, SWT.READ_ONLY);
		cbFocused.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
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
		cbFocused.setItems(ITEMS);
		
		Label lblNewLabel_7 = new Label(container, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("CHECKED:");
		
		cbChecked = new Combo(container, SWT.READ_ONLY);
		cbChecked.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		cbChecked.setItems(ITEMS);
		
		setMaskAndFlag();
		return container;
	}
	
	public void setMask(int mask) {
		this.mask = mask;
	}
	
	public int getMask() {
		return this.mask;
	}
	
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public int getFlag() {
		return this.flag;
	}
	
	private void setMaskAndFlag() {
		if((mask & Widget.DISABLED) != 0) {
			cbDisable.select((flag & Widget.DISABLED) == 0 ? 2 : 1);
		} else {
			cbDisable.select(0);
		}

		if((mask & Widget.PUSHED) != 0) {
			cbPushed.select((flag & Widget.PUSHED) == 0 ? 2 : 1);
		} else {
			cbPushed.select(0);
		}

		if((mask & Widget.HIGHLIGHT) != 0) {
			cbHighlight.select((flag & Widget.HIGHLIGHT) == 0 ? 2 : 1);
		} else {
			cbHighlight.select(0);
		}

		if((mask & Widget.CHECKED) != 0) {
			cbChecked.select((flag & Widget.CHECKED) == 0 ? 2 : 1);
		} else {
			cbChecked.select(0);
		}

		if((mask & Widget.FOCUSED) != 0) {
			cbFocused.select((flag & Widget.FOCUSED) == 0 ? 2 : 1);
		} else {
			cbFocused.select(0);
		}

		if((mask & Widget.SELECTED) != 0) {
			cbSelected.select((flag & Widget.SELECTED) == 0 ? 2 : 1);
		} else {
			cbSelected.select(0);
		}

		if((mask & Widget.STATE_CUSTOM1) != 0) {
			cbCustom1.select((flag & Widget.STATE_CUSTOM1) == 0 ? 2 : 1);
		} else {
			cbCustom1.select(0);
		}

		if((mask & Widget.STATE_CUSTOM2) != 0) {
			cbCustom2.select((flag & Widget.STATE_CUSTOM2) == 0 ? 2 : 1);
		} else {
			cbCustom2.select(0);
		}

		if((mask & Widget.STATE_CUSTOM3) != 0) {
			cbCustom3.select((flag & Widget.STATE_CUSTOM3) == 0 ? 2 : 1);
		} else {
			cbCustom3.select(0);
		}

		if((mask & Widget.STATE_CUSTOM4) != 0) {
			cbCustom4.select((flag & Widget.STATE_CUSTOM4) == 0 ? 2 : 1);
		} else {
			cbCustom4.select(0);
		}

	}
	
	
	
	@Override
	protected void okPressed() {
		buildMaskAndFlag();
		super.okPressed();
	}

	private void buildMaskAndFlag() {
		
		int newMask = 0, newFlag = 0;
		
		if(cbDisable.getSelectionIndex() != 0) {
			newMask |= Widget.DISABLED;
			if(cbDisable.getSelectionIndex() == 1) {
				newFlag |= Widget.DISABLED;
			} 
		} 
		
		if(cbPushed.getSelectionIndex() != 0) {
			newMask |= Widget.PUSHED;
			if(cbPushed.getSelectionIndex() == 1) {
				newFlag |= Widget.PUSHED;
			} 
		}

		if(cbSelected.getSelectionIndex() != 0) {
			newMask |= Widget.SELECTED;
			if(cbSelected.getSelectionIndex() == 1) {
				newFlag |= Widget.SELECTED;
			}
		}

		if(cbHighlight.getSelectionIndex() != 0) {
			newMask |= Widget.HIGHLIGHT;
			if(cbHighlight.getSelectionIndex() == 1) {
				newFlag |= Widget.HIGHLIGHT;
			}
		}

		if(cbChecked.getSelectionIndex() != 0) {
			newMask |= Widget.CHECKED;
			if(cbChecked.getSelectionIndex() == 1) {
				newFlag |= Widget.CHECKED;
			}
		}

		if(cbFocused.getSelectionIndex() != 0) {
			newMask |= Widget.FOCUSED;
			if(cbFocused.getSelectionIndex() == 1) {
				newFlag |= Widget.FOCUSED;
			}
		}

		if(cbCustom1.getSelectionIndex() != 0) {
			newMask |= Widget.STATE_CUSTOM1;
			if(cbCustom1.getSelectionIndex() == 1) {
				newFlag |= Widget.STATE_CUSTOM1;
			}
		}

		if(cbCustom2.getSelectionIndex() != 0) {
			newMask |= Widget.STATE_CUSTOM2;
			if(cbCustom2.getSelectionIndex() == 1) {
				newFlag |= Widget.STATE_CUSTOM2;
			}
		}

		if(cbCustom3.getSelectionIndex() != 0) {
			newMask |= Widget.STATE_CUSTOM3;
			if(cbCustom3.getSelectionIndex() == 1) {
				newFlag |= Widget.STATE_CUSTOM3;
			}
		}

		if(cbCustom4.getSelectionIndex() != 0) {
			newMask |= Widget.STATE_CUSTOM4;
			if(cbCustom4.getSelectionIndex() == 1) {
				newFlag |= Widget.STATE_CUSTOM4;
			}
		}
		this.mask = newMask;
		this.flag = newFlag;
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
		return new Point(557, 481);
	}

}
