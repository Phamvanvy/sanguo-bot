package com.pip.sanguo.editor.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.LevelTableDialog.CellModifier;
import com.pip.sanguo.editor.util.AnimatePreviewer;

public class NPCFunctionDialog extends Dialog {
    class CellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            return true;
        }
        
        public Object getValue(Object element, String property) {
            int index = ((Integer)element).intValue();
            if ("name".equals(property)) {
                return functionNames[index];
            } else if ("script".equals(property)) {
                return functionScripts[index];
            }
            return "";
        }

        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            int index = ((Integer)ti.getData()).intValue();
            if ("name".equals(property)) {
                functionNames[index] = (String)value;
            } else if ("script".equals(property)) {
                functionScripts[index] = (String)value;
            }
            tableViewer.refresh();
        }
    }

    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = new Object[10];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new Integer(i);
            }
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            int index = ((Integer)element).intValue();
            if (columnIndex == 0) {
                return functionNames[index];
            } else if (columnIndex == 1) {
                return functionScripts[index];
            } else {
                return "";
            }
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    private String[] functionNames = new String[10];
    private String[] functionScripts = new String[10];
    
    private Table table;
    private TableViewer tableViewer;
    
    public String[] getFunctions() {
        String[] ret = new String[] { "", "" };
        for (int i = 0; i < 10; i++) {
            if (functionNames[i].trim().length() > 0) {
                if (ret[0].length() > 0) {
                    ret[0] += ";";
                }
                ret[0] += functionNames[i].trim();
                if (ret[1].length() > 0) {
                    ret[1] += ";;;;";
                }
                ret[1] += functionScripts[i].trim();
            }
        }
        return ret;
    }
    
    public void setFunctions(String[] info) {
        String[] names = info[0].split(";");
        String[] scripts = info[1].split(";;;;");
        System.arraycopy(names, 0, functionNames, 0, names.length > 10 ? 10 : names.length);
        System.arraycopy(scripts, 0, functionScripts, 0, scripts.length > 10 ? 10 : scripts.length);
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public NPCFunctionDialog(Shell parentShell) {
        super(parentShell);
        Arrays.fill(functionNames, "");
        Arrays.fill(functionScripts, "");
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout());

        tableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer.setContentProvider(new ContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());
        table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final TableColumn nameColumn = new TableColumn(table, SWT.NONE);
        nameColumn.setWidth(198);
        nameColumn.setText("功能名称");

        final TableColumn scriptColumn = new TableColumn(table, SWT.NONE);
        scriptColumn.setWidth(618);
        scriptColumn.setText("功能脚本");

        tableViewer.setColumnProperties(new String[] {
                "name", "script"
        });
        tableViewer.setCellModifier(new CellModifier());
        tableViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table),
                new TextCellEditor(table)
        });
        tableViewer.setInput(this);

        final Label label = new Label(container, SWT.NONE);
        label.setText("注意：Java版三国只支持一个功能。");
        
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
        return new Point(852, 599);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("编辑NPC功能");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            for (int i = 0; i < 10; i++) {
                String func = functionNames[i].trim();
                String script = functionScripts[i].trim();
                if (func.length() != 0 && script.length() == 0) {
                    MessageDialog.openError(getShell(), "错误", "请填写功能脚本。");
                    return;
                }
                if (func.length() == 0 && script.length() != 0) {
                    MessageDialog.openError(getShell(), "错误", "请填写功能名称。");
                    return;
                }
            }
        }
        super.buttonPressed(buttonId);
    }

}
