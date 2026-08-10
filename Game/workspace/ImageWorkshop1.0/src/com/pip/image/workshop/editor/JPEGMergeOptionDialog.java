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
public class JPEGMergeOptionDialog extends Dialog {
    private Text textBorderWidth;
    private Text textQuality;
    private Combo comboAlphaBits;

    private float quality = 0.5f;
    private int alphaBits = 4;
    private int borderWidth = 2;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public JPEGMergeOptionDialog(Shell parentShell) {
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

        final Label label = new Label(container, SWT.NONE);
        label.setText("压缩质量：");

        textQuality = new Text(container, SWT.BORDER);
        textQuality.setText(String.valueOf(quality));
        final GridData gd_textQuality = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textQuality.setLayoutData(gd_textQuality);
        textQuality.addFocusListener(AutoSelectAll.instance);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("Alpha通道：");

        comboAlphaBits = new Combo(container, SWT.READ_ONLY);
        comboAlphaBits.setItems(new String[] {"8位", "4位", "2位", "1位"});
        final GridData gd_comboAlphaBits = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboAlphaBits.setLayoutData(gd_comboAlphaBits);
        switch (alphaBits) {
        case 8:
        	comboAlphaBits.select(0);
        	break;
        case 4:
        	comboAlphaBits.select(1);
        	break;
        case 2:
        	comboAlphaBits.select(2);
        	break;
        case 1:
        	comboAlphaBits.select(3);
        	break;
        }

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
        newShell.setText("JPEG选项");
    }
    
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				quality = Float.parseFloat(textQuality.getText());
				if (quality <= 0 || quality >= 1) {
					throw new Exception();
				}
			} catch (Exception e) {
				MessageDialog.openError(this.getShell(), "错误", "请输入0-1之间的浮点数。");
				return;
			}
			int sel = comboAlphaBits.getSelectionIndex();
			switch (sel) {
			case 0:
				alphaBits = 8;
				break;
			case 1:
				alphaBits = 4;
				break;
			case 2:
				alphaBits = 2;
				break;
			case 3:
				alphaBits = 1;
				break;
			}
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

	public float getQuality() {
		return quality;
	}

	public void setQuality(float quality) {
		this.quality = quality;
	}

	public int getAlphaBits() {
		return alphaBits;
	}

	public void setAlphaBits(int alphaBits) {
		this.alphaBits = alphaBits;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}
	
	/**
	 * 弹出对话框让用户选择JPEG合并图片选项。
	 * @param ref 参考选项，可以为null。
	 * @return 如果用户取消选择，返回null。
	 */
	public static JPEGMergeOption choose(JPEGMergeOption ref) {
		if (ref == null) {
			ref = new JPEGMergeOption();
		}
		JPEGMergeOptionDialog dlg = new JPEGMergeOptionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dlg.setQuality(ref.quality);
		dlg.setAlphaBits(ref.alphaBits);
		dlg.setBorderWidth(ref.borderWidth);
		if (dlg.open() == Dialog.OK) {
			return new JPEGMergeOption(dlg.getQuality(), dlg.getAlphaBits(), dlg.getBorderWidth());
		} else {
			return null;
		}
	}
}
