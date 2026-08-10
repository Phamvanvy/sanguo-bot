package com.pip.servermgr;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.Product;
import com.pip.servermgr.data.Server;
import com.pip.servermgr.data.ServerGroup;

public class ProbeUtils extends Thread {
	private static ConcurrentHashMap<String, String> resultCache = new ConcurrentHashMap<String, String>();
	private static ProbeUtils instance;
	private static ConcurrentHashMap<String, Object> commandLocks = new ConcurrentHashMap<String, Object>();
	
	public static void startProbe() {
		instance = new ProbeUtils();
		instance.start();
	}
	
	public static void stopProbe() {
		try {
			instance.interrupt();
			instance = null;
			instance.join(5000);
			instance.stop();
		} catch (Exception e) {
		}
	}
	
	public void run() {
		while (instance == this) {
			Product[] prs = Configuration.products;
			long now = System.currentTimeMillis();
			for (Product product : prs) {
				// 探测一个产品的所有服务
				ArrayList<Server> failServers = new ArrayList<Server>();
				for (ServerGroup group : product.servers) {
					for (Server server : group.servers) {
						if (server.nextProbeTime > now) {
							if (!server.isTestServer() && !server.isServerOn()) {
								failServers.add(server);
							}
							continue;
						}
						try {
							String result = probeByShell(server.getShellScript(), "status", true, true);
							server.updateStatus(result);
							if (!server.isTestServer() && !server.isServerOn()) {
								failServers.add(server);
								System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + 
										"服务状态异常：" + server.getShellScript() + "\n" + result);
								server.nextProbeTime = System.currentTimeMillis() + 60 * 1000L;
							} else {
								server.nextProbeTime = System.currentTimeMillis() + 10 * 60 * 1000L;
							}
						} catch (Exception e) {
						}
					}
				}
				
				// 如果发现服务状态异常，则安排1分钟后重测，再次异常则报错
				if (failServers.size() > 0) {
					if (product.status == 0) {
						// 第一次发现
						product.status = 1;
					} else if (product.status == 1) {
						// 第二次发现
						product.status = 2;
					} else if (product.status == 2) {
						// 第三次发现，发送短信
						product.status = 3;
						String msg = product.name + "异常：";
						for (Server svr : failServers) {
							msg += svr.parent.name + "-" + svr.name + ","; 
						}
						System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + 
										"发送服务错误通知短信：" + msg);
						for (String phone: product.owners) {
							if (SmsUtil.send(phone, msg)) {
								System.out.println("发送给" + phone + "成功");
							} else {
								System.out.println("发送给" + phone + "失败");
							}
						}
					}
				} else {
					// 所有服务恢复正常
					if (product.status == 3) {
						String msg = product.name + "恢复正常。";
						System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + 
										"发送服务恢复通知短信：" + msg);
						for (String phone: product.owners) {
							if (SmsUtil.send(phone, msg)) {
								System.out.println("发送给" + phone + "成功");
							} else {
								System.out.println("发送给" + phone + "失败");
							}
						}
					}
					product.status = 0;
				}
			}
			try {
				Thread.sleep(60 * 1000L);
			} catch (Exception e) {
			}
		}
	}
	
	public static String probeByShell(String cmd, String param, boolean hasRet, boolean forceUpdate) throws IOException {
		String key = cmd + " " + param;
		String result;
		if (!hasRet) {
			// 如果没有返回，缺省都是每次刷新。
			result = executeShell(cmd, param, hasRet);
		} else if (forceUpdate) {
			// 强制刷新
			result = executeShell(cmd, param, hasRet);
			resultCache.put(key, result);
		} else {
			// 如果不强制刷新，则到缓存中取结果
			if (resultCache.containsKey(key)) {
				result = resultCache.get(key);
			} else {
				result = executeShell(cmd, param, hasRet);
				resultCache.put(key, result);
			}
		}
		return result;
	}
		
	private static String executeShell(String cmd, String param, boolean hasRet) throws IOException {
		String fullPath = Utils.basePath + File.separator + cmd + " " + param;
		Object lock = commandLocks.get(fullPath);
		if (lock == null) {
			lock = new Object();
			commandLocks.put(fullPath, lock);
		}
		synchronized (lock) {
			System.out.println("execute: " + fullPath);
			Process p = Runtime.getRuntime().exec(fullPath);
			new StreamTerminator(p.getInputStream()).start();
			new StreamTerminator(p.getErrorStream()).start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				throw new IOException();
			}
			p.destroy();
			System.out.println("over: " + fullPath);
			if (!hasRet) {
				return "";
			}
			
			File retFile = new File(Utils.basePath, cmd + ".temp");
			byte[] buf = new byte[(int)retFile.length()];
			FileInputStream fis = new FileInputStream(retFile);
			new DataInputStream(fis).readFully(buf);
			fis.close();
			
			// 如果文件开始4个字符是utf8，则用utf8编码，否则用系统缺省编码。
			String resultText;
			if (buf.length >= 4 && buf[0] == 'u' && buf[1] == 't' && buf[2] == 'f' && buf[3] == '8') {
				resultText = new String(buf, 4, buf.length - 4, "UTF-8");
			} else {
				resultText = new String(buf);
			}
			return System.currentTimeMillis() + "\n" + resultText;
		}
	}
	
	/**
	 * 简单执行一个命令行。
	 * @param cmd
	 * @throws IOException
	 */
	public static void executeShell(String cmd) throws IOException {
		System.out.println("execute: " + cmd);
		Process p = Runtime.getRuntime().exec(cmd);
		new StreamTerminator(p.getInputStream()).start();
		new StreamTerminator(p.getErrorStream()).start();
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new IOException();
		}
		p.destroy();
	}
}
