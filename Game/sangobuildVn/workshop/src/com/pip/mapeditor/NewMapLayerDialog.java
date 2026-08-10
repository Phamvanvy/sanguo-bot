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

public class NewMapLayerDialog extends Dialog {
	private Text textName;
	private Combo cbType;
	
	private String layerName;
	private int layerType;


    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public int getLayerType() {
        return layerType;
    }

    public void setLayerType(int layerType) {
        this.layerType = layerType;
    }

    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public NewMapLayerDialog(Shell parentShell) {
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

		final Label label = new Label(container, SWT.NONE);
		label.setText("图层类型：");

		cbType = new Combo(container, SWT.READ_ONLY);
		cbType.setItems(new String[] {"精细贴图层", "模糊贴图层", "NPC层"});
        cbType.select(0);
		final GridData gd_cbType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		cbType.setLayoutData(gd_cbType);

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("图层名称：");

		textName = new Text(container, SWT.BORDER);
		textName.setText("图层");
		final GridData gd_textName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textName.setLayoutData(gd_textName);
		
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

//	/**
//	 * Return the initial size of the dialog
//	 */
//	protected Point getInitialSize() {
//		return new Point(223, 131);
//	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("新建地图");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
		    layerName = textName.getText().trim();
		    if (layerName.length() == 0) {
		        MessageDialog.openError(getShell(), "错误", "必须输入图层名称。");
                return;
		    }
			layerType = cbType.getSelectionIndex();
			if (layerType == -1) {
			    MessageDialog.openError(getShell(), "错误", "必须选择一个图层类型。");
                return;
			}
		}
		super.buttonPressed(buttonId);
	}
}
