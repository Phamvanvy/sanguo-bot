package com.pip.sanguo.editor.util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.property.ChooseItemDialog;

public class ItemChooser extends Composite {
    private int itemId = -1;
	private Text textID;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ItemChooser(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		textID = new Text(this, SWT.BORDER);
		textID.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		textID.setEditable(false);

		final Button browseButton = new Button(this, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		browseButton.setText("...");
		browseButton.addSelectionListener(new SelectionAdapter(){
		    public void widgetSelected(SelectionEvent e) {
		        selectItem();
		    }
		});
	}
	
	private void selectItem(){
	    ChooseItemDialog itemDialog = new ChooseItemDialog(Display.getCurrent().getActiveShell());
	    if(itemDialog.open() == IDialogConstants.OK_ID){
	        setItemID(itemDialog.getSelectedItem());
	    }
	}
	
	public void setItemID(int id) {
	    itemId = id;
	    
	    Item selItem = EditorApplication.getProj().findItemOrEquipment(itemId);
	    if(selItem != null){	        
	        textID.setText(selItem.toString());
	    }
	}
	
	public int getItemID() {
		return itemId;
	}
}
