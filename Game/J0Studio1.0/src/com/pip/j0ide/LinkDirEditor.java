package com.pip.j0ide;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.GTLProgGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.preprocess.GTLPreProcessor;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.data.Variable;

public class LinkDirEditor extends EditorPart {
	public LinkDirEditor() {
	}

	public static final String ID = "com.pip.j0ide.LinkDirEditor"; //$NON-NLS-1$
	private boolean dirty = false;
	private Button btnAddLinkDir;
	private Button btnClearLinkDir;
	private Text text;
	private DirectoryDialog fileDlg;
	private Label label;
	private Text textSourceEncoding;
	private Label label_1;
	private Button buttonShortCircuit;
	private Label label_2;
	private Button buttonTypeCheck;
	private Label label_3;
	private Button buttonUnreachCheck;
	private Label label_4;
	private Button buttonReturnCheck;
		
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);
		
		btnAddLinkDir = new Button(container, SWT.NONE);
		btnAddLinkDir.setText("Link目录");
		btnAddLinkDir.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        if (fileDlg == null) {
		            fileDlg = new DirectoryDialog(btnAddLinkDir.getShell(), SWT.OPEN);
		            if(Application.getInstance().getProjectData().getLinkDir() != null) {
		            	fileDlg.setFilterPath(Application.getInstance().getProjectData().getLinkDir().getAbsolutePath());		            	
		            }
		        }
		        String file = fileDlg.open();
		        if (file != null) {
		        	text.setText(file);
		        	setDirty(true);
		        }
		    }
		});
		
		
        text = new Text(container, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        text.setEditable(false);
        if(Application.getInstance().getProjectData().getLinkDir() != null) {
        	text.setText(Application.getInstance().getProjectData().getLinkDir().getAbsolutePath());        	
        }

		btnClearLinkDir = new Button(container, SWT.NONE);
		btnClearLinkDir.setText("清空Link目录");
		btnClearLinkDir.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
	        	text.setText("");
	        	setDirty(true);
		    }
		});
		
		label = new Label(container, SWT.NONE);
		label.setText("源代码编码：");
		
		textSourceEncoding = new Text(container, SWT.BORDER);
		textSourceEncoding.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				setDirty(true);
			}
		});
		textSourceEncoding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textSourceEncoding.setText(Application.getInstance().getProjectData().sourceEncoding);
		new Label(container, SWT.NONE);
		
		label_1 = new Label(container, SWT.NONE);
		label_1.setText("支持短路计算");
		
		buttonShortCircuit = new Button(container, SWT.CHECK);
		buttonShortCircuit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
			}
		});
		buttonShortCircuit.setSelection(Application.getInstance().getProjectData().shortCircuit);
		new Label(container, SWT.NONE);
		
		label_2 = new Label(container, SWT.NONE);
		label_2.setText("强制类型检查");
		
		buttonTypeCheck = new Button(container, SWT.CHECK);
		buttonTypeCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
			}
		});
		buttonTypeCheck.setSelection(Application.getInstance().getProjectData().typeCheck);
		new Label(container, SWT.NONE);

		label_3 = new Label(container, SWT.NONE);
		label_3.setText("检查不可到达代码");
		
		buttonUnreachCheck = new Button(container, SWT.CHECK);
		buttonUnreachCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
			}
		});
		buttonUnreachCheck.setSelection(Application.getInstance().getProjectData().unreachCheck);
		new Label(container, SWT.NONE);

		label_4 = new Label(container, SWT.NONE);
		label_4.setText("检查返回值");
		
		buttonReturnCheck = new Button(container, SWT.CHECK);
		buttonReturnCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
			}
		});
		buttonReturnCheck.setSelection(Application.getInstance().getProjectData().multiReturnCheck);
		new Label(container, SWT.NONE);
		
		setDirty(false);
		setPartName("项目设置");
	}
	
	public void setFocus() {
	}

    public void doSave(IProgressMonitor monitor) {               
        try {
        	ProjectData pd = Application.getInstance().getProjectData();
        	pd.setLinkDir(text.getText());
        	pd.sourceEncoding = textSourceEncoding.getText().trim();
        	pd.shortCircuit = buttonShortCircuit.getSelection();
        	pd.typeCheck = buttonTypeCheck.getSelection();
        	pd.unreachCheck = buttonUnreachCheck.getSelection();
        	pd.multiReturnCheck = buttonReturnCheck.getSelection();
        	pd.save();
            
        	if (pd.getLinkDir() != null) {
        		SystemFunctionManager.configure(new File[] {
        				new File(Settings.workingDir, "gtl/functions.properties"),
        				new File(pd.getLinkDir(), "core_functions.properties")
        		});
        	} else {
        		SystemFunctionManager.configure(new File[] {
        				new File(Settings.workingDir, "gtl/functions.properties")
        		});
        	}
            GTLPreProcessor.sourceEncoding = pd.sourceEncoding;
            GTLProgGenerator.supportShortCircuit = pd.shortCircuit;
            GTLProgGenerator.supportTypeCheck_G = pd.typeCheck;
            GTLProgGenerator.supportUnreachCheck = pd.unreachCheck;
            GTLProgGenerator.supportMultiReturnCheck = pd.multiReturnCheck;
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "保存数据失败", e.toString());
            monitor.setCanceled(true);
            return;
        }
        setDirty(false);
    }

	public void doSaveAs() {}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

    private void setDirty(boolean value) {
        dirty = value;
        firePropertyChange(PROP_DIRTY);
    }
}
