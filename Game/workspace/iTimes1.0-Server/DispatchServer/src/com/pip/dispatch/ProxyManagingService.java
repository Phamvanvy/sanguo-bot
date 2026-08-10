package com.pip.dispatch;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

/**
 * 代理服务器管理服务。当系统运行为singlesocket模式时，可能会有多个proxy服务器连接到此dispatch。
 * 在这种模式下，本类负责管理所有proxy的连接。当proxy连接断开时，需要定时通知proxy重连。
 * @author lighthu
 */
public class ProxyManagingService implements Runnable {
	private static final Logger log = Logger.getLogger(ProxyManagingService.class);
	/*
	 * 用于表示proxy配置的内部类。
	 */
	private static class Proxy {
		public int id;
		public String ip;
		public int port;
		public String password;
		public int maxPlayer;
		public IoSession connection;
		public long lastNotifyTime;
		public int checkTimes;
	}
	
	/*
	 * 配置表。
	 */
	private Configuration configuration;
	/*
	 * 所有配置的代理。
	 */
	private List<Proxy> allProxy = new ArrayList<Proxy>();
	/*
	 * 用于发送通知包的socket。
	 */
	private DatagramSocket localSocket;
	
	/**
	 * 创建代理服务器管理服务。这个服务是一个自动运行的线程。
	 * @param config
	 */
	public ProxyManagingService(Configuration config) throws IOException {
		configuration = config;
		for (int i = 1; i < 100; i++) {
			// 例：proxy1=218.206.80.188,7001,adminpass
			List list = configuration.getList("proxy" + i);
        	if (list == null || list.size() != 4) {
        		break;
        	}
        	Proxy newProxy = new Proxy();
        	newProxy.id = i;
        	newProxy.ip = (String)list.get(0);
        	newProxy.port = Integer.parseInt((String)list.get(1));
        	newProxy.password = (String)list.get(2);
        	newProxy.maxPlayer = Integer.parseInt((String)list.get(3));
        	allProxy.add(newProxy);
		}
		localSocket = new DatagramSocket();
		
		// 启动线程
		new Thread(this, "ProxyManagingService").start();
	}
	
	/**
	 * 注册一个代理服务器连接。
	 * @param session 对应的连接对象
	 * @param ip 代理服务器服务IP
	 * @param port 代理服务器服务端口
	 */
	public void registerConnection(IoSession session, String ip, int port) {
		for (Proxy proxy : allProxy) {
			if (proxy.ip.equals(ip) && proxy.port == port) {
				proxy.connection = session;
			}
		}
	}
	
	/**
	 * 注销一个代理服务器连接。
	 * @param session
	 */
	public void unregisterConnection(IoSession session) {
		for (Proxy proxy : allProxy) {
			if (proxy.connection == session) {
				proxy.checkTimes = 0;
				proxy.lastNotifyTime = System.currentTimeMillis();
				proxy.connection = null;
			}
		}
	}
	
	/**
	 * 列出当前所有可用的用户连接。返回的数组中，每行表示一个可用连接，其中的4个元素分别表示：ID、连接地址、最大允许连接数、当前连接数。
	 * 连接地址的格式为：socket://<proxyaddr>:<proxyport>#<localip><localport>，其中<localip>为8位16进制，<localport>为
	 * 4位16进制。
	 * @return
	 */
	public String[][] listAvailableAddress() {
		String localip = configuration.getString("localip");
		int localPort = configuration.getInt("port");
		String ipid = ip2id(localip, localPort);
		List<String[]> list = new ArrayList<String[]>();
		for (Proxy p : allProxy) {
			if (p.connection != null) {
				int online = ((AtomicInteger)p.connection.getAttribute(SingleSocketDispatcher.SESSION_COUNTER)).get();
				String fullAddr = "socket://" + p.ip + ":" + p.port + "#" + ipid;
				list.add(new String[] { String.valueOf(p.id), fullAddr, String.valueOf(p.maxPlayer), String.valueOf(online) });
			}
		}
		String[][] ret = new String[list.size()][];
		list.toArray(ret);
		return ret;
	}

	/*
	 * 把IP地址和端口号转换为16进制格式。
	 */
    private static String ip2id(String ip, int port) {
    	try {
	        String[] secs = ip.split("\\.");
	        if (secs.length != 4) {
	            return null;
	        }
	        int ipv = (Integer.parseInt(secs[0]) << 24) | (Integer.parseInt(secs[1]) << 16) |
	            (Integer.parseInt(secs[2]) << 8) | Integer.parseInt(secs[3]);
	        String ipStr = Integer.toHexString(ipv);
	        while (ipStr.length() < 8) {
	            ipStr = "0" + ipStr;
	        }
	        String portStr = Integer.toHexString(port);
	        while (portStr.length() < 4) {
	            portStr = "0" + portStr;
	        }
	        return (ipStr + portStr).toUpperCase();
    	} catch (Exception e) {
    		return null;
    	}
    }

	/**
	 * 服务主线程，无限循环检查所有代理服务器是否已经连接成功。
	 */
	public void run() {
		while (true) {
			try {
				for (Proxy proxy : allProxy) {
					if (proxy.connection != null) {
						continue;
					}
					
					// 如果没有连接，根据重试次数计算间隔时间，如果到时间了，重发通知包
					long interval = proxy.checkTimes * proxy.checkTimes * 5000L;
					if (interval > 60000L) {
						interval = 60000L;
					}
					if (System.currentTimeMillis() > proxy.lastNotifyTime + interval) {
						proxy.checkTimes++;
		            	proxy.lastNotifyTime = System.currentTimeMillis();
		            	
		            	// 通过UDP发送服务器注册通知包
						InetAddress addr = InetAddress.getByName(proxy.ip);
		            	String cmd = proxy.password + ":addserver:" + configuration.getString("localip") + ":" +  
		            		configuration.getInt("port") + ":1";
		            	byte[] sendData = cmd.getBytes("ASCII");
		            	DatagramPacket dpack = new DatagramPacket(sendData, sendData.length, addr, proxy.port);
		            	localSocket.send(dpack);
		            	
		            	// 通过TCP再发送一次，确保注册
	            		Socket socket = null;
	            		InputStream is = null;
	            		OutputStream os = null;
		            	try {
		            		Thread.sleep(1000);
		            		cmd = "REGSVR" + cmd;
		            		sendData = cmd.getBytes("ASCII");
		            		socket = new Socket(proxy.ip, proxy.port);
		            		is = socket.getInputStream();
		            		os = socket.getOutputStream();
		            		os.write(sendData);
		            		byte[] buf = new byte[2];
		            		new DataInputStream(is).read(buf);
		            	} catch (Exception e) {
		            	} finally {
		            		if (is != null) {
		            			try {
		            				is.close();
		            			} catch (Exception e) {
		            			}
		            		}
		            		if (os != null) {
		            			try {
		            				os.close();
		            			} catch (Exception e) {
		            			}
		            		}
		            		if (socket != null) {
		            			try {
		            				socket.close();
		            			} catch (Exception e) {
		            			}
		            		}
		            	}
					}
				}
				Thread.sleep(30000);
			} catch (Exception e) {
			}
		}
	}
}
