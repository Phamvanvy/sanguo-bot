package com.pip.sanguo.editor.skill;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.AnimatePreviewer;
import com.pipimage.image.PipImage;

public class ChooseSkillIconDialog extends Dialog {
    
    private int selectedIconIndex = -1;
    private ImageViewer previewer;
    private PipImage pimg;
    
    public int getSelectedIconIndex() {
        return selectedIconIndex;
    }

    public void setSelectedTemplate(int t) {
        this.selectedIconIndex = t;
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseSkillIconDialog(Shell parentShell, PipImage pimg) {
        super(parentShell);
        this.pimg = pimg;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());

        previewer = new ImageViewer(container, SWT.NONE);
        previewer.setInput(pimg);
        previewer.setSelectedFrame(selectedIconIndex);
        previewer.zoomin();
        
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
        return new Point(720, 644);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择技能图标");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            selectedIconIndex = previewer.getSelectedFrame();
        }
        super.buttonPressed(buttonId);
    }
}
