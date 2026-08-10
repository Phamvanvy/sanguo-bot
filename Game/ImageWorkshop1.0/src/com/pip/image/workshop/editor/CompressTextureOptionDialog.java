package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.pip.util.AutoSelectAll;
import com.pipimage.image.JPEGMergeOption;

/**
 * 编辑JPEG合并模式图片选项。
 * @author lighthu
 */
public class CompressTextureOptionDialog extends Dialog {
    private Text textBorderWidth;

    private int borderWidth = 2;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public CompressTextureOptionDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("图片描边宽度：");

        textBorderWidth = new Text(container, SWT.BORDER);
        textBorderWidth.setText(String.valueOf(borderWidth));
        final GridData gd_textBorderWidth = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textBorderWidth.setLayoutData(gd_textBorderWidth);
        textBorderWidth.addFocusListener(AutoSelectAll.instance);

        return container;
    }
    
    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(228, 191);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("图片选项");
    }
    
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				borderWidth = Integer.parseInt(textBorderWidth.getText());
				if (borderWidth < 0 || borderWidth > 10) {
					throw new Exception();
				}
			} catch (Exception e) {
				MessageDialog.openError(this.getShell(), "错误", "请输入0-10之间的整数。");
				return;
			}
		}
		super.buttonPressed(buttonId);
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}
	
	public static int choose(int ref) {
		CompressTextureOptionDialog dlg = new CompressTextureOptionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dlg.setBorderWidth(ref);
		if (dlg.open() == Dialog.OK) {
			return dlg.getBorderWidth();
		} else {
			return ref;
		}
	}
}
