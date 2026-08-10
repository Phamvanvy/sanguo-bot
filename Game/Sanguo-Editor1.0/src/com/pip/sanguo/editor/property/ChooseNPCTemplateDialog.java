package com.pip.sanguo.editor.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.AnimatePreviewer;

public class ChooseNPCTemplateDialog extends Dialog {
    class ListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            List<DataObject> list = ((ProjectData)inputElement).getDataListByType(NPCTemplate.class);
            List<DataObject> retList = new ArrayList<DataObject>();
            for (int i = 0; i < list.size(); i++) {
                NPCTemplate t = (NPCTemplate)list.get(i);
                if (matchCondition(t)) {
                    retList.add(t);
                }
            }
            return retList.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private ListViewer listViewer;
    private org.eclipse.swt.widgets.List list;
    private Text text;
    private String searchCondition;
    private int selectedTemplate = -1;
    private AnimatePreviewer previewer;
    
    public int getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(int t) {
        this.selectedTemplate = t;
    }
    
    private boolean matchCondition(NPCTemplate t) {
        if (searchCondition == null || searchCondition.length() == 0) {
            return true;
        }
        if (t.title.indexOf(searchCondition) >= 0 || String.valueOf(t.id).indexOf(searchCondition) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseNPCTemplateDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("查找：");

        text = new Text(container, SWT.BORDER);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                searchCondition = text.getText();
                StructuredSelection sel = (StructuredSelection)listViewer.getSelection();
                Object selObj = sel.isEmpty() ? null : sel.getFirstElement();
                listViewer.refresh();
                if (selObj != null) {
                    sel = new StructuredSelection(selObj);
                    listViewer.setSelection(sel);
                }
            }
        });
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                updatePreviewer();
            }
        });
        listViewer.setContentProvider(new ListContentProvider());
        list = listViewer.getList();
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        listViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                StructuredSelection sel = (StructuredSelection)event.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                buttonPressed(IDialogConstants.OK_ID);
            }
        });
        listViewer.setInput(EditorApplication.getInstance().getProjectData());

        previewer = new AnimatePreviewer(container, SWT.NONE);
        previewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        previewer.setListVisible(false);
        previewer.setEditEnable(false);
        
        if (selectedTemplate != -1) {
            try {
                // 查找这个NPC在tree中的位置
                NPCTemplate t = (NPCTemplate)EditorApplication.getProj().findObject(NPCTemplate.class, selectedTemplate);
                if (t != null) {
                    searchCondition = t.title;
                    text.setText(searchCondition);
                    text.selectAll();
                    StructuredSelection sel = new StructuredSelection(t);
                    listViewer.setSelection(sel);
                }
            } catch (Exception e) {
            }
        }

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
        return new Point(520, 644);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择NPC模板");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            StructuredSelection sel = (StructuredSelection)listViewer.getSelection();
            if (sel.isEmpty() || !(sel.getFirstElement() instanceof NPCTemplate)) {
                selectedTemplate = -1;
            } else {
                selectedTemplate = ((NPCTemplate)sel.getFirstElement()).id;
            }
        }
        super.buttonPressed(buttonId);
    }

    private void updatePreviewer() {
        StructuredSelection sel = (StructuredSelection)listViewer.getSelection();
        if (sel.isEmpty()) {
            previewer.setAnimateFile(null);
            return;
        }
        NPCTemplate t = (NPCTemplate)sel.getFirstElement();
        if(t.image != null){
            previewer.setAnimateFile(t.image.source);
        }
        else{
            //TODO 把动画预览部分设置为空
            
        }
    }
}
