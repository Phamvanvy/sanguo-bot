package com.pip.servermgr.data;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * 一个服务器进程。
 * @author lighthu
 */
public class Server {
	/**
	 * 所属服务器组。
	 */
	public ServerGroup parent;
	/**
	 * 服务器类型。目前支持的类型有：dispatcher, world, agent, account, gameaccount, billing, proxy, ipd
	 */
	public String type;
	/**
	 * 服务器名称。
	 */
	public String name;
	/**
	 * 对应的控制脚本。
	 */
	public String shell;
	/**
	 * 是否测试服务器。
	 */
	public boolean isTest;
    /**
     * 附加操作表。每个String[]包含2个元素，第一个是操作名称，第二个是操作参数。
     */
    public String[][] addOps;
    /**
	 * 服务器日志信息。
	 */
	public ServerLogInfo logInfo;
	/**
	 * 服务器数据库信息。
	 */
	public ServerDBInfo dbInfo;
	/**
	 * 当前数据更新时间，-1表示没有数据。
	 */
	public long statusTime = -1;
	/**
	 * 服务器类型（仅对dispatcher有效）
	 */
	public String dispatcherType;
	/**
	 * 服务器IP地址（对于agent无效）
	 */
	public String serverIP;
	/**
	 * 监听端口（对于agent无效）
	 */
	public String serverPort;
	/**
	 * 进程号。
	 */
	public int processID;
	/**
	 * 进程是否存在(对于ipd类型，这个标志表示应用是否启动)。
	 */
	public boolean processExist;
	/**
	 * 端口是否正在监听（对于agent无效）
	 */
	public boolean portListen;
	/**
	 * ipd类型的应用名称。
	 */
	public String appName;
	/**
	 * 管理帐号。
	 */
	public String adminName;
	/**
	 * 管理密码。
	 */
	public String adminPassword;
	/**
	 * ipd类型，是否在维护模式标志。
	 */
	public boolean maintaining;
	/**
	 * ipd类型，当前维护消息。
	 */
	public String maintainMsg;
	/**
	 * 访问测试结果。
	 */
	public boolean accessResult;
	
	public Server(ServerGroup pa, String t, String n, String sh, String[][] aops) {
		parent = pa;
		type = t;
		name = n;
		shell = sh;
		addOps = aops;
	}
	
	public String getShellScript() {
		return parent.getPath() + "/" + shell;
	}
	
	public String toString() {
		if (statusTime == -1) {
			return name;
		} else if ("agent".equals(type)) {
			return name;
		} else if ("dispatcher".equals(type)) {
			return name + "(" + dispatcherType + "://" + serverIP + ":" + serverPort + ")"; 
		} else if ("proxy".equals(type)) {
			return serverIP + ":" + serverPort;
		} else if ("ipd".equals(type)) {
			return name + "(http://" + serverIP + ":" + serverPort + "/" + appName + "/)";
		} else {
			return name + "(" + serverIP + ":" + serverPort + ")";
		}
	}
	
	public String getFullName() {
		return parent.parent.name + "-" + parent.name + "-" + toString();
	}
	
