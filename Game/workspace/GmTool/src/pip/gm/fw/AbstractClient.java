package pip.gm.fw;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import pip.io.uwap.PDataFactory;
import pip.io.uwap.UWAPApp;
import pip.io.uwap.UWapClientConnection;
import pip.io.uwap.UWapData;
import pip.gm.cmd.GeneralFunction;
import pip.gm.cmd.GeneralFunction.Data;
import java.awt.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import pip.util.ui.*;

/**
 * 一个UWAP的应用.
 * 抽象出不同游戏的界面,命令,以及处理过程的中间桥梁.
 */
public abstract class AbstractClient implements UWAPApp {
	public UWapClientConnection conn;
	public Controller con;
	public PDataFactory factory= new PDataFactory();
	/** 有可能希望一个包被多个模块处理，因此这里没有使用注册机制，而是使用了轮询消耗机制。 */
	public HashSet<PDProcessor> processor = new HashSet<PDProcessor>();
	public long sharpId;
	public Auth auth;
	
	public String host;
	public int port;
	
	HashMap<Integer,UWapData> needReceiptSet = new HashMap<Integer,UWapData>(); 

	public AbstractClient() {
		con = new Controller(this);
	}
	public void quit() throws Exception {
		close();
		con = null; 
	}
	public abstract pip.gm.fw.BaseConfig getConfig();
	/** 得到最外界的控件，Frame 或 Window. 用来弹出DIALOG */
	public Window getUiContainer() {
		return null;
	}
	/** 有关场景名称等信息 */
	public abstract GameInfo getGameInfo();
	public UWapData loginPackage = null;
	
	/** 服务器唯一ID，根据IP地址和端口号决定，用来统一不同命名规则 */
    private String serverId;
	/** 获取服务器唯一ID，根据IP地址和端口号决定，用来统一不同命名规则。用在记录GM日志 */
	public String getUniqServerId() {
		if (serverId == null) {
			String sharpId = getConfig().getStringProperty("sharpId");
	     	if (sharpId == null || sharpId.length() == 0 || sharpId.equals("0")) {
	     		serverId = getConfig().host + ":" + getConfig().port;
	     	} else {
	     		serverId = getConfig().host + ":" + getConfig().port + "#" + sharpId;
	     	}
		}
		return serverId;
	}

