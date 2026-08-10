package pip.util.ui;

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
import java.util.HashMap;
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

import pip.gm.ServerConfig;
import pip.gm.fw.AbstractGmForm;
import pip.gm.fw.Auth;
import pip.gm.fw.Broadcastable;
import pip.gm.fw.ReceiptListener;
import pip.io.uwap.UWapData;
import pip.util.Res;
import pip.util.StringUtil;
import cwu.util.DebugUtil;
/**
 * GM 通用广播界面，用以实现跨游戏、跨区广播。
 * 支持广播的游戏项目需要在其 GmForm 上实现 Broadcastable 接口以帮助通用模块构建广播协议包。
 * GmForm所构建的广播协议(UWapData)需要实现 Receiptable 接口，用来确认广播是否正确发送。
 */
public class BroadCastDialog extends JDialog implements Runnable, ReceiptListener {
	// 通用广播发出的包序列号
	private static int SERIAL_NUM = 2; 
	/** 当前所有定制任务集合 */
    private Vector<ScheduleBroadcastTask> scheduledBroadcastTask = new Vector<ScheduleBroadcastTask>();
    private MyTableModel mdl = new MyTableModel();
    /** 在界面操作中当前选中的任务 */
    private ScheduleBroadcastTask currentSelectedTask;
    /** 当前线程是否正在运行  */
    boolean running = false;
    /** 定时任务处理的线程 */
    Thread runningThread = null;
    /** 发出的广播包的状态侦听序列 */
    private HashMap<Integer,MsgStatus> feedbacks = new HashMap<Integer,MsgStatus>();
    // 构建UI所用的辅助工具
	private LayoutUtil uiutil = new LayoutUtil();

	// 下面是UI部件
	JobTable jtb;
	// 任务名称
	JTextField tfJobName;
	// 广播内容
	JTextField tfJobContent;
	JRadioButton rbLoopEver;
	JRadioButton rbLoopByTimes;
	JRadioButton rbLoopUnitl;
	TimeSelect startTime;
	TimeSelect endTime;
	TimeSelect deltaTime;
	JTextField tfLoopTime;
	JPopupMenu taskPop;
	JButton popBun;
	ServerConfig configRoot;
	JPanel listServerPanel;
	
	// 支持广播的所有已经打开的区
	private ArrayList<ServerConfig> serverList;
	// 对应广播游戏分区的勾选项
	private ArrayList<JCheckBox> serverCheckBox;

	/**
	 * ReceiptListener 接口。当收到回执后将成功发送状态赋值。
	 */
    public void onReceipt(UWapData original, UWapData receipt) {
    	int ser = receipt.getProtocolSerialNumber();
    	MsgStatus stat = feedbacks.get(ser);
    	if (stat != null) {
    		feedbacks.remove(ser);
    		stat.succeedTimes++;
        	mdl.fireTableDataChanged();
    	}
    }
    
