package com.pip.sanguo.performancetest.client;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewClientDataWizardPage extends WizardPage{
    private Text txNamePrefix;
    private Text txNameBegin;
    private Text txNameEnd;

    private String namePrefix;
    private int nameBegin;
    private int nameEnd;

    public NewClientDataWizardPage(){
        super("新建测试数据");
    }
    
    public void createControl(Composite parent){
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label = new Label(container, SWT.NONE);
        label.setText("帐户名前缀：");

        txNamePrefix = new Text(container, SWT.BORDER);
        final GridData gd_namePrefix = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        txNamePrefix.setLayoutData(gd_namePrefix);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("开始编号：");

        txNameBegin = new Text(container, SWT.BORDER);
        final GridData gd_nameBegin = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        txNameBegin.setLayoutData(gd_nameBegin);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("结束编号：");

        txNameEnd = new Text(container, SWT.BORDER);
        final GridData gd_nameEnd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        txNameEnd.setLayoutData(gd_nameEnd);
    }

    public boolean makeInputData(){
        boolean result = false;

        try{
            namePrefix = txNamePrefix.getText();
            nameBegin = Integer.parseInt(txNameBegin.getText());
            nameEnd = Integer.parseInt(txNameEnd.getText());

            if(nameBegin >= 0 && nameEnd >= 0 && nameEnd >= nameBegin){
                result = true;
            }else{
                result = false;
            }
        }catch(Exception e){
            e.printStackTrace();

            result = false;
        }

        return result;
    }

    public String getNamePrefix(){
        return namePrefix;
    }

    public int getNameBegin(){
        return nameBegin;
    }

    public int getNameEnd(){
        return nameEnd;
    }
}
