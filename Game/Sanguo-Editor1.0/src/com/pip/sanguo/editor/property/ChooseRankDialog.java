package com.pip.sanguo.editor.property;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.editor.EditorApplication;

public class ChooseRankDialog extends Dialog {
    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            List list = (List)inputElement;
            return list.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class ListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            return element.toString();
        }
        public Image getImage(Object element) {
            return null;
        }
    }
    private Combo combo;

    private int selectedItem = -1;
    private List<Rank> selectedItems;
    private List ranks;
    private ComboViewer comboViewer;
    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }
    
    public List<Rank> getSelectedItems() {
        return selectedItems;
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseRankDialog(Shell parentShell) {
        super(parentShell);
        ranks = EditorApplication.getProj().getDictDataListByType(Rank.class);
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

        comboViewer = new ComboViewer(container, SWT.BORDER);
        comboViewer.setContentProvider(new ContentProvider());
        comboViewer.setLabelProvider(new ListLabelProvider());
        combo = comboViewer.getCombo();
        combo.setVisibleItemCount(20);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        comboViewer.setInput(ranks);
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
        return new Point(520, 99);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择军衔");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            selectedItem = EditorApplication.getProj().getDictObjectIndex((DataObject)ranks.get(combo.getSelectionIndex()));
        }
        super.buttonPressed(buttonId);
    }
    
}

