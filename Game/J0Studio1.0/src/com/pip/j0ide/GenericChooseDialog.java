package com.pip.j0ide;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class GenericChooseDialog extends Dialog {
    class ListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            return element.toString();
        }
        public Image getImage(Object element) {
            return null;
        }
    }
    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return candidates.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private Combo combo;
    private java.util.List candidates;
    private String title;
    private Object selection;
    private ComboViewer comboViewer;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public GenericChooseDialog(Shell parentShell, String t, java.util.List cands) {
        super(parentShell);
        title = t;
        candidates = cands;
    }

    public Object getSelection() {
        return selection;
    }
    
    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);

        comboViewer = new ComboViewer(container, SWT.READ_ONLY);
        comboViewer.setLabelProvider(new ListLabelProvider());
        comboViewer.setContentProvider(new ContentProvider());
        combo = comboViewer.getCombo();
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboViewer.setInput(this);
        combo.select(0);

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
        return new Point(367, 157);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            StructuredSelection sel = (StructuredSelection)comboViewer.getSelection();
            if (sel.isEmpty()) {
                MessageDialog.openError(getShell(), "错误", "请选择一个选项。");
                return;
            }
            selection = sel.getFirstElement();
        }
        super.buttonPressed(buttonId);
    }
}