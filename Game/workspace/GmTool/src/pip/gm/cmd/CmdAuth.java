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

import pip.gm.MainApp;
import pip.gm.fw.AbstractClient;
import pip.gm.fw.Auth;
import pip.gm.fw.GmChatTrace;
import pip.gm.fw.GmFunction;
import pip.gm.fw.IMessage;
import pip.gm.fw.PDProcessor;
import pip.io.uwap.PDataFactory;
import pip.util.Res;
import pip.util.ui.LayoutUtil;

public class CmdAuth extends GmFunction {
	/** 记录GM日志用的服务器ID */
	private String server = null;

    static AbstractClient world;
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
                	return true;
                }
            }
        }
        return false;
    }
    public String getCommand(Auth auth) {
        return CmdAuthRES.authCmd;
    }
    public String getName(Auth auth) {
        return CmdAuthRES.authName;
    }
    public String getDescription(Auth auth) {
    	return Res.format(CmdAuthRES.authDesc, CmdAuthRES.authCmd);
    }

}
