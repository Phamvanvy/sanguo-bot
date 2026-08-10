package pip.gm.cmd;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import pip.gm.fw.AbstractClient;
import pip.gm.fw.Auth;
import pip.gm.fw.AuthConstants;
import pip.gm.fw.Command;
import pip.gm.fw.IMessage;
import pip.util.Res;
import pip.util.StringUtil;
import pip.util.ui.LayoutUtil;
import cwu.util.DebugUtil;
import java.io.*;
/** 定制任务控制。 */
public class CmdSchedule extends Command implements Runnable {
    static Random r = new Random();
	/** 定时任务唯一ID生成器 */
    private int SERNUM = 0;
	/** 当前所有定制任务集合 */
    private Vector<ScheduleTask> scheduledTask = new Vector<ScheduleTask>();
    MyTableModel mdl = new MyTableModel();
    /** 在界面操作中当前选中的任务 */
    ScheduleTask currentSelectedTask;
    /** 当前线程是否正在运行  */
    boolean running = false;
    /** 定时任务处理的线程 */
    Thread runningThread = null;
    /** 定时任务管理界面 */
    JobEditDialog dlg;
    /** 
     * 在当前定制任务列表中扫描到期任务并执行.
     * @return 如果没有定制任务则返回真.处理定时任务的线程将停止。
     */
    private boolean processTasks() {
    	if (scheduledTask.size() == 0) {
    		return true;
    	}
        Calendar c = Calendar.getInstance();
        synchronized(scheduledTask) {
            for (int i = scheduledTask.size() - 1; i >= 0; i--) {
                ScheduleTask t = scheduledTask.get(i);
                if (t.alertTime.before(c)) {
                    String ss[] = t.getCommand();
                    if (ss != null) {
                        t.gmClient.con.processCommand(ss);
                        t.executedTimes++;
                    }
                    t.next();
                    if (t.loopTimes == 0) {
                        scheduledTask.remove(t);
                    }
                    mdl.fireTableDataChanged();
                }
            }
        }
        return false;
    }
    public void run() {
    	running = true;
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
            if (processTasks()) {
            	break;
            }
        }
        running = false;
    }
    public boolean exec(String cmd, AbstractClient client, String []s) throws Exception {
        if (s != null && s.length > 1) {
            if (isCommand(client.auth, cmd)) {
                if ("add".equals(s[1])) {
                	if(s.length > 2) {
	                    ScheduleTask t = new ScheduleTask(client, s, 2);
	                    scheduledTask.add(t);
	                    mdl.fireTableDataChanged();
	                    if (!running) {
	                    	runningThread = new Thread(this);
	                    	runningThread.start();
	                    }
	                    client.onMessage(IMessage.MSG_TYPE_LOG, Res.format(CmdScheduleRES.addTimmer_title_succeed, s[2]), null);
                	} else {
                		client.onMessage(IMessage.MSG_TYPE_LOG, CmdScheduleRES.paraErr, null);
                	}
                } else if ("del".equals(s[1]) && s.length > 2) {
                    synchronized(scheduledTask) {
                        for (int j = 2; j < s.length; j++) {
                            int k = Integer.parseInt(s[j]);
                            boolean done = false;
                            for (int i = scheduledTask.size() - 1; i >= 0; i--) {
                                ScheduleTask t = scheduledTask.get(i);
                                if (t.id == k) {
                                    scheduledTask.remove(t);
                                    client.onMessage(IMessage.MSG_TYPE_LOG, Res.format(CmdScheduleRES.delTimmer_title_succeed, s[2]), null);
                                    done = true;
                                    break;
                                }
                            }
                            if (!done) {
                            	client.onMessage(IMessage.MSG_TYPE_LOG, Res.format(CmdScheduleRES.delErr_id, k), null);
                            } else {
                            	mdl.fireTableDataChanged();
                            }
                        }
                    }
                } else if ("ui".equals(s[1])) {
                	if (dlg == null) {
                		dlg = new JobEditDialog(client);
                	}
                	if (dlg.isVisible()) {
                		dlg.toFront();
                	} else {
                		new Thread() {
                			public void run() {
                        		dlg.setVisible(true);
                			}
                		}.start();
                	}
                } else if ("lst".equals(s[1])) {
                    StringBuffer buf = new StringBuffer();
                    for (int i = scheduledTask.size() - 1; i >= 0; i--) {
                        buf.append((scheduledTask.get(i)).getInfo());
                        buf.append("\n");
                    }
                    client.onMessage(IMessage.MSG_TYPE_LOG, buf.toString(), null);
                } else {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    public class JobEditDialog extends JDialog {
    	AbstractClient client;
    	LayoutUtil uiutil = new LayoutUtil();

    	JobTable jtb;
    	JRadioButton rawCommand;
    	JTextField tfCommand;
    	JRadioButton broadcastCmd;
    	JTextField tfJobName;
    	JTextField tfJobContent;
//    	JCheckBox cbLoop;
    	JRadioButton rbLoopEver;
    	JRadioButton rbLoopByTimes;
    	JRadioButton rbLoopUnitl;
    	TimeSelect startTime;
    	TimeSelect endTime;
    	TimeSelect deltaTime;
    	JTextField tfLoopTime;
    	JPopupMenu taskPop;
    	JButton popBun;
    	public JobEditDialog(AbstractClient client) {
    		super(client.getUiContainer());
    		this.client = client;
    		initControls();
    		Window w = client.getUiContainer();
    		setTitle(CmdScheduleRES.dialogTitle);
    		setModal(true);
    		add(BorderLayout.SOUTH, getControlPane());
    		add(BorderLayout.CENTER, getListPane());
    		setSize(800, 600);
    		if (w != null) {
        		setLocation(w.getX() + ((w.getWidth() - getWidth()) >> 1), w.getY() + ((w.getHeight() - getHeight()) >> 2));
    		}
    	}
    	/** 初始化控件 */
    	private void initControls() {
    		jtb = new JobTable();
        	rbLoopEver = new JRadioButton(CmdScheduleRES.btnLoop);
        	
        	rbLoopByTimes = new JRadioButton(CmdScheduleRES.btnLoopTime);
        	rbLoopUnitl = new JRadioButton(CmdScheduleRES.tillTime);
        	startTime = new TimeSelect(CmdScheduleRES.startTime, true);
        	endTime = new TimeSelect(CmdScheduleRES.endTime, true);
        	deltaTime = new TimeSelect(CmdScheduleRES.deltaTime, false);
        	tfLoopTime = new JTextField("50", 4);
    		tfJobName = new JTextField(20);
    		taskPop = new JPopupMenu();
    		ActionListener listener = new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				tfJobContent.setText(((JMenuItem)e.getSource()).getText());
    			}
    		};
    		File f = new File(System.getProperty("user.home"));
    		f  = new File(f, "broadcast.txt");
    		if (f.exists()) {
    			LineNumberReader reader = null;
				try {
					reader = new LineNumberReader(new FileReader(f));
					while (true) {
						String s = reader.readLine();
						if (s == null) {
							break;
						}
						if (s.length() > 0 && !s.startsWith("#")) {
							JMenuItem mi = new JMenuItem(s);
							mi.addActionListener(listener);
							taskPop.add(mi);
						}
					}
				} catch (Exception e1) {
				}
				if (reader != null) {
	    			try {
						reader.close();
					} catch (IOException e1) {
					}
				}
    		}
    		tfJobContent = new JTextField(40);
    		
    		rawCommand = new JRadioButton(CmdScheduleRES.rawCommand);
        	if (!client.auth.hasAuth(AuthConstants.root)) {
        		rawCommand.setEnabled(false);
        	}

    		rawCommand.setToolTipText(CmdScheduleRES.rawCmdTip);
    		broadcastCmd = new JRadioButton(CmdScheduleRES.broadcast);
    		broadcastCmd.setToolTipText(CmdScheduleRES.broadcastTip);
    		// 广播和命令互斥
    		ButtonGroup bg = new ButtonGroup();
    		bg.add(rawCommand);
    		bg.add(broadcastCmd);
    		broadcastCmd.setSelected(true);
    		 tfCommand = new JTextField(20);
    		tfCommand.setEnabled(false);
    		ActionListener cmdEnableListener = new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				tfCommand.setEnabled(rawCommand.isSelected());
    			}
    		}; 
    		rawCommand.addActionListener(cmdEnableListener);
    		broadcastCmd.addActionListener(cmdEnableListener);
    		endTime.setEnabled(false);
    		tfLoopTime.setEnabled(false);
    		
    		// 不同循环模式互斥
    		bg = new ButtonGroup();
    		bg.add(rbLoopByTimes);
    		bg.add(rbLoopEver);
    		bg.add(rbLoopUnitl);
    		rbLoopByTimes.setEnabled(true);
    		rbLoopByTimes.setSelected(true);
    		if (!client.auth.hasAuth(AuthConstants.root)) {
        		rbLoopEver.setEnabled(false);
        	}
			rbLoopUnitl.setEnabled(true);
			deltaTime.setEnabled(true);
			endTime.setEnabled(false);
			tfLoopTime.setEnabled(true);
			
//			cbLoop = new JCheckBox("重复执行");
//			cbLoop.setSelected(true);
			ActionListener loopEnableListener = new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
//    				if (cbLoop.isSelected()) {
//    					rbLoopByTimes.setEnabled(true);
//    					if (!client.auth.hasAuth(AuthConstants.root)) {
//    		        		rbLoopEver.setEnabled(false);
//    		        	}
//    					rbLoopUnitl.setEnabled(true);
//    					deltaTime.setEnabled(true);
    					endTime.setEnabled(rbLoopUnitl.isSelected());
    					tfLoopTime.setEnabled(rbLoopByTimes.isSelected());
//    				} else {
//    					endTime.setEnabled(false);
//    					deltaTime.setEnabled(false);
//    					rbLoopByTimes.setEnabled(false);
//    					rbLoopEver.setEnabled(false);
//    					rbLoopUnitl.setEnabled(false);
//    					tfLoopTime.setEnabled(false);
//    				}
    			}
    		}; 
