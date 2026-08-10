package com.pip.server.billing.f3g;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.util.ajax.JSON;

/**
 * 获取事件列表（畅游）。
 * @author lighthu
 */
public class EventListServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(EventListServlet.class);
    private Set<String> allows = null;
    private List<String> allowPrefix = null;
    
    public EventListServlet(String[] allows) {
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
	    	String[] sers = request.getParameter("ser").split(",");
	    	int[] serIDs = new int[sers.length];
	    	for (int i = 0; i < sers.length; i++) {
	    		serIDs[i] = Integer.parseInt(sers[i]);
	    	}
	    	int limit = Integer.parseInt(request.getParameter("limit"));
	    	int[] types;
	    	String typeStr = request.getParameter("type");
	    	if (typeStr == null || typeStr.length() == 0) {
	    		types = new int[0];
	    	} else {
	    		String[] secs = typeStr.split(",");
	    		types = new int[secs.length];
	    		for (int i = 0; i < secs.length; i++) {
	    			types[i] = Integer.parseInt(secs[i]);
	    		}
	    	}
	    	List<GameEvent> events = GameEventManager.getEvent(serIDs, limit, types);
	    	Object[] objs = new Object[events.size()];
	    	for (int i = 0; i < events.size(); i++) {
	    		GameEvent evt = events.get(i);
	    		Map map = new HashMap();
	    		map.put("ser_id", String.valueOf(evt.serverID));
	    		map.put("user_code", evt.accountName);
	    		map.put("user_role", evt.playerName);
	    		map.put("log_info", evt.eventInfo);
	    		map.put("logtime", getTimeStr(evt.eventTime));
	    		objs[i] = map;
	    	}

	    	response.setContentType("text/plain;charset=utf-8");
	        response.setCharacterEncoding("UTF-8");
	        PrintWriter out = response.getWriter();
	        out.print(JSON.toString(objs));
    	} catch (Exception e) {
    		log.error(e, e);
    		throw new ServletException(e);
    	}
    }
    
    /**
     * 得到一个日期距离现在过去的时间。
     * @param date
     * @return
     */
    public static String getTimeStr(Date date){
    	if (date == null) {
    		return "";
    	}
    	long remain = (System.currentTimeMillis() - date.getTime()) / 60000L;
    	if (remain <= 0) {
    		return "1分钟前";
    	} else if (remain < 60) {
    		return remain + "分钟前";
    	} else if (remain < 60 * 24) {
    		return (remain / 60) + "小时前";
    	} else {
    		return (remain / 1440) + "天前";
    	}
    }
}
