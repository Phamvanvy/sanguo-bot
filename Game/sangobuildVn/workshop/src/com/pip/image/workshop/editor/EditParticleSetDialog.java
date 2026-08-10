package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import com.pip.propertysheet.PropertySheetViewer;
import com.pipimage.image.PipAnimateSet;
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

public class EditParticleSetDialog extends Dialog {
    private Text textGenerateCountRange;
    private Combo comboPathType;
    private Text textLiveTimeRange;
    private Text textLiveTime;
    private Text textYRange;
    private Text textY;
    private Text textXRange;
    private Text textX;
    private Text textGenerateTimes;
    private Text textGenerateInterval;
    private Text textGenerateCount;
    private Text textStartTime;
    private Text textTitle;
    private AnimateSelector animateChooser;
    private PropertySheetViewer pathPropertySheet;
    
    private PipAnimateSet animates;
    private PipParticleSet editObject;
    private PipParticleEffectSet effectSet;
    private boolean updating = false;
    
    private static Class[] PATH_TYPE_CLASSES = { 
    	StayPath.class, 
    	HelixPath.class, 
    	LinePath.class, 
    	ParabolaPath.class, 
    	Helix2Path.class, 
    	FirePath.class, 
    	SinusoidPath.class 
    };
    private static String[] PATH_TYPE_NAMES = new String[PATH_TYPE_CLASSES.length];
    static {
    	for (int i = 0; i < PATH_TYPE_CLASSES.length; i++) {
    		try {
    			PATH_TYPE_NAMES[i] = ((PipParticlePath)PATH_TYPE_CLASSES[i].newInstance()).getTypeName();
    		} catch (Exception e) {
    			PATH_TYPE_NAMES[i] = "错误路径类型";
    		}
    	}
    }
    
    private static class PathPropertyEnable implements IPropertySource, IPropertySheetEnable {
    	private PipParticlePath path;
        
    	public PathPropertyEnable(PipParticlePath path) {
    		this.path = path;
    	}
        