	public void updateStatus(String text) {
		try {
			text = translateUnicodeString(text);
			BufferedReader br = new BufferedReader(new StringReader(text));
			statusTime = Long.parseLong(br.readLine().trim());
			if ("agent".equals(type)) {
				// 如果是agent类型，则只有两行：进程号和进程状态
				processID = Integer.parseInt(br.readLine().trim());
				processExist = Integer.parseInt(br.readLine().trim()) == 1;
			} else if ("ipd".equals(type)) {
				// 如果是ipd类型，则前3行是IP地址，端口号，应用名称，管理帐号和管理密码，从第六行开始是分配器返回内容。
				// 如果分配器没有启动，则第一行返回内容应包含Error report字样
				// 如果返回内容中没有xxx=xx,socket://的字样，则说明服务器在维护状态，或者有服务器没有启动。
				serverIP = br.readLine().trim();
				serverPort = br.readLine().trim();
				appName  = br.readLine().trim();
				adminName = br.readLine().trim();
				adminPassword = br.readLine().trim();

				String infoLine;
				processExist = true;
				maintaining = true;
				maintainMsg = "随便填什么吧，用户不会看到的。";
				while ((infoLine = br.readLine()) != null) {
					if (infoLine.indexOf("Error report") >= 0) {
						processExist = false;
						maintaining = false;
						break;
					} else if (infoLine.contains("socket://")) {
						maintaining = false;
						break;
					}
				}
			} else if ("dispatcher".equals(type)) {
				// 对于dispatcher服务，依次是服务器类型，服务器IP，端口，进程ID，进程是否存在，端口是否监听，是否可访问
				// 最后一个是否可访问标志在老版本服务器没有探测
				dispatcherType = br.readLine().trim();
				serverIP = br.readLine().trim();
				serverPort = br.readLine().trim();
				processID = Integer.parseInt(br.readLine().trim());
				processExist = Integer.parseInt(br.readLine().trim()) == 1;
				portListen = Integer.parseInt(br.readLine().trim()) == 1;
				accessResult = Integer.parseInt(br.readLine().trim()) == 1;
			} else if ("billing".equals(type)) {
				// 对于billing服务，依次是服务器IP，端口，进程ID，进程是否存在，端口是否监听，是否可访问
				serverIP = br.readLine().trim();
				serverPort = br.readLine().trim();
				processID = Integer.parseInt(br.readLine().trim());
				processExist = Integer.parseInt(br.readLine().trim()) == 1;
				portListen = Integer.parseInt(br.readLine().trim()) == 1;
				accessResult = Integer.parseInt(br.readLine().trim()) == 1;
			} else {
				// 对于普通TCP服务，依次是服务器IP，端口，进程ID，进程是否存在，端口是否监听
				serverIP = br.readLine().trim();
				serverPort = br.readLine().trim();
				processID = Integer.parseInt(br.readLine().trim());
				processExist = Integer.parseInt(br.readLine().trim()) == 1;
				portListen = Integer.parseInt(br.readLine().trim()) == 1;
			}
		} catch (Exception e) {
		}
	}
	
	public void setError() {
		processExist = false;
		statusTime = System.currentTimeMillis();
	}
	
	public boolean isServerOn() {
		if ("agent".equals(type)) {
			return processExist;
		} else if ("ipd".equals(type)) {
			return processExist && !maintaining;
		} else if ("dispatcher".equals(type)) {
			return processExist && portListen && accessResult;
		} else if ("billing".equals(type)) {
			return processExist && portListen && accessResult;
		} else {
			return processExist && portListen;
		}
	}
	
	public boolean canStart() {
		if ("agent".equals(type)) {
			return !processExist;
		} else if ("ipd".equals(type)) {
			return !processExist;
		} else if ("dispatcher".equals(type)) {
		    // http依托tomcat运行，特殊处理
		    if ("http".equals(dispatcherType)) {
		        return !accessResult;
		    }
			return !processExist && !portListen && !accessResult;
		} else if ("billing".equals(type)) {
			return !processExist && !portListen && !accessResult;
		} else {
			return !processExist && !portListen;
		}
	}
	
	public boolean canStop() {
		return processExist;
	}
	
	public boolean isTestServer() {
		return isTest || parent.isTest;
	}
	
	/**
	 * 解析带u xxxx格式的字符串。
	 * @param source
	 * @return
	 */
	public String translateUnicodeString(String source) {
		StringBuilder sb = new StringBuilder();
		char[] arr = source.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			if (i <= arr.length - 6 && arr[i] == '\\' && arr[i + 1] == 'u') {
				int ch = Integer.parseInt(source.substring(i + 2, i + 6), 16);
				sb.append((char)ch);
			} else {
				sb.append(arr[i]);
			}
		}
		return sb.toString();
	}
}
