package com.pip.sanguo.editor.skill;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.pip.sanguo.data.skill.SkillConfig;

public class SkillAnalyseDialog extends Dialog {

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            int level = ((Integer)element).intValue();
            float value = analyzer.getParamValue(level, columnIndex);
            return DescriptionPattern.formatFloat(value);
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = new Object[skill.maxLevel];
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
    private Table table;
    private SkillConfig skill;
    private SkillAnalyzer analyzer;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public SkillAnalyseDialog(Shell parentShell) {
        super(parentShell);
    }
    
    public void setSkill(SkillConfig skill) {
        this.skill = skill;
        analyzer = new SkillAnalyzer(skill);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        final TableViewer tableViewer = new TableViewer(container, SWT.BORDER);
        tableViewer.setLabelProvider(new TableLabelProvider());
        tableViewer.setContentProvider(new ContentProvider());
        table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        String[] params = analyzer.getParamNames();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
                newColumnTableColumn.setWidth(70);
                newColumnTableColumn.setText(params[i]);
            }
        }
        
        tableViewer.setInput(this);
        
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "关闭", true);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(702, 541);
    }
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("技能分析");
    }
}
