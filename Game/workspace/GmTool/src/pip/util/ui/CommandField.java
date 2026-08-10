package pip.util.ui;

import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.*;

import pip.gm.fw.AbstractGmForm;
import pip.gm.fw.BaseConfig;
import pip.gm.fw.IMessage;
import pip.gm.fw.MessageListener;
import pip.gm.fw.TextProcesser;
import pip.gm.ui.data.NewGameCharacter;
import pip.util.*;
/**
 * 命令行输入窗口.有滚屏控制,有非命令操作渠道选项.
 * @author Administrator
 */
@SuppressWarnings("serial")
public class CommandField extends JPanel {
	/** 命令历史记录 */
    ArrayList<String> history = new ArrayList<String>();
    /** 当前的历史记录位置 */
    int currentPos = 0;

    /** 回调句柄,负责处理输入的命令;显示命令输出内容,*/
    AbstractGmForm gmForm;
    /** 提供内容的处理方式,并在此输入条上显示,然后根据输入方式的选择回调处理流程 */
    TextProcesser textProcess;  // 执行聊天

    public JTextArea fld;
    /** 文字正文处理的方式,第一个为私聊,其他的为TextProcessor指定 */
    JComboBox cbmodes;
    
    private LayoutUtil lu = new LayoutUtil();

    /** 当前还没有提交的输入行,方便滚动历史输入时回到当前输入内容 */
    String currentTempText = "";
    JPopupMenu popShortcurSelector;
    JPopupMenu popUserSelector;
    NewGameCharacter[] lstContactedUsers = new NewGameCharacter[40];
    NewGameCharacter selectedTargetUser;
    public HashMap<String, String> shortCutMap = new HashMap<String, String>();
    /** 但前玩家的显示部分,点击可弹出最近玩家列表供选择 */
    JButton currentTarget = new JButton(UiRES.noObject);

    public CommandField(AbstractGmForm form, TextProcesser textContentProcess) {
    	gmForm = form;
    	textProcess = textContentProcess;
        layoutComponents();
    }

    public NewGameCharacter getTarUser(NewGameCharacter nm) {
        int i = 0;
        for (; i < lstContactedUsers.length && lstContactedUsers[i] != null; i++) {
            if (lstContactedUsers[i].getId() == nm.getId()) {
            	if (nm.getName() == null) {
            		nm.setName(lstContactedUsers[i].getName());
            	}
                break;
            }
        }
        if (i > 0) {
            if (i >= lstContactedUsers.length) {
                i = lstContactedUsers.length - 1;
            }
            System.arraycopy(lstContactedUsers, 0, lstContactedUsers, 1, i);
        }
        lstContactedUsers[0] = nm;
        return nm;
    }


    public void appendHistory(String s) {
        int n = history.size();
        if (n > 0) {
            if (s.equals(history.get(n - 1))) {
                return;
            }
        }
        history.add(s);
        currentPos = n + 1;
    }
    public String getShortCuts() {
        StringBuffer buf = new StringBuffer(UiRES.shortCutList);
        for (String key: shortCutMap.keySet()) {
            String value = shortCutMap.get(key);
            key = "                  " + key;
            key = key.substring(key.length() - 12);
            buf.append("\n");
            buf.append(key);
            buf.append("  ");
            buf.append(value);
        }
        return buf.toString();
    }
    public void addShortCut(String s) {
        if (s != null) {
            int k;
            if (s.startsWith("\\") && (k = s.indexOf(';')) > 1) {
                int k2 = s.indexOf(' ');
                if (k2 > k || k2 == -1) {
                    String v = s.substring(k+1);
                    if (v.length() == 0) {
                        shortCutMap.put(s.substring(1, k), null);
                    } else {
                        shortCutMap.put(s.substring(1, k), v);
                    }
                    return;
                }
            }
            JMenuItem mi = new JMenuItem(s);
            mi.addActionListener(shortCutsActionListener);
            popShortcurSelector.add(mi);
        }
    }

    private void initCompoents() {
        fld = new JTextArea(2, 80);
        fld.setBackground(Color.BLACK);
        fld.setForeground(Color.GREEN);
        fld.setCaretColor(Color.CYAN);

        fld.addKeyListener(keyListener);
        fld.setText("");
        fld.setLineWrap(true);

        popShortcurSelector = new JPopupMenu();
    }
    
