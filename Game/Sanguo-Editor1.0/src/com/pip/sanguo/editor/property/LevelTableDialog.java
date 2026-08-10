package com.pip.sanguo.editor.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.shop.BuyRequirementsCellEditor;

public class LevelTableDialog extends Dialog {
    class CellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            return "value".equals(property);
        }
        
        public Object getValue(Object element, String property) {
            int index = ((Integer)element).intValue();
            if ("value".equals(property)) {
                return String.valueOf(values[index]);
            }
            return "";
        }

        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            int index = ((Integer)ti.getData()).intValue();
            if ("value".equals(property)) {
                int newValue;
                boolean updateAll = false;
                try {
                    String s = (String)value;
                    if (s.startsWith("a")) {
                        newValue = Integer.parseInt(s.substring(1));
                        updateAll = true;
                    } else {
                        newValue = Integer.parseInt(s);
                    }
                } catch (Exception e) {
                    return;
                }
                if (!updateAll) {
                    values[index] = newValue;
                } else if (index == 0) {
                    Arrays.fill(values, newValue);
                } else {
                    int d = newValue - values[index - 1];
                    for (int i = index; i < values.length; i++) {
                        values[i] = values[i - 1] + d;
                    }
                }
                tableViewer.refresh();
            }
        }
    }

    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = new Object[values.length];
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
                return String.valueOf(index + 1);
            } else {
                return String.valueOf(values[index]);
            }
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    private Table table;
    private int[] values = new int[100];
    private TableViewer tableViewer;

    public String getMapString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(values[i]);
        }
        return sb.toString();
    }
    
    public void setMapString(String value) {
        String[] secs = value.split(",");
        Arrays.fill(values, 0);
        for (int i = 0; i < values.length && i < secs.length; i++) {
            try {
                values[i] = Integer.parseInt(secs[i]);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public LevelTableDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);

        tableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer.setContentProvider(new ContentProvider());
        tableViewer.setLabelProvider(new TableLabelProvider());
        table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final TableColumn levelColumn = new TableColumn(table, SWT.NONE);
        levelColumn.setWidth(100);
        levelColumn.setText("级别");

        final TableColumn valueColumn = new TableColumn(table, SWT.NONE);
        valueColumn.setWidth(185);
        valueColumn.setText("取值");
        
        tableViewer.setColumnProperties(new String[] {
                "level", "value"
        });
        tableViewer.setCellModifier(new CellModifier());
        tableViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table),
                new TextCellEditor(table)
        });
        tableViewer.setInput(this);

        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    protected Point getInitialSize() {
        return new Point(356, 465);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("编辑映射表");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        }
        super.buttonPressed(buttonId);
    }
}
