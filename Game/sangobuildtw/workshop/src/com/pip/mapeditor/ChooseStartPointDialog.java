package com.pip.mapeditor;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ChooseStartPointDialog extends Dialog {
    
    class ListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            return "[" + ((Point)element).x + ", " + ((Point)element).y + "]";  
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
    
    private ListViewer listViewer;
    private List<Point> candidates;
    private int selected;

    public Point getSelected() {
        return candidates.get(selected);
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseStartPointDialog(Shell parentShell, List<Point> cands) {
        super(parentShell);
        this.candidates = cands;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());

        listViewer = new ListViewer(container, SWT.BORDER);
        listViewer.setLabelProvider(new ListLabelProvider());
        listViewer.setContentProvider(new ContentProvider());
        listViewer.setInput(new Object());
        
        listViewer.getList().select(0);
        
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定",
                true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    protected Point getInitialSize() {
        return new Point(222, 247);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择起点");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            selected = listViewer.getList().getSelectionIndex();
            if (selected == -1) {
                MessageDialog.openError(this.getShell(), "错误", "必须选择一个起点。");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