    private void layoutComponents() {
        initCompoents();
        this.setLayout(new GridBagLayout());
        // 把 fld 加入 JScrollPane的目的是绕过 JSplitPane 不能扩充的bug
        this.add(new JScrollPane(fld), lu.getConstrains(0, 0, 1, 1));
        this.add(getConfigPane(), lu.getConstrains(0, 1, 1, 1));
    }
    private JComponent getConfigPane() {
    	JPanel configPanel;
    	configPanel = new JPanel(new GridLayout(1, 5));
        String mdStr[] = textProcess.getTypes();
        String ms[] = new String[mdStr.length+1];
        ms[0] = UiRES.privateChating;
        System.arraycopy(mdStr, 0, ms, 1, mdStr.length);
        
        cbmodes = new JComboBox(ms);
        configPanel.add(cbmodes);
        JPanel pp = new JPanel(new BorderLayout());
        pp.add(BorderLayout.WEST, currentTarget);
    	configPanel.add(pp);
    	configPanel.add(new JLabel(""));
    	configPanel.add(new JLabel(""));
    	configPanel.add(new JLabel(""));
        currentTarget.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		if (popUserSelector == null) {
        			popUserSelector = new JPopupMenu();
        		}
        		popUserSelector.removeAll();
        		User mi = new User(null);
				popUserSelector.add(mi);
				mi.addActionListener(userSelectActionHandler);
        		for (NewGameCharacter u : lstContactedUsers) {
        			if (u != null) {
        				mi = new User(u);
        				popUserSelector.add(mi);
        				mi.addActionListener(userSelectActionHandler);
        			}
        		}
        		popUserSelector.show(currentTarget, 0, 0);
        	}
        });
        return configPanel;
    }
    public void setChattingTarget(NewGameCharacter c) {
    	cbmodes.setSelectedIndex(0);
    	selectedTargetUser = c;
    	if (c == null) {
    		currentTarget.setText(UiRES.noObject);
    	} else {
	    	String s = c.getName();
			if (s == null) {
				s = Res.format(UiRES.playerInfo, Integer.valueOf(c.getId())); 
			}
	    	currentTarget.setText(s);
    	}
    }
    public void resetConfig() {
    	SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
		        String mdStr[] = textProcess.getTypes();
		        String ms[] = new String[mdStr.length+1];
		        ms[0] = UiRES.privateChating;
		        System.arraycopy(mdStr, 0, ms, 1, mdStr.length);
		        cbmodes.setModel(new DefaultComboBoxModel(ms));
		        cbmodes.setSelectedIndex(0);
		        cbmodes.updateUI();
		        repaint();
		    }
    	});
    }
    
    public void fastHelp(String txt, int pos) {
        if (txt.length() == 0) {
            popShortcurSelector.show(this, 0, 0);
        } else {
            if (txt.startsWith("/")) {
                String []ss = pip.util.StringUtil.splitLines(txt.substring(1));
                gmForm.getUwapApp().con.processCommand(ss.length > 0 ?  new String[] {"help", ss[0]} : new String[] {"help"});
            } else {
                popShortcurSelector.show(this, 0, 0);
            }
        }
    }

    public String getText() {
        return fld.getText();
    }
    public void insertTxt(String as) {
        String s = fld.getText();
        int k = fld.getCaretPosition();
        fld.setText(s.substring(0, k) + as + s.substring(k));
        fld.setCaretPosition(k + as.length());
    }
    public void insertTxt(int off, String as) {
        String s = fld.getText();
        int k = fld.getCaretPosition();
        fld.setText(s.substring(0, off) + as + s.substring(off));
        if (off <= k) {
            k += as.length();
        }
        fld.setCaretPosition(k);
    }
    
    private KeyListener keyListener = new KeyListener() {
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode() == 0 && e.getKeyChar() == 10) {
                String tcmd = fld.getText();
                int cursorPos = fld.getCaretPosition();
                boolean esced = false;

                // 去掉插入的回车
                if (cursorPos == 0) {
                    if (tcmd.length() > 0 && tcmd.charAt(0) == '\n') {
                        tcmd = tcmd.substring(1);
                    }
                } else if (cursorPos < tcmd.length() && tcmd.charAt(cursorPos - 1) == '\n') {
                    tcmd = tcmd.substring(0, cursorPos-1) + tcmd.substring(cursorPos);
                }
                // 处理转义字符
                if (!tcmd.startsWith("/set shortcut ")) {
                    int k2 = tcmd.lastIndexOf('\\');
                    while (k2 >= 0) {
                    	int k3 = tcmd.indexOf(';', k2);
                        int k4 = tcmd.indexOf(' ', k2);
                        if (k4 > k2 && k3 == -1) {
                        	k3 = k4;
                        }
                        if (k3 > k2) {
                            String kk = tcmd.substring(k2+1, k3);
                            String rep = shortCutMap.get(kk);
                            if (rep != null) {
                                esced = true;
                                k3++;
                                tcmd = tcmd.substring(0, k2) + rep + tcmd.substring(k3);
                                if (cursorPos > k3) {
                                    cursorPos += rep.length() - kk.length();
                                } else if (cursorPos > k2) {
                                    cursorPos = k2 + rep.length();
                                }
                            }
                        }
                        if (k2 < 3) {
                            break;
                        } else {
                            k2 = tcmd.lastIndexOf('\\', k2 - 1);
                        }
                    }
                }
                String cmd = tcmd.trim();
                if (e.isControlDown()) {
                    if (esced) {
                        fld.setText(tcmd);
                        fld.setCaretPosition(cursorPos);
                    } else {
                        fastHelp(cmd, fld.getCaretPosition());
                    }
                } else {
                    if (cmd.length() > 0) {
                        appendHistory(cmd);
                        String []cmdlines = pip.util.StringUtil.splitLines(cmd);
                        if (cmdlines.length > 0 && cmdlines[0].startsWith("/")) {
                        	String[] lines = cmd.split("\n");
                        	for (String line : lines) {
                        		line = line.trim();
                        		if (line.length() == 0) {
                        			continue;
                        		}
                        		cmdlines = pip.util.StringUtil.splitLines(line);
                        		if (cmdlines.length > 0 && cmdlines[0].startsWith("/")) {
                        			cmdlines[0] = cmdlines[0].substring(1);
                        			gmForm.onMessage(IMessage.MSG_TYPE_COMMAND, line, null);
                        			gmForm.getUwapApp().con.processCommand(cmdlines);
                        		}
                        	}
                        } else {
                            try {
                            	int mode = cbmodes.getSelectedIndex();
                            	if (mode == 0) {
                            		if (selectedTargetUser != null) {
                            			textProcess.processText("私:" + selectedTargetUser.getId() + "[" + selectedTargetUser.getName() + "]"  , cmd);
                            		} else {
                            			gmForm.onMessage(IMessage.MSG_TYPE_COMMAND, "No target", null);
                            		}
                            	} else {
                            		textProcess.processText(cbmodes.getSelectedItem().toString(), cmd);
                            	}
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    fld.setText("");
                    currentTempText = "";
                    currentPos = history.size();
                }
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.isControlDown()) {
                switch (e.getKeyCode()) {
                case 36:
                    if (currentPos == history.size()) {
                        currentTempText = fld.getText();
                    }
                    if (currentPos != 0) {
                        currentPos = 0;
                        fld.setText(history.get(currentPos));
                        fld.requestFocus();
                    }
                    break;
                case 35:
                    if (currentPos != history.size()) {
                        currentPos = history.size();
                        fld.setText(currentTempText);
                        fld.requestFocus();
                    }
                    break;
                case 38:
                    if (currentPos > 0) {
                        if (currentPos >= history.size()) {
                            currentTempText = fld.getText();
                            currentPos = history.size();
                        }
                        currentPos--;
                        fld.setText(history.get(currentPos));
                        fld.requestFocus();
                    }
                    break;
                case 40:
                    if (currentPos < history.size()) {
                        currentPos++;
                        if (currentPos < history.size()) {
                            fld.setText(history.get(currentPos));
                        } else {
                            fld.setText(currentTempText);
                        }
                        fld.requestFocus();
                        break;
                    }
                }
            }
        }
        public void keyReleased(KeyEvent e) {
        }
    };

    private ActionListener shortCutsActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            if (obj instanceof JMenuItem) {
                String as = ((JMenuItem)obj).getText();
                insertTxt(as);
            }
        }
    };

    private ActionListener userSelectActionHandler = new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		cbmodes.setSelectedIndex(0);
    		User mi = (User)e.getSource();
    		setChattingTarget(mi.u);
    	}
    };

    class User extends JMenuItem {
    	NewGameCharacter u;
    	public User(NewGameCharacter u) {
    		this.u = u;
    		setText(this.toString());
    	}
    	public String toString() {
    		if (u == null) {
    			return UiRES.noObject;
    		}
    		String s = u.getName();
    		if (s == null) {
    			return Res.format(UiRES.playerInfo, Integer.valueOf(u.getId()));
    		}
    		return s;
    	}
    }
}