//    		cbLoop.addActionListener(loopEnableListener);
    		rbLoopByTimes.addActionListener(loopEnableListener);
    		rbLoopEver.addActionListener(loopEnableListener);
    		rbLoopUnitl.addActionListener(loopEnableListener);
    	}
        private JPanel getControlPane() {
        	JPanel p = new JPanel(new GridBagLayout());
        	int row = 0;
        	JLabel lbl = new JLabel(CmdScheduleRES.lblTaskName);
        	lbl.setToolTipText(CmdScheduleRES.taskNameTip);
        	p.add(uiutil.getRightAlignComponent(lbl), uiutil.getConstrains(0, row, 1, 1));
        	p.add(tfJobName, uiutil.getConstrains(1, row, 2, 1));
        	p.add(uiutil.getRightAlignComponent(broadcastCmd), uiutil.getConstrains(3, row, 1, 1));
        	JPanel subP = new JPanel(new BorderLayout());
        	subP.add(BorderLayout.WEST, rawCommand);
        	subP.add(BorderLayout.CENTER, tfCommand);
        	p.add(subP, uiutil.getConstrains(4, row, 2, 1));
        	row++;
        	popBun = new JButton(CmdScheduleRES.broadcastContent);
        	popBun.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			taskPop.show(popBun, 1, 1);
        		}
        	});
        	popBun.setToolTipText(CmdScheduleRES.contentTip);
        	p.add(uiutil.getRightAlignComponent(popBun), uiutil.getConstrains(0, row, 1, 1));
        	p.add(tfJobContent, uiutil.getConstrains(1, row, 5, 1));
        	row++;

