package peony.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import peony.game.Server;
import peony.service.Service;

/**
 * 代理服务器管理服务。当系统运行为dispatch模式时，可能会有多个proxy服务器连接到此服务器。
 * 在这种模式下，本类负责管理所有proxy的连接。当proxy连接断开时，需要定时通知proxy重连。
 * @author lighthu
 */
public class ProxyManagingService implements Service, Runnable {
	private static final Logger log = Logger.getLogger(ProxyManagingService.class);
	/*
	 * 用于表示proxy配置的内部类。
	 */
	public static class Proxy {
		public int id;
		public String ip;
		public int port;
		public String password;
		public int maxPlayer;
		public String[] ipds;
		public IoSession connection;
		public long lastNotifyTime;
		public int checkTimes;
		public String aliasIp;
		public String redirServer;  // 如果不为空，表示此代理服务器连接本服需要使用转发代理
		public int redirPort;
	}
	
	/*
	 * 所有配置的代理。
	 */
	private List<Proxy> allProxy = new ArrayList<Proxy>();
	/*
	 * 用于发送通知包的socket。
	 */
	private DatagramSocket localSocket;
	/*
	 * 是否活动
	 */
	private boolean active = true;
	/*
	 * 服务启动时间。
	 */
	private long startupTime = System.currentTimeMillis();
	
	/**
	 * 创建代理服务器管理服务。这个服务是一个自动运行的线程。
	 * @param config
	 */
	public ProxyManagingService(XMLConfiguration config) throws IOException {
		List<SubnodeConfiguration> pcs = config.configurationsAt("proxys.proxy");
		for (int i = 0; i < pcs.size(); i++) {
			Proxy newProxy = new Proxy();
        	newProxy.id = i + 1;
        	newProxy.ip = pcs.get(i).getString("address");
        	newProxy.port = pcs.get(i).getInt("port");
        	newProxy.password = pcs.get(i).getString("password");
        	newProxy.maxPlayer = pcs.get(i).getInt("maxplayer");
        	newProxy.ipds = pcs.get(i).getStringArray("ipd");
        	newProxy.aliasIp = pcs.get(i).getString("alias", "");
        	if (pcs.get(i).getString("redirect_server") != null) {
        		newProxy.redirServer = pcs.get(i).getString("redirect_server");
        		newProxy.redirPort = pcs.get(i).getInt("redirect_port");
        	}
        	allProxy.add(newProxy);
		}
	}
	
	public List<Proxy> getProxys(){
		return allProxy;
	}
	
	public void reload(XMLConfiguration config) {
	    List<SubnodeConfiguration> pcs = config.configurationsAt("proxys.proxy");
        for (int i = 0; i < pcs.size(); i++) {
            boolean found = false;
            String ip = pcs.get(i).getString("address");
            int port = pcs.get(i).getInt("port");
            for (Proxy p : allProxy) {
                if (p.ip.equals(ip) && p.port == port) {
                    p.password = pcs.get(i).getString("password");
                    p.maxPlayer = pcs.get(i).getInt("maxplayer");
                    p.ipds = pcs.get(i).getStringArray("ipd");
                    p.aliasIp = pcs.get(i).getString("alias", "");
                	if (pcs.get(i).getString("redirect_server") != null) {
                		p.redirServer = pcs.get(i).getString("redirect_server");
                		p.redirPort = pcs.get(i).getInt("redirect_port");
                	} else {
                		p.redirServer = null;
                	}
                    found = true;
                    break;
                }
            }
            if (!found) {
                Proxy newProxy = new Proxy();
                newProxy.id = allProxy.size() + 1;
                newProxy.ip = ip;
                newProxy.port = port;
                newProxy.password = pcs.get(i).getString("password");
                newProxy.maxPlayer = pcs.get(i).getInt("maxplayer");
                newProxy.ipds = pcs.get(i).getStringArray("ipd");
                newProxy.aliasIp = pcs.get(i).getString("alias", "");
            	if (pcs.get(i).getString("redirect_server") != null) {
            		newProxy.redirServer = pcs.get(i).getString("redirect_server");
            		newProxy.redirPort = pcs.get(i).getInt("redirect_port");
            	} else {
            		newProxy.redirServer = null;
            	}
                allProxy.add(newProxy);
            }
        }
	}
	
