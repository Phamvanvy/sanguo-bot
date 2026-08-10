package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.propertysheet.IPropertySheetEnable;
import com.pip.propertysheet.PropertySheetEntry;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipParticleEffect;
import com.pipimage.image.PipParticleEffectSet;
import com.pipimage.image.PipParticlePath;
import com.pipimage.image.PipParticleSet;
import com.pipimage.image.path.FirePath;
import com.pipimage.image.path.Helix2Path;
import com.pipimage.image.path.HelixPath;
import com.pipimage.image.path.LinePath;
import com.pipimage.image.path.ParabolaPath;
import com.pipimage.image.path.SinusoidPath;
import com.pipimage.image.path.StayPath;

public class EditParticleEffectDialog extends Dialog {
    private Text textStopTick;
    private Text textStartTick;
    private Text textTitle;
    
    private PipParticleEffect editObject;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditParticleEffectDialog(Shell parentShell, PipParticleEffect effect) {
        super(parentShell);
        this.editObject = effect;
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
        label.setText("标题：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("开始时间(-1表示不限制)：");

        textStartTick = new Text(container, SWT.BORDER);
        final GridData gd_textStartTick = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textStartTick.setLayoutData(gd_textStartTick);

        textStartTick.setText(String.valueOf(this.editObject.startTick));
        textTitle.setText(this.editObject.title);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("结束时间(-1表示不限制)：");

        textStopTick = new Text(container, SWT.BORDER);
        final GridData gd_textStopTick = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textStopTick.setLayoutData(gd_textStopTick);
        textStopTick.setText(String.valueOf(this.editObject.stopTick));

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
        return new Point(392, 216);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("粒子效果设定");
    }
    
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				checkInput();
			} catch (Exception e) {
				MessageDialog.openError(this.getShell(), "错误", e.toString());
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
	
	protected void checkInput() throws Exception {
		int startTick = Integer.parseInt(textStartTick.getText());
		int stopTick = Integer.parseInt(textStopTick.getText());
		if (startTick < -1 || stopTick < -1) {
			throw new Exception("请输入大于或等于-1的数字。");
		}
		if (startTick >= 0 && stopTick >= 0 && stopTick <= startTick) {
			throw new Exception("结束时间必须大于起始时间。");
		}
		String title = textTitle.getText();
		this.editObject.startTick = startTick;
		this.editObject.stopTick = stopTick;
		this.editObject.title = title;
	}
}
