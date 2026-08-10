package com.pip.image.workshop.editor;

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

public class EditLandformFrameDialog extends Dialog {
	private Combo cbPriority;
	private Combo cbType;
	private int type;
	private int priority;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public EditLandformFrameDialog(Shell parentShell) {
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
		label.setText("图块类型：");

		cbType = new Combo(container, SWT.READ_ONLY);
		cbType.setVisibleItemCount(10);
		cbType.setItems(new String[] {"100%", "75%", "50%(水平)", "50%(垂直)", "50%(对角)", "25%", "75%(向下)", "50%(水平向下)", "50%(垂直向右)", "25%(向下)" });
        cbType.select(type);
		final GridData gd_cbType = new GridData(SWT.FILL, SWT.CENTER, true, false);
		cbType.setLayoutData(gd_cbType);

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("出现频率：");

		cbPriority = new Combo(container, SWT.READ_ONLY);
		cbPriority.setItems(new String[] {"经常出现", "有时出现", "偶尔出现", "很少出现"});
		if (priority == 100) {
		    cbPriority.select(0);
		} else if (priority == 50) {
		    cbPriority.select(1);
		} else if (priority == 10) {
		    cbPriority.select(2);
		} else {
		    cbPriority.select(3);
		}
		final GridData gd_cbPriority = new GridData(SWT.FILL, SWT.CENTER, true, false);
		cbPriority.setLayoutData(gd_cbPriority);
		
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
//		return new Point(223, 151);
//	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("地形块属性");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
		    type = cbType.getSelectionIndex();
		    priority = cbPriority.getSelectionIndex();
		    switch (priority) {
		    case 0:
		        priority = 100;
		        break;
		    case 1:
		        priority = 50;
		        break;
		    case 2:
		        priority = 10;
		        break;
		    case 3:
		        priority = 1;
		        break;
		    }
		}
		super.buttonPressed(buttonId);
	}
}
