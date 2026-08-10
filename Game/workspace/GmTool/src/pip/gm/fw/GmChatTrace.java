package pip.gm.fw;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import pip.gm.cmd.CmdPasswordRES;
import pip.util.Excel;
import pip.util.Res;
import pip.util.StringUtil;
import pip.util.ui.LayoutUtil;
import pip.util.ui.ParameterInputDialog;
import cwu.util.sort.SortAgent;

public class GmChatTrace extends Command {
	public static String gmServerURL;
	public static String loginedUser;
	public static String loginedUserPass;
	public static long loginedUserAuth;
	
//	static Connection con;
//	static PreparedStatement stmt;
//	static PreparedStatement fakeStmt;
//	static PreparedStatement fakeTimeStmt;
//	static PreparedStatement stmtAccount;
//	static PreparedStatement stmtChangePass;
//	static PreparedStatement stmtGmHistory;
	public static final int MODE_CHAT = 1;  //聊天
	public static final int MODE_MAIL = 2; // 邮件
	public static final int MODE_FORBID = 3; // 禁号
	public static final int MODE_KICK = 4; // 踢出
	public static final int MODE_BROADCAST = 5; // 广播
	public static final int MODE_DELETE_MAIL = 6; // 删除邮件
	public static final int MODE_MARK = 7; // 标注邮件已处理
	public static String actionNames[] = {
		GmChatTraceRES.actionNameNo,
		GmChatTraceRES.actionNameChat, 
		GmChatTraceRES.actionNameMail, 
		GmChatTraceRES.actionNameForbid, 
		GmChatTraceRES.actionNameKick, 
		GmChatTraceRES.actionNameBrocast, 
		GmChatTraceRES.delMail,
		GmChatTraceRES.actionNameMarkMail,
	};

//	public boolean logined = false;
//	public static String dburl = null;
//	public static String dbusername = null;
//	public static String dbpassword = null;
	public static boolean traceOff = false;
//	private static final SimpleDateFormat formate = new SimpleDateFormat("MM-dd HH:mm");
	/**
	 * 
	 * @param gm
	 * @param server
	 * @param mode 1:Chat, 2:Mail, 3:Forbid 4:Kick 5:broadcast
	 * @param target
	 * @param s
	 */
//	public static boolean initConnection() {
//		if (traceOff) {
//			return false;
//		}
//		if (con == null) {
//			try {
//				if (dburl == null) {
//					File baseDir = new File(System.getProperty("user.home"), BaseConfig.configFileDirName);
//					File f = new File(baseDir, "gm.properties");
//					if (f.exists() && f.isFile()) {
//						Properties p = new Properties();
//						FileInputStream fin = null;
//						try {
//							fin = new FileInputStream(f);
//							p.load(fin);
//						} catch (Exception e) {
//						} finally {
//							if (fin != null) {
//								try {
//									fin.close();
//								} catch (Exception e) {
//								}
//							}
//						}
//						dbpassword = p.getProperty("password");
//						dbusername = p.getProperty("username");
//						dburl = p.getProperty("dburl");
//					}
//				} 
//				if (dburl == null) {
//					if (BaseConfig.BRANCH == BaseConfig.BRANCH_PIP) {
//						dburl="jdbc:mysql://211.151.99.68/pipgm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "pipgm";
//						dbpassword = "M.@*K5pe%Y";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_TW) {
//						dburl="jdbc:mysql://60.199.162.100:3306/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo";
//						dbpassword = "yes&2009@at";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_5DING) {
//						dburl="jdbc:mysql://60.12.203.38/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "]Sp2tg)(PQ";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_GameFlier) {
//						dburl="jdbc:mysql://210.242.206.10/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "6Ts_iI$9";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_MY) {
//						dburl="jdbc:mysql://14.192.66.52/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "ex5+IVfPXS";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_VIETNAM_TinhVan) {
//						dburl="jdbc:mysql://210.211.99.52/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "+_EYs0pm+x";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_360) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_360?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm_360";
//						dbpassword = "]nLvWJ;FQi";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_US) {
//						dburl="jdbc:mysql://173.234.255.162:3307/sanguo_gm?useUnicode=true&characterEncoding=UTF8";
//						dbusername = "sanguo_gm";
//						dbpassword = "w#w2XT%o{#";
//					} else if (BaseConfig.BRANCH == BaseConfig.BRANCH_JP) {
//						dburl="jdbc:mysql://210.168.88.89/sanguo_gm?useUnicode=true&characterEncoding=UTF8";
//						dbusername = "sanguo_gm";
//						dbpassword = "FkOE@a@ij-";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_XD) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_xingdie?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm_xd";
//						dbpassword = "Q6AL9+*E3N";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_SHUWEI) {
//						dburl="jdbc:mysql://61.160.234.201/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "w^OC)Xzv)_";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_KAIXIN) {
//						dburl="jdbc:mysql://27.131.223.12/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "5333Ee!0";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_RENREN) {
//						dburl="jdbc:mysql://211.151.101.59:4000/sanguo_gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "jffa0A[[";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_DAYOU) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_dagame?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "gbxx7S.:";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_FUYUN) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_fuyun?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "2777Pp:4";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_QIDIAN) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_qdzw?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "2777Pp:4";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_KOREA_MOBILE) {
//						dburl="jdbc:mysql://222.231.37.35/sanguo_gm?useUnicode=true&characterEncoding=UTF8";
//						dbusername = "sanguo_gm";
//						dbpassword = "+_EYs0pm+x";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_JAPAN_MOBILE) {
//						dburl="jdbc:mysql://59.106.193.85/sanguo_gm?useUnicode=true&characterEncoding=UTF8";
//						dbusername = "sanguo_gm";
//						dbpassword = "+_EYs0pm+x";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_I8) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_i8?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "2777Pp:4";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_TH) {
//						dburl="jdbc:mysql://61.47.43.162/sanguo_gm_thai?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "2777Pp:4";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_SKY) {
//						dburl="jdbc:mysql://111.1.17.201:9967/gm?useUnicode=true&characterEncoding=GBK";
//						dbusername = "gm";
//						dbpassword = "gm@@2011";
//					}else if (BaseConfig.BRANCH == BaseConfig.BRANCH_CMCC) {
//						dburl="jdbc:mysql://113.31.17.100/sanguo_gm_cmcc?useUnicode=true&characterEncoding=GBK";
//						dbusername = "sanguo_gm";
//						dbpassword = "2777Pp:4";
//					}
//					
//				}
////				int k = dburl.indexOf('/', 13);
////				if (k > 0) {
////					String serverName = dburl.substring(13, k);
////					InetAddress ip = InetAddress.getByName(serverName);
////					boolean reachable = ip.isReachable(null, 10, 800);
////					if (!reachable) {
////						System.out.println("GM工具仅限内网使用");
////						return false;
////					}
////				}
//				Class.forName("com.mysql.jdbc.Driver");
//				if (dbusername != null && dbpassword != null) {
//					con = DriverManager.getConnection(dburl,dbusername,dbpassword);
//				} else {
//					con = DriverManager.getConnection(dburl);
//				}
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return true;
//	}
	
