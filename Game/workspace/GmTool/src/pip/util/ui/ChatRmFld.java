package pip.util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import pip.gm.fw.AbstractGmForm;
import pip.gm.fw.IMessage;
import pip.util.Res;
import pip.util.ui.RichConsole.Message;

/** 同固定玩家的独立聊天室 */
public class ChatRmFld extends JDialog implements Runnable, KeyListener, ActionListener, WindowListener {
    ArrayList<String> history = new ArrayList<String>();
    int currentPos = 0;

    public int tracingUserId; // 跟踪的玩家，这个玩家的聊天都发过来
    AbstractGmForm form;
    public RichConsole console;
    public JTextArea fld;

    String tmp = "";
    JPopupMenu sk;
    public HashMap<String, String> shortCutMap = new HashMap<String, String>();
    public boolean firstShow = true;
    
    public void run() {
    	setVisible(true);
    }
    public ChatRmFld(AbstractGmForm f, int id) {
    	super(f.main, Res.format(UiRES.chatRoomTitle, Integer.valueOf(id)), false);
        this.form = f;
        tracingUserId = id;
        console = new RichConsole(null);
        ArrayList<Message> oldMsgs = f.getExistingMessages();
        onMsg(oldMsgs);
        layoutComponents();
        this.addWindowListener(this);
    }

    public boolean onMsg(ArrayList<Message> ms) {
    	ArrayList<Message> flt = new ArrayList<Message>();
    	for (Message m: ms) {
	    	if (m.isRelated(tracingUserId)) {
	    		flt.add(m.cloneMeOut());
	    	}
    	}
    	if (flt.size() > 0) {
    		console.appendMessages(flt, null);
    		return true;
    	} else {
    		return false;
    	}
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
                    k++;
                    String v = s.substring(k);
                    if (v.length() == 0) {
                        shortCutMap.put(s.substring(0, k), null);
                    } else {
                        shortCutMap.put(s.substring(0, k), v);
                    }
                    return;
                }
            }
            JMenuItem mi = new JMenuItem(s);
            mi.addActionListener(this);
            sk.add(mi);
        }
    }

    public void focusMe() {
        fld.requestFocus();
        firstShow = false;
    }

    private void initCompoents() {
        fld = new JTextArea(2, 80);
        fld.setBackground(Color.BLACK);
        fld.setForeground(Color.GREEN);
        fld.setCaretColor(Color.CYAN);

        fld.addKeyListener(this);
        fld.setText("");
        fld.setLineWrap(false);

        sk = new JPopupMenu();
    }
    private void layoutComponents() {
        initCompoents();
        this.setLayout(new java.awt.BorderLayout(5,5));
        this.add(BorderLayout.SOUTH, fld);
        JScrollPane jsp = new JScrollPane(console);
        this.add(BorderLayout.CENTER, jsp);
    }


    public void fastHelp(String txt, int pos) {
        if (txt.length() == 0) {
//            fireCommand(new String[] {"help"});
            sk.show(this, 0, 0);
        } else {
            if (txt.startsWith("/")) {
                String []ss = pip.util.StringUtil.splitLines(txt.substring(1));
                if (ss.length > 0) {
                    fireCommand(new String[] {"help", ss[0]});
                } else {
                    fireCommand(new String[] {"help"});
                }
            } else {
                sk.show(this, 0, 0);
                // pop up
            }
        }
    }
    ///////////////////////
    public void fireCommand(String []s) {
    	form.getUwapApp().con.processCommand(s);
    }
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == 0 && e.getKeyChar() == 10) {
            String tcmd = fld.getText();
            int k = fld.getCaretPosition();
            boolean esced = false;

            // 去掉插入的回车
            if (k == 0) {
                if (tcmd.length() > 0 && tcmd.charAt(0) == '\n') {
                    tcmd = tcmd.substring(1);
                }
            } else if (k < tcmd.length() && tcmd.charAt(k - 1) == '\n') {
                tcmd = tcmd.substring(0, k-1) + tcmd.substring(k);
            }
            // 处理转义字符
            if (!tcmd.startsWith("/set shortcut ")) {
                int k2 = tcmd.lastIndexOf('\\');
                while (k2 >= 0) {
                    int k3 = tcmd.indexOf(';', k2);
                    int k4 = tcmd.indexOf(' ', k2);
                    if (k4 > k3 || k4 < 0) {
                        k3++;
                        String kk = tcmd.substring(k2, k3);
                        String rep = shortCutMap.get(kk);
                        if (rep != null) {
                            esced = true;
                            tcmd = tcmd.substring(0, k2) + rep +
                                   tcmd.substring(k3);
                            if (k > k3) {
                                k += rep.length() - kk.length();
                            } else if (k > k2) {
                                k = k2 + rep.length();
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
                    fld.setCaretPosition(k);
                } else {
                    fastHelp(cmd, fld.getCaretPosition());
                }
            } else {
                if (cmd.length() > 0) {
                    appendHistory(cmd);
                    String []cmdlines = pip.util.StringUtil.splitLines(cmd);
                    if (cmdlines.length > 0 && cmdlines[0].startsWith("/") && !cmd.startsWith("//")) {
                        cmdlines[0] = cmdlines[0].substring(1);
                        form.onMessage(IMessage.MSG_TYPE_COMMAND, cmd, null);
                        fireCommand(cmdlines);
                    } else {
                    	String ncmds[] = new String[cmdlines.length + 3];
                    	System.arraycopy(cmdlines, 0, ncmds, 3, cmdlines.length);
                    	ncmds[0] = "m";
                    	ncmds[1] = "t";
                    	ncmds[2] = String.valueOf(tracingUserId);
                    	fireCommand(ncmds);
                    }
                }
                fld.setText("");
                tmp = "";
                currentPos = history.size();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.isControlDown()) {
            switch (e.getKeyCode()) {
            case 36:
                if (currentPos == history.size()) {
                    tmp = fld.getText();
                }
                if (currentPos != 0) {
                    currentPos = 0;
                    fld.setText(history.get(currentPos));
                }
                break;
            case 35:
                if (currentPos != history.size()) {
                    currentPos = history.size();
                    fld.setText(tmp);
                }
                break;
            case 38:
                if (currentPos > 0) {
                    if (currentPos >= history.size()) {
                        tmp = fld.getText();
                        currentPos = history.size();
                    }
                    currentPos--;
                    fld.setText(history.get(currentPos));
                }
                break;
            case 40:
                if (currentPos < history.size()) {
                    currentPos++;
                    if (currentPos < history.size()) {
                        fld.setText(history.get(currentPos));
                    } else {
                        fld.setText(tmp);
                    }
                    break;
                }
            }
        }
    }
    public void keyReleased(KeyEvent e) {
    }
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj instanceof JMenuItem) {
            String as = ((JMenuItem)obj).getText();
            insertTxt(as);
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

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e){
    	form.chatDlg.remove(this);
    }

    public void windowClosed(WindowEvent e){}

    public void windowIconified(WindowEvent e){}

    public void windowDeiconified(WindowEvent e){}

    public void windowActivated(WindowEvent e){}

    public void windowDeactivated(WindowEvent e) {
    	
    }
}