	public void registerLoginPkg(UWapData d) {
		loginPackage = d;
	}
	public void addProcessor(PDProcessor p) {
		if (p != null) {
			processor.add(p);
		}
	}
	public void removeProcessor(PDProcessor p) {
		if (p != null) {
			processor.remove(p);
		}
	}
	public void onGotData(UWapData d) {
		int ser = d.getProtocolSerialNumber();
		if (ser > 1) {
			UWapData originalData = needReceiptSet.get(ser);
			if (originalData != null) {
				needReceiptSet.remove(ser);
				((Receiptable)originalData).getReceiptListener().onReceipt(originalData, d);
			}
		}
		for (PDProcessor p: processor) {
			if (p.process(this, d)) {
				return;
			}
		}
//		System.out.println("LOST PKG:" + d.getClass());
	}
	public void sndRequest(UWapData d) {
		if (conn == null) {
			conn = new UWapClientConnection(this, host, port, sharpId, factory);
			String s = getConfig().getStringProperty("UWAP.Version");
			if (s != null) {
			    conn.uwapVersion = Integer.parseInt(s);
			}
		}
		if (conn.session == null) {
		    new Thread(conn).start();
		}
		try {
			if (d instanceof Receiptable) {
				needReceiptSet.put(d.getProtocolSerialNumber(), d);
			}
			conn.send(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** To Close the connection */
	public void close() throws Exception {
		if (conn != null) {
			UWapClientConnection c = conn;
			conn = null;
			c.close();
		}
	}
	/** 读取缺省的协议配置。从游戏模块中 /《game》/protocol.xml 和 配置文件中指定的 protocol中 */
	public void loadConfigFunction() {
		InputStream in = null;
		try {
			StringBuilder resNameBuf = new StringBuilder("/");
			resNameBuf.append(getConfig().game.toLowerCase()).append("/protocol");
			resNameBuf.append(".xml");
			String resName = resNameBuf.toString();
			in = AbstractClient.class.getResourceAsStream(resName);
			if (in != null) {
				loadConfigFunction(in);
			} else {
				System.out.println("No Res:" + resName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String protocolConfig = getConfig().getStringProperty("protocol");
		if (protocolConfig != null) {
	        try {
	        	File f = new File(protocolConfig);
	        	if (!f.exists()) {
	        		throw new Exception("File not exist：" + f.getAbsolutePath());
	        	}
	        	in = new FileInputStream(f);
	        	loadConfigFunction(in);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public void loadConfigFunction(InputStream in) throws IOException,DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(in);
		Element root = document.getRootElement();
		for(Iterator<Element> i = root.elementIterator("ReceiveData"); i.hasNext();) {
			Element elem = (Element)i.next();
			String valid = elem.attributeValue("valid");
			if (valid == null || valid.equals("1")) {
				String engineClassName = elem.attributeValue("modelClass");
				int k = Integer.parseInt(elem.attributeValue("protocolId"));
				if (engineClassName != null) {
					try {
						Class kls = Class.forName(engineClassName);
						if (kls != null) {
							factory.register(k, kls);
						}
						String processorName = elem.attributeValue("processor");
						if (processorName != null) {
							kls = Class.forName(processorName);
							if (kls != null) {
								addProcessor((PDProcessor)kls.newInstance());
							}
						}
					} catch (Exception e) {
						System.out.println(engineClassName + " Load error.");
					}
				} else {
					factory.receivingProtocolSettings.put(Integer.valueOf(k), elem);
					factory.register(k, Data.class);
				}
			}
		}
		for(Iterator<Element> i = root.elementIterator("Function"); i.hasNext();) {
			Element elem = (Element)i.next();
			String valid = elem.attributeValue("valid");
			if (valid == null || valid.equals("1")) {
				String engineClassName = elem.attributeValue("class");
				if (engineClassName != null) {
					try {
						Class kls = Class.forName(engineClassName);
						if (kls != null) {
							addFunction((Command)kls.newInstance());
						}
					} catch (Exception e) {
						System.out.println(engineClassName + " Load error.");
					}
				} else {
					GeneralFunction gf = new GeneralFunction(elem);
					addFunction(gf);
				}
			}
		}
		addFunction(new UiExec());
	}
	public HashSet<IdleTask> idleTasks = new HashSet<IdleTask>();
	public void addFunction(Command cmd) { 
		con.addCommand(cmd);
		if (cmd instanceof IdleTask) {
			idleTasks.add((IdleTask)cmd);
		}
		if (cmd instanceof GmFunction) {
			addProcessor(((GmFunction)cmd).getPackageProcessor());
			((GmFunction)cmd).registerPackage(factory);
		}
	}
	public class UiExec extends Command { // UI Exec
		public JDialog dialog;
		AbstractClient wd;
		Component [][]components;
		Command cmd;
		public String getCommand(Auth auth) {
			return "ue";
		}
		public String getName(Auth auth) {
			return "UI Execute";
		}
		public String getDescription(Auth auth) {
			return "To fulfill command parameters with one dialog.";
		}
		public long getAuth() {
	    	return 0;
	    }
		public boolean exec(String cmd, AbstractClient wd, String []s) throws Exception {
			this.wd = wd;
			if (s != null && (s.length == 2 || s.length == 3)) {
	            if (cmd != null) {
	                if (isCommand(wd.auth, cmd)) {
	                	if (s.length == 2) {
	                		launchUi(s[1], 0);
	                	} else {
	                		launchUi(s[1], Integer.parseInt(s[2]));
	                	}
	                }
	    			return true;
	            }
			}
			return false;
		}
		public void launchUi(String commandName, int n) {
			if (dialog == null) {
				dialog = new JDialog(wd.getUiContainer());
				dialog.setModal(true);
			}
			cmd = con.getCommand(commandName);
			if (cmd != null && cmd instanceof ArrayOfParameters) {
				ArrayOfParameters func = (ArrayOfParameters)cmd;
				if (n >= func.getNumOfParameterSet()) {
					System.out.println("No Sepecified UI set " + commandName + "-" + n);
					return;
				}
				int paranNum = func.getNumOfParameters(n);
				components = new Component[paranNum+1][];
				LayoutUtil lu = new LayoutUtil();
				for (int i = 0; i < paranNum; i++) {
					String pName = func.getParameterTitle(n, i);
					if (pName.equals("*")) {
						continue;
					}
					JLabel lb = new JLabel(pName);
					String tip = func.getParameterTips(n, i);
					if (tip != null) {
						lb.setToolTipText(tip);
					}
					components[i] = new Component[]{null, lu.getRightAlignComponent(lb), new TextField(40)};
				}
				JButton btn = new JButton("Go");
				btn.addActionListener(new DialogActionListener(func, n));
				components[paranNum] = new Component[]{new JLabel(" "), null, btn, new JLabel(" ")};

				dialog.setTitle(func.getParameterSetName(n));
				Container container = dialog.getContentPane();
				container.removeAll();
				lu.layoutGridBagComponents(container, components);
				dialog.pack();
				lu.setWindowCentrallize(dialog, wd.getUiContainer());
				dialog.setVisible(true);
			} else {
				System.out.println("The command " + commandName + " does not support UI input.");	
			}
		}
		public class DialogActionListener implements ActionListener {
			public DialogActionListener(ArrayOfParameters func, int n) {
				this.func = func;
				this.n = n;
			}
			ArrayOfParameters func;
			int n;
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				String [] para = new String[components.length];
				para[0] = cmd.getCommand(wd.auth);
				for (int i = 1; i < para.length; i++) {
					if (components[i-1] == null) {
						para[i] = func.getParameterTips(n, i-1);
					} else {
						para[i] = ((TextField)components[i-1][2]).getText();
					}
				}
				try {
					cmd.exec(para[0], wd, para);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
