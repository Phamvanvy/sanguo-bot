package com.pip.dispatch;

import java.net.*;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 定时向分配器通报连接服务器状态的服务。
 * @author Jeffrey
 * @version 1.0
 */
public class IpdService implements Runnable{
    private static final Logger log = Logger.getLogger(IpdService.class);

    /*
	 * 所有配置的分配器地址。
	 */
    private String[] url = null;
    /*
     * 服务器配置。
     */
    private Configuration configuration;
    /*
     * 协议类型：socket, http或singlesocket。
     */
    private String protocol;
    /*
     * 控制发送的队列。
     */
    private BlockingQueue<OnlineNotify> messages = new LinkedBlockingQueue<OnlineNotify>();
    /*
     * singlesocket模式下的代理服务器管理器。
     */
    private ProxyManagingService proxyService;
    /*
     * embedhttp模式下的URL。
     */
    private String embedURL;
    /*
     * 工作线程。
     */
    private Thread workingThread;
    /*
     * 是否关闭。
     */
    private boolean stopped = false;

    /**
     * 创建并启动服务。
     * @param protocol 本连接服务器的协议
     * @param url 所有配置的分配器的地址
     * @param configuration 
     */
    public IpdService(String protocol, String[] url, Configuration configuration) {
        this.protocol = protocol;
        this.url = url;
        this.configuration = configuration;
        workingThread = new Thread(this);
        workingThread.start();
    }
    
    /**
     * 关闭服务。
     */
    public void shutdown() {
        stopped = true;
        workingThread.interrupt();
    }
    
    /**
     * 设置embedhttp模式下的地址。
     * @param u
     */
    public void setEmbedURL(String u) {
        embedURL = u;
    }
    
    /**
     * 设置代理服务器管理器。
     * @param proxyService
     */
    public void setProxyService(ProxyManagingService proxyService) {
    	this.proxyService = proxyService;
    }

    /**
     * 发起一个到分配器的状态通知。这个方法通常由时间控制服务器调用。
     * @param current 世界服务器PUSH下来的当前在线数
     * @param maxPlayer 世界服务器PUSH下来的最大在线数
     * @throws Exception
     */
    public void connect(int current, int maxPlayer) throws Exception {
    	// 如果有proxy管理器存在，那么所有连接信息从proxy管理器获取
    	String str = configuration.getString("aliases");
    	String[] aliases;
    	if (str == null || str.length() == 0) {
    		str = configuration.getString("serverid");
    		aliases = new String[] { str };
    	} else {
    		aliases = configuration.getStringArray("aliases");
    	}
    	if (proxyService == null) {
    		String serviceURL; 
    		if ("http".equals(protocol)) {
    			serviceURL = protocol + "://" + configuration.getString("localip") + ":" + 
    				configuration.getString("port") + "/";
    		} else if ("embedhttp".equals(protocol)) {
    		    if (embedURL == null) {
    		        return;
    		    }
    		    serviceURL = embedURL;
    		} else {
    			serviceURL = protocol + "://" + configuration.getString("localip") + ":" + 
    				configuration.getString("port");
    		}
    		for (String alias : aliases) {
		        OnlineNotify notify = new OnlineNotify(alias, serviceURL, current, maxPlayer);
		        messages.put(notify);
    		}
    	} else {
    		String[][] servers = proxyService.listAvailableAddress();
    		for (String[] si : servers) {
    			for (String alias : aliases) {
	    			OnlineNotify notify = new OnlineNotify(alias + "_" + si[0], si[1], 
	    					Integer.parseInt(si[3]), Integer.parseInt(si[2]));
	    			messages.put(notify);
    			}
    		}
    	}
    }

	/**
	 * 服务主线程，随时等候时间控制器发出的通知请求。
	 */
    public void run() {
        while (!stopped) {
            try {
                OnlineNotify notify = messages.take();
                for (int i = 0; i < url.length; i++) {
                    HttpURLConnection conn = null;
                    try {
                    	URL urlObj = notify.buildUrl(url[i]);
                        conn = (HttpURLConnection)(notify.buildUrl(url[i]).openConnection());
                        conn.setConnectTimeout(3000);
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
            } catch (InterruptedException ex1) {
            }
        }
    }

    /**
     * 用于控制发送序列的对象。这个请求由时间控制器定时发出，由本服务线程接收。
     * @author Jeffrey
     */
    class OnlineNotify {
    	/*
    	 * 服务名称。
    	 */
    	public String serviceName;
    	/*
    	 * 服务访问地址。
    	 */
    	public String serviceURL;
    	/*
    	 * 当前在线数。
    	 */
        public int current;
        /*
         * 最大在线数。
         */
        public int maxPlayer;

        /**
         * 构造一个分配器通知请求。
         */
        public OnlineNotify(String name, String url, int current, int maxPlayer) {
        	this.serviceName = name;
        	this.serviceURL = url;
            this.current = current;
            this.maxPlayer = maxPlayer;
        }
        
        /**
         * 构造一个到分配器的通知请求URL。发送到分配器的参数需要包括：服务名称、服务地址、当前在线、最大在线。
         * @param url 原始配置URL（不带任何参数）
         * @return 完整的请求URL。
         */
        public URL buildUrl(String url) throws IOException {
        	String encodeName = URLEncoder.encode(serviceName, "GBK");
        	String encodeURL = URLEncoder.encode(serviceURL, "GBK");
        	return new URL(url + "?name=" + encodeName + "&url=" + encodeURL + "&maxnum=" + 
        			maxPlayer + "&curnum=" + current);
	    }
    }
}
