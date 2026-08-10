package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.rcp.itimes.admin.data.CommandData;


public class CommandWizardPage extends WizardPage{
    private Text[] texts;
    private Composite parent;

    private String errorMessage;
    private CommandData commandData;
    private String commandString;

    public CommandWizardPage(){
        super("执行命令");
        setDescription("");
    }

    public void createControl(Composite p){
        parent = p;
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);
        setControl(container);

        String[] parms = commandData.getParms();

        texts = new Text[parms.length];

        for(int i = 0; i < parms.length; i++){

            Label label = new Label(container, SWT.NONE);
            label.setText(parms[i] + "：");

            texts[i] = new Text(container, SWT.BORDER);
            texts[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }

        setTitle("请填写命令 [ " + commandData.getName() + " : " + commandData.getCommand() + "] 的参数");
    }

    public boolean makeInputData(){
        boolean result = true;
        commandString = commandData.getCommand();

        for(int i = 0; i < texts.length; i++){
            String para = texts[i].getText().trim();

            if(para.length() == 0){
                result = false;
                errorMessage = "参数未填写完整";

                break;
            }else{
                commandString += " " + para;
            }
        }

        return result;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

    public void setCommand(CommandData commandData){
        this.commandData = commandData.clone();
    }

    public String getCommand(){
        return commandString;
    }
}
