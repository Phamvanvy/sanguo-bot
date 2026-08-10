package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.pip.rcp.itimes.admin.data.CommandData;


public class NewCommandListWizardPage extends WizardPage{
    private List lstParms;
    private Combo cbConfirm;
    private Text txCommand;
    private Text txName;
    private Composite parent;

    private String errorMessage;
    private CommandData commandData;

    public NewCommandListWizardPage(){
        super("新建命令");
        setTitle("请输入命令信息");
        setDescription("");
    }

    public void createControl(Composite p){
        parent = p;
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label = new Label(container, SWT.NONE);
        label.setText("名称：");

        txName = new Text(container, SWT.BORDER);
        final GridData gd_txName = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txName.setLayoutData(gd_txName);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("命令：");

        txCommand = new Text(container, SWT.BORDER);
        final GridData gd_txCommand = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txCommand.setLayoutData(gd_txCommand);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("确认：");

        cbConfirm = new Combo(container, SWT.READ_ONLY);
        cbConfirm.setItems(new String[]{
                        "否", "是"
        });
        cbConfirm.select(0);
        final GridData gd_cbConfirm = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        cbConfirm.setLayoutData(gd_cbConfirm);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("参数：");

        lstParms = new List(container, SWT.BORDER);
        final GridData gd_lstParms = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
        lstParms.setLayoutData(gd_lstParms);

        final Button button = new Button(container, SWT.NONE);
        button.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(final SelectionEvent e){
                InputDialog input = new InputDialog(null, "请输入参数名称", null, null, null);

                if(input.open() == InputDialog.OK){
                    lstParms.add(input.getValue());
                }

            }
        });

        button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        button.setText("添加");
        new Label(container, SWT.NONE);

        final Button button_1 = new Button(container, SWT.NONE);
        button_1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(final SelectionEvent e){
                if(lstParms.getSelectionIndex() >= 0){
                    InputDialog input = new InputDialog(null, "请修改参数名称", null, lstParms.getItem(lstParms.getSelectionIndex()), null);

                    if(input.open() == InputDialog.OK){
                        lstParms.setItem(lstParms.getSelectionIndex(), input.getValue());
                    }
                }
            }
        });

        button_1.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        button_1.setText("修改");
        new Label(container, SWT.NONE);

        final Button button_2 = new Button(container, SWT.NONE);
        button_2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(final SelectionEvent e){
                if(lstParms.getSelectionIndex() >= 0){
                    int index = lstParms.getSelectionIndex();
                    String parm = lstParms.getItem(index);

                    if(MessageDialog.openConfirm(null, "删除命令", "确实要删除命令：" + parm + "吗？")){
                        lstParms.remove(lstParms.getSelectionIndex());

                        if(index > 0){
                            index--;
                        }

                        lstParms.select(index);
                    }
                }
            }
        });
        button_2.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        button_2.setText("删除");
    }

    public boolean makeInputData(){
        boolean result = true;

        String name = txName.getText();
        String command = txCommand.getText();
        boolean confirm = cbConfirm.getSelectionIndex() != 0;

        String[] parms = lstParms.getItems();

        if(name.trim().length() == 0 || command.trim().length() == 0){
            result = false;
            errorMessage = "名称或命令不能为空";
        }

        if(result){
            commandData = new CommandData();

            commandData.setName(name);
            commandData.setCommand(command);
            commandData.setNeedConfirm(confirm);

            for(int i = 0; i < parms.length; i++){
                commandData.addParm(parms[i]);
            }
        }

        return result;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

    public CommandData getCommandList(){
        return commandData;
    }
}
