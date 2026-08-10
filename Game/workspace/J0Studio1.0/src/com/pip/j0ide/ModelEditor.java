package com.pip.j0ide;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.j0ide.data.Variable;

public class ModelEditor extends EditorPart {

	private Composite composite;
	private Table table;
	private Text tfComments;
	private Text tfDevice;
	private Text tfTitle;
	private Text tfID;
	public static final String ID = "com.pip.j0ide.ModelEditor"; //$NON-NLS-1$
	private ModelInput modelInput;
	private ArrayList<Variable> variables;
	private boolean dirty = false;
	private Button btnAddVariable;
	private Button btnModifyVariable;
	private Button btnDeleteVariable;
	TableViewer tableViewer;

	class VariableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return variables.toArray();
		}
		
		public void dispose() {}

	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	    }
	}
	
	class VariableLabelProvider extends LabelProvider implements ITableLabelProvider {
	    public Image getColumnImage(Object element, int columnIndex) {
	    	return null;
	    }

	    public String getColumnText(Object element, int columnIndex) {
	    	Variable var = (Variable)element;
	    	if (columnIndex == 0) {
	    		return var.name;
	    	} else {
	    		return var.value;
	    	}
	    }
	}
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		this.setPartName(this.getEditorInput().getName());
		
		variables = new ArrayList<Variable>();
		for (int i = 0; i < modelInput.model.variables.size(); i++) {
			variables.add((Variable)modelInput.model.variables.get(i).clone());
		}

		final Label lblID = new Label(container, SWT.NONE);
		lblID.setText("机型ID：");

		tfID = new Text(container, SWT.BORDER);
		tfID.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		tfID.addFocusListener(AutoSelectAll.instance);
		tfID.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});
		tfID.setText(modelInput.model.id);

		final Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setText("机型名称：");

		tfTitle = new Text(container, SWT.BORDER);
		tfTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		tfTitle.addFocusListener(AutoSelectAll.instance);
		tfTitle.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});
		tfTitle.setText(modelInput.model.title);

		final Label lblDevice = new Label(container, SWT.NONE);
		lblDevice.setText("Polish机型：");

		tfDevice = new Text(container, SWT.BORDER);
		tfDevice.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		tfDevice.addFocusListener(AutoSelectAll.instance);
		tfDevice.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});
		tfDevice.setText(modelInput.model.device);

		final Label lblComments = new Label(container, SWT.NONE);
		lblComments.setText("备注：");
		new Label(container, SWT.NONE);

		this.tfComments = new Text(container, SWT.MULTI | SWT.BORDER);
		final GridData gd_tfComments = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_tfComments.heightHint = 60;
		tfComments.setLayoutData(gd_tfComments);
		tfComments.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});
		tfComments.setText(modelInput.model.comments);

		final Label lblVariables = new Label(container, SWT.NONE);
		lblVariables.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		lblVariables.setText("自定义编译变量：");

		tableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					Variable oldvar = variables.get(index);
					Variable newvar = editVariable(oldvar);
					if (newvar != null && (!newvar.name.equals(oldvar.name) ||
							!newvar.value.equals(oldvar.value))) {
						oldvar.name = newvar.name;
						oldvar.value = newvar.value;
						tableViewer.refresh(oldvar);
						setDirty(true);
					}
				}
			}
		});
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				btnModifyVariable.setEnabled(table.getSelectionCount() == 1);
				btnDeleteVariable.setEnabled(table.getSelectionCount() == 1);
			}
		});
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.setHeaderVisible(true);

		final TableColumn nameColumn = new TableColumn(table, SWT.NONE);
		nameColumn.setWidth(150);
		nameColumn.setText("名称");

		final TableColumn valueColumn = new TableColumn(table, SWT.NONE);
		valueColumn.setWidth(200);
		valueColumn.setText("值");
		
		tableViewer.setContentProvider(new VariableContentProvider());
		tableViewer.setLabelProvider(new VariableLabelProvider());
		tableViewer.setInput(variables);

		composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 2, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		composite.setLayout(gridLayout_1);

		btnAddVariable = new Button(composite, SWT.NONE);
		btnAddVariable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Variable newvar = newVariable();
				if (newvar != null) {
					variables.add(newvar);
					tableViewer.refresh();
					setDirty(true);
				}
			}
		});
		btnAddVariable.setText("添加");

		btnModifyVariable = new Button(composite, SWT.NONE);
		btnModifyVariable.setEnabled(false);
		btnModifyVariable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					Variable oldvar = variables.get(index);
					Variable newvar = editVariable(oldvar);
					if (newvar != null && (!newvar.name.equals(oldvar.name) ||
							!newvar.value.equals(oldvar.value))) {
						oldvar.name = newvar.name;
						oldvar.value = newvar.value;
						tableViewer.refresh(oldvar);
						setDirty(true);
					}
				}
			}
		});
		btnModifyVariable.setText("修改");

		btnDeleteVariable = new Button(composite, SWT.NONE);
		btnDeleteVariable.setEnabled(false);
		btnDeleteVariable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					variables.remove(index);
					tableViewer.refresh();
					setDirty(true);
				}
			}
		});
		btnDeleteVariable.setText("删除");
		container.setTabList(new Control[] {tfID, tfTitle, tfDevice, tfComments, table, composite});
		
		setDirty(false);
	}
	
	private Variable newVariable() {
		VariableDialog dlg = new VariableDialog(getSite().getShell(), true);
		if (dlg.open() == IDialogConstants.OK_ID) {
			return dlg.getValue();
		} else {
			return null;
		}
	}
	
	private Variable editVariable(Variable var) {
		VariableDialog dlg = new VariableDialog(getSite().getShell(), false);
		dlg.setValue(var);
		if (dlg.open() == IDialogConstants.OK_ID) {
			return dlg.getValue();
		} else {
			return null;
		}
	}

	public void setFocus() {
		tfID.setFocus();
	}

	private void detectInput() throws Exception {
		if (tfID.getText().length() == 0) {
			tfID.setFocus();
			throw new Exception("必须输入机型ID。");
		}
		if (tfTitle.getText().length() == 0) {
			tfTitle.setFocus();
			throw new Exception("必须输入机型名称。");
		}
		if (tfDevice.getText().length() == 0) {
			tfDevice.setFocus();
			throw new Exception("必须输入Polish机型代码。");
		}
	}
	
	public void doSave(IProgressMonitor monitor) {
		try {
			detectInput();
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "输入错误", e.getMessage());
			monitor.setCanceled(true);
			return;
		}
		modelInput.model.id = tfID.getText();
		modelInput.model.title = tfTitle.getText();
		modelInput.model.device = tfDevice.getText();
		modelInput.model.comments = tfComments.getText();
		modelInput.model.variables.clear();
		for (int i = 0; i < variables.size(); i++) {
			modelInput.model.variables.add((Variable)variables.get(i).clone());
		}
		
		try {
			Application.getInstance().getProjectData().save();
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
		modelInput = (ModelInput)input;
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
