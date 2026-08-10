package com.pip.image.workshop.editor;

import java.io.File;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.propertysheet.IPropertySheetEnable;
import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.util.SWTUtils;
import com.pipimage.image.PipParticle;
import com.pipimage.image.PipParticleEffect;
import com.pipimage.image.PipParticleEffectSet;
import com.pipimage.image.PipParticlePath;
import com.pipimage.image.PipParticleSet;

public class CopyPathDialog extends Dialog {
    private Text textPathType;
    private Combo comboParticle;
    private Combo comboEffects;
    private Text textFile;
    private PropertySheetViewer pathPropertySheet;
    
    private PipParticleEffectSet sourceEffectSet;
    private PipParticleEffectSet currentEffectSet;

    // 选中的效果
    private PipParticleEffect selectedEffect;
    // 选中的粒子
    private PipParticleSet selectedParticle;
    
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
                ret[i] = new PropertyDescriptor(new Integer(i), path.getParamDesc(i));
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
        }
        
        public IPropertySource getPropertySource() {
            return this;
        }
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public CopyPathDialog(Shell parentShell, PipParticleEffectSet sourceEffectSet) {
        super(parentShell);
        this.sourceEffectSet = sourceEffectSet;
        this.currentEffectSet = sourceEffectSet;
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
        label.setText("文件：");

        final Composite composite_3 = new Composite(container, SWT.NONE);
        composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        final GridLayout gridLayout_2 = new GridLayout();
        gridLayout_2.marginWidth = 0;
        gridLayout_2.marginHeight = 0;
        gridLayout_2.numColumns = 2;
        composite_3.setLayout(gridLayout_2);

        textFile = new Text(composite_3, SWT.READ_ONLY | SWT.BORDER);
        final GridData gd_textFile = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFile.setLayoutData(gd_textFile);

        final Button buttonBrowse = new Button(composite_3, SWT.NONE);
        buttonBrowse.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		// 选择其他文件
        		FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
        		dlg.setFilterPath(currentEffectSet.getOriginalFile().getAbsolutePath());
        		dlg.setFilterExtensions(new String[] { "*.pef" });
        		dlg.setFilterNames(new String[] { "粒子效果文件(*.pef)" });
        		String file = dlg.open();
        		if (file != null) {
        			try {
        				if (file.equals(sourceEffectSet.getOriginalFile().getAbsolutePath())) {
        					currentEffectSet = sourceEffectSet;
        				} else {
	        				PipParticleEffectSet newSet = new PipParticleEffectSet();
	        				newSet.load(new File(file));
	        				currentEffectSet = newSet;
        				}
        				updateEffectList();
        			} catch (Exception e1) {
        				SWTUtils.showError(getShell(), "错误", e1);
        			}
        		}
        	}
        });
        buttonBrowse.setText("浏览...");

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("效果：");

        comboEffects = new Combo(container, SWT.READ_ONLY);
        comboEffects.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		effectSelectionChanged();
        	}
        });
        final GridData gd_comboEffects = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboEffects.setLayoutData(gd_comboEffects);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("粒子：");

        comboParticle = new Combo(container, SWT.READ_ONLY);
        comboParticle.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		particleSelectionChanged();
        	}
        });
        final GridData gd_comboParticle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboParticle.setLayoutData(gd_comboParticle);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setText("路径算法：");

        textPathType = new Text(container, SWT.READ_ONLY | SWT.BORDER);
        final GridData gd_textPathType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPathType.setLayoutData(gd_textPathType);

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("路径参数：");
        new Label(container, SWT.NONE);

        final Composite composite_1 = new Composite(container, SWT.NONE);
        composite_1.setLayout(new FillLayout());
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
        
        pathPropertySheet = new PropertySheetViewer(composite_1, SWT.BORDER, false);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        pathPropertySheet.setRootEntry(rootEntry);

        updateEffectList();

        return container;
    }
    
    private void updateEffectList() {
    	textFile.setText(currentEffectSet.getOriginalFile().getAbsolutePath());
    	String[] effectNames = new String[currentEffectSet.getEffectCount()];
    	for (int i = 0; i < effectNames.length; i++) {
    		effectNames[i] = (i + 1) + ". " + currentEffectSet.getEffect(i).title;
    	}
    	comboEffects.setItems(effectNames);
    	if (effectNames.length > 0) {
    		comboEffects.select(0);
    	} else {
    		comboEffects.select(-1);
    	}
    	effectSelectionChanged();
    }
    
    private void effectSelectionChanged() {
    	// 选择了效果，列出所有粒子
		int index = comboEffects.getSelectionIndex();
		if (index == -1) {
			selectedEffect = null;
			comboParticle.setItems(new String[0]);
			comboParticle.select(-1);
		} else {
			selectedEffect = currentEffectSet.getEffect(index);
			String[] particleNames = new String[selectedEffect.particleSets.size()];
	    	for (int i = 0; i < particleNames.length; i++) {
	    		particleNames[i] = (i + 1) + ". " + selectedEffect.particleSets.get(i).title;
	    	}
	    	comboParticle.setItems(particleNames);
	    	if (particleNames.length > 0) {
	    		comboParticle.select(0);
	    	} else {
	    		comboParticle.select(-1);
	    	}
		}
		particleSelectionChanged();
    }
    
    private void particleSelectionChanged() {
		// 选择了粒子，更新路径显示
		int index = comboParticle.getSelectionIndex();
		if (index == -1) {
			textPathType.setText("");
			pathPropertySheet.setInput(new Object[] { });
		} else {
			selectedParticle = selectedEffect.particleSets.get(index);
			textPathType.setText(selectedParticle.path.getTypeName());
			pathPropertySheet.setInput(new Object[] { new PathPropertyEnable(selectedParticle.path) });
		}
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
        return new Point(608, 543);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("复制路径");
    }
    
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				checkInput();
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", e);
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
	
	protected void checkInput() throws Exception {
		if (selectedParticle == null) {
			throw new Exception("请选择一个参考粒子。");
		}
	}
	
	public PipParticlePath getSelectedPath() {
		return selectedParticle.path;
	}
}
