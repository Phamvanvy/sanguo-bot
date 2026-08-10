package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.rcp.itimes.admin.data.ServerData;


public class ModifyServerListWizardPage extends WizardPage{
    private Text tPassword;
    private Text tUser;
    private Text tDesc;
    private Text tPort;
    private Text tIp4;
    private Text tIp3;
    private Text tIp2;
    private Text tIp1;
    private Composite parent;

    private String errorMessage;
    private ServerData serverData;

    public ModifyServerListWizardPage(){
        super("修改服务器");
        setTitle("请修改服务器信息");
        setDescription("");
    }

    public void createControl(Composite p){
        parent = p;
        Composite container = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 8;
        container.setLayout(gridLayout);
        setControl(container);

        final Label label = new Label(container, SWT.NONE);
        label.setText("服务器地址：");

        tIp1 = new Text(container, SWT.BORDER);
        tIp1.setTextLimit(3);
        final GridData gd_tIp1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        tIp1.setLayoutData(gd_tIp1);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText(".");

        tIp2 = new Text(container, SWT.BORDER);
        tIp2.setTextLimit(3);
        final GridData gd_tIp2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        tIp2.setLayoutData(gd_tIp2);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText(".");

        tIp3 = new Text(container, SWT.BORDER);
        tIp3.setTextLimit(3);
        final GridData gd_tIp3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        tIp3.setLayoutData(gd_tIp3);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText(".");

        tIp4 = new Text(container, SWT.BORDER);
        tIp4.setTextLimit(3);
        final GridData gd_tIp4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        tIp4.setLayoutData(gd_tIp4);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("服务器端口：");

        tPort = new Text(container, SWT.BORDER);
        tPort.setTextLimit(5);
        final GridData gd_tPort = new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1);
        tPort.setLayoutData(gd_tPort);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("服务器说明：");

        tDesc = new Text(container, SWT.BORDER);
        final GridData gd_tDesc = new GridData(SWT.FILL, SWT.CENTER, false, false, 7, 1);
        tDesc.setLayoutData(gd_tDesc);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("登陆用户名：");

        tUser = new Text(container, SWT.BORDER);
        final GridData gd_tUser = new GridData(SWT.FILL, SWT.CENTER, false, false, 7, 1);
        gd_tUser.widthHint = 156;
        tUser.setLayoutData(gd_tUser);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("登陆密码：");

        tPassword = new Text(container, SWT.BORDER);
        final GridData gd_tPassword = new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1);
        tPassword.setLayoutData(gd_tPassword);

        updateData();
    }

    private void updateData(){
        if(serverData == null){
            return;
        }

        String[] ips = serverData.getIp().split("\\.");

        tIp1.setText(ips[0]);
        tIp2.setText(ips[1]);
        tIp3.setText(ips[2]);
        tIp4.setText(ips[3]);

        tPort.setText(serverData.getPort());
        tDesc.setText(serverData.getDesc());
        tUser.setText(serverData.getUser());
        tPassword.setText(serverData.getPassword());
    }

    public boolean makeInputData(){
        boolean result = true;

        int ip_1, ip_2, ip_3, ip_4;
        int port;
        String desc;
        String user;
        String password;

        try{
            if(tIp1.getText().trim().length() == 0 || tIp2.getText().trim().length() == 0 || tIp3.getText().trim().length() == 0 || tIp4.getText().trim().length() == 0
                            || tPort.getText().trim().length() == 0){
                throw new Exception();
            }

            ip_1 = Integer.parseInt(tIp1.getText());
            ip_2 = Integer.parseInt(tIp2.getText());
            ip_3 = Integer.parseInt(tIp3.getText());
            ip_4 = Integer.parseInt(tIp4.getText());

            port = Integer.parseInt(tPort.getText());

            serverData.setIp("" + ip_1 + "." + ip_2 + "." + ip_3 + "." + ip_4);
            serverData.setPort("" + port);
        }catch(Exception e){
            result = false;
            errorMessage = "服务器地址或端口输入有误！";

            e.printStackTrace();
        }

        if(result){
            desc = tDesc.getText();
            user = tUser.getText();
            password = tPassword.getText();

            if(desc.trim().length() == 0 || user.trim().length() == 0 || password.trim().length() == 0){
                result = false;
                errorMessage = "服务器信息或登陆信息输入不完整";
            }else{
                serverData.setDesc(desc);
                serverData.setUser(user);
                serverData.setPassword(password);
            }
        }

        return result;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

    public ServerData getServerData(){
        return serverData;
    }

    public void setServerData(ServerData serverData){
        this.serverData = serverData;
    }
}