//        	p.add(uiutil.getRightAlignComponent(cbLoop), uiutil.getConstrains(0, row, 1, 1));
        	JPanel pp = new JPanel(new GridLayout(1, 3));
        	pp.add(rbLoopEver);
        	pp.add(rbLoopByTimes);
        	pp.add(rbLoopUnitl);
        	pp.setBorder(new TitledBorder(CmdScheduleRES.loopMode));
        	p.add(pp, uiutil.getConstrains(0, row, 6, 1));
        	row++;
        	pp = new JPanel(new GridBagLayout());
        	pp.add(deltaTime, uiutil.getConstrains(0, 0, 2, 1));
        	JPanel titleP = new JPanel();
        	titleP.add(tfLoopTime);
        	titleP.setBorder(new TitledBorder(CmdScheduleRES.loopTimes));
        	pp.add(titleP, uiutil.getConstrains(2, 0, 1, 1));
        	pp.add(endTime, uiutil.getConstrains(3, 0, 3, 1));
        	p.add(pp, uiutil.getConstrains(0, row, 6, 1));

        	row++;
        	p.add(startTime, uiutil.getConstrains(0, row, 6, 1));
        	row++;
        	JButton btnQuit = new JButton(CmdScheduleRES.closeWin);
        	btnQuit.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			JobEditDialog.this.setVisible(false);
        		}
        	});
        	JButton btnNew = new JButton(CmdScheduleRES.addTask);
        	btnNew.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			ArrayList<String> cmds = new ArrayList<String>();
        			cmds.add(CmdScheduleRES.cmdCommand);
        			cmds.add("add");
        			cmds.add(tfJobName.getText());
        			cmds.add(startTime.getTimeString());
