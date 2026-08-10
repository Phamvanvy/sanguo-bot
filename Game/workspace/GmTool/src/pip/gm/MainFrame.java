package pip.gm;

import pip.util.ui.BroadCastDialog;
import pip.util.ui.LayoutUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import pip.gm.fw.AbstractClient;
import pip.gm.fw.AbstractGmForm;
import pip.gm.fw.BaseConfig;
import pip.gm.fw.DoubleClickZoomerAdapter;
import pip.util.Res;
import pip.util.UiUtil;
import pip.util.ui.TodoTable;
import cwu.util.sort.CompareAgent;
import cwu.util.sort.SortAgent;
/**
 * GM工具的主题Form.包含节点列表,TODOs,以及主体操作平台.
 * @todo 增加动态窗口以及复制窗口等功能
 * 
 * 修改记录：
 * 1. 为了弹出的子界面可以获取到服务器列表，将私有变量configs修改为public，frank于2011/2/22修改。
 */
public class MainFrame extends JFrame {
	/**各服务器的配置文件,按游戏标识分组 */
    public ServerConfig configs = new ServerConfig("root");
    /** 根面板,可以将小窗口最大化的位置 */
    JPanel contentPane; 
    /** 服务器列表所在的面板 */
    JPanel listPanel; 
    /** 主面板 */
	JPanel mainPanel = new JPanel(); 
	/** 左服务器列表,右主窗口 */
    JSplitPane jsp1; 

    /** 普通对话框,用来显示通用信息 */
    private JDialog generalDialog;
    /** 进度条对话框 */
    private JDialog scrollBarDialog;
    /** 进度条 */
    private JProgressBar jscrollBar;
    private JLabel scrollMsg;

    JPopupMenu serverFunctionPop = new JPopupMenu();
    /** 待办事宜列表 */
    public TodoTable todoTbl = new TodoTable();
    /** 服务器列表。 */
    JTree serverListTree;