	/**
	 * 访问gmserver执行请求。
	 * @param cmd 服务器函数名
	 * @param requestData 请求数据
	 * @return 返回数据。
	 * @exception 如果功能执行出错，抛出异常。
	 */
	public static DataInputStream remoteExec(String cmd, byte[] requestData) throws Exception {
		HttpURLConnection conn = null;
		DataInputStream in = null;
		try {
			URL url = new URL("http://" + gmServerURL + "/gmop");
			conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeUTF(cmd);
			dos.writeUTF(loginedUser);
			dos.writeUTF(loginedUserPass);
			dos.writeUTF(BaseConfig.DOMAIN);
			dos.write(requestData);
			dos.close();
			int code = conn.getResponseCode();
			if (code != 200) {
				throw new Exception("服务器错误");
			}
			in = new DataInputStream(conn.getInputStream());
			code = in.readInt();
			if (code != 0) {
				throw new Exception(in.readUTF());
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buf = new byte[256];
				int len;
				while ((len = in.read(buf)) >= 0) {
					if (len > 0) {
						bos.write(buf, 0, len);
					}
				}
				return new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	/**
	 * 包装一个字符串参数。
	 * @param param
	 * @return
	 */
	public static byte[] composeRequestData(String param) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeUTF(param);
			dos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 包装两个字符串参数。
	 * @param param
	 * @return
	 */
	public static byte[] composeRequestData(String param1, String param2) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeUTF(param1);
			dos.writeUTF(param2);
			dos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 包装一个字符串参数和一个整数参数。
	 * @param param
	 * @return
	 */
	public static byte[] composeRequestData(String param1, int param2) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeUTF(param1);
			dos.writeInt(param2);
			dos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 修改密码。
	 * @param name
	 * @param oldpassword
	 * @param newPass
	 * @return
	 */
	public static String changePassword(String name, String oldpassword, String newPass) {
		if (!name.equals(loginedUser) || !oldpassword.equals(loginedUserPass)) {
			return GmChatTraceRES.faildInChgPass;
		}
		try {
			DataInputStream in = remoteExec("changePassword", composeRequestData(newPass));
			return GmChatTraceRES.succeedInChgPass;
		} catch (Exception e) {
			return GmChatTraceRES.faildInChgPass + "\n" + e.getMessage();
		}
	}
	
	/**
	 * 查询服务器GM密码。
	 * @param gameType
	 * @param server
	 * @return
	 */
	public static String getInstancePassword(String gameType, String server) {
		try {
			DataInputStream in = remoteExec("getInstancePassword", composeRequestData(gameType, server));
			return in.readUTF();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	/**
	 * 登录并查询用户权限。
	 * @param name
	 * @param password
	 * @param server
	 * @return
	 */
	public static long login() {
		try {
			DataInputStream in = remoteExec("login", new byte[0]);
			return in.readLong();
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * 登录并查询用户权限。
	 * @param name
	 * @param password
	 * @param server
	 * @return
	 */
	public static long getAuth(String name, String password, String server) {
		return loginedUserAuth;
	}
	
    private void showAdminAuthHex(AbstractClient world, String auth) {
    	Auth a = new Auth(AuthConstants.authStrings);
    	a.setAuth(auth);
    	 world.onMessage(IMessage.MSG_TYPE_LOG, "0x" + Long.toHexString(a.authMask) + "    " + a.authMask, null);
    }
    
    /**
     * 查询GM操作历史。
     * @param server
     * @param id
     * @return
     */
	public static ArrayList<String[]> getHistory(String server, int id) {
		try {
			DataInputStream in = remoteExec("getHistory", composeRequestData(server, id));
			ArrayList<String[]> ret = new ArrayList<String[]>();
			int count = in.readInt();
			for (int i = 0; i < count; i++) {
				ret.add(new String[] {
					in.readUTF(),
					in.readUTF(),
					in.readUTF(),
					in.readUTF(),
					in.readUTF()
				});
			}
			return ret;
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * 记录GM操作日志。
	 * @param gm
	 * @param server
	 * @param mode
	 * @param target
	 * @param s
	 * @param ref
	 */
	public static void traceGm(String gm, String server, int mode, int target, String s, String ref) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeUTF(gm);
			dos.writeUTF(server);
			dos.writeInt(mode);
			dos.writeInt(target);
			dos.writeUTF(s);
			dos.writeUTF(ref);
			dos.flush();
			byte[] reqData = bos.toByteArray();
			DataInputStream in = remoteExec("traceGm", reqData);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	public class SaveHistoryToExcel implements ParameterInputDialog.ParameterFilled {
		public HashMap<String,ArrayList<String[]>> map;
		AbstractClient wd;
		public SaveHistoryToExcel(AbstractClient wd, HashMap<String,ArrayList<String[]>> map) {
			this.map = map;
			this.wd = wd;
		}
		public void finished(Object obj) {
			SavingFile f = (SavingFile)obj;
			File file = new File(f.fileName); 
			f.fileName = file.getAbsolutePath();
			
			String []keys = new String[map.size()];
			HashMap<String,String> keyMaps = new HashMap<String,String>();
			int off = 0;
			for (String key : map.keySet()) {
				String name = null;
				ArrayList<String[]> lst = map.get(key);
				for (int i = 0; i < lst.size(); i++) {
				    String s[] = lst.get(i);
				    if (s.length > 0 && s[0].length() > 0) {
				    	name = s[0];
				    	break;
				    }
				}
				if (name == null) {
					name = key;
				}
			    keys[off++] = name;
			    keyMaps.put(name, key);
			}
			SortAgent.sort(keys, new cwu.util.sort.CompareAgent(){
				public int compare(java.lang.Object arg0, java.lang.Object arg1) {
					if (arg0 == null) {
						return -1;
					}
					if (arg1 == null) {
						return 1;
					}
					return ((String)arg0).compareTo((String)arg1);
				}
			}, 0);
			if (!Excel.hasF1()) {
				try {
					PrintStream fout = new PrintStream(f.fileName);
					fout.println("\"" + GmChatTraceRES.hisTitleServer + "\",\"" + GmChatTraceRES.hisTitleTime + "\",\"" + GmChatTraceRES.hisTitlePlayerId + 
							"\",\"" + GmChatTraceRES.hisTitleRequest + "\",\"" + GmChatTraceRES.hisTitleAction + "\",\"" + GmChatTraceRES.hisTitleReply + "\",");
					for (String sheetName : keys) {
						String key = keyMaps.get(sheetName);
						ArrayList<String[]> lst = map.get(key);
						for (int i = 0; i < lst.size(); i++) {
						    String s[] = lst.get(i);
						    for (int j = 0; j < s.length; j++) {
						    	fout.print("\"" + s[j] + "\",");
						    }
						    fout.println();
						}
					}
					fout.close();
					wd.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.excelFileSaved, f.fileName), null); 
				} catch (Exception e1) {
					e1.printStackTrace();
					wd.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.excelFileSaveException, f.fileName, e1.getMessage()), null);
				}
			} else {
				Excel e = new Excel(map.size());
				int sheetId = 0;
				
				for (String sheetName : keys) { // TODO Why null
					if (sheetName != null) {
						String key = keyMaps.get(sheetName);
						ArrayList<String[]> lst = map.get(key);
						e.getSheet(sheetId++);
						e.setText(0, 0, GmChatTraceRES.hisTitleServer);
						e.setText(0, 1, GmChatTraceRES.hisTitleTime);
						e.setText(0, 2, GmChatTraceRES.hisTitlePlayerId);
						e.setText(0, 3, GmChatTraceRES.hisTitleRequest);
						e.setText(0, 4, GmChatTraceRES.hisTitleAction);
						e.setText(0, 5, GmChatTraceRES.hisTitleReply);
						for (int i = 0; i < lst.size(); i++) {
						    String s[] = lst.get(i);
						    for (int j = 0; j < s.length; j++) {
						    	e.setText(i+1, j, s[j]);
						    }
						}
						sheetName = sheetName.replace('/', '_');
						sheetName = sheetName.replace(':', '_');
						sheetName = sheetName.replace('\\', '_');
						e.setSheetName(sheetName);
					}
				}
				try {
					e.saveAs(f.fileName);
					wd.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.excelFileSaved, f.fileName), null); 
				} catch (Exception e1) {
					e1.printStackTrace();
					wd.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.excelFileSaveException, f.fileName, e1.getMessage()), null);
				}
			}
		}
		
	}
    public boolean exec(String cmd, AbstractClient aworld, String []s) throws Exception {
        if (s != null && s.length >= 2) {
            if (cmd != null) {
                if (isCommand(aworld.auth, cmd)) {
                	if (s[1].equals("list") && aworld.auth.hasAuth(AuthConstants.traceAdmin)) {
                		processListGmHis(aworld, s, false);
                	} else if (s[1].equals("list2file") && aworld.auth.hasAuth(AuthConstants.traceAdmin)) {
                		processListGmHis(aworld, s, true);
                	} else if (s.length == 4 && s[1].equals("grant") && aworld.auth.hasAuth(AuthConstants.root)) {
                        grantAdmin(aworld, s[2], s[3]);
                	} else if (s.length == 3 && s[1].equals("grant") && aworld.auth.hasAuth(AuthConstants.root)) {
                        grantAdmin(aworld, s[2]);
                	} else if (s.length == 2 && s[1].equals("listgm") && aworld.auth.hasAuth(AuthConstants.root)) {
                        listAdmin(aworld);
                	} else if (s.length == 3 && s[1].equals("auth") && aworld.auth.hasAuth(AuthConstants.root)) {
                        showAdminAuthHex(aworld, s[2]);
                	} else if (s.length == 3 && s[1].equals("addgm") && aworld.auth.hasAuth(AuthConstants.root)) {
                        addAdmin(aworld, s[2]);
                	} else if (s.length == 2 && s[1].equals("su")) {
                        lauchSu(aworld);
                	} else if (s[1].equals("switch") && aworld.auth.hasAuth(AuthConstants.root)) {
                		traceOff = !traceOff;
                		aworld.onMessage(IMessage.MSG_TYPE_LOG, traceOff ? GmChatTraceRES.logOff : GmChatTraceRES.logOn, null);
                	}
                	return true;
                }
            }
        }
        return false;
    }
    AbstractClient world;
    JDialog suDialog;
    JPasswordField passFld = new JPasswordField(12);
    JTextField nameFld = new JTextField(12);

    public void lauchSu(AbstractClient aworld) {
    	world = aworld;
    	Window main = world.getUiContainer(); 
    	if (suDialog == null) {
    		LayoutUtil lu = new LayoutUtil();
    		passFld.setText("");
    		nameFld.setText("");

    		suDialog = new JDialog(main, GmChatTraceRES.dlgTitle);
    		suDialog.setModal(true);
    		suDialog.setLayout(new BorderLayout(10, 10));
    		// 输入区域
    		JPanel p = new JPanel();
    		p.setLayout(new GridBagLayout());
    		
    		JPanel p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel(GmChatTraceRES.userName));
    		p.add(p1, lu.getConstrains(2, 2, 1, 1));
    		p.add(nameFld, lu.getConstrains(3, 2, 1, 1));
    		
    		p1 = new JPanel(new BorderLayout());
    		p1.add(BorderLayout.EAST, new JLabel(GmChatTraceRES.password));
    		p.add(p1, lu.getConstrains(2, 3, 1, 1));
    		p.add(passFld, lu.getConstrains(3, 3, 1, 1));
    		passFld.setEchoChar('*');
    		
    		suDialog.add(BorderLayout.CENTER, p);
    		
    		// 控制
    		suDialog.add(BorderLayout.NORTH, new JLabel(""));
    		suDialog.add(BorderLayout.EAST, new JLabel("    "));
    		suDialog.add(BorderLayout.WEST, new JLabel("   "));
    		
    		// 下部 Button 条
    		p = new JPanel();
    		p.setLayout(new FlowLayout());
    		JButton btn = new JButton(GmChatTraceRES.suDo);
    		btn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				su();
        		}
    		});
    		p.add(btn);

        	btn = new JButton(CmdPasswordRES.cancel);
    		btn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				suDialog.setVisible(false);
        		}
    		});
    		p.add(btn);
    		
    		suDialog.add(BorderLayout.SOUTH, p);
    		suDialog.pack();
    		suDialog.setLocation(main.getX() + ((main.getWidth() - suDialog.getWidth()) >> 1),
    				main.getY() + ((main.getHeight() - suDialog.getHeight()) >> 2));
    	}
    	suDialog.setVisible(true);
    }
    public void su() {
    	suDialog.setVisible(false);
    	String name = nameFld.getText();
    	String pass = passFld.getText();
		long t = GmChatTrace.getAuth(name, pass, world.getUniqServerId());
		if (t != 0) {
			world.auth.setAuth(t);
			world.getConfig().setConfig("account", name, true);
			world.getConfig().setConfig("password", pass, true);
			world.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.suSucceed, world.getUniqServerId(), Auth.getAuth(t)), null);
		} else {
			world.onMessage(IMessage.MSG_TYPE_LOG, GmChatTraceRES.accountErr, null);
			world.auth.setAuth(t);
		}
    }
    
    private String getAuthString(long l) {
    	StringBuffer buf = new StringBuffer();
    	for (int i = 0; i < AuthConstants.authStrings.length; i++) {
    		if ((l & (1L << i)) != 0) {
    			buf.append(":").append(AuthConstants.authStrings[i]);
    		}
    	}
    	return buf.toString();
    }
    
    /**
     * 列出所有管理员。
     * @param aworld
     */
    private void listAdmin(AbstractClient aworld) {
    	DataInputStream in = null;
    	try {
    		in = remoteExec("listAdmin", new byte[0]);
    		StringBuffer buf = new StringBuffer();
			int count = in.readInt();
			for (int i = 0; i < count; i++) {
				int id = in.readInt();
				String name = in.readUTF();
				long auth = in.readLong();
				buf.append("\n  ");
				buf.append(id);
				buf.append("[");
				buf.append(name);
				buf.append("]");
				buf.append(getAuthString(auth));
			}
			aworld.onMessage(IMessage.MSG_TYPE_LOG, buf.toString(), null);
		} catch (Exception e) {
    		aworld.onMessage(IMessage.MSG_TYPE_LOG, e.getMessage(), null);
    		return;
    	}
    }
    
    /**
     * 修改用户授权。
     * @param aworld
     * @param user
     * @param auth
     */
    private void grantAdmin(AbstractClient aworld, String user, String auth) {
    	try {
    		remoteExec("grantAdmin", composeRequestData(user, auth));
			aworld.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.updateResult, user, auth), null);
		} catch (Exception e) {
    		aworld.onMessage(IMessage.MSG_TYPE_LOG, e.getMessage(), null);
    		return;
    	}
    }
    public static void closeRes(Statement st, ResultSet res) {
    	if (res != null) {
			try {
				res.close();
			} catch (SQLException e) {
			}
		}
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
			}
		}
    }
    
    /**
     * 查询用户授权。
     * @param aworld
     * @param user
     */
    private void grantAdmin(AbstractClient aworld, String user) {
    	try {
    		DataInputStream in = remoteExec("queryGrant", composeRequestData(user));
    		String name = in.readUTF();
    		long l = in.readLong();
    		Auth auth = new Auth(null);
			auth.setAuth(l);
			aworld.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.authInfo,name, getAuthString(l), aworld.con.getCommandList(auth)), null);
		} catch (Exception e) {
    		aworld.onMessage(IMessage.MSG_TYPE_LOG, e.getMessage(), null);
    		return;
    	}
    }
    
    private void addAdmin(AbstractClient aworld, String user) {
    	try {
    		DataInputStream in = remoteExec("addAdmin", composeRequestData(user));
    		String pass = in.readUTF();
    		aworld.onMessage(IMessage.MSG_TYPE_LOG, Res.format(GmChatTraceRES.addAdminSucceed,user,pass), null);
		} catch (Exception e) {
    		aworld.onMessage(IMessage.MSG_TYPE_LOG, e.getMessage(), null);
    		return;
    	}
    }
    
    private void processListGmHis(AbstractClient aworld, String []s, boolean toExcel) throws Exception {
    	try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(s.length - 2);
			for (int i = 2; i < s.length; i++) {
				dos.writeUTF(s[i]);
			}
			dos.flush();
			byte[] reqData = bos.toByteArray();
			DataInputStream in = remoteExec("processListGmHis", reqData);
			List<Object[]> data = new ArrayList<Object[]>();
			int count = in.readInt();
			for (int i = 0; i < count; i++) {
				Object[] row = new Object[6];
				row[0] = in.readUTF();
				row[1] = in.readInt();
				row[2] = in.readInt();
				row[3] = in.readUTF();
				row[4] = in.readUTF();
				row[5] = in.readUTF();
				data.add(row);
			}
			processListGmHis(aworld, data, toExcel, s[2]);
		} catch (Exception e) {
			aworld.onMessage(IMessage.MSG_TYPE_GM, e.getMessage(), null);
		}
    }
    public static class SavingFile {
    	public String fileName;
    }
    private void processListGmHis(AbstractClient wd, List<Object[]> data, boolean toExcel, String gmId) throws Exception {
    	if (toExcel) {
			HashMap<String,ArrayList<String[]>> map = new HashMap<String,ArrayList<String[]>>();
			for (Object[] row : data) {
				String ss = (String)row[0];
				if (ss == null) {
					ss = "NoServer";
				}
				int action = (Integer)row[1];
				int target = (Integer)row[2];
				String msg = (String)row[3];
				String ref = (String)row[5];
				for (int i = 0; i < replStrs.length; i++) {
					int k = msg.indexOf(replStrs[i][0]);
					if (k >= 0) {
						msg = msg.substring(0, k) + replStrs[i][1] + msg.substring(k + replStrs[i][0].length());
					}
				}
				String time = (String)row[4];
				ArrayList<String[]> lst = map.get(ss);
				if (lst == null) {
					lst = new ArrayList<String[]>();
					map.put(ss, lst);
				}
				String orig = "";
				String serverName = "";
				if (ref != null) {
					int k = ref.indexOf(":");
					if (k > 0) {
						orig = ref.substring(k + 1);
						serverName = ref.substring(0, k);
					}
				}
				lst.add(new String[]{serverName, time, String.valueOf(target), orig, actionNames[action], msg});
			}
    		SavingFile savingFile = new SavingFile();
    		savingFile.fileName = "GmHis_" + gmId + ".xls"; 
    		ParameterInputDialog dlg = new ParameterInputDialog();
    		dlg.cancelBtnTitle = GmChatTraceRES.cancelBtnTitle;
    		dlg.confirmBtnTitle = GmChatTraceRES.confirmBtnTitle;
    		dlg.setParameterNames(SavingFile.class, GmChatTraceRES.savingFileParameterDesc);
    		SaveHistoryToExcel callBack = new SaveHistoryToExcel(wd, map);
    		dlg.openDialog(GmChatTraceRES.selectFileDlgTitle, wd.getUiContainer(), savingFile, callBack);
    	} else {
			HashMap<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
			for (Object[] row : data) {
				String ss = (String)row[0];
				int action = (Integer)row[1];
				int target = (Integer)row[2];
				String msg = (String)row[3];
				String ref = (String)row[5];
				for (int i = 0; i < replStrs.length; i++) {
					int k = msg.indexOf(replStrs[i][0]);
					if (k >= 0) {
						msg = msg.substring(0, k) + replStrs[i][1] + msg.substring(k + replStrs[i][0].length());
					}
				}
				String time = (String)row[4];
				ArrayList<String> lst = map.get(ss);
				if (lst == null) {
					lst = new ArrayList<String>();
					map.put(ss, lst);
				}
				lst.add(StringUtil.formal(time + " " + actionNames[action] + " [" + target + "] " + msg + "{" + ref + "}"));
			}
			int j = 0;
			StringBuffer buf = new StringBuffer();
			for (String key : map.keySet()) {
				ArrayList<String> lst = map.get(key);
				buf.append(GmChatTraceRES.serverId).append(key);
				int jj = 1;
				for (String s: lst) {
					buf.append("\n  ").append(jj++).append(".").append(s);
				}
				j += jj - 1;
			}
			buf.append("\nTotal: ").append(j).append(" ");
			wd.onMessage(IMessage.MSG_TYPE_GM, buf.toString(), null); 
    	}
	}
	
    public static String [][] replStrs = {
    	{GmChatTraceRES.thanksInfo, GmChatTraceRES.thanks},
    	{GmChatTraceRES.acceptInfo, GmChatTraceRES.accept},
    };
    public long getAuth() {
    	return 0; // AuthConstants.shutdown; 
    }
    public String getCommand(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return "gm";
    	} else {
    		return null;
    	}
    }
    public String getName(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
    		return GmChatTraceRES.cmdName;
    	} else {
    		return null;
    	}
    }
    public String getDescription(Auth auth) {
    	if (auth.hasAuth(getAuth())) {
	    	StringBuffer buf = new StringBuffer();
	    	buf.append(GmChatTraceRES.cmdDesc); // su
	    	if (auth.hasAuth(AuthConstants.traceAdmin)) {
	    		buf.append(GmChatTraceRES.tracecmdDesc);
	    	}
	    	if (auth.hasAuth(AuthConstants.root)) {
	    		buf.append(GmChatTraceRES.rootcmdDesc);
	    	}
	    	return buf.toString();
    	} 
    	return null;
    }
}
