package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.pip.util.AutoSelectAll;
import com.swtdesigner.SWTResourceManager;

/**
 * 图块颜色变换颜色选择。
 * @author lighthu
 */
public class PieceColorDialog extends Dialog implements PaintListener, ModifyListener, SelectionListener {
    private Text textColor;
    private Text textB;
    private Text textG;
    private Text textR;
    private Text textA;
	private Scale scaleR;
	private Scale scaleG;
	private Scale scaleB;
	private Scale scaleA;

    private int color;
	private Composite previewer;
	private ColorChangeListener listener;
	private boolean updating = false;
	
	public static interface ColorChangeListener {
		public void colorChanged(int newClr);
	}

    /**
     * Create the dialog
     * @param parentShell
     */
    public PieceColorDialog(Shell parentShell, int clr, ColorChangeListener l) {
        super(parentShell);
        color = clr;
        listener = l;
    }
    
    private String getColorString() {
    	String ret = Integer.toHexString(color);
    	while (ret.length() < 8) {
    		ret = "0" + ret;
    	}
    	return ret.toUpperCase();
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("A：");

        textA = new Text(container, SWT.BORDER);
        textA.addModifyListener(this);
        final GridData gd_textA = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd_textA.widthHint = 40;
        textA.setLayoutData(gd_textA);
        textA.addFocusListener(AutoSelectAll.instance);

        scaleA = new Scale(container, SWT.NONE);
        scaleA.addSelectionListener(this);
        scaleA.setMaximum(255);
        final GridData gd_scaleA = new GridData(SWT.FILL, SWT.CENTER, true, false);
        scaleA.setLayoutData(gd_scaleA);

        final Label rLabel = new Label(container, SWT.NONE);
        rLabel.setText("R:");

        textR = new Text(container, SWT.BORDER);
        textR.addModifyListener(this);
        final GridData gd_textR = new GridData(SWT.FILL, SWT.CENTER, false, false);
        textR.setLayoutData(gd_textR);

        scaleR = new Scale(container, SWT.NONE);
        scaleR.addSelectionListener(this);
        scaleR.setMaximum(255);
        final GridData gd_scaleR = new GridData(SWT.FILL, SWT.CENTER, true, false);
        scaleR.setLayoutData(gd_scaleR);

        final Label gLabel = new Label(container, SWT.NONE);
        gLabel.setText("G:");

        textG = new Text(container, SWT.BORDER);
        textG.addModifyListener(this);
        final GridData gd_textG = new GridData(SWT.FILL, SWT.CENTER, false, false);
        textG.setLayoutData(gd_textG);

        scaleG = new Scale(container, SWT.NONE);
        scaleG.addSelectionListener(this);
        scaleG.setMaximum(255);
        final GridData gd_scaleG = new GridData(SWT.FILL, SWT.CENTER, true, false);
        scaleG.setLayoutData(gd_scaleG);

        final Label bLabel = new Label(container, SWT.NONE);
        bLabel.setText("B:");

        textB = new Text(container, SWT.BORDER);
        textB.addModifyListener(this);
        final GridData gd_textB = new GridData(SWT.FILL, SWT.CENTER, false, false);
        textB.setLayoutData(gd_textB);

        scaleB = new Scale(container, SWT.NONE);
        scaleB.addSelectionListener(this);
        scaleB.setMaximum(255);
        final GridData gd_scaleB = new GridData(SWT.FILL, SWT.CENTER, true, false);
        scaleB.setLayoutData(gd_scaleB);

        previewer = new Composite(container, SWT.NONE);
        final GridData gd_previewer = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        previewer.setLayoutData(gd_previewer);
        previewer.setLayout(new GridLayout());
        previewer.addPaintListener(this);

        textColor = new Text(container, SWT.BORDER);
        textColor.addModifyListener(this);
        final GridData gd_textColor = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textColor.setLayoutData(gd_textColor);
        
        int a = (color >> 24) & 0xFF;
        textA.setText(String.valueOf(a));
        scaleA.setSelection(a);
        int r = (color >> 16) & 0xFF;
        textR.setText(String.valueOf(r));
        scaleR.setSelection(r);
        int g = (color >> 8) & 0xFF;
        textG.setText(String.valueOf(g));
        scaleG.setSelection(g);
        int b = (color >> 0) & 0xFF;
        textB.setText(String.valueOf(b));
        scaleB.setSelection(b);
        textColor.setText(getColorString());
        
        previewer.redraw();

        
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
        return new Point(353, 438);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("颜色");
    }
    
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}
	
	public static int choose(int initColor, ColorChangeListener l) {
		PieceColorDialog dlg = new PieceColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), initColor, l);
		if (dlg.open() == Dialog.OK) {
			return dlg.color;
		} else {
			return initColor;
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (updating) {
			return;
		}
		updating = true;
		if (e.widget == scaleA) {
			int a = scaleA.getSelection();
			color &= 0x00FFFFFF;
			color |= a << 24;
			textA.setText(String.valueOf(a));
			textColor.setText(getColorString());
		} else if (e.widget == scaleR) {
			int r = scaleR.getSelection();
			color &= 0xFF00FFFF;
			color |= r << 16;
			textR.setText(String.valueOf(r));
			textColor.setText(getColorString());
		} else if (e.widget == scaleG) {
			int g = scaleG.getSelection();
			color &= 0xFFFF00FF;
			color |= g << 8;
			textG.setText(String.valueOf(g));
			textColor.setText(getColorString());
		} else if (e.widget == scaleB) {
			int b = scaleB.getSelection();
			color &= 0xFFFFFF00;
			color |= b;
			textB.setText(String.valueOf(b));
			textColor.setText(getColorString());
		}
		previewer.redraw();
		if (listener != null) {
			listener.colorChanged(color);
		}
		updating = false;
	}

	@Override
	public void paintControl(PaintEvent e) {
		Composite comp = (Composite)e.widget;
		e.gc.setBackground(SWTResourceManager.getColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
		e.gc.fillRectangle(0, 0, comp.getBounds().width, comp.getBounds().height);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (updating) {
			return;
		}
		updating = true;
		if (e.widget == textA) {
			int a = getInputValue(textA);
			color &= 0x00FFFFFF;
			color |= a << 24;
			scaleA.setSelection(a);
			textColor.setText(getColorString());
		} else if (e.widget == textR) {
			int r = getInputValue(textR);
			color &= 0xFF00FFFF;
			color |= r << 16;
			scaleR.setSelection(r);
			textColor.setText(getColorString());
		} else if (e.widget == textG) {
			int g = getInputValue(textG);
			color &= 0xFFFF00FF;
			color |= g << 8;
			scaleG.setSelection(g);
			textColor.setText(getColorString());
		} else if (e.widget == textB) {
			int b = getInputValue(textB);
			color &= 0xFFFFFF00;
			color |= b;
			scaleB.setSelection(b);
			textColor.setText(getColorString());
		} else if (e.widget == textColor) {
			try {
				color = (int)Long.parseLong(textColor.getText(), 16);
				int a = (color >> 24) & 0xFF;
				textA.setText(String.valueOf(a));
				scaleA.setSelection(a);
				int r = (color >> 16) & 0xFF;
				textR.setText(String.valueOf(r));
				scaleR.setSelection(r);
				int g = (color >> 8) & 0xFF;
				textG.setText(String.valueOf(g));
				scaleG.setSelection(g);
				int b = (color >> 0) & 0xFF;
				textB.setText(String.valueOf(b));
				scaleB.setSelection(b);
			} catch (Exception e1) {
				updating = false;
				return;
			}
		}
		previewer.redraw();
		if (listener != null) {
			listener.colorChanged(color);
		}
		updating = false;
	}
	
	protected int getInputValue(Text input) {
		int ret = 0;
		try {
			ret = Integer.parseInt(input.getText());
			if (ret < 0)
				ret = 0;
			if (ret > 255) {
				ret = 255;
			}
		} catch (Exception e1) {
		}
		return ret;
	}
}