//        			if (cbLoop.isSelected()) {
        				if (rbLoopEver.isSelected()) {
        					cmds.add(deltaTime.getTimeString());	
        				} else if (rbLoopByTimes.isSelected()) {
        					cmds.add(deltaTime.getTimeString() + "*" + tfLoopTime.getText().trim());
        				} else {
        					cmds.add(deltaTime.getTimeString() + "-" + endTime.getTimeString());	
        				}
//        			} else {
//        				cmds.add("-");
//        			}
        			if (broadcastCmd.isSelected()) {
        				String s = tfJobContent.getText();
        				if (s.length() > 0) {
	        				cmds.add("m");
	        				cmds.add("s");
	        				cmds.add(s);
        				} else {
        					// TODO 提示
        					return;
        				}
        			} else {
        				String ss = tfCommand.getText().trim();
        				if (ss.length() == 0) {
        					// TODO 提示
        					return;
        				}
        				for (String s : ss.split(" ")) {
        					cmds.add(s);
        				}
        				ss = tfJobContent.getText();
        				if (ss.length() > 0) {
        					cmds.add(ss);
        				}
        			}
        			String cs[] = new String[cmds.size()];
        			cmds.toArray(cs);
        			client.con.processCommand(cs);
        			tfJobName.setText("");
        			tfJobContent.setText("");
        		}
        	});
        	pp = new JPanel(new FlowLayout());
        	pp.add(btnQuit);
        	pp.add(btnNew);
        	p.add(pp, uiutil.getConstrains(0, row, 6, 1));
        	
        	p.setBorder(new TitledBorder(CmdScheduleRES.taskProperty));
        	return p;
        }
        /** 时间选择控件 */
        class TimeSelect extends JPanel {
        	JComboBox sHour;
        	JComboBox sMinute;
        	JComboBox sSecond;
        	JComboBox jcb;
        	/** 内部日期数据,为显示日期用 */
        	class MyDate {
        		long date;
        		public MyDate() {
        			date = System.currentTimeMillis();
        		}
        		public String toString() {
        			return StringUtil.getDateFormat(date, CmdScheduleRES.mdFormat);
        		}
        	}
        	public void setEnabled(boolean t) {
        		if (jcb != null) {
        			jcb.setEnabled(t);
        		}
        		sHour.setEnabled(t);
        		sMinute.setEnabled(t);
        		if (sSecond != null) {
        			sSecond.setEnabled(t);
        		}
        	}
        	/** 取得命令行支持的时间格式 */
        	public String getTimeString() {
        		StringBuilder sb = new StringBuilder();
        		if (jcb != null) {
        			sb.append(StringUtil.getDateFormat(((MyDate)jcb.getSelectedItem()).date, "yyyy/M/d/"));
        		}	
        		sb.append(sHour.getSelectedIndex());
        		sb.append("/");
        		sb.append(sMinute.getSelectedIndex() * 60 / sMinute.getItemCount());
        		sb.append("/");
        		sb.append(sSecond == null ? "0" : sSecond.getSelectedIndex());
        		return sb.toString();
        	}
        	
        	public TimeSelect(String title, boolean includeDate) {
        		super(new FlowLayout());
        		if (title != null) {
        			setBorder(new TitledBorder(title));
        		}
        		Integer hours[] = new Integer[48];
        		for (int i = 0; i < hours.length; i++) {
        			hours[i] = Integer.valueOf(i);
        		}
        		sHour = new JComboBox(hours);
        		Integer minuts[] ;
        		if (client.auth.hasAuth(AuthConstants.root)) {
        			minuts = new Integer[60];
	        		for (int i = 0; i < 60; i++) {
	        			minuts[i] = Integer.valueOf(i);
	        		}
        		} else {
        			minuts = new Integer[12];
	        		for (int i = 0; i < 12; i++) {
	        			minuts[i] = Integer.valueOf(i*5);
	        		}
        		}
        		sMinute = new JComboBox(minuts); 
        		Integer seconds[] = new Integer[60];
        		for (int i = 0; i < 60; i++) {
        			seconds[i] = Integer.valueOf(i);
        		}
        		Calendar calendar = Calendar.getInstance();
        		if (includeDate) {
        			MyDate mcalendar[] = new MyDate[7];
        			for (int i = 0; i < 7; i++) {
        				mcalendar[i] = new MyDate();
        				mcalendar[i].date += 1000 * 60 * 60 * 24 * i;
        			}
        			jcb = new JComboBox(mcalendar);
        			add(jcb);
        			if (client.auth.hasAuth(AuthConstants.root)) {
        				sSecond = new JComboBox(seconds); 
        				sSecond.setSelectedIndex(0);
        			}
        			sMinute.setSelectedIndex(((calendar.get(Calendar.MINUTE)+4) % 60) * sMinute.getItemCount() / 60);
        			sHour.setSelectedIndex(calendar.get(Calendar.HOUR_OF_DAY));
        		} else if (client.auth.hasAuth(AuthConstants.root)) {
        			sMinute.setSelectedIndex(2);
        			sSecond = new JComboBox(seconds); 
        		} else {
        			sMinute.setSelectedIndex(2);
        		}
        		add(sHour);
        		add(new JLabel(includeDate ? "点" : "小时"));
        		add(sMinute);
        		add(new JLabel(includeDate ? "分": "分钟"));
        		if (client.auth.hasAuth(AuthConstants.root)) {
        			add(sSecond);
        			add(new JLabel("秒"));
        		}
        	}
        }
        /** 任务列表 */
        private JPanel getListPane() {
        	JPanel p = new JPanel(new BorderLayout());
        	jtb.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        	jtb.setAutoCreateRowSorter(true);
        	p.add(BorderLayout.CENTER, new JScrollPane(jtb));
        	p.setBorder(new TitledBorder(CmdScheduleRES.taskList));
        	return p;
        }
    }

    public long getAuth() {
    	return 0;
    }
    public String getCommand(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return CmdScheduleRES.cmdCommand;
    	}
    	return null;
    }
    public String getName(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return CmdScheduleRES.cmdName;
    	}
    	return null;
    }
    public String getDescription(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return CmdScheduleRES.cmdDescription;
    	} 
    	return null;
    }
    
    public class ScheduleTask {
    	public AbstractClient gmClient;
        public Calendar alertTime;
        public int loopTimes;
        public long loopDeltaTime;
        public String [][]commands;
        public int chances[];
        public int id;
        public String name;
        public int executedTimes = 0;
        /** 用来展示任务的内容 */
        public String firstCommand;

        public String getInfo() {
            return "" + id + ". " + name + " " + DebugUtil.getDate(alertTime.getTime()) +
                    " x " + loopTimes;
        }
        public String getLoopInfo() {
        	String s = StringUtil.getDurationString(loopDeltaTime);
        	StringBuilder buf = new StringBuilder();
        	if (loopTimes == 1) {
        		buf.append(CmdScheduleRES.once);
        	} else {
        		buf.append(Res.format(CmdScheduleRES.deltaTime_0, s));
            	if (loopTimes < 0) {
            		buf.append(CmdScheduleRES.forever);
            	} else {
            		buf.append(Res.format(CmdScheduleRES.execTimes_0, loopTimes));
            	}
        	}
    		return buf.toString();
        }
        public String[] getCommand() {
            if (commands != null && commands.length > 0) {
                return commands[r.nextInt(commands.length)];
            }
            return null;
        }
        public ScheduleTask(AbstractClient client, String s[], int pos) throws Exception {
        	gmClient = client;
            this.id = SERNUM++;
            name = s[pos++];
            alertTime = parseToDate(s[pos++]);

            setRepeat(s[pos++]);
            ArrayList<ArrayList<String>> lst = new ArrayList<ArrayList<String>>();
            ArrayList<String> subLst = new ArrayList<String>();
            StringBuffer buf = new StringBuffer();
            while (pos < s.length) {
                if (s[pos].equals("|")) {
                	if (firstCommand == null && buf.length() > 0) {
                		firstCommand = buf.toString();
                	}
                    if (subLst.size() > 0) {
                        lst.add(subLst);
                        subLst = new ArrayList<String>();
                    }
                } else {
                	if (firstCommand == null) {
                		buf.append(s[pos]);
                		buf.append(" ");
                	}
                    subLst.add(s[pos]);
                }
                pos++;
            }
            if (subLst.size() > 0) {
                lst.add(subLst);
                if (firstCommand == null && buf.length() > 0) {
            		firstCommand = buf.toString();
            	}
            }
            commands = new String[lst.size()][];
            for (int i = commands.length - 1; i >= 0; i--) {
                subLst = lst.get(i);
                commands[i] = new String[subLst.size()];
                for (int j = commands[i].length - 1; j >= 0; j--) {
                    commands[i][j] = subLst.get(j);
                }
            }
            if (firstCommand == null) {
            	throw new Exception("WrongParam");
            } else if (firstCommand.startsWith("m b ")) {
            	firstCommand = Res.format(CmdScheduleRES.brocast_content, firstCommand.substring(4));
            }
        }
        public void next() {
            if (loopTimes > 0) {
                loopTimes--;
                alertTime.add(Calendar.MILLISECOND, (int)loopDeltaTime);
                Calendar currentTime = Calendar.getInstance();
                if (alertTime.before(currentTime)) {
                	alertTime = currentTime;
                }
            }
        }

        public void setRepeat(String s) {
            if (s.startsWith("-")) {
                loopTimes = 1;
                return;
            }
            int k = s.indexOf("-");
            if (k > 0) {
                loopDeltaTime = parseToDuration(s.substring(0, k));
                Calendar endTime = parseToDate(s.substring(k + 1));
                long k1 = endTime.getTimeInMillis();
                long k2 = alertTime.getTimeInMillis();
                loopTimes = (int) ((k1 - k2) / loopDeltaTime);
                if (loopTimes < 1) {
                    loopTimes = 1;
                }
                return;
            }
            k = s.indexOf("*");
            if (k > 0) {
                loopDeltaTime = parseToDuration(s.substring(0, k));
                loopTimes = Integer.parseInt(s.substring(k + 1));
                if (loopTimes < 1) {
                    loopTimes = 1;
                }
                return;
            }
            loopDeltaTime = parseToDuration(s);
            loopTimes = -1;
        }
        public long parseToDuration(String dStr) {
            String[] s = dStr.split("/");
            int i = s.length - 1;
            long ret = 0;
            if (i >= 0) {
                try {
                    ret = Long.parseLong(s[i]) * 1000L;
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    ret += Long.parseLong(s[i]) * 1000L * 60L;
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    ret += Long.parseLong(s[i]) * 1000L * 60L * 60L;
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    ret += Long.parseLong(s[i]) * 1000L * 60L * 60L * 24L;
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            return ret;
        }
        public Calendar parseToDate(String dStr) {
            Calendar c = Calendar.getInstance();
            if (dStr.equals("-")) {
                return c;
            }
            String[] s = dStr.split("/");
            int i = s.length - 1;
            if (i >= 0) {
                try {
                    c.set(Calendar.SECOND, Integer.parseInt(s[i]));
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    c.set(Calendar.MINUTE, Integer.parseInt(s[i]));
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s[i]));
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s[i]));
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    c.set(Calendar.MONTH, Integer.parseInt(s[i]) - 1);
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            if (i >= 0) {
                try {
                    c.set(Calendar.YEAR, Integer.parseInt(s[i]));
                } catch (NumberFormatException ex) {
                }
                i--;
            }
            return c;
        }
    }
	static final int widthOfCols[][] = {
		{20, 40, 80}, // 标题	
		{40, 80, 600}, // 内容	
		{40, 80, 160}, // 执行时间	
		{20, 40, 160}, // 重复条件
		{10, 20, 60}, // 执行次数	
	};
    public class JobTable extends JTable {
		JPopupMenu pop;

    	public JobTable() {
    		super(mdl);
    		// 设置表格的列宽
    		TableColumnModel tcm = getColumnModel();
    		for (int i = widthOfCols.length; i--> 0; ) {
        		TableColumn clm = tcm.getColumn(i);
        		if (widthOfCols[i][0] > 0) {
        			clm.setMinWidth(widthOfCols[i][0]);
        		}
        		if (widthOfCols[i][1] > 0) {
        			clm.setPreferredWidth(widthOfCols[i][1]);
        		}
        		if (widthOfCols[i][2] > 0) {
        			clm.setMaxWidth(widthOfCols[i][2]);
        		}
    		}
    		pop = new JPopupMenu();
    		JMenuItem mi = new JMenuItem(CmdScheduleRES.delTask);
    		pop.add(mi);
    		mi.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent e) {
    					ScheduleTask t = currentSelectedTask;
    					pop.setVisible(false);
    					if (t != null) {
    						dlg.client.con.processCommand(new String[]{"job", "del", String.valueOf(t.id)});
    					}
    				}
    		});

    		addMouseListener(new MouseListener() {
    			public void mouseClicked(MouseEvent e) {}
    			public void mousePressed(MouseEvent e) {
    				if (e.getButton() == MouseEvent.BUTTON3) {
    					java.awt.Point p = e.getPoint();
    			        int rowIndex = rowAtPoint(p);
    			        if (rowIndex >= 0 && rowIndex <scheduledTask.size()) {
    			        	rowIndex = convertRowIndexToModel(rowIndex);
    			        	currentSelectedTask = scheduledTask.get(rowIndex);
    						pop.show(JobTable.this, e.getX(), e.getY());
    			        }
    				}
    			}
    			public void mouseReleased(MouseEvent e) {}
    			public void mouseEntered(MouseEvent e) {}
    			public void mouseExited(MouseEvent e) {}
    		});

    	}
    	public boolean isPaintingTitle() {
    		return true;
    	}
    }
    public class MyTableModel extends AbstractTableModel {
		String columnNames[] = {CmdScheduleRES.title, CmdScheduleRES.content, 
				CmdScheduleRES.nextTime, CmdScheduleRES.repetCondition, CmdScheduleRES.doneFlag};
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
	    public int getRowCount() { 
	    	return scheduledTask.size(); 
	    }
	    public int getColumnCount() { 
	    	return columnNames.length; 
	    }
	    public Object getValueAt(int row, int col) {
	    	if (row < scheduledTask.size()) {
	    		ScheduleTask t =scheduledTask.get(row);
	    		if (t != null) {
			    	switch (col) {
			    	case 0:
			    		return t.name;
			    	case 1:
			    		return t.firstCommand;
			    	case 2:
			    		return StringUtil.getDateFormat(t.alertTime.getTimeInMillis(), CmdScheduleRES.dateFormatStr);
			    	case 3:
			    		return t.getLoopInfo();
			    	case 4:
			    		if (t.executedTimes > 0) {
			    			return t.executedTimes;
			    		}
			    		return "";
			    	}
	    		}
	    	}
	        return null;
	    }
	    public boolean isCellEditable(int row, int col) { 
	    	return false; 
	    }
	    public void setValueAt(Object value, int row, int col) {
	    	scheduledTask.setElementAt((ScheduleTask)value, row);
	        fireTableCellUpdated(row, col);
	    } 
	}
}
