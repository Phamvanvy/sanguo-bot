package com.pip.image.workshop.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.pip.propertysheet.*;

public class MappingDialog extends Dialog {
    private PropertySheetViewer mappingSheet;
    private String newTitle;
    private StringMapping mapping;

    /**
     * Create the dialog
     * @param parentShell
     */
    public MappingDialog(Shell parentShell, String title) {
        super(parentShell);
        newTitle = title;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());
        
        mappingSheet = new PropertySheetViewer(container, SWT.BORDER, false);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        mappingSheet.setRootEntry(rootEntry);
        mappingSheet.setInput(new Object[] { mapping });
        //
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
        return new Point(500, 375);
    }
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(newTitle);
    }

    public StringMapping getMapping() {
        return mapping;
    }

    public void setMapping(StringMapping mapping) {
        this.mapping = mapping;
    }
}
