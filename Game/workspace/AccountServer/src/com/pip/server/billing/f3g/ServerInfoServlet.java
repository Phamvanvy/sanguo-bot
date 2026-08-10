package com.pip.server.billing.f3g;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * 根据ID取得服务器信息。
 * 请求参数：
 *     regionid = 区号
 * 输出(UTF-8)：
 *     第一行：返回世界服务器连接地址
 *     第二行：返回服务器标题
 *     第三行：返回充值页面地址
 *     第四行：返回客户端下载地址
 *     第五行：返回入口web地址
 *     第六行：返回页面标题
 *     第七行：返回充值转发地址
 * @author lighthu
 */
public class ServerInfoServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(ServerInfoServlet.class);
    private ServerInfoDAO serverInfoDAO;
    private Set<String> allows = null;
    private List<String> allowPrefix = null;
    
    public ServerInfoServlet(String[] allows) {
    	serverInfoDAO = new ServerInfoDAO();
        if (allows != null) {
        	this.allows = new HashSet<String>();
        	this.allowPrefix = new ArrayList<String>();
	        for (String s : allows) {
	        	if (s.endsWith("*")) {
	        		this.allowPrefix.add(s.substring(0, s.length() - 1));
	        	} else {
	        		this.allows.add(s);
	        	}
	        }
        }
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {
    	// 验证请求IP
    	if (allows != null) {
    		String addr = request.getRemoteAddr();
    		boolean ok = false;
    		if (allows.contains(addr)) {
    			ok = true;
    		} else {
    			for (String prefix : allowPrefix) {
    				if (addr.startsWith(prefix)) {
    					ok = true;
    					break;
    				}
    			}
    		}
    		if (!ok) {
    			log.warn("Possible attack from [" + addr + "] is rejected.");
    			return;
    		}
        }
        
    	int regionID = Integer.parseInt(request.getParameter("regionid"));

        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        ServerInfo server = serverInfoDAO.getByRegionID(regionID);
        String worldURL = "socket://" + server.getWorldHost() + ":8080#7F000001";
        String portStr = Integer.toHexString(server.getPort()).toUpperCase();
        while (portStr.length() < 4) {
        	portStr = "0" + portStr;
        }
        worldURL += portStr;
        out.println(worldURL);
        out.println(server.getTitle());
        out.println(server.getPayURL());
        out.println(server.getResourceURL());
        out.println("http://" + server.getHost() + "/sanguo/");
        out.println(server.getPageTitle());
        String chargeURL = "http://" + server.getWorldHost() + ":" + server.getHttpPort() + "/charge5ding";
        out.println(chargeURL);
    }
}