    /** 
     * 在当前定制任务列表中扫描到期任务并执行.
     * @return 如果没有定制任务则返回真.处理定时任务的线程将停止。
     */
    private boolean processTasks() {
        Calendar c = Calendar.getInstance();
        synchronized(scheduledBroadcastTask) {
            for (int i = scheduledBroadcastTask.size() - 1; i >= 0; i--) {
                ScheduleBroadcastTask t = scheduledBroadcastTask.get(i);
                if (t.alertTime.before(c)) {
                	for (ServerConfig cfg : t.configs.keySet()) {
                		MsgStatus status = t.configs.get(cfg);
                		if (!status.stop) {
                			AbstractGmForm form = cfg.ins;
                			if (form instanceof Broadcastable) {
                				 UWapData pkg = ((Broadcastable)form).genBroadCastInfo(t.message, this);
                				 pkg.setProtocolSerialNumber(SERIAL_NUM++);
                				 status.sendTimes++;
                				 form.getUwapApp().sndRequest(pkg);
                				 feedbacks.put(pkg.getProtocolSerialNumber(), status);
                			}
                		}
                	}
                    t.next();
                    if (t.loopTimes == 0) {
                        scheduledBroadcastTask.remove(t);
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

	public BroadCastDialog(Window w, ServerConfig configs) {
		super(w);
		this.configRoot = configs;
		initControls();
		setTitle(BroadCastDialogRES.dialogTitle);
		setModal(true);
		add(BorderLayout.SOUTH, getControlPane());
		add(BorderLayout.CENTER, getListPane());
		setSize(800, 600);
		if (w != null) {
    		setLocation(w.getX() + ((w.getWidth() - getWidth()) >> 1), w.getY() + ((w.getHeight() - getHeight()) >> 2));
		}
		updateServerList();
		new Thread(this).start();
	}
	/** 
	 * 更新支持广播的服务器列表。
	 */
	public void updateServerList() {
		serverList = new ArrayList<ServerConfig>();
		addConfig(configRoot);
		listServerPanel.removeAll();
		listServerPanel.setLayout(new GridBagLayout());
		int n = serverList.size();
		serverCheckBox = new ArrayList<JCheckBox>();
		for (int i = 0; i < n; i++) {
			JCheckBox cb = new JCheckBox(serverList.get(i).name);
			serverCheckBox.add(cb);
			listServerPanel.add(cb, uiutil.getConstrains(0, i, 1, 1));
		}
	}
	// 递归方式加入所有符合条件的游戏分区（已经打开并支持广播的游戏）
	private void addConfig(ServerConfig configs) {
		Vector<ServerConfig> cfgs = configs.getSubConfigs();
		if (cfgs == null) {
			if (configs.ins != null) {
				if (configs.ins instanceof Broadcastable) {
					serverList.add(configs);
				}
			}
		} else {
			for (ServerConfig sc : cfgs) {
				addConfig(sc);
			}
		}
	}
	/** 初始化控件 */
	private void initControls() {
		jtb = new JobTable();
    	rbLoopEver = new JRadioButton(BroadCastDialogRES.btnLoop);
    	
    	rbLoopByTimes = new JRadioButton(BroadCastDialogRES.btnLoopTime);
    	rbLoopUnitl = new JRadioButton(BroadCastDialogRES.tillTime);
    	startTime = new TimeSelect(BroadCastDialogRES.startTime, true);
    	endTime = new TimeSelect(BroadCastDialogRES.endTime, true);
    	deltaTime = new TimeSelect(BroadCastDialogRES.deltaTime, false);
    	tfLoopTime = new JTextField("50", 4);
		tfJobName = new JTextField(20);
		taskPop = new JPopupMenu();
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfJobContent.setText(((JMenuItem)e.getSource()).getText());
			}
		};
		tfJobContent = new JTextField(40);
		
		endTime.setEnabled(false);
		tfLoopTime.setEnabled(false);
		
		// 不同循环模式互斥
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbLoopByTimes);
		bg.add(rbLoopEver);
		bg.add(rbLoopUnitl);
		rbLoopByTimes.setEnabled(true);
		rbLoopByTimes.setSelected(true);
		rbLoopUnitl.setEnabled(true);
		deltaTime.setEnabled(true);
		endTime.setEnabled(false);
		tfLoopTime.setEnabled(true);
		
		ActionListener loopEnableListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				endTime.setEnabled(rbLoopUnitl.isSelected());
				tfLoopTime.setEnabled(rbLoopByTimes.isSelected());
			}
		}; 
		rbLoopByTimes.addActionListener(loopEnableListener);
		rbLoopEver.addActionListener(loopEnableListener);
		rbLoopUnitl.addActionListener(loopEnableListener);
	}
	private JPanel getControlPane() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.CENTER, getControlPaneLeft());
		p.add(BorderLayout.EAST, getControlPaneRight());
		return p;
	}
    private JPanel getControlPaneRight() {
    	listServerPanel = new JPanel();
    	JPanel p = new JPanel(new BorderLayout());
    	p.add(BorderLayout.NORTH, new JLabel("游戏服务器列表"));
    	p.add(BorderLayout.CENTER, new JScrollPane(listServerPanel));
    	return p;
    }
    private JPanel getControlPaneLeft() {
    	JPanel p = new JPanel(new GridBagLayout());
    	int row = 0;
    	JLabel lbl = new JLabel(BroadCastDialogRES.lblTaskName);
    	lbl.setToolTipText(BroadCastDialogRES.taskNameTip);
    	p.add(uiutil.getRightAlignComponent(lbl), uiutil.getConstrains(0, row, 1, 1));
    	p.add(tfJobName, uiutil.getConstrains(1, row, 2, 1));
    	row++;
    	popBun = new JButton(BroadCastDialogRES.broadcastContent);
    	popBun.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			taskPop.show(popBun, 1, 1);
    		}
    	});
