package com.pip.rcp.itimes.admin.editors;


import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoSession;
import org.apache.mina.util.NewThreadExecutor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.rcp.itimes.admin.inputs.ServerWindowEditorInput;
import com.pip.rcp.itimes.admin.net.ClientSession;
import com.pip.rcp.itimes.admin.net.IClientSessionListener;
import com.pip.rcp.itimes.admin.net.Packet;
import com.pip.rcp.itimes.admin.net.ServerConstants;
import com.pip.rcp.itimes.admin.net.Session;
import com.pip.rcp.itimes.admin.net.SessionHandler;
import com.pip.rcp.itimes.admin.net.SessionRegistry;
import com.pip.rcp.itimes.admin.net.UWAPConnector;
import com.pip.rcp.itimes.admin.net.UWAPSegment;


public class ServerWindowEditor extends EditorPart implements IClientSessionListener, KeyListener{
    public static final String ID = "com.pip.rcp.itimes.admin.editors.ServerWindowEditor";

    private static final Logger log = Logger.getLogger(ServerWindowEditor.class);

    private CCombo cbServerCommand;
    private StyledText stServerResponse;
    private ServerWindowEditorInput input;
    private ClientSession cSession;

    public ServerWindowEditor(){
        cbServerCommand = null;
    }

    public void doSave(IProgressMonitor monitor){
    }

    public void doSaveAs(){
    }

    public void init(IEditorSite site, IEditorInput input) throws PartInitException{
        setSite(site);
        setInput(input);
        this.input = (ServerWindowEditorInput)input;
        setPartName(input.getName());
    }

    public boolean isDirty(){
        return false;
    }

    public boolean isSaveAsAllowed(){
        return false;
    }

    public void createPartControl(Composite parent){
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        stServerResponse = new StyledText(container, SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY | SWT.H_SCROLL | SWT.BORDER);
        stServerResponse.setEditable(false);
        stServerResponse.setWordWrap(true);
        final GridData gd_stServerResponse = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        stServerResponse.setLayoutData(gd_stServerResponse);

        cbServerCommand = new CCombo(container, SWT.BORDER);
        cbServerCommand.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(final SelectionEvent e){
                String cmd = cbServerCommand.getItem(cbServerCommand.getSelectionIndex());
                cbServerCommand.remove(cmd);
                fireCommand(cmd, true);
            }
        });

        final GridData gd_cbServerCommand = new GridData(SWT.FILL, SWT.CENTER, true, false);
        cbServerCommand.setLayoutData(gd_cbServerCommand);
        cbServerCommand.addKeyListener(this);

        final Button btClear = new Button(container, SWT.NONE);

        btClear.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(final SelectionEvent e){
                if(MessageDialog.openConfirm(null, "清除屏幕", "确实清除屏幕吗？")){
                    stServerResponse.setText("");
                }
            }
        });

        btClear.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        btClear.setText("清屏");

        addReceive("正在连接....\n");
        connect();
    }

    public void setFocus(){
        cbServerCommand.setFocus();
    }

    public void keyPressed(KeyEvent e){
    }

    public void keyReleased(KeyEvent e){
        String cmd = cbServerCommand.getText();

        if(cmd.length() > 0 && (e.character == '\n' || e.character == '\r')){
            fireCommand(cmd, false);
        }
    }

    public void connect(){
        SessionRegistry registry = new SessionRegistry();
        IoConnector connector = new UWAPConnector(2, new NewThreadExecutor());
        connector.connect(new InetSocketAddress(input.getServer().getIp(), Integer.parseInt(input.getServer().getPort())), new SessionHandler(registry){
            public Session createSession(IoSession session){
                cSession = new ClientSession(session);
                cSession.addListener(ServerWindowEditor.this);

                return cSession;
            }
        });
    }

    public void fireCommand(String command, boolean replace){
        log.info("client : " + command);

        saveCommand(command, replace);
        addReceive("\n[" + command + "]:\n");
        sendCommand(command);
    }

    private void sendCommand(String command){
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_COMMAND);
        seg.writeString(command);
        cSession.write(seg);
    }

    private void addReceive(final String s){
        Display.getDefault().syncExec(new Thread(){
            public void run(){
                stServerResponse.setText(stServerResponse.getText() + s);
                stServerResponse.setSelection(stServerResponse.getCharCount());
            }
        });
    }

    private void saveCommand(final String s, final boolean replayace){
        Display.getDefault().syncExec(new Thread(){
            public void run(){
                if(!replayace){
                    cbServerCommand.add(s);
                    cbServerCommand.setText("");
                }
            }
        });
    }

    public void messageReceived(Packet packet){
    }

    public void messageReceived(String s){
        log.info("server : " + s);
        addReceive(s);
    }

    public void sessionClosed(){
        log.info("连接断开");
    }

    public void sessionOpened(){
        addReceive("连接成功...\n");

        String command = "login " + input.getServer().getUser() + " " + input.getServer().getPassword();
        fireCommand(command, false);
    }

    public void dispose(){
        if(cSession != null){
            cSession.close();
        }
    }
}
