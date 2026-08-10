package com.pip.sanguo.editor.util;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapExit;

public class TestWidgetDialog extends Dialog {

    private Table table;
    /**
     * Create the dialog
     * @param parentShell
     */
    public TestWidgetDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        final TableViewer tableViewer = new TableViewer(container, SWT.BORDER);
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
            }
        });
        table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        //
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }

    public static void main(String[] args) throws Exception {
        ProjectData prj = new ProjectData();
        prj.serverMode = true;
        prj.load(new File("c:/workspace/Sanguo-Editor1.0/data"));
        GameMapExit[] path = prj.getPathFinder().findPath(3, 1056, 277, 1327, 1056, 848, 566);
        if (path == null) {
            System.out.println("no path");
        }
    }
}
