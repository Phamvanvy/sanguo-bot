package com.pip.sanguo.editor.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.AnimatePreviewer;

public class ChooseHorseTypeDialog extends Dialog {
    private int[] selectedTypes;
    private HorseType[] candidates;
    private Button[] buttons;
    
    public int[] getSelectedTypes() {
        return selectedTypes;
    }

    public void setSelectedTypes(int[] t) {
        this.selectedTypes = t;
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseHorseTypeDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());

        final ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        final Composite choiceContainer = new Composite(scrolledComposite, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.numColumns = 5;
        choiceContainer.setLayout(gl);
        choiceContainer.setSize(89, 89);
        scrolledComposite.setContent(choiceContainer);
        
        List<DataObject> list = EditorApplication.getProj().getDataListByType(HorseType.class);
        candidates = new HorseType[list.size()];
        list.toArray(candidates);
        
        buttons = new Button[candidates.length];
        Map<Integer, Integer> idToIndex = new HashMap<Integer, Integer>();
        for (int i = 0; i < candidates.length; i++) {
            buttons[i] = new Button(choiceContainer, SWT.CHECK);
            buttons[i].setText(candidates[i].getTitle());
            idToIndex.put(candidates[i].id, i);
        }

        if (selectedTypes != null) {
            for (int i = 0; i < selectedTypes.length; i++) {
                if (idToIndex.containsKey(selectedTypes[i])) {
                    buttons[idToIndex.get(selectedTypes[i])].setSelection(true);
                }
            }
        }
        choiceContainer.pack();

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
        newShell.setText("选择坐骑类型");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < candidates.length; i++) {
                if (buttons[i].getSelection()) {
                    list.add(candidates[i].id);
                }
            }
            selectedTypes = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                selectedTypes[i] = list.get(i);
            }
        }
        super.buttonPressed(buttonId);
    }

}
