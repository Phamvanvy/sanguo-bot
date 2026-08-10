package peony.net;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
        while (active) {
            try {
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
}
