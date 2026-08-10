package log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import log.define.Definings;
import log.define.Log;
import log.define.LogDefine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SangoLogApplication {

	private static String log;
	public static File preFile;
	public static File orderFile;
	private static String toolPath;
	public static Text logName;
	public static Text logTool;
	private Button yesButton;
	private Button cancelButton;
	private Button browserButton1;
	private Button browserButton2;
	private static int type = 0;
	private static int ty;
	private static Text searchText;
	private Button seaButton;
	private Button typeButton;
	private Button searchButton;
	private Button cancelButton2;
	private static Combo combo;
	private static String typeStr = "";
	private static String[] ITEMS = { "聊天记录", "邮件", "商店出售", "商店购买", "元宝商店购买",
			"删除物品", "交易", "放生坐骑" };

	private void initShell(final Shell shell) {
		shell.setText("三国日志分析器");
		shell.setSize(800, 500);
		// 文件菜单
		Menu mainMenu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mainMenu);
		MenuItem fileItem = new MenuItem(mainMenu, SWT.CASCADE);
		fileItem.setText("&File");
		// 文件下的子菜单
		Menu filemMenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(filemMenu);
		MenuItem newFileItem = new MenuItem(filemMenu, SWT.CASCADE);
		newFileItem.setText("Exit");
		newFileItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// 关闭日志分析工具
				shell.close();
			}
		});

		{
			{
				final Label label = new Label(shell, SWT.NONE);
				label.setBounds(20, 20, 135, 25);
				label.setText("日志文件:");
			}
			{
				final Label label = new Label(shell, SWT.NONE);
				label.setBounds(20, 60, 135, 25);
				label.setText("用系统编辑器打开文件:");
			}

			{
				logName = new Text(shell, SWT.BORDER);
				logName.setBounds(160, 15, 550, 25);
				browserButton1 = new Button(shell, SWT.PUSH);
				browserButton1.setBounds(720, 15, 50, 25);
				browserButton1.setText("浏览...");
				browserButton1.addMouseListener(new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						// 打开文件对话框
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setText("日志目录");
						dialog.setFilterPath(log);
						log = dialog.open();
						preFile = new File(log);
						logName.setText(log);
					}
				});
			}
			{
				logTool = new Text(shell, SWT.BORDER);
				logTool
						.setText("C:\\Program Files\\IDM Computer Solutions\\UltraEdit\\Uedit32.exe");
				logTool.setBounds(160, 55, 550, 25);
				browserButton2 = new Button(shell, SWT.PUSH);
				browserButton2.setBounds(720, 55, 50, 25);
				browserButton2.setText("浏览...");
				browserButton2.addMouseListener(new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						// 打开文件对话框
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setText("编辑器地址");
						dialog.setFilterPath(toolPath);
						toolPath = dialog.open();
						orderFile = new File(toolPath);
						logTool.setText(toolPath);
					}
				});
			}
		}

		// 确定按钮
		{
			yesButton = new Button(shell, SWT.NONE);
			yesButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

				}
			});
			yesButton.setBounds(525, 100, 90, 25);
			yesButton.setText("确定");
			yesButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					// 打开文件对话框
					if (log == null || logName.getText() == "") {
						MessageBox messageBox = new MessageBox(shell,
								SWT.ICON_INFORMATION);
						messageBox.setMessage("必须选择一个日志文件！");
						messageBox.setText("选择");
						messageBox.open();
						return;
					}

					String str = logName.getText();
					String fileName = getFileName(str);
					type = 0;
					try {
						losdFile(fileName, shell);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
		}

		// 取消按钮
		{
			cancelButton = new Button(shell, SWT.NONE);
			cancelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

				}
			});

			cancelButton.setBounds(620, 100, 90, 25);
			cancelButton.setText("取消");
			cancelButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					clearText();
				}
			});
		}

		Group group = new Group(shell, SWT.NONE);
		group.setText("具体分析");
		group.setBounds(10, 170, 760, 200);
		// 搜索
		{
			searchText = new Text(group, SWT.BORDER);
			searchText.setBounds(160, 55, 550, 25);
			seaButton = new Button(group, SWT.RADIO);
			seaButton.setBounds(20, 55, 135, 18);
			seaButton.setText("搜索:");
			seaButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					type = 1;
					ty = 0;
				}
			});
		}
		// 具体类型
		{
			// 添加下拉按钮样式的下拉列表框
			combo = new Combo(group, SWT.DROP_DOWN);
			// 设置下拉列表项
			combo.setItems(ITEMS);
			Font font = new Font(group.getDisplay(), "宋体", 12, SWT.NORMAL);
			combo.setFont(font);
			combo.setBounds(160, 95, 550, 25);
			combo.select(0);
			typeButton = new Button(group, SWT.RADIO);
			typeButton.setBounds(20, 95, 135, 18);
			typeButton.setText("具体类型");
			typeButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					type = 1;
					ty = 1;
				}
			});
		}
		// 详细查询按钮
		{
			searchButton = new Button(group, SWT.NONE);
			searchButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

				}
			});
			searchButton.setBounds(525, 155, 90, 25);
			searchButton.setText("详细查询");
			searchButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					// 打开文件对话框
					if (log == null || logName.getText() == "") {
						MessageBox messageBox = new MessageBox(shell,
								SWT.ICON_INFORMATION);
						messageBox.setMessage("必须选择一个日志文件！");
						messageBox.setText("选择");
						messageBox.open();
						return;
					}

					String logName = SangoLogApplication.logName.getText();
					String fileName = SangoLogApplication.getFileName(logName);
					try {
						losdFile(fileName, shell);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		// 退出按钮
		{
			cancelButton2 = new Button(group, SWT.NONE);
			cancelButton2.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

				}
			});
			cancelButton2.setBounds(620, 155, 90, 25);
			cancelButton2.setText("退出");
			cancelButton2.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					shell.setVisible(false);
					open();
					type = 0;
				}
			});
		}

	}

	public static void losdFile(String fileName, Shell shell)
			throws IOException {
		try {
			Definings.loadDefine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (type == 1) {
			if (ty == 0) {
				typeStr = searchText.getText();
			} else if (ty == 1) {
				typeStr = getTextStr(ITEMS[combo.getSelectionIndex()]);
			}

			if (typeStr == "") {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setMessage("选择的程序不存在！");
				messageBox.setText("选择");
				messageBox.open();
				return;
			}
		}
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			LogDefine define = Definings.getLogDefine("sango");
			br = new BufferedReader(new FileReader(preFile));
			String line = br.readLine();
			while (line != null) {
				try {
					if (type == 1 && !line.contains(typeStr))
						continue;
					Log log = new Log(define, line);
					log.process();
					sb.append(line);
					sb.append("\r\n");
					sb.append(log.toString());
					sb.append("\r\n");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					line = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File logFile = new File("D:\\log\\" + fileName + ".txt");
		PrintWriter pw = null;
		try {
			if (logFile.exists()) {
				logFile.delete();
			}
			if (!logFile.exists()) // 如果文件不存在,则新建.
			{
				File parentDir = new File(logFile.getParent());
				if (!parentDir.exists()) // 如果所在目录不存在,则新建.
					parentDir.mkdirs();
				logFile.createNewFile();
			}
			pw = new PrintWriter(new FileWriter(logFile), true);
			pw.println(sb.toString());
			pw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Runtime rn = Runtime.getRuntime();
		Process p = null;
		try {
			String pa = "D:\\log\\" + fileName + ".txt";
			p = rn.exec(logTool.getText() + " " + pa);
		} catch (Exception ex) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
			messageBox.setMessage("选择的程序不存在！");
			messageBox.setText("错误");
			messageBox.open();
			return;
		}
	}

	public void clearText() {
		logName.setText("");
	}

	public static String getFileName(String filePath) {
		char[] strChar = filePath.toCharArray();
		for (int i = 0; i < strChar.length; i++) {
			if (strChar[i] == '\\')
				strChar[i] = ',';
		}
		String str = String.valueOf(strChar);
		String[] ss = str.split(",");
		return String.valueOf(ss[ss.length - 1]);
	}

	public static String getTextStr(String type) {
		String temp = "";
		if (type.equals("邮件")) {
			temp = "MAILPOST";
		} else if (type.equals("商店出售")) {
			temp = "SHOPSELL";
		} else if (type.equals("商店购买")) {
			temp = "SHOPBUY";
		} else if (type.equals("元宝商店购买")) {
			temp = "IMONEYBUY";
		} else if (type.equals("删除物品")) {
			temp = "REMOVEITEM";
		} else if (type.equals("交易")) {
			temp = "EXCHANGE";
		} else if (type.equals("放生坐骑")) {
			temp = "REMOVEHORSE";
		} else if (type.equals("聊天记录")) {
			temp = "CHAT";
		}
		return temp;
	}

	public void open() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		initShell(shell);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

	public String processOthers(String s) {
		// 解析登录
		if (s.startsWith("LOGIN[")) {

		}
		return null;
	}

	// public String processPlayerId(String s){
	// StringBuilder sb = new StringBuilder();
	// if(s.contains("ID")){
	// // sb.append("账号ID["+)
	// }
	// }
	// EQU(1007412,19067534,S=5,JEW=1368+1417+1396+1459+1375,NR=9/15+13/40,MK=——
	// 03真诚的爱你ヾ莫_尛嘉 、—— lov心碎铸)
	public String getEquip(String s) {
		if (s.startsWith("INFO[") || s.startsWith("ITEM[")) {
			String temp = s.substring(5);
			if (temp.startsWith("EQU")) {
				String ts = s.substring(5, s.length() - 1);
				String[] tString = ts.split("\\)");
				for (int i = 0; i < tString.length; i++) {
					StringBuilder sb = new StringBuilder();
					String tempString = tString[i];
					String[] ss = tempString.split(",");
					String itemId = ss[0];
					sb.append("装备ID:" + itemId + ";");
					String instanceId = ss[1];
					sb.append("instanceId:" + instanceId + ";");
					if (ss.length > 2) {
						for (int k = 2; k < ss.length; k++) {
							String se = ss[k];
							if (se.startsWith("S=")) {
								String star = se.substring(2);
								sb.append("星级鉴定:" + star + ";");
							} else if (se.startsWith("H=")) {
								String hole = se.substring(2);
								sb.append("打孔数:" + hole + ";");
							} else if (se.startsWith("JEW=")) {
								sb.append("镶嵌的宝石:");
								String[] jewStrings = se.substring(4).split(
										"\\+");
								int[] jewels = new int[jewStrings.length];
								for (int j = 0; j < jewStrings.length; j++) {
									String jewelId = jewStrings[j];
									sb.append("宝石id:" + jewelId + " ");
									sb.append("宝石名称:"
											+ Definings.getItemName(jewelId)
											+ ";");
								}
							} else if (se.startsWith("NR=")) {
								String naturalsString = se.substring(3);
								sb.append("资质鉴定:" + naturalsString);
							} else if (se.startsWith("MK=")) {
								String mkString = se.substring(3);
								sb.append("刻的字为:" + mkString);
							}
						}
					}
					return sb.toString();
				}
			}
		}
		return s;
	}

	public static void main(String[] args) {
		new SangoLogApplication().open();
	}
}