    BroadCastDialog broadCastDialog;
    public MainFrame() {
        try {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            layoutComponents();
            new InitialThread().start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    /** 显示一个进度条.当进度条显示完成时需要关闭 */
    public void showScroll(String title, String msg, int value, int max) {
    	if (scrollBarDialog == null) {
    		jscrollBar = new JProgressBar(JProgressBar.HORIZONTAL);
    		scrollBarDialog = new JDialog(this, true);
    		
    		scrollBarDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    		scrollBarDialog.setLayout(new BorderLayout());
    		scrollBarDialog.add(BorderLayout.SOUTH, jscrollBar);
    		scrollMsg = new JLabel(msg);
        	jscrollBar.setMinimum(0);
    		scrollBarDialog.add(BorderLayout.CENTER, scrollMsg);
        	scrollBarDialog.pack();
        	scrollBarDialog.setSize(scrollBarDialog.getWidth() << 1, scrollBarDialog.getHeight());
        	scrollBarDialog.setLocation(this.getX() + ((this.getWidth() - scrollBarDialog.getWidth()) >> 1),
    				this.getY() + ((this.getHeight() - scrollBarDialog.getHeight()) >> 2));
    	} else {
    		scrollMsg.setText(msg);
    	}
    	scrollBarDialog.setTitle(title);
    	jscrollBar.setMaximum(max);
    	jscrollBar.setValue(value);
    	
    	if (!scrollBarDialog.isVisible()) {
    		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					scrollBarDialog.setVisible(true);
				}
			});
    		// 避免由于界面还没显示出来CPU已经完成任务关闭进度条,而造成一个模式对话框不能关闭
    		while (!scrollBarDialog.isVisible()) {
    			try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
    /** 隐藏进度条 */
    public void hideScroll() {
    	scrollBarDialog.setVisible(false);
    }
    JPanel jp;
    /** 显示提示消息 */
    public void showMessages(String title, String ss) {
    	showMessages(title, ss.split("\n"));
    }
    public void showMessages(String title, String []ss) {
    	if (generalDialog == null) {
    		generalDialog = new JDialog(this, title, true);
    		generalDialog.setLayout(new BorderLayout());
    		generalDialog.add(BorderLayout.NORTH, new JLabel(" "));
    		generalDialog.add(BorderLayout.SOUTH, new JLabel(" "));
    		generalDialog.add(BorderLayout.EAST, new JLabel(" "));
    		generalDialog.add(BorderLayout.WEST, new JLabel(" "));
    		jp = new JPanel();
    		jp.setLayout(new GridLayout(ss.length,1));
    		generalDialog.add(BorderLayout.CENTER, jp);
    	} else {
    		if (generalDialog.isShowing()) {
    			for (String s: ss) {
    				jp.add(new JLabel(s));
    			}
    			generalDialog.pack();
    			return;
    		}
    		if (jp != null) {
    			jp.removeAll();
    		}
    	}
		for (String s: ss) {
			jp.add(new JLabel(s));
		}
		generalDialog.pack();
		generalDialog.setLocation(this.getX() + ((this.getWidth() - generalDialog.getWidth()) >> 1),
				this.getY() + ((this.getHeight() - generalDialog.getHeight()) >> 2));
		generalDialog.setVisible(true);
    }
    
    private void layoutComponents() throws Exception {
    	setSize(new Dimension(1000, 618));
        setTitle(getTitleString());
        
        contentPane = (JPanel) getContentPane();
        jsp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getServerListPane(), getOpPane());
        jsp1.setDividerSize(5);
        jsp1.setDividerLocation(200);
        contentPane.add(BorderLayout.CENTER, jsp1);
    }
    private Component getCommandPane() {
    	JPanel p = new JPanel(new FlowLayout());
    	JButton btb = new JButton(MainAppRES.btnBroadcast);
    	btb.setToolTipText(MainAppRES.btnBroadcastTips);
    	btb.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
    		if (broadCastDialog == null) {
    			broadCastDialog = new BroadCastDialog(MainFrame.this, configs);
    		} else {
    			broadCastDialog.updateServerList();
    		}
    		broadCastDialog.setVisible(true);
    		
    	};});
    	p.add(btb);
    	return p;
    }
    private Component getServerListPane() {
    	if (listPanel == null) {
    		listPanel = new JPanel();
    		listPanel.setLayout(new BorderLayout());
    		// TODO CWU 通用广播功能 只有掌上明珠自己才同时运营多个产品
    		if (BaseConfig.DOMAIN.equals(BaseConfig.DOMAIN_PIP)) {
    			listPanel.add(BorderLayout.SOUTH, getCommandPane());
    		}
    	}
    	JScrollPane scrollPane = new JScrollPane(todoTbl);
    	todoTbl.setFillsViewportHeight(true);
    	JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, scrollPane);
    	jsp.setDividerLocation(300);
    	return jsp;
    }
	private Component getOpPane() {
    	BorderLayout layout = new BorderLayout();
    	mainPanel.setLayout(layout);
    	return mainPanel;
    }
	/** 初始化配置文件线程 */
    class InitialThread extends Thread {
		public void run() {
			loadConfig();
		}
		public void loadConfig() {
			try {
				System.out.println(MainAppRES.LoadConfigFile);
				showScroll(MainAppRES.LoadConfigFile, MainAppRES.CollectConfigFile, 0, 100);
			    File baseDir;
			    baseDir = new File(BaseConfig.configFileDirName);
			    if (!baseDir.exists() || !baseDir.isDirectory()) {
			    	baseDir = new File(System.getProperty("user.home"), BaseConfig.configFileDirName);
			    }
			    System.out.println(Res.format(MainAppRES.ConfigDirIs, baseDir.getAbsolutePath()));
			    File files[] = baseDir.listFiles();
			    ArrayList<File> configFileNames = new ArrayList<File>();
			    int n = files.length;
			    for (int i = n; i-- > 0;) {
			    	showScroll(MainAppRES.LoadConfigFile, MainAppRES.CollectConfigFile, (i * 10 / n), 100);
			        String nm = files[i].getName();
			        if (nm.startsWith("Gm") && nm.endsWith(".xml")) {
			        	configFileNames.add(files[i]);
			        }
			    }
			    n =  configFileNames.size();
			    for (int i = 0; i < n; i++) {
			    	File f = configFileNames.get(i);
			    	String nm = f.getName();
			    	showScroll(MainAppRES.LoadConfigFile, Res.format(MainAppRES.file, nm), 10 + (i * 90 / n), 100);	    
			    	ServerConfig cfg = new ServerConfig(nm.substring(2, nm.length() - 4), f.getAbsolutePath());
			    	String ss = cfg.cfg.gameGroup;
			    	ServerConfig pconfig = configs;
			    	String sss[] = ss.split("/");
			    	for (int j = 0; j < sss.length; j++) {
			    		if (sss[j].length() > 0) { 
				    		ServerConfig scv = null;
					    	for (ServerConfig v : pconfig.getSubConfigs()) {
					    		if (v.name.equals(sss[j])) {
					    			scv = v;
					    			break;
					    		}
					    	}
					    	if (scv == null) {
					    		scv = new ServerConfig(sss[j]);
					    		pconfig.addConfig(scv);
					    	}
					    	pconfig = scv;
			    		}
			    	}
			    	pconfig.addConfig(cfg);
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		    scrollBarDialog.setVisible(false);
		    SwingUtilities.invokeLater(new UpdateServerListTree());
		}
	}
   
	/** 更新配置文件线程.配置文件数据加载后调用,激活显示 */
    class UpdateServerListTree implements Runnable {
	    CompareAgent agent = new CompareAgent() {
	    	 public int compare(Object a, Object b) {
	    		 if (a == null|| b == null) {
	    			 return 0;
	    		 }
	    		 return ((ServerConfig)a).name.compareTo(((ServerConfig)b).name);
	    	 }
	    };
    	public void addNode(DefaultMutableTreeNode nd, ServerConfig cfg) {
    		Vector<ServerConfig> cfgs = cfg.getSubConfigs();
    		if (cfgs == null) {
    			DefaultMutableTreeNode child = new DefaultMutableTreeNode(cfg);
    			nd.add(child);
    		} else {
    			DefaultMutableTreeNode child = new DefaultMutableTreeNode(cfg.name);
    			nd.add(child);
    			ServerConfig d[] = new ServerConfig[cfgs.size()];
		    	cfgs.toArray(d);
		    	d = (ServerConfig [])SortAgent.sort(d, agent, 0);
				for (ServerConfig sc : d) {
					addNode(child, sc);
				}
    		}
    		
    	}
		public void run() {
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(MainAppRES.GameServerList);
			for (ServerConfig c : configs.getSubConfigs()) {
				addNode(root, c);
			}
			serverListTree = new JTree(root);
			JScrollPane jsp = new JScrollPane(serverListTree);
			listPanel.add(BorderLayout.CENTER, jsp);
			MainFrame.this.validate(); 
			
			// 接收弹出菜单功能
			serverListTree.addMouseListener(new ServerSelectAction());
			// 显示状态图标
			serverListTree.setCellRenderer(new MyTreeRenderer());
		}
	}
    private long maxMemorySize = Runtime.getRuntime().maxMemory();
    private String getTitleString() {
    	long mem = Runtime.getRuntime().freeMemory();
    	long k =mem;
    	k *= 10000L;
    	k /=maxMemorySize;
    	String ss = String.valueOf(k);
    	int n = ss.length();
    	if (n == 1) {
    		ss = "0.0" + ss + "%";
    	} else if (n == 2) {
    		ss = "0." + ss + "%";
    	} else {
    		ss = ss.substring(0, n - 2) + "." + ss.substring(n-2) + "%";
    	}
    	return Res.format(MainAppRES.GmTool_ver_idle, BaseConfig.APP_VERSION, ss, (mem/1024));
    }
    public void updateServeListState() { 
    	System.gc();
    	this.setTitle(getTitleString());
    	serverListTree.repaint();
    }
    /** 为不同状态的服务器显示不同图标 */
    class MyTreeRenderer extends DefaultTreeCellRenderer {
    	public   Component   getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
    			boolean expanded, boolean leaf, int row, boolean hasFocus)   {  
    		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);  
    		DefaultMutableTreeNode   node=(DefaultMutableTreeNode)value;  
    		Object   someThing=node.getUserObject();  
    		if(someThing   instanceof   ServerConfig){  
    			setIcon(((ServerConfig)someThing).getIcon());
    		}  
    		return   this;  
    	}  
    }


	private Object intialLock = new Object(); // 避免冲突

	/** 断开一个已经关闭的服务器的连接。本过程不负责关闭连接等具体事物。 */
    public void unregistServer(AbstractGmForm frm) {
    	// 如果当前连接正在被显示,隐藏它
    	if (mainPanel.getComponentCount() > 0 && mainPanel.getComponent(0) == frm) {
    		mainPanel.remove(frm);
    		repaint();
		}
    	unregisterConfig(configs, frm);
    }
    private boolean unregisterConfig(ServerConfig cfg, AbstractGmForm frm) {
    	Vector<ServerConfig> cfgs = cfg.getSubConfigs();
    	if (cfgs == null) {
    		if (cfg.ins == frm) {
				cfg.ins = null;
				return true;
			}
    	} else {
    		for (ServerConfig c: cfgs) {
    			if (unregisterConfig(c, frm)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
	class ServerSelectAction implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				TreePath paths[] = serverListTree.getSelectionPaths();
				if (paths != null && paths.length > 1) {
					TreePath t = paths[0];
					Object obj = t.getLastPathComponent();
					DefaultMutableTreeNode   node=(DefaultMutableTreeNode)obj;
					Object   someThing=node.getUserObject();  
					if(someThing   instanceof   ServerConfig){  
						ServerConfig sc = (ServerConfig)someThing;
						if (sc.ins != null) {
							serverFunctionPop.removeAll();
							JMenuItem mi = new JMenuItem(MainAppRES.DisconectServer);
							mi.setActionCommand("DisconnectServer");
	                		mi.setToolTipText(MainAppRES.DisconectServerTip);
	                		mi.addActionListener(multiServerActionListener);
	                		serverFunctionPop.add(mi);
	                		
							ArrayList<String[]> funcs = sc.ins.getActions();
							for (int i = 1; i < paths.length; i++) {
								t = paths[i];
								obj = t.getLastPathComponent();
								node=(DefaultMutableTreeNode)obj;
								someThing=node.getUserObject();  
								if(someThing   instanceof   ServerConfig){  
									sc = (ServerConfig)someThing;
									if (sc.ins != null)  {
										ArrayList<String[]> subFuncs = sc.ins.getActions();
										for (int j = funcs.size(); j-- > 0;) {
											String name = funcs.get(j)[0];
											boolean found = false;
											for (String [] ss : subFuncs) {
												if (ss[0].equals(name)) {
													found = true;
													break;
												}
											}
											if (!found) {
												funcs.remove(j);
											}
										}
									} else {
										// TODO show error
										return;
									}
								}
							}
							for (String []ss : funcs) {
								mi = new JMenuItem(ss[0]);
								mi.setActionCommand(ss[1]);
		                		mi.setToolTipText(ss[2]);
		                		mi.addActionListener(multiServerActionListener);
		                		serverFunctionPop.add(mi);
							}
							//// 同意执行功能
							if (mainPanel.getComponentCount() == 1) {
								AbstractGmForm ins = (AbstractGmForm)mainPanel.getComponent(0);
								String s = ins.getCurrentInputBuf();
								if (s != null && s.length() > 1 && s.startsWith("/")) {
									mi = new JMenuItem(MainAppRES.ExecName); 
									mi.setActionCommand(s.substring(1));
			                		mi.setToolTipText(MainAppRES.ExecTip);
			                		mi.addActionListener(multiServerActionListener);
			                		serverFunctionPop.add(mi);
								}
							}
						} else {
							serverFunctionPop.removeAll();
							JMenuItem mi = new JMenuItem(MainAppRES.ConnectServer);
							mi.setActionCommand("ConnectServer");
	                		mi.setToolTipText(MainAppRES.ConnectServerTip);
	                		mi.addActionListener(multiServerActionListener);
	                		serverFunctionPop.add(mi);
	                		
	                		mi = new JMenuItem(MainAppRES.LoginServer);
							mi.setActionCommand("LoginServer");
	                		mi.setToolTipText(MainAppRES.LoginServerTip);
	                		mi.addActionListener(multiServerActionListener);
	                		serverFunctionPop.add(mi);
	                		
							for (int i = 1; i < paths.length; i++) {
								t = paths[i];
								obj = t.getLastPathComponent();
								node=(DefaultMutableTreeNode)obj;
								someThing=node.getUserObject();  
								if(someThing   instanceof   ServerConfig){  
									sc = (ServerConfig)someThing;
									if (sc.ins != null)  {
										return;
									}
								}
							}
						}
						serverFunctionPop.show(serverListTree, e.getX(), e.getY());
		            }
				} else {
		            TreePath t = serverListTree.getClosestPathForLocation(e.getX(), e.getY());
		            serverListTree.setSelectionPaths(new TreePath[]{t});
		            if (t != null) {
		            	Object obj = t.getLastPathComponent();
		            	DefaultMutableTreeNode   node=(DefaultMutableTreeNode)obj;
		            	Object   someThing=node.getUserObject();  
		                if(someThing   instanceof   ServerConfig){  
		                	ServerConfig sc = (ServerConfig)someThing;
	                		serverFunctionPop.removeAll();
		                	if (sc.ins == null) {
		                		JMenuItem mi = new JMenuItem(Res.format(MainAppRES.Connect_server, sc.name));
		                		mi.addActionListener(new OpenServerActionListener(sc));
		                		serverFunctionPop.add(mi);
		                	} else {
		                		JMenuItem mi;
		                		if (mainPanel.getComponentCount() != 1 || mainPanel.getComponent(0) != sc.ins) {
			                		mi = new JMenuItem(Res.format(MainAppRES.Show_server, sc.name));
			                		mi.setToolTipText(MainAppRES.showServerTip);
			                		mi.addActionListener(new OpenServerActionListener(sc));
			                		serverFunctionPop.add(mi);
		                		}
		                		mi = new JMenuItem(Res.format(MainAppRES.Disconnect_server, sc.name));
		                		mi.setToolTipText(MainAppRES.DisconectServerTip);
		                		mi.addActionListener(new CloseServerActionListener(sc));
		                		serverFunctionPop.add(mi);
		                		serverFunctionPop.addSeparator();
								for (String []ss :  sc.ins.getActions()) {
									mi = new JMenuItem(ss[0]);
									mi.setActionCommand(ss[1]);
			                		mi.setToolTipText(ss[2]);
			                		mi.addActionListener(multiServerActionListener);
			                		serverFunctionPop.add(mi);
								}
		                	}
		                	serverFunctionPop.show(serverListTree, e.getX(), e.getY());
		                }
		            }
				}
			} else if (e.getClickCount() == 2) {
				JTree tree = (JTree)e.getSource();
				Object obj = tree.getLastSelectedPathComponent();
				if (obj instanceof DefaultMutableTreeNode) {
					obj = ((DefaultMutableTreeNode)obj).getUserObject();
					if (obj instanceof ServerConfig) {
						SwingUtilities.invokeLater(new ServerStartThread((ServerConfig)obj));
					}
				}
			}
		}
	    public void mousePressed(MouseEvent e) {}
	    public void mouseReleased(MouseEvent e) {}
	    public void mouseEntered(MouseEvent e) {}
	    public void mouseExited(MouseEvent e)  {}
	}
    
	class OpenServerActionListener implements ActionListener {
		ServerConfig cfg;
		public OpenServerActionListener(ServerConfig serverConfig) {
			cfg = serverConfig;
		}
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new ServerStartThread(cfg));
		}
	}
	JDialog loginDialog;
	JTextField nameFld = new JTextField(12);
	JPasswordField passFld = new JPasswordField(12);
	
	private ActionListener multiServerActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			if (obj instanceof JMenuItem) {
				String cmd = ((JMenuItem)obj).getActionCommand();
				boolean breakConn = "DisconnectServer".equals(cmd);
				boolean connConn = "ConnectServer".equals(cmd);
				boolean loginServer = "LoginServer".equals(cmd);
				if (loginServer) {
					popLoginDialog();
				} else {
					for (TreePath t : serverListTree.getSelectionPaths()) {
						obj = t.getLastPathComponent();
						DefaultMutableTreeNode   node=(DefaultMutableTreeNode)obj;
						Object   someThing=node.getUserObject();  
						if(someThing   instanceof   ServerConfig){  
							ServerConfig sc = (ServerConfig)someThing;
							if (sc.ins != null) {
								if (breakConn) {
									AbstractGmForm ins = sc.ins;
									sc.ins = null;
									if (mainPanel.getComponentCount() == 1 && mainPanel.getComponent(0) == ins) {
										mainPanel.remove(ins);
										mainPanel.repaint();
									}
									ins.close();
									pip.gm.fw.AbstractClient client = ins.getUwapApp();
									if (client != null) {
										try {
											client.quit();
										} catch (Exception ex) {
										}
									}
								} else {
									AbstractClient engine = sc.ins.getUwapApp();
									if (engine != null) {
										engine.con.processCommand(cmd);
									}
								}
							} else if (connConn) {
								// 后台连接服务器请求
								Class kls = sc.getGmFormClass();
				            	if (kls != null) {
									try {
										AbstractGmForm frm = (AbstractGmForm)kls.newInstance();
				                		frm.init(MainFrame.this, sc.configFile);
				                		sc.ins = frm;
				                		frm.getUwapApp().con.processCommand("gmlogin");
									} catch (Exception e1) {
										e1.printStackTrace();
				                		showMessages(MainAppRES.initException, Res.format(MainAppRES.init_server_ErrMsg, sc.name, e1.getMessage()));
									}
				            	} else {
			                		showMessages(MainAppRES.err, Res.format(MainAppRES.model_server_NotFound, sc.name,sc.gmFormClass));
				            		return;
				            	}
							}
						}
					}
					if (breakConn) {
						serverListTree.repaint();
					}
				}
			}
		}
	};
	LayoutUtil lu = new LayoutUtil();
	private void loginFromUi() {
		loginDialog.setVisible(false);
		String name = nameFld.getText();
		String pass = passFld.getText();
		for (TreePath t : serverListTree.getSelectionPaths()) {
			Object obj = t.getLastPathComponent();
			DefaultMutableTreeNode   node=(DefaultMutableTreeNode)obj;
			Object   someThing=node.getUserObject();  
			if (someThing  instanceof  ServerConfig){  
				ServerConfig sc = (ServerConfig)someThing;
				// 后台连接服务器请求
				Class kls = sc.getGmFormClass();
            	if (kls != null) {
					try {
						AbstractGmForm frm = (AbstractGmForm)kls.newInstance();
                		frm.init(MainFrame.this, sc.configFile);
                		sc.ins = frm;
                		frm.getUwapApp().con.processCommand("gmlogin '" + name + "' '" + pass + "'");
					} catch (Exception e1) {
						e1.printStackTrace();
                		showMessages(MainAppRES.initException, Res.format(MainAppRES.init_server_ErrMsg, sc.name, e1.getMessage()));
					}
            	} else {
            		showMessages(MainAppRES.err, Res.format(MainAppRES.model_server_NotFound, sc.name,sc.gmFormClass));
            		return;
            	}
			}
		}
	}
	private  java.awt.event.KeyListener loginKeyListener = new java.awt.event.KeyListener() {
    	public void keyTyped(KeyEvent e) {
    	}
        public void keyPressed(KeyEvent e) {
    		if (e.getKeyCode() ==  java.awt.event.KeyEvent.VK_ENTER) {
    			Component com = e.getComponent();
    			if (com instanceof JPasswordField) {
    				loginFromUi();
    			} else {
    				com.transferFocus();
    			}
    		}
        }
        public void keyReleased(KeyEvent e) {
        }
    };
    private void popLoginDialog() {
    	if (loginDialog == null) {
    		loginDialog = new JDialog(this, "请输入密码", true);
    		loginDialog.setLayout(new BorderLayout(10, 10));
    		// 输入区域
    		JPanel p = new JPanel();
    		p.setLayout(new GridBagLayout());
    		
    		JPanel p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel("GM账号:"));
    		p.add(p1, lu.getConstrains(2, 2, 1, 1));
    		p.add(nameFld, lu.getConstrains(3, 2, 1, 1));
    		
    		p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel("GM密码:"));
    		p.add(p1, lu.getConstrains(2, 3, 1, 1));
    		p.add(passFld, lu.getConstrains(3, 3, 1, 1));
    		passFld.setEchoChar('*');
    		passFld.addKeyListener(loginKeyListener);
    		nameFld.addKeyListener(loginKeyListener);
    		loginDialog.add(BorderLayout.CENTER, p);
    		
    		// 控制
    		loginDialog.add(BorderLayout.NORTH, new JLabel(""));
    		loginDialog.add(BorderLayout.EAST, new JLabel("    "));
    		loginDialog.add(BorderLayout.WEST, new JLabel("   "));
    		
    		// 下部 Button 条
    		p = new JPanel();
    		p.setLayout(new FlowLayout());
    		JButton btn = new JButton("登陆");
    		btn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				loginFromUi();
        		}
    		});
    		p.add(btn);

        	btn = new JButton("取消");
    		btn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				loginDialog.setVisible(false);
        		}
    		});
    		p.add(btn);
    		
    		loginDialog.add(BorderLayout.SOUTH, p);
    		loginDialog.pack();
    		loginDialog.setLocation(this.getX() + ((this.getWidth() - loginDialog.getWidth()) >> 1),
    				this.getY() + ((this.getHeight() - loginDialog.getHeight()) >> 2));
    	}
    	loginDialog.setVisible(true);
    }
	class CloseServerActionListener implements ActionListener {
		ServerConfig cfg;
		public CloseServerActionListener(ServerConfig serverConfig) {
			cfg = serverConfig;
		}
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new ServerCloseThread(cfg));
		}
	}

    /** 初始化服务器的线程。 */
	class ServerStartThread implements Runnable {
		public ServerConfig server;
		ServerStartThread(ServerConfig cfg) {
			server = cfg;
		}
		public void run() {
			synchronized (intialLock) {
				boolean initialed = false;
				AbstractGmForm frm = server.ins;
				if (frm == null) {
					initialed = true;
					Class kls = server.getGmFormClass();
	            	if (kls != null) {
						try {
							frm = (AbstractGmForm)kls.newInstance();
	                		frm.init(MainFrame.this, server.configFile);
	                		server.ins = frm;
						} catch (Exception e1) {
							e1.printStackTrace();
							showMessages(MainAppRES.initException, Res.format(MainAppRES.init_server_ErrMsg, server.name, e1.getMessage()));
	                		return;
						}
	            	} else {
                		showMessages(MainAppRES.err, Res.format(MainAppRES.model_server_NotFound, server.name,server.gmFormClass));
	            		return;
	            	}
	            	frm.addMouseListener(new DoubleClickZoomerAdapter(contentPane, frm));
				} 
				if (mainPanel.getComponentCount() > 0) {
					mainPanel.remove(mainPanel.getComponent(0));
				}
        		mainPanel.add(BorderLayout.CENTER, frm);
        		mainPanel.updateUI();
        		MainFrame.this.repaint();
        		if (initialed) {
        			frm.postInitial();
        		}
			}
		}
	}
	class ServerCloseThread implements Runnable {
		public ServerConfig server;
		ServerCloseThread(ServerConfig cfg) {
			server = cfg;
		}
		public void run() {
			AbstractGmForm ins = server.ins;
			server.ins = null;
			if (ins != null) {
				if (mainPanel.getComponentCount() == 1 && mainPanel.getComponent(0) == ins) {
					mainPanel.remove(ins);
					mainPanel.repaint();
				}
				ins.close();
				pip.gm.fw.AbstractClient client = ins.getUwapApp();
				if (client != null) {
					try {
						client.quit();
					} catch (Exception e) {
					}
				}
				serverListTree.repaint();
			}
		}
	}
}

