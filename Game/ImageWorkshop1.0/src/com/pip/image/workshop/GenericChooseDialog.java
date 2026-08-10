package com.pip.image.workshop;

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
import org.eclipse.swt.widgets.Label;
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
            return candidates;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private Combo combo;
    private Object[] candidates;
    private String title;
    private String message;
    private Object selection;
    private int selectionIndex;
    private ComboViewer comboViewer;
	private Label labelMessage;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public GenericChooseDialog(Shell parentShell, String t, String m, java.util.List cands) {
        super(parentShell);
        title = t;
        message = m;
        candidates = cands.toArray();
    }
    
    public GenericChooseDialog(Shell parentShell, String t, String m, Object[] cands) {
    	super(parentShell);
        title = t;
        message = m;
        candidates = cands;
    }

    public Object getSelection() {
        return selection;
    }
    
    public int getSelectionIndex() {
    	return selectionIndex;
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

        labelMessage = new Label(container, SWT.NONE);
        labelMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        comboViewer = new ComboViewer(container, SWT.READ_ONLY);
        comboViewer.setLabelProvider(new ListLabelProvider());
        comboViewer.setContentProvider(new ContentProvider());
        combo = comboViewer.getCombo();
        combo.setVisibleItemCount(50);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboViewer.setInput(this);
        combo.select(0);
        
        labelMessage.setText(message);

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
            selectionIndex = comboViewer.getCombo().getSelectionIndex();
        }
        super.buttonPressed(buttonId);
    }
}