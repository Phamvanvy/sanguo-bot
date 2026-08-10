package pip.gm.fw;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


import java.io.*;

import cwu.util.DebugUtil;

import pip.gm.MainFrame;
import pip.util.StringUtil;
import pip.util.ui.ChatRmFld;
import pip.util.ui.RichConsole;
import pip.util.ui.RichConsole.Message;
import pip.util.ui.TodoTable.TodoProcessor;

/**
 * 节点展示Panel.
 * 抽象不同游戏的UI
 */
public abstract class AbstractGmForm extends JPanel implements TodoProcessor {
	/** 本服务器界面挂载的主控界面,用以刷新待办事宜 */
	public MainFrame main;
	public PrintStream out;
	/** 获得同本服务器负责交互的连接模块，即绑定的UWap客户端应用 */
	public abstract AbstractClient getUwapApp();
	public abstract ArrayList<Message> getExistingMessages();
    public abstract String getServerState();
    public abstract void postInitial();
    /** 取得本服务器支持的动作。在外部根据本命令可以直接操作，而无须进入界面.在外部 */
    public abstract ArrayList<String[]> getActions();
    /** 返回GM客户端的当前状态图标 */
	public abstract ImageIcon getIcon();
    public void close() {
    }
    public void init(MainFrame fm, String xml) {    	
    	main = fm;
    }
    public RichConsole consoles[];
    public int consoleTypes[];
    public void setOut(PrintStream out) {
    	this.out = out;
    }
    public ArrayList<ChatRmFld> chatDlg = new ArrayList<ChatRmFld>();
    /**
     * 处理系统消息
     * @param type 消息类型,
     * @param s 消息内容
     * @param refId 参考ID
     */
    public void onMessage(int type, String s, int refId[]) {
    	StringBuffer buf = new StringBuffer();
    	buf.append("<style name=\"");
    	buf.append(IMessage.TYPES[type]);
    	buf.append("\">");
    	buf.append(s);
    	buf.append("</style>\n");
    	ArrayList<pip.util.ui.RichConsole.Message> msgs = null;
    	int typeMsk = 1 << type;
    	for (int i = consoles.length; i-- > 0; ) {
    		if ((typeMsk & consoleTypes[i]) != 0) {
    			msgs = consoles[i].appendMessage(buf.toString(), refId);
    		}
    	}
    	if (msgs != null) {
	        for (ChatRmFld ct : chatDlg) {
	        	ct.onMsg(msgs);
	        }
    	}
    	AbstractClient client = getUwapApp();
    	if (client != null) {
    		BaseConfig config = client.getConfig();
    		if (config != null) {
    			config.printHistory("[" +  DebugUtil.getDate() + "] " + type + ": " + s);
    		}
    	}
    	if (out != null) {
    		out.println(StringUtil.removeTags(s));
    	}
    }
    public String getCurrentInputBuf() {
		return null;
	}
}