	public void startup() throws Exception {
		localSocket = new DatagramSocket();
		
		// 启动线程
		new Thread(this, "ProxyManagingService").start();
	}
	
	public void shutdown() {
		active = false;
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
	 * 取得最大连接数。
	 * @param session
	 * @return
	 */
	public int getMaxPlayer(IoSession session) {
	    for (Proxy proxy : allProxy) {
            if (proxy.connection == session) {
            	// 新增动态上限控制，服务器刚启动时，只提供正常上限的30%，每6秒提高1%，7分钟内逐渐提高到100%。
            	int percent = (int)((System.currentTimeMillis() - startupTime) / 6000L) + 30;
            	if (percent > 100) {
            		percent = 100;
            	}
            	return percent * proxy.maxPlayer / 100;
            }
        }
	    return 10000;
	}
	
	/**
	 * 列出当前所有可用的用户连接。返回的数组中，每行表示一个可用连接，其中的4个元素分别表示：ID、连接地址、最大允许连接数、当前连接数。
	 * 连接地址的格式为：socket://<proxyaddr>:<proxyport>#<localip><localport>，其中<localip>为8位16进制，<localport>为
	 * 4位16进制。
	 * @return
	 */
	public String[][] listAvailableAddress() {
		DispatchClientSessionService ss = (DispatchClientSessionService)
			Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
		if (ss == null) {
			return new String[0][];
		}
		String localip = ss.getLocalIP();
		int localPort = ss.getLocalPort();
		String ipid = ip2id(localip, localPort);
		List<String[]> list = new ArrayList<String[]>();
		for (Proxy p : allProxy) {
			if (p.connection != null) {
				int online = ((SyncInteger)p.connection.getAttribute(DispatchClientSessionService.SESSION_COUNTER)).get();
				String fullAddr = null;
				
				if(p.aliasIp.length() > 0){
				    fullAddr = "socket://" + p.aliasIp + ":" + p.port + "#" + ipid;
				}else{
				    fullAddr = "socket://" + p.ip + ":" + p.port + "#" + ipid;
				}
				
				int ipdCount = p.ipds == null ? 0 : p.ipds.length;
				String[] arr = new String[4 + ipdCount];
				arr[0] = String.valueOf(p.id);
				arr[1] = fullAddr;
				arr[2] = String.valueOf(p.maxPlayer);
				arr[3] = String.valueOf(online);
				if (p.ipds != null) {
					System.arraycopy(p.ipds, 0, arr, 4, p.ipds.length);
				}
				list.add(arr);
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
		while (active) {
			try {
				DispatchClientSessionService ss = (DispatchClientSessionService)
				Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
				if (ss != null) {
					String localip = ss.getLocalIP();
					int localPort = ss.getLocalPort();
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
							InetAddress addr = InetAddress.getByName(proxy.ip);
			            	String cmd;
			            	if (proxy.redirServer == null) {
			            		cmd = proxy.password + ":addserver:" + localip + ":" + localPort + ":3";
			            	} else {
			            		cmd = proxy.password + ":addserver:" + localip + ":" + localPort + "#" + 
			            			ip2id(proxy.redirServer, proxy.redirPort) + ":3";
			            	}
			            	byte[] sendData = cmd.getBytes();
			            	DatagramPacket dpack = new DatagramPacket(sendData, sendData.length, addr, proxy.port);
			            	localSocket.send(dpack);
						}
					}
				}
				Thread.sleep(30000);
			} catch (Exception e) {
			}
		}
	}
}
