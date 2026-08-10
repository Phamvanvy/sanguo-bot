package pip.gm.cmd;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import pip.gm.fw.AbstractClient;
import pip.gm.fw.Auth;
import pip.gm.fw.GmChatTrace;
import pip.gm.fw.GmFunction;
import pip.gm.fw.IMessage;
import pip.gm.fw.PDProcessor;
import pip.io.uwap.PDataFactory;
import pip.util.ui.LayoutUtil;

public class CmdPassword extends GmFunction {
    AbstractClient world;
	/** 记录GM日志用的服务器ID */
	private String server = null;
	JDialog changePassDialog;
    JPasswordField oldpassFld = new JPasswordField(12);
    JPasswordField newpassFld = new JPasswordField(12);
    JPasswordField newpassFld2 = new JPasswordField(12);
    LayoutUtil lu = new LayoutUtil();

    /** 已经加入的聊天频道 */
	public void registerPackage(PDataFactory factory) {
	}
	public PDProcessor getPackageProcessor() {
		return null;
	}
    public long getAuth() {
    	return 0;
    }
    public boolean exec(String cmd, AbstractClient aworld, String []s) throws Exception {
        if (s != null && s.length >= 1) {
            if (cmd != null) {
                if (isCommand(aworld.auth, cmd)) {
                	world = aworld;
                	popChangePass((Window) aworld.getUiContainer());
                	return true;
                }
            }
        }
        return false;
    }
    private void popChangePass(Window main) {
    	if (changePassDialog == null) {
    		oldpassFld.setText("");
    		newpassFld.setText("");
    		newpassFld2.setText("");

    		changePassDialog = new JDialog(main, CmdPasswordRES.dlgTitle);
    		changePassDialog.setModal(true);
    		changePassDialog.setLayout(new BorderLayout(10, 10));
    		// 输入区域
    		JPanel p = new JPanel();
    		p.setLayout(new GridBagLayout());
    		
    		JPanel p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel(CmdPasswordRES.oldPass));
    		p.add(p1, lu.getConstrains(2, 2, 1, 1));
    		p.add(oldpassFld, lu.getConstrains(3, 2, 1, 1));
    		oldpassFld.setEchoChar('*');
    		
    		p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel(CmdPasswordRES.newPass));
    		p.add(p1, lu.getConstrains(2, 3, 1, 1));
    		p.add(newpassFld, lu.getConstrains(3, 3, 1, 1));
    		newpassFld.setEchoChar('*');
    		
    		p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel(CmdPasswordRES.newPass2));
    		p.add(p1, lu.getConstrains(2, 4, 1, 1));
    		p.add(newpassFld2, lu.getConstrains(3, 4, 1, 1));
    		newpassFld2.setEchoChar('*');
    		
    		changePassDialog.add(BorderLayout.CENTER, p);
    		
    		// 控制
    		changePassDialog.add(BorderLayout.NORTH, new JLabel(""));
    		changePassDialog.add(BorderLayout.EAST, new JLabel("    "));
    		changePassDialog.add(BorderLayout.WEST, new JLabel("   "));
    		
    		// 下部 Button 条
    		p = new JPanel();
    		p.setLayout(new FlowLayout());
    		JButton btn = new JButton(CmdPasswordRES.chgPass);
    		btn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				changePassword();
        		}
    		});
    		p.add(btn);

        	btn = new JButton(CmdPasswordRES.cancel);
    		btn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				changePassDialog.setVisible(false);
        		}
    		});
    		p.add(btn);
    		
    		changePassDialog.add(BorderLayout.SOUTH, p);
    		changePassDialog.pack();
    		changePassDialog.setLocation(main.getX() + ((main.getWidth() - changePassDialog.getWidth()) >> 1),
    				main.getY() + ((main.getHeight() - changePassDialog.getHeight()) >> 2));
    	}
    	changePassDialog.setVisible(true);
    }
    public void changePassword() {
    	String pass = newpassFld.getText();
    	String pass2 = newpassFld2.getText();
    	if (pass.equals(pass2)) {
    		String s = GmChatTrace.changePassword(world.getConfig().getStringProperty("account"), 
    				oldpassFld.getText(), pass);
    		world.onMessage(IMessage.MSG_TYPE_LOG, s, null);
    	} else {
    		world.onMessage(IMessage.MSG_TYPE_LOG, CmdPasswordRES.misMatch, null);
    	}
    	changePassDialog.setVisible(false);
    }
    public String getCommand(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return "password";
    	}
    	return null;
    }
    public String getName(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return CmdPasswordRES.cmdName;
    	}
    	return null;
    }
    public String getDescription(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		StringBuilder sb = new StringBuilder(CmdPasswordRES.cmdDesc);
    		return sb.toString();
    	}
    	return null;
    }

}
