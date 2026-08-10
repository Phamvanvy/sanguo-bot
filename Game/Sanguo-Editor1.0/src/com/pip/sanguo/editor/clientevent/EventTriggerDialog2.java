package com.pip.sanguo.editor.clientevent;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.clientEvent.EventTrigger;
import com.pip.sanguo.editor.clientevent.trigger.TempManagerEvent;

/**
 * @author bqzhang
 *
 */
public class EventTriggerDialog2  extends Dialog {
    Composite container;
    Composite conSub;   //包含的模板界面
    
    private Combo comboTaskFlag;    //条件
    
    private EventTrigger trigger;
    
    /**
     * 选中项目id
     * @return
     */
    public EventTrigger getSelectedObject() {
        return trigger;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public EventTriggerDialog2(Shell parentShell, EventTrigger trigger) {
        super(parentShell);
        this.trigger = trigger;
        //this.parentShell = parentShell;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        container.setLayout(gridLayout);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("事件触发类型：");
        comboTaskFlag = new Combo(container, SWT.READ_ONLY);
        comboTaskFlag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        comboTaskFlag.setItems(TempManagerEvent.TEMPLATES_NAMES);
        comboTaskFlag.setVisibleItemCount(15);
        comboTaskFlag.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if(trigger.type != comboTaskFlag.getSelectionIndex()){
                    trigger.type = comboTaskFlag.getSelectionIndex();
                    trigger.exprEvent = TempManagerEvent.getExprEvent(trigger.type);
                    repaintDialog();
                }
            }
        });
        
        repaintDialog();
        
        return container;
    }
    
    public void repaintDialog(){
        if(conSub != null){
            Control[] con= conSub.getChildren();
            for(int i = 0; i < con.length; i++){
                con[i].dispose();
            }
            conSub.dispose();
            conSub = null;
        }
        if(trigger.exprEvent != null){
            conSub = trigger.exprEvent.getProperty(container);
            container.layout(true);
            initValues();
        }
    }
    
    /**
     * 初始化界面 各项 数值
     */
    public void initValues(){
        comboTaskFlag.select(trigger.type);
        trigger.exprEvent.setPropertyValue(trigger.exprEvent.getParamsValue());
    }
    
    /**
     * Create contents of the button bar
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(644, 320);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("事件触发条件设定");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            trigger.type = comboTaskFlag.getSelectionIndex();
            if(conSub != null){
                trigger.exprEvent.setParamsValue();
            }
        }
        super.buttonPressed(buttonId);
    }
}
