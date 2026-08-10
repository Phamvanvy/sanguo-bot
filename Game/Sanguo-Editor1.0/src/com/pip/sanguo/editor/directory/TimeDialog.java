package com.pip.sanguo.editor.directory;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TimeDialog extends Dialog  implements ModifyListener {
    
    private Combo comboTime1;
    private Combo comboTime2;
    private String time1;
    private String time2;
    private String[] time = new String[] {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", 
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", 
            "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    
    public TimeDialog(Shell parentShell,String txt) {
        super(parentShell);
        createDialogArea(parentShell);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());
        
        final Label label_1 = new Label(parent, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("开始时间：");
        
        comboTime1 = new Combo(parent, SWT.READ_ONLY);
        comboTime1.setItems(time);
        final GridData gd_comboFaction = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboTime1.setLayoutData(gd_comboFaction);
        comboTime1.addModifyListener(this);
        
        final Label label_2 = new Label(parent, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("结束时间：");
        
        comboTime2 = new Combo(parent, SWT.READ_ONLY);
        comboTime2.setItems(time);
        final GridData gd_comboFaction1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboTime2.setLayoutData(gd_comboFaction1);
        comboTime2.addModifyListener(this);
        
        return container;
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
    
    public String getText(){
        return time1 + "-" + time2;
    }
    
    public void setText(String txt){
        
    }

    public void modifyText(ModifyEvent e) {
        time1 = time[comboTime1.getSelectionIndex()];
        time2 = time[comboTime2.getSelectionIndex()];
    }

}