//    	popBun.setToolTipText(BroadCastDialogRES.contentTip);
    	p.add(uiutil.getRightAlignComponent(popBun), uiutil.getConstrains(0, row, 1, 1));
    	p.add(tfJobContent, uiutil.getConstrains(1, row, 5, 1));
    	row++;

    	JPanel pp = new JPanel(new GridLayout(1, 3));
    	pp.add(rbLoopEver);
    	pp.add(rbLoopByTimes);
    	pp.add(rbLoopUnitl);
    	pp.setBorder(new TitledBorder(BroadCastDialogRES.loopMode));
    	p.add(pp, uiutil.getConstrains(0, row, 6, 1));
    	row++;
    	pp = new JPanel(new GridBagLayout());
    	pp.add(deltaTime, uiutil.getConstrains(0, 0, 2, 1));
    	JPanel titleP = new JPanel();
    	titleP.add(tfLoopTime);
    	titleP.setBorder(new TitledBorder(BroadCastDialogRES.loopTimes));
    	pp.add(titleP, uiutil.getConstrains(2, 0, 1, 1));
    	pp.add(endTime, uiutil.getConstrains(3, 0, 3, 1));
    	p.add(pp, uiutil.getConstrains(0, row, 6, 1));

    	row++;
    	p.add(startTime, uiutil.getConstrains(0, row, 6, 1));
    	row++;
    	JButton btnQuit = new JButton(BroadCastDialogRES.closeWin);
    	btnQuit.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			BroadCastDialog.this.setVisible(false);
    		}
    	});
    	JButton btnNew = new JButton(BroadCastDialogRES.addTask);
    	btnNew.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			ArrayList<ServerConfig> servers = new ArrayList<ServerConfig>();
    			for (int i = 0; i < serverCheckBox.size(); i++) {
    				JCheckBox box = serverCheckBox.get(i);
    				if (box.isSelected()) {
    					servers.add(serverList.get(i));
    				}
    			}
    			if (servers.size() > 0) {
	    			String repeat = null;
					if (rbLoopEver.isSelected()) {
						repeat = deltaTime.getTimeString();	
					} else if (rbLoopByTimes.isSelected()) {
						repeat = deltaTime.getTimeString() + "*" + tfLoopTime.getText().trim();
					} else {
						repeat = deltaTime.getTimeString() + "-" + endTime.getTimeString();	
					}
	    			ScheduleBroadcastTask tsk = new ScheduleBroadcastTask(tfJobName.getText(), startTime.getTimeString(), repeat, tfJobContent.getText(), servers);
	    			synchronized (scheduledBroadcastTask) {
	    				scheduledBroadcastTask.add(tsk);
	    				mdl.fireTableDataChanged();
	    			}
	    			tfJobName.setText("");
	    			tfJobContent.setText("");
    			}
    		}
    	});
    	pp = new JPanel(new FlowLayout());
    	pp.add(btnQuit);
    	pp.add(btnNew);
    	p.add(pp, uiutil.getConstrains(0, row, 6, 1));
    	
    	p.setBorder(new TitledBorder(BroadCastDialogRES.taskProperty));
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
    			return StringUtil.getDateFormat(date, BroadCastDialogRES.mdFormat);
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
    		minuts = new Integer[12];
    		for (int i = 0; i < 12; i++) {
    			minuts[i] = Integer.valueOf(i*5);
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
    			sMinute.setSelectedIndex(((calendar.get(Calendar.MINUTE)+4) % 60) * sMinute.getItemCount() / 60);
    			sHour.setSelectedIndex(calendar.get(Calendar.HOUR_OF_DAY));
    		} else {
    			sMinute.setSelectedIndex(2);
    		}
    		add(sHour);
    		add(new JLabel(includeDate ? "点" : "小时"));
    		add(sMinute);
    		add(new JLabel(includeDate ? "分": "分钟"));
    	}
    }
    /** 任务列表 */
    private JPanel getListPane() {
    	JPanel p = new JPanel(new BorderLayout());
    	jtb.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
    	jtb.setAutoCreateRowSorter(true);
    	p.add(BorderLayout.CENTER, new JScrollPane(jtb));
    	p.setBorder(new TitledBorder(BroadCastDialogRES.taskList));
    	return p;
    }

    static final int widthOfCols[][] = {
		{20, 40, 80}, // 标题	
		{40, 80, 600}, // 内容	
		{40, 80, 400}, // 执行时间	
		{20, 40, 200}, // 重复条件
		{10, 20, 800}, // 执行次数	
	};
    /** 
     * 某游戏分区的广播消息状态.
     */
    public static class MsgStatus {
    	/** 总执行次数 */
    	public int sendTimes;
    	/** 收到回执（正确执行）次数 */
    	public int succeedTimes;
    	/** 是否暂停？ */
    	public boolean stop;
    }
    /**
     * 定时执行广播消息
     */
    public static class ScheduleBroadcastTask {
    	/** 定时任务唯一ID生成器 */
        private int SERNUM = 0;
    	/** 有哪些区需要广播 */
    	public HashMap<ServerConfig,MsgStatus> configs = new HashMap<ServerConfig,MsgStatus>();
    	public Calendar alertTime;
        public int loopTimes;
        public long loopDeltaTime;
        public String message;
        public int id;
        public String name;

    	public String genExecuteStatus() {
    		StringBuilder ret = new StringBuilder();
    		boolean firstTime = true;
    		for (ServerConfig cfg : configs.keySet()) {
    			if (!firstTime) {
    				ret.append(" ");
    			}
    			MsgStatus stat = configs.get(cfg);
    			ret.append(cfg.name);
    			ret.append("(");
    			if (stat.stop) {
    				ret.append("-");
    			} else {
	    			ret.append(stat.succeedTimes);
	    			ret.append("/");
	    			ret.append(stat.sendTimes);
    			}
    			ret.append(")");
    			firstTime = false;
    		}
    		return ret.toString();
    	}

        public String getLoopInfo() {
        	String s = StringUtil.getDurationString(loopDeltaTime);
        	StringBuilder buf = new StringBuilder();
        	if (loopTimes == 1) {
        		buf.append(BroadCastDialogRES.once);
        	} else {
        		buf.append(Res.format(BroadCastDialogRES.deltaTime_0, s));
            	if (loopTimes < 0) {
            		buf.append(BroadCastDialogRES.forever);
            	} else {
            		buf.append(Res.format(BroadCastDialogRES.execTimes_0, loopTimes));
            	}
        	}
    		return buf.toString();
        }
        public ScheduleBroadcastTask(String name, String time, String repeat, String message, ArrayList<ServerConfig> cfgs) {
            this.id = SERNUM++;
            this.name = name;
            this.alertTime = parseToDate(time);
            setRepeat(repeat);
            this.message = message;
            for (ServerConfig cfg: cfgs) {
            	configs.put(cfg, new MsgStatus());
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
    		JMenuItem mi = new JMenuItem(BroadCastDialogRES.delTask);
    		pop.add(mi);
    		mi.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent e) {
    					ScheduleBroadcastTask t = currentSelectedTask;
    					pop.setVisible(false);
    					if (t != null) {
    						 synchronized(scheduledBroadcastTask) {
    							 scheduledBroadcastTask.remove(t);
    							 mdl.fireTableDataChanged();
    						 }
    					}
    				}
    		});

    		addMouseListener(new MouseListener() {
    			public void mouseClicked(MouseEvent e) {
    				if (e.getButton() == MouseEvent.BUTTON1) {
    					java.awt.Point p = e.getPoint();
    			        int rowIndex = rowAtPoint(p);
    			        if (rowIndex >= 0 && rowIndex <scheduledBroadcastTask.size()) {
    			        	rowIndex = convertRowIndexToModel(rowIndex);
    			        	currentSelectedTask = scheduledBroadcastTask.get(rowIndex);
    			        	tfJobName.setText(currentSelectedTask.name);
    			        	tfJobContent.setText(currentSelectedTask.message);
    			        	for (int i = serverCheckBox.size(); --i >= 0;) {
    			        		ServerConfig cfg = serverList.get(i);
    			        		JCheckBox box = serverCheckBox.get(i);
    			        		MsgStatus status = currentSelectedTask.configs.get(cfg);
    			        		if (status != null && !status.stop) {
    			        			box.setSelected(true);
    			        		} else {
    			        			box.setSelected(false);
    			        		}
    			        	}
    			        }
    				}
    			}
    			public void mousePressed(MouseEvent e) {
    				if (e.getButton() == MouseEvent.BUTTON3) {
    					java.awt.Point p = e.getPoint();
    			        int rowIndex = rowAtPoint(p);
    			        if (rowIndex >= 0 && rowIndex <scheduledBroadcastTask.size()) {
    			        	rowIndex = convertRowIndexToModel(rowIndex);
    			        	currentSelectedTask = scheduledBroadcastTask.get(rowIndex);
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
		String columnNames[] = {BroadCastDialogRES.title, BroadCastDialogRES.content, 
				BroadCastDialogRES.nextTime, BroadCastDialogRES.repetCondition, BroadCastDialogRES.doneFlag};
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
	    public int getRowCount() { 
	    	return scheduledBroadcastTask.size(); 
	    }
	    public int getColumnCount() { 
	    	return columnNames.length; 
	    }
	    public Object getValueAt(int row, int col) {
	    	if (row < scheduledBroadcastTask.size()) {
	    		ScheduleBroadcastTask t =scheduledBroadcastTask.get(row);
	    		if (t != null) {
			    	switch (col) {
			    	case 0:
			    		return t.name;
			    	case 1:
			    		return t.message;
			    	case 2:
			    		return StringUtil.getDateFormat(t.alertTime.getTimeInMillis(), BroadCastDialogRES.dateFormatStr);
			    	case 3:
			    		return t.getLoopInfo();
			    	case 4:
			    		return t.genExecuteStatus();
			    	}
	    		}
	    	}
	        return null;
	    }
	    public boolean isCellEditable(int row, int col) { 
	    	return false; 
	    }
	    public void setValueAt(Object value, int row, int col) {
	    	scheduledBroadcastTask.setElementAt((ScheduleBroadcastTask)value, row);
	        fireTableCellUpdated(row, col);
	    } 
	}

}
