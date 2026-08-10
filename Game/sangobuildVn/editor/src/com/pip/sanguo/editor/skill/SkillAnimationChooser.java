package com.pip.sanguo.editor.skill;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.skill.ChooseSkillIconDialog;
import com.pipimage.image.PipImage;

public class SkillAnimationChooser extends Composite {
    private ModifyListener listener = null;
    
    private Text textAnimationID;
    
    public SkillAnimationChooser(Composite parent, int style) {
        super(parent, SWT.NONE);
        
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);
        
        textAnimationID = new Text(this, SWT.BORDER);
        textAnimationID.setEditable(false);
        final GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAnimationID.setLayoutData(gd_text_1);
        textAnimationID.setText("-1");
        
        final Button chooseButton = new Button(this, SWT.NONE);
        chooseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        chooseButton.setText("...");
        chooseButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                onChooseAnimation();
            }
        });
    }
    
    public void setHandler(DefaultDataObjectEditor handle) {
        this.listener = handle;
    }
    
    public int getAnimationID() {
        return Integer.parseInt(textAnimationID.getText());
    }
    
    public void setAnimationID(int value) {
        textAnimationID.setText(String.valueOf(value));
    }
    
    private void onChooseAnimation(){
        ChooseSkillAnimationDialog iconChoose = new ChooseSkillAnimationDialog(getShell());
        iconChoose.setSelectedFrame(getAnimationID());
        if (iconChoose.open() == IDialogConstants.OK_ID) {
            int frameIndex = iconChoose.getSelectedFrame();
            setAnimationID(frameIndex);
            fireModified();
        }
    }

    public void addModifyListener(ModifyListener l) {
        this.listener = l;
    }
    
    private void fireModified() {
        if (listener != null) {
            Event e = new Event();
            e.widget = this;
            ModifyEvent event = new ModifyEvent(e);
            listener.modifyText(event);
        }
    }
}
