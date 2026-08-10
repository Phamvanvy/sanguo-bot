package pip.util.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Stack;

import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import pip.util.StringUtil;

/**
 * 带格式和快捷动作的输出控制台.
 * 通过类似于heml的tag标注正文的展示效果,同时快捷动作通过快捷菜单实现.
 * 支持的tag：
 *  action： 
 */
public class RichConsole extends JTextPane implements MouseListener, MouseMotionListener, ActionListener {
    private StyleContext styleContext = new StyleContext();
    private StyledDocument src = new DefaultStyledDocument(styleContext);
    public ArrayList<Message> messages = new ArrayList<Message>();

    public int lineFeed = 8;
    public int maxTextSize = 1024 * 35;//mengjie modify 20120203
    int _n = 0;
    int _start = 0;
    int fh = 13;
    JPanelPopup tip = new JPanelPopup();
    public boolean scrollingMode = true;
    ArrayList<ConsoleActionListener> consoleActionListeners = new ArrayList<ConsoleActionListener>();
    JPopupMenu sk = new JPopupMenu();
    JPopupMenu pop = new JPopupMenu();
    JMenuItem miClear = new JMenuItem(UiRES.clearScreen);
    public boolean needUpdate = true;
    private Container getDockingParent() {
    	for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof JRootPane) {
                if (p.getParent() instanceof JInternalFrame) {
                    continue;
                }
                return ((JRootPane)p).getLayeredPane();
            } else if (p instanceof Window) {
                return p;
            }
        }
        return getParent();
    }
    public void addConsoleActionListener(ConsoleActionListener l) {
    	if (l != null) {
    		synchronized (consoleActionListeners) {
    			consoleActionListeners.add(l);
    		}
    	}
    }
    public void removeConsoleActionListener(ConsoleActionListener l) {
    	if (l != null) {
    		synchronized (consoleActionListeners) {
    			consoleActionListeners.remove(l);
    		}
    	}
    }
    public RichConsole(Style defaultStyle) {
        setDocument(src);
        setBackground(Color.BLACK);
        setEditable(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        setSelectedTextColor(Color.GREEN);
        Style style = styleContext.addStyle("default", defaultStyle);
        if (defaultStyle == null) {
            StyleConstants.setFontFamily(style, "Monospaced");
            StyleConstants.setBackground(style, Color.BLACK);
            StyleConstants.setForeground(style, Color.white);
            StyleConstants.setFontSize(style, 12);
        }
        pop.add(miClear);
        miClear.addActionListener(this);
        
    }
    public Style addStyle(String name, Style style) {
    	return styleContext.addStyle(name, style);
    }

    public synchronized void clearScreen() {
        try {
            messages.clear();
            src.remove(0, src.getLength());
        } catch (BadLocationException ex) {
        }
    }
    public ArrayList<Message> getMessages() {
    	return messages;
    }
    public synchronized void appendMessages(ArrayList<Message> msgs, int refIds[]) {
		try {
	    	int len = src.getLength();
	    	for (Message msg : msgs) {
	    		Style style = msg.style;
	    		if (style == null) {
	    			style = styleContext.getStyle("default");
	    		}
				src.insertString(len, msg.text, style);
				if (refIds != null) {
					msg.refIds = refIds;
				}
				messages.add(msg);
	    		msg.startPos = len;
	    		len += msg.text.length();
	    	}
	    	int pos = scrollingMode ? len - 1: getCaretPosition();
	    	if (len > maxTextSize) {
	    		synchronized (messages) {
	    			int dSize = len - maxTextSize;
	    			if (dSize > 0) {
			    		int rd = 0;
			    		int i = 0;
			    		for (; i < messages.size() - 1; i++) {
			    			rd = messages.get(i).startPos;
			    			if (rd > dSize) {
			    				break;
			    			}
			    		}
			    		ArrayList<Message> tmp = new ArrayList<Message>();
			    		for (; i < messages.size(); i++) {
			    			tmp.add(messages.get(i));
			    			messages.get(i).startPos -= rd;
			    		}
			    		src.remove(0, rd);
			    		messages = tmp;
			    		pos -= rd;
	    			}
	    		}
	    	}
	    	setCaretPosition(pos);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    }

    public synchronized ArrayList<Message> appendMessage(String msg, int refIds[]) {
    	ArrayList<Message> newMsgs = new ArrayList<Message>();
        try {
            // 使用小状态机分析添加内容,功能结构相对固定,不为OO而构造冗余类
            Style style = styleContext.getStyle("default");
            int n = msg.length();
            Stack<Style> styleStack = new Stack<Style>();
            char []data = msg.toCharArray();
            StringBuffer line = new StringBuffer();
            int status = 0;
            StringBuffer tmpStr = new StringBuffer();
            Properties prop = new Properties(); // tag的属性
            String tagName = null;
            Stack<String> tagStack = new Stack<String>();
            Stack<MenuItemInfo> menuItems = new Stack<MenuItemInfo>();
            Stack<String[]> paras = new Stack<String[]>();
            String link = null;
            String propName = null;
            int lstTagPos = 0;
            styleStack.push(style);
            for (int i = 0; i < n; i++) {
            	char c = data[i];
            	switch (status) { 
            	case 0: // 文本状态  "..."
            		if (c == '<') {
            			tmpStr.setLength(0);
            			status = 1;
            			lstTagPos = i;
            		} else if (c == '&'){
            			tmpStr.setLength(0);
            			status = 2;
            			lstTagPos = i;
            		} else {
            			line.append(c);
            		}
            		break;
            	case 1: // 开始tag "..<"
            		if (c == '/') {
            			status = 4;
            			tmpStr.setLength(0);
            		} else if (c == '>') {
            			status = 0; // 忽略 <   >
            			break;
            		} else if (c == ' ') { // 忽略tag前空格
            		} else {
            			tmpStr.append(c);
            			tagName = null;
            			prop.clear();
            			status = 3;
            		}
            		break;
            	case 2: // 进入转义 "..&"
            		if (c == ';') {
            			if (tmpStr.length() > 0) {  
            				String escStr = tmpStr.toString();
            				if ("lt".equals(escStr)) {
            					line.append('<');
            				} else if ("gt".equals(escStr)) {
            					line.append('>');
            				} else if ("nbsp".equals(escStr)) {
            					line.append(' ');
            				} else if ("amp".equals(escStr)) {
            					line.append('&');
            				} else {
            					// 忽略不识别内容
            				}
            			}
            			status = 0;
            		} else {
            			tmpStr.append(c);
            		}
            		break;
            	case 3: // 进入tag "<."
            		if (c == '>') {
            			tagName = tmpStr.toString();
            			i--;
            			status = 14;
            		} else if (c == '/'){
            			i++;
            			if (data[i] == '>') {
            				tagName = tmpStr.toString();
            				i--;
            				status = 15;
            			} else {
            				// 格式错误, 出现 "..<name/."
            				i--;
            				status = 10; // 转为文本.
            			}
            		} else if (c == ' '){
            			tagName = tmpStr.toString();
            			tmpStr.setLength(0);
            			status = 5;
            		} else {
            			tmpStr.append(c);
            		}
            		break;
            		
            	case 4: // 结束tag "..</"
            		if (c == '>') {
            			status = 0;
            		} else if (c == ' ') { // skip
            		} else if (c == '/') { // error
            			i--;
            			status = 10;
            		} else {
            			tmpStr.append(c);
            			status = 9;
            		}
            		break;
            	case 5: // tag属性开始 "..<name "
            		if (c == ' ') {
            			// do nothing
            		} else if (c == '>') {
            			i--;
            			status = 14;
            		} else if (c == '/') {
            			i++;
            			if (data[i] == '>') {
            				i--;
            				status = 15;
            			} else {
            				i--;
            				status = 10; // 转为文本.
            			}
            		} else if (c == '=') {
            			propName = tagName;
            			status = 7;
            		} else {
            			tmpStr.append(c);
            			status = 6;
            		}
            		break;
            	case 6: // 开始tag属性 "..<name ."
            		if (c == '=') {
            			propName = tmpStr.toString();
            			tmpStr.setLength(0);
            			status = 7;
            		} else if (c == '/') {
            			i++;
            			if (data[i] == '>') {
            				prop.setProperty(propName, tmpStr.toString());
            				i--;
            				status = 15;
            			} else {
            				i--;
            				status = 10; // 转为文本.
            			}
            		} else if (c == '>') {
        				prop.setProperty(propName, tmpStr.toString());
        				i--;
        				status = 14;
            		} else {
            			tmpStr.append(c);
            		}
            		break;
            	case 7: // 开始tag属性值 "..<name ..="
            		if (c == '\"') {
            			status = 11;
            		} else if (c == ' ') {
            		} else if (c == '\'') {
            			status = 12;
            		} else {
            			tmpStr.append(c);
            			status = 13;
            		}
            		break;
            	case 9: // 结束tag "..</."
            		if (Character.isJavaIdentifierPart(c)) {
            			tmpStr.append(c);
            		} else if (c == '>') {
            			if (tagStack.size() > 0) {
            				String mt = tagStack.pop();
                			tagName = tmpStr.toString();
            				if (mt.equals(tagName)) {
                    			if (tagName.equals("style") || tagName.equals("font")) {
                					if (line.length() > 0) {
                						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
                		        		line.setLength(0);
                		            }
                    				if (styleStack.size() > 0) {
                    					style = styleStack.pop();
                    				}
                    			} else if (tagName.equals("param")) {
                					if (line.length() > 0) {
                						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
                		        		line.setLength(0);
                		            }
                					if (paras.size() > 0) {
                						paras.pop();
                					}
                    			} else if (tagName.equals("a")) {
                					if (line.length() > 0) {
                						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
                		        		line.setLength(0);
                		            }
                					link = null;
                    			} else if (tagName.equals("action")) {
                					if (line.length() > 0) {
                						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
                		        		line.setLength(0);
                		            }
                    				if (menuItems.size() > 0) {
                    					menuItems.pop();
                    				}
                    			}
            				} else {
            					// 错误,tag不匹配
            				}
            			}
            			status = 0;
            		} else {
            			i--;
            			status = 10; // error
            		} 
            		break;
            	case 10: // 错误,退字符
            		line.append(new String(data, lstTagPos, i-1-lstTagPos));
    				i--;
    				status = 0;
    				break;
            	case 11: // 开始tag属性值 "..<name ..=\""
            		if (c == '\"') {
            			prop.setProperty(propName, tmpStr.toString());
            			tmpStr.setLength(0);
            			status = 5;
            		} else {
            			tmpStr.append(c);
            		}
            		break;
            	case 12: // 开始tag属性值 "..<name ..=\'"
            		if (c == '\'') {
            			prop.setProperty(propName, tmpStr.toString());
            			tmpStr.setLength(0);
            			status = 5;
            		} else {
            			tmpStr.append(c);
            		}
            		break;
            	case 13: // 开始tag属性值 "..<name ..=."
            		if (c == ' ') {
            			prop.setProperty(propName, tmpStr.toString());
            			tmpStr.setLength(0);
            			status = 5;
            		} else if (c == '/'){
            			i++;
            			if (data[i] == '>') {
            				prop.setProperty(propName, tmpStr.toString());
            				i--;
            				status = 15;
            			} else {
            				i--;
            				status = 10; // 转为文本.
            			}
            		} else if (c == '>'){
            			prop.setProperty(propName, tmpStr.toString());
            			i--;
            			status = 14;
            		} else {
            			tmpStr.append(c);
            		}
            		break;
    				
            	case 14: // 处理自结尾属性,不读字符
            		tagStack.push(tagName);
            		// walkthrough
            	case 15: // 处理属性,不读字符
    				if ("br".equals(tagName)) {
    					line.append('\n');
    				} else if ("action".equals(tagName)) {
    					if (line.length() > 0) {
    						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
    		        		line.setLength(0);
    		            }
        				String actionName = prop.getProperty("title");
        				String command = prop.getProperty("command");
        				MenuItemInfo mi = new MenuItemInfo(actionName, command);
        				menuItems.push(mi);
    				} else if ("param".equals(tagName)) {
    					if (line.length() > 0) {
    						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
    		        		line.setLength(0);
    		            }
        				String value[] = prop.getProperty("value").split(";");
        				for (int ii = 0; ii < value.length; ii++) {
        					if (value[ii].length() == 0) {
        						value[ii] = null;
        					}
        				}
        				paras.push(value);
    				} else if ("a".equals(tagName)) {
    					if (line.length() > 0) {
    						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
    		        		line.setLength(0);
    		            }
    					link = prop.getProperty("url");
    				} else if ("style".equals(tagName)) {
    					if (line.length() > 0) {
    						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
    		        		line.setLength(0);
    		            }
    					styleStack.push(style);
    					String color = prop.getProperty("name");
    					if (color != null) {
    						style = styleContext.getStyle(color);
    						if (style == null) {
    							style = styleStack.peek();
    						}
    					}
    				} else if ("font".equals(tagName)) {
    					if (line.length() > 0) {
    						newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
    		        		line.setLength(0);
    		            }
    					styleStack.push(style);
    					String name = prop.getProperty("name");
    					if (name != null) {
    						style = styleContext.getStyle(name);
    						if (style == null) {
    							String family = prop.getProperty("family");
    							if (family == null) {
    								family = "Monospaced";
    							}
    							String bg = prop.getProperty("bg");
    							if (bg == null) {
    								bg = "0x000000";
    							} else {
    								bg = bg.toLowerCase();
    							}
    							String color = prop.getProperty("color");
    							if (color == null) {
    								color = "0xFFFFFF";
    							} else {
    								color = color.toLowerCase();
    							}
    							String sizeStr = prop.getProperty("size");
    							int size;
    							if (sizeStr == null) {
    								size = 12;
    							} else {
    								size = Integer.parseInt(sizeStr);
    							}
    							Color cBg;
    							Color cFg;
    							try {
									if (bg.startsWith("0x")) {
										cBg = new Color(Integer.parseInt(bg.substring(2), 16));
									} else {
										cBg = new Color(Integer.parseInt(bg));
									}
									if (color.startsWith("0x")) {
										cFg = new Color(Integer.parseInt(color.substring(2), 16));
									} else {
										cFg = new Color(Integer.parseInt(color));
									}
									style = styleContext.addStyle(name, null);
						            StyleConstants.setFontFamily(style, family);
						            StyleConstants.setBackground(style, cBg);
						            StyleConstants.setForeground(style, cFg);
						            StyleConstants.setFontSize(style, size);
								} catch (Exception e) {
									e.printStackTrace();
								}
    						}
    					}
    				}
    				status = 0;
    				break;
            	}
            }
            if (line.length() > 0) {
            	newMsgs.add(new Message(line.toString(), style, link, menuItems, paras));
            }
            appendMessages(newMsgs, refIds);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        return newMsgs;
    }

    private void msgDbleClicked(Message m, int x, int y) {
    	String s = m.clickCommand;
    	for (int i = 0; s == null && i < m.params.size(); i++) {
    		String para[] = m.params.get(i);
    		s = paramMenuBuilder.genCommand(para);
    	}
    	if (s != null) {
    		for (ConsoleActionListener l : consoleActionListeners) {
            	l.processCommand(s);
            }
    	}
    }
    private void msgClicked(Message m, int x, int y) {
        int cs = m.startPos;
        int ce = cs + m.text.length();

        sk.removeAll();
        boolean has = false;
        for (MenuItemInfo info : m.menuItemInfo) {
        	JMenuItem mi = new JMenuItem(info.title);
        	mi.setActionCommand(info.action);
        	mi.addActionListener(this);
        	sk.add(mi);
        	has = true;
        }
        if (paramMenuBuilder != null) {
	        for (String para[] : m.params) {
	        	ArrayList<String[]> ss = paramMenuBuilder.genMenu(para);
	        	if (ss.size() > 0) {
		        	if (has) {
		        		sk.addSeparator();
		        	}
		            for (String []s : ss) {
		            	JMenuItem mi = new JMenuItem(s[0]);
		            	mi.setActionCommand(s[1]);
		            	mi.addActionListener(this);
		            	sk.add(mi);
		            }
		            has = true;
	        	}
	        }
        }
        
        select(cs, ce);
        if (sk.getComponentCount() > 0) {
            sk.show(this, x+1, y+1);
        }
    }
    
    public void mouseDragged(MouseEvent e) {
    	tip.setVisible(false);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e){
    	tip.setVisible(false);
    }
    
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            pop.show(this, e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            int k = this.getCaretPosition();
            for (int i = messages.size(); i-- > 0; ) {
            	Message m = messages.get(i);
            	if (m.startPos <= k) {
            		if (e.isControlDown()) {
            			if (e.isShiftDown()) {
	                		Date d = new Date(m.time);
	            			tip.setHint(d.getHours() + ":" + d.getMinutes() + " [" + m.style.getName() + "]");
	            			tip.pack();
	            			if (tip.getParent() != null) {
	            				tip.repaint();
	            			}
	                        Point p = getLocationOnScreen();
	                        tip.setLocationOnScreen(p.x + e.getX(), p.y + e.getY() + 16);
	                        if (tip.getParent() == null) {
	                            tip.show(this);
	                        }
            			} else {
            				msgDbleClicked(m, e.getX(), e.getY());
            			}
                    } else {
            			msgClicked(m, e.getX(), e.getY());
            		}
            		return;
	            }
        	}
        }
    }
    public void mousePressed(MouseEvent e) {
//    	if (e.getButton() == MouseEvent.BUTTON1) {
//            int k = this.getCaretPosition();
//            for (int i = messages.size(); i-- > 0; ) {
//            	Message m = messages.get(i);
//            	if (m.startPos <= k) {
//            		poptip.showMsg(e.getX(), e.getY(), new Date(m.time).toString());
//            		System.out.print("=" + m.text);
//            		return;
//	            }
//            }
//        }
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == miClear) {
            this.clearScreen();
            return;
        }
        String cmd = e.getActionCommand();
        for (ConsoleActionListener l : consoleActionListeners) {
        	l.processCommand(cmd);
        }
    }
    
    private Style addStyle(String name, Color color) {
        Style style = styleContext.addStyle(name, null);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setBackground(style, Color.BLACK);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontSize(style, 12);
        return style;
    }

    private static class MenuItemInfo {
		String title;
		String action;
		public MenuItemInfo(String title, String action) {
			this.title = title;
			this.action = action;
		}
	}

    public static class Message {
    	public String text;
    	public Style style;
    	public ArrayList<MenuItemInfo> menuItemInfo = new ArrayList<MenuItemInfo>();
    	public String clickCommand = null;
    	public int startPos;
    	public ArrayList<String[]> params = new ArrayList<String[]>();
    	public long time; 
    	public int[] refIds;
    	public Message(String text, Style style) {
    		time = System.currentTimeMillis();
    		this.text = text;
    		this.style = style;
    	}
        private Message(String text, Style style, String link, Stack<MenuItemInfo> menuItems, Stack<String[]> p) {
        	this(text, style);
        	this.clickCommand = link;
        	for (MenuItemInfo info : menuItems) {
        		menuItemInfo.add(info);
        	}
        	for (String[] info : p) {
        		params.add(info);
        	}
        }
        public boolean isRelated(int id) {
        	if (refIds != null) {
    	    	for (int k : refIds) {
    	    		if (k == id) {
    	    			return true;
    	    		}
    	    	}
        	}
        	return false;
        }
    	public Message cloneMeOut() {
    		Message ret = new Message(text, style);
    		ret.time = time;
    		for (MenuItemInfo info : menuItemInfo) {
    			ret.menuItemInfo.add(info);
    		}
    		ret.clickCommand = clickCommand;
    		return ret;
    	}
    	
    }
    
	public static interface ConsoleActionListener {
		public void processCommand(String command);
	}
	public void setParamMenuBuilder(ParamMenuItemBuilder b) {
		paramMenuBuilder = b;
	}
	private ParamMenuItemBuilder paramMenuBuilder;
	public static interface ParamMenuItemBuilder {
		/** 
		 * 根据参数构造菜单选项。
		 * 返回一系列菜单，每个菜单项为标题、动作字符串对。
		 * 参数和应用绑定，一般是玩家id，玩家名称。
		 * 不允许返回空 
		 */
		public ArrayList<String[]> genMenu(String param[]);
		/**
		 * 构造直接执行的命令。
		 * 参数和应用绑定，一般是玩家id，玩家名称。
		 */
		public String genCommand(String param[]);
	}

    public static StringBuffer genActionTag(String title, String action) {
    	title = StringUtil.formal(title);
    	action = StringUtil.formal(action);
    	StringBuffer ret = new StringBuffer("<action title=\"");
    	ret.append(title.replace('\"', '\''));
		ret.append("\" command=\"");
		ret.append(action.replace('\"', '\''));
		ret.append("\">");
    	return ret;
    }
    /** 构造参数标注。以玩家名称和id作为基础。
     * 返回标注的开始字串和结束字串对 */
    public static String[] genPlayerHyperString(String playerName, int playerId) {
    	String sId = playerId > 0 ? String.valueOf(playerId) : "";
    	if (playerName == null) {
    		playerName = "";
    	} else {
    		playerName = StringUtil.formal(playerName);
    	}
    	return new String[]{
        		"<param value=\"" + playerId + ";" + playerName + "\">",
        		"</param>"
        	};
    }
}