        public Object getEditableValue() {
            return this;
        }

        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] ret = new IPropertyDescriptor[path.getParamCount()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new TextPropertyDescriptor(new Integer(i), path.getParamDesc(i));
            }
            return ret;
        }

        public Object getPropertyValue(Object id) {
            int index = ((Integer)id).intValue();
            return String.valueOf(path.getParam(index));
        }

        public boolean isPropertySet(Object id) {
            return false;
        }

        public void resetPropertyValue(Object id) {}

        public void setPropertyValue(Object id, Object value) {
            try {
                int index = ((Integer)id).intValue();
                double value1 = Double.parseDouble((String)value);
                path.setParam(index, value1);
            } catch (Exception e) {
            }
        }
        
        public IPropertySource getPropertySource() {
            return this;
        }
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public EditParticleSetDialog(Shell parentShell, PipAnimateSet animates, PipParticleSet editObject,
    		PipParticleEffectSet effectSet) {
        super(parentShell);
        this.animates = animates;
        this.editObject = editObject;
        this.effectSet = effectSet;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);
        
        final Label label = new Label(container, SWT.NONE);
        label.setText("标题：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textTitle.setLayoutData(gd_textTitle);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("开始时间：");

        textStartTime = new Text(container, SWT.BORDER);
        final GridData gd_textStartTime = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textStartTime.setLayoutData(gd_textStartTime);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("生成间隔：");

        textGenerateInterval = new Text(container, SWT.BORDER);
        final GridData gd_textGenerateInterval = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textGenerateInterval.setLayoutData(gd_textGenerateInterval);
        textGenerateInterval.setText(String.valueOf(this.editObject.generateInterval));

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("生成数量：");

        textGenerateCount = new Text(container, SWT.BORDER);
        final GridData gd_textGenerateCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textGenerateCount.setLayoutData(gd_textGenerateCount);
        textGenerateCount.setText(String.valueOf(this.editObject.generateCount));

        final Label label_14 = new Label(container, SWT.NONE);
        label_14.setLayoutData(new GridData());
        label_14.setText("浮动范围：");

        textGenerateCountRange = new Text(container, SWT.BORDER);
        final GridData gd_textGenerateCountRange = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textGenerateCountRange.setLayoutData(gd_textGenerateCountRange);
        textGenerateCountRange.setText(String.valueOf(this.editObject.generateCountRange));

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("生成次数：");

        textGenerateTimes = new Text(container, SWT.BORDER);
        final GridData gd_textGenerateTimes = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textGenerateTimes.setLayoutData(gd_textGenerateTimes);
        textGenerateTimes.setText(String.valueOf(this.editObject.generateTimes));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("X：");

        textX = new Text(container, SWT.BORDER);
        final GridData gd_textX = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textX.setLayoutData(gd_textX);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("浮动范围：");

        textXRange = new Text(container, SWT.BORDER);
        final GridData gd_textXRange = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textXRange.setLayoutData(gd_textXRange);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("Y：");

        textY = new Text(container, SWT.BORDER);
        final GridData gd_textY = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textY.setLayoutData(gd_textY);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setText("浮动范围：");

        textYRange = new Text(container, SWT.BORDER);
        final GridData gd_textYRange = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textYRange.setLayoutData(gd_textYRange);

        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setText("粒子动画：");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new FillLayout());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
        
        animateChooser = new AnimateSelector(composite, SWT.NONE);
        animateChooser.setInput(animates);

        final Label label_10 = new Label(container, SWT.NONE);
        label_10.setText("生存时间：");

        textLiveTime = new Text(container, SWT.BORDER);
        final GridData gd_textLiveTime = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLiveTime.setLayoutData(gd_textLiveTime);

        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setText("浮动范围：");

        textLiveTimeRange = new Text(container, SWT.BORDER);
        final GridData gd_textLiveTimeRange = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLiveTimeRange.setLayoutData(gd_textLiveTimeRange);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setText("路径算法：");

        final Composite composite_2 = new Composite(container, SWT.NONE);
        final GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
        composite_2.setLayoutData(gd_composite_2);
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.marginWidth = 0;
        gridLayout_1.marginHeight = 0;
        gridLayout_1.numColumns = 2;
        composite_2.setLayout(gridLayout_1);

        comboPathType = new Combo(composite_2, SWT.READ_ONLY);
        comboPathType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        comboPathType.setVisibleItemCount(50);
        comboPathType.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		if (updating) {
        			return;
        		}
        		int index = comboPathType.getSelectionIndex();
        		Class cls = PATH_TYPE_CLASSES[index];
        		if (cls != editObject.path.getClass()) {
        			try {
        				editObject.path = (PipParticlePath)cls.newInstance();
        				pathPropertySheet.setInput(new Object[] { new PathPropertyEnable(editObject.path) });
        				pathPropertySheet.refresh();
        			} catch (Exception e1) {
        				e1.printStackTrace();
        			}
        		}
        	}
        });
        comboPathType.setItems(PATH_TYPE_NAMES);
        comboPathType.select(pathClassToIndex(this.editObject.path.getClass()));

        final Button copyButton = new Button(composite_2, SWT.NONE);
        copyButton.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		CopyPathDialog dlg = new CopyPathDialog(getShell(), effectSet);
        		if (dlg.open() == Dialog.OK) {
        			PipParticlePath path = dlg.getSelectedPath().dup();
        			editObject.path = path;
        			updating = true;
        			comboPathType.select(pathClassToIndex(editObject.path.getClass()));
        	        pathPropertySheet.setInput(new Object[] { new PathPropertyEnable(editObject.path) });
        	        updating = false;
        		}
        	}
        });
        copyButton.setText("从其他粒子复制...");

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("路径参数：");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Composite composite_1 = new Composite(container, SWT.NONE);
        composite_1.setLayout(new FillLayout());
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
        
        pathPropertySheet = new PropertySheetViewer(composite_1, SWT.BORDER, false);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        pathPropertySheet.setRootEntry(rootEntry);

        this.updating = true;
        textX.setText(String.valueOf(this.editObject.x));
        textXRange.setText(String.valueOf(this.editObject.xrange));
        textY.setText(String.valueOf(this.editObject.y));
        textYRange.setText(String.valueOf(this.editObject.yrange));
        textLiveTime.setText(String.valueOf(this.editObject.liveTime));
        textLiveTimeRange.setText(String.valueOf(this.editObject.liveTimeRange));
        textStartTime.setText(String.valueOf(this.editObject.startTime));
        textTitle.setText(this.editObject.title);
        animateChooser.setSelectedIndex(this.editObject.particleID);
        pathPropertySheet.setInput(new Object[] { new PathPropertyEnable(this.editObject.path) });
        this.updating = false;

        return container;
    }
    
    private int pathClassToIndex(Class cls) {
    	for (int i = 0; i < PATH_TYPE_CLASSES.length; i++) {
    		if (cls == PATH_TYPE_CLASSES[i]) {
    			return i;
    		}
    	}
    	return -1;
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
        return new Point(1011, 786);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("粒子组设定");
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
	
	protected void checkInput() {
		this.editObject.x = Integer.parseInt(textX.getText());
		this.editObject.xrange = Integer.parseInt(textXRange.getText());
		this.editObject.y = Integer.parseInt(textY.getText());
		this.editObject.yrange = Integer.parseInt(textYRange.getText());
		this.editObject.liveTime = Integer.parseInt(textLiveTime.getText());
		this.editObject.liveTimeRange = Integer.parseInt(textLiveTimeRange.getText());
		this.editObject.startTime = Integer.parseInt(textStartTime.getText());
		this.editObject.generateTimes = Integer.parseInt(textGenerateTimes.getText());
		this.editObject.generateInterval = Integer.parseInt(textGenerateInterval.getText());
		this.editObject.generateCount = Integer.parseInt(textGenerateCount.getText());
		this.editObject.generateCountRange = Integer.parseInt(textGenerateCountRange.getText());
		this.editObject.title = textTitle.getText();
        this.editObject.particleID = animateChooser.getSelectedIndex();
	}
}
