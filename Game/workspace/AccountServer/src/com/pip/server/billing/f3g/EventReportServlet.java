package com.pip.server.billing.f3g;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
 * 世界服务器向事件服务器报告新的游戏事件。
 * @author lighthu
 */
public class EventReportServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(EventReportServlet.class);
    private Set<String> allows = null;
    private List<String> allowPrefix = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public EventReportServlet(String[] allows) {
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
        
    	try {
	    	GameEvent evt = new GameEvent();
	    	evt.serverID = Integer.parseInt(request.getParameter("serverID"));
	    	evt.accountName = request.getParameter("accountName");
	    	evt.playerName = request.getParameter("playerName");
	    	evt.eventType = Integer.parseInt(request.getParameter("eventType"));
	    	evt.eventInfo = request.getParameter("eventInfo");
	    	evt.eventTime = dateFormat.parse(request.getParameter("eventTime"));
	    	GameEventManager.addEvent(evt);
    	} catch (Exception e) {
    		log.error(e, e);
    		throw new ServletException(e);
    	}

        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("OK");
    }
}
