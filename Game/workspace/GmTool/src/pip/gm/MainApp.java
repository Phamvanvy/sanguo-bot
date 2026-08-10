package pip.gm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pip.gm.fw.BaseConfig;
import pip.gm.fw.GmChatTrace;
import pip.util.ui.CommonMetalTheme;
import pip.util.ui.LayoutUtil;
/**
 * 程序启动入口
 */
public class MainApp {
	JTextField nameFld;
	JPasswordField passFld;
	JTextField serverFld;
	JFrame loginDialog;
	JCheckBox cbSavePass;
	JTextField domainFld;
	
	private String lastUser = "";
	private String lastPass = "";
	private String lastDomain = "pip";
	private String lastServer = "211.151.99.70:8219";
	private boolean lastSavePass = false;
	
    public MainApp() {
    	loadSetting();
    	
    	// 首先login
    	loginDialog = new JFrame("GM Login");
    	loginDialog.getContentPane().setLayout(new BorderLayout(10, 10));
    	
    	LayoutUtil lu = new LayoutUtil();
    	
    	// 输入区域
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	
    	// 输入账号
    	p.add(new JLabel("Account:"), lu.getConstrains(0, 0, 1, 1, 0, 0));
    	nameFld = new JTextField(lastUser);
    	p.add(nameFld, lu.getConstrains(1, 0, 1, 1));
    	
    	// 输入密码
    	p.add(new JLabel("Password:"), lu.getConstrains(0, 1, 1, 1, 0, 0));
    	passFld = new JPasswordField(lastPass);
    	p.add(passFld, lu.getConstrains(1, 1, 1, 1));

    	// 输入域
    	p.add(new JLabel("Domain:"), lu.getConstrains(0, 2, 1, 1, 0, 0));
    	domainFld = new JTextField(lastDomain);
    	p.add(domainFld, lu.getConstrains(1, 2, 1, 1));
    	
    	// 输入服务器地址
    	p.add(new JLabel("Server:"), lu.getConstrains(0, 3, 1, 1, 0, 0));
    	serverFld = new JTextField(lastServer);
    	p.add(serverFld, lu.getConstrains(1, 3, 1, 1));
    	
    	// 保存密码
    	cbSavePass = new JCheckBox("Save Password");
    	cbSavePass.setSelected(lastSavePass);
    	p.add(cbSavePass, lu.getConstrains(0, 4, 2, 1));
    	
    	// Button
    	JPanel btnp = new JPanel(new FlowLayout(FlowLayout.CENTER));
    	JButton btn = new JButton("Login");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GmChatTrace.loginedUser = nameFld.getText();
				GmChatTrace.loginedUserPass = new String(passFld.getPassword());
				BaseConfig.DOMAIN = domainFld.getText(); 
				GmChatTrace.gmServerURL = serverFld.getText();
				GmChatTrace.loginedUserAuth = GmChatTrace.login();
				if (GmChatTrace.loginedUserAuth == 0) {
					JOptionPane.showMessageDialog(loginDialog, "Login failed.");
				} else {
					loginDialog.setVisible(false);
					lastUser = GmChatTrace.loginedUser;
					lastPass = GmChatTrace.loginedUserPass;
					lastDomain = BaseConfig.DOMAIN;
					lastServer = GmChatTrace.gmServerURL;
					lastSavePass = cbSavePass.isSelected();
					saveSetting();
					
					MainFrame frame = new MainFrame();
			    	frame.validate();
			    	// Center the window
			        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			        Dimension frameSize = frame.getSize();
			        if (frameSize.height > screenSize.height) {
			            frameSize.height = screenSize.height;
			        }
			        if (frameSize.width > screenSize.width) {
			            frameSize.width = screenSize.width;
			        }
			        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
			        frame.setVisible(true);
				}
    		}
		});
    	btnp.add(btn);
    	p.add(btnp, lu.getConstrains(0, 5, 2, 1));
    	loginDialog.getContentPane().add(p, BorderLayout.CENTER);
    	loginDialog.setSize(240, 200);
    	loginDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	centerWindow(loginDialog);
		
    	loginDialog.setVisible(true);
    }
    
    /**
     * To place a component at the center of the screen.
     */
    public static void centerWindow(Window w) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension d = toolkit.getScreenSize();
        Dimension size = w.getSize();
        int x = (d.width - size.width) / 2;
        int y = (d.height - size.height) / 2;
        w.setLocation(x, y);
    }

    /** 程序的主入口。命令行可带一个参数为home路径。如果不指定，home为启动应用的帐号home路径。 */
    public static void main(String[] args) {
    	// 程序参数为缺省的配置文件路径
    	if (args.length == 1) {
    		File f = new File(args[0]);
    		if (f.exists() && f.isDirectory()) {
    			System.setProperty("user.home", f.getAbsolutePath());
    		}
    	}
        javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new CommonMetalTheme());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                new MainApp();
            }
        });
    }
    
    
	/**
	 * 从HOME目录中加载基本设置，包括上次登录的信息。
	 */
	public void loadSetting() {
		String home = System.getProperty("user.home");
		FileInputStream fis = null;
		try {
			Properties props = new Properties();
			fis = new FileInputStream(new File(home, "gmtool.properties"));
			props.load(fis);
			lastUser = props.getProperty("user", "");
			lastPass = props.getProperty("password", "");
			lastDomain = props.getProperty("domain", "pip");
			lastServer = props.getProperty("server", "211.151.99.70:8219");
			lastSavePass = "true".equals(props.getProperty("save_password", "false"));
		} catch (IOException e) {
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * 保存设置。
	 */
	public void saveSetting() {
		String home = System.getProperty("user.home");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(home, "gmtool.properties"));
			Properties props = new Properties();
			props.setProperty("user", lastUser);
			if (lastSavePass) {
				props.setProperty("password", lastPass);
			} else{
				props.setProperty("password", "");
			}
			props.setProperty("domain", lastDomain);
			props.setProperty("server", lastServer);
			props.setProperty("save_password", lastSavePass ? "true" : "false");
			props.store(fos, "GmTool Configuration");
		} catch (IOException e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
