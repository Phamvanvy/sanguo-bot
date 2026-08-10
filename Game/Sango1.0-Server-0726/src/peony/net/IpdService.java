package peony.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.Service;

/**
 * 定时向分配器通报连接服务器状态的服务。
 */
public class IpdService implements Runnable, Service {
    private static final Logger log = Logger.getLogger(IpdService.class);

    /*
	 * 分配器地址。
	 */
    private String[] urls = null;
    /*
     * 是否激活
     */
    private boolean active = true;
    /*
     * 账号角色计数缓存
     */
    private Map<String, Integer> roleCountCache = new HashMap<String, Integer>();

    /**
     * 创建并启动服务。
     * @param protocol 本连接服务器的协议
     * @param url 所有配置的分配器的地址
     * @param configuration 
     */
    public IpdService() {
    	urls = Server.server.getConfig().getStringArray("ipd");
    }
    
    public void reload() {
        urls = Server.server.getConfig().getStringArray("ipd");
    }
    
    public void startup() {
    	new Thread(this, "IpdNotify").start();
    }
    
    public void shutdown() {
    	active = false;
    }
    
	/**
	 * 服务主线程，随时等候时间控制器发出的通知请求。
	 */
    public void run() {
    	long lastReportTime = System.currentTimeMillis();
    	int lastReportCount = 0;
    	long lastReportRoleCountTime = System.currentTimeMillis();
        while (active) {
            try {
            	// 每1分钟报告一次角色数变化
            	if (System.currentTimeMillis() - lastReportRoleCountTime > 60000) {
            		lastReportRoleCountTime = System.currentTimeMillis();
            		try {
            			reportCachedRoleCount();
            		} catch (Exception e) {
            			log.error(e, e);
            		}
            	}
            	
            	// 如果有proxy管理器存在，那么所有连接信息从proxy管理器获取
            	ProxyManagingService pms = (ProxyManagingService)
            		Server.server.getServiceRegistry().getService(ProxyManagingService.class);
            	if (pms == null) {
            		Thread.sleep(60000);
            		continue;
            	}
            	
            	// 如果服务器列表改变，或者到1分钟，向IPD发起通知
            	String[][] servers = pms.listAvailableAddress();
            	if (System.currentTimeMillis() - lastReportTime > 60000 || lastReportCount != servers.length) {
            		String[] names = Server.server.getName().split(";");
            		for (String serviceName : names) {
		            	for (String[] info : servers) {
		            		// 通知URL格式为：http://xx/report?name=xx&url=xx&maxnum=xx&curnum=xx
		                	String encodeName = URLEncoder.encode(serviceName + "_" + info[0], "GBK");
		                	String encodeURL = URLEncoder.encode(info[1], "GBK");
	
		                	if (info.length == 4) {
		            			// 如果某个PROXY连接没有配置特殊IPD，那么采用通用配置
			                	for (String url : urls) {
			                	    if (url.trim().length() == 0) {
			                	        continue;
			                	    }
		    	                	String notifyURL = url + "?name=" + encodeName + "&url=" + encodeURL + 
		    	                		"&maxnum=" + info[2] + "&curnum=" + info[3];
		    	                	report(notifyURL);
			                	}
		                	} else {
		                		// 如果配置了特殊IPD，则通知这些IPD
		                		for (int i = 4; i < info.length; i++) {
		                			String url = info[i];
			                	    if (url.trim().length() == 0) {
			                	        continue;
			                	    }
		    	                	String notifyURL = url + "?name=" + encodeName + "&url=" + encodeURL + 
		    	                		"&maxnum=" + info[2] + "&curnum=" + info[3];
		    	                	report(notifyURL);
		                		}
		                	}
		            	}
            		}
	            	lastReportTime = System.currentTimeMillis();
	            	lastReportCount = servers.length;
            	}
            	Thread.sleep(3000);
            } catch (Exception ex1) {
            }
        }
    }
    
    /*
     * 向IPD服务器发送一条服务器记录报告。
     */
    private void report(String url) {
        HttpURLConnection conn = null;
        try {
        	URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(2000);
            int code = conn.getResponseCode();
            log.info("IPD[" + urlObj.toString() + "]: " + code);
        } catch(Exception ex) {
        	log.error(ex, ex);
        } finally {
            if (conn != null) {
            	try {
            		conn.disconnect();
            	} catch (Exception e) {
            	}
            }
        }
    }
    
    /**
     * 记录一个账号最新的角色数量。这个数据将会定期向分配器同步。
     * @param accountName
     * @param roleCount
     */
    public synchronized void updateRoleCount(String accountName, int roleCount) {
    	roleCountCache.put(accountName, roleCount);
    }
    
    /*
     * 把所有缓存的账号角色数数据报告给分配器。
     */
    protected void reportCachedRoleCount() throws Exception {
    	// 取得并清空缓存
    	Map<String, Integer> needReportCache;
    	synchronized (this) {
    		needReportCache = roleCountCache;
    		roleCountCache = new HashMap<String, Integer>();
    	}
    	if (needReportCache.size() == 0) {
    		return;
    	}
    	
    	// 把所有数据拼成一个字节流
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	DataOutputStream dos = new DataOutputStream(bos);
    	dos.writeInt(needReportCache.size());
    	Iterator<String> keyItor = needReportCache.keySet().iterator();
    	while (keyItor.hasNext()) {
    		String name = keyItor.next();
    		dos.writeUTF(name);
    		dos.writeInt(needReportCache.get(name));
    	}
    	dos.flush();
    	byte[] buf = bos.toByteArray();
    	
    	// 如果有多个服务器名字，只算第一个
    	String[] names = Server.server.getName().split(";");
    	String mainName = names[0];
    	for (String url : urls) {
    		url = url.substring(0, url.lastIndexOf("report")) + "rcreport";
    		try {
    			httpPost(url, mainName, buf);
    		} catch (Exception e) {
    			log.error(e, e);
    		}
    	}
    }
    
    /**
     * 向指定URL发送一个POST数据。
     * @param url
     * @param data
     * @throws IOException
     */
    private void httpPost(String url, String name, byte[] data) throws IOException {
    	HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeUTF(name);
            dos.writeInt(data.length);
            dos.write(data);
            dos.close();
            
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new IOException("错误码" + retCode);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
    }
}
