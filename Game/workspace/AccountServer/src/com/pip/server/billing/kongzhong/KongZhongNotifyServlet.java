package com.pip.server.billing.kongzhong;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.billing.Server;

/**
 * 空中网短信渠道通知。
 * 请求参数：
 *     toicp：上行队列
 *     spid：cmcc(运营商)
 *     linkid： 唯一标识号
 *     innerid：时间戳
 *     channel：渠道
 *     fromobile：手机号
 *     content：用户上行内容
 * 返回：
 *     Y
 */
public class KongZhongNotifyServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(KongZhongNotifyServlet.class);
    private Server server;
    
    public KongZhongNotifyServlet(Server s) {
        server = s;
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        String addr = request.getRemoteAddr();
        if (!addr.equals("61.135.154.102")) {
            log.warn("[kongzhong_notify]Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 得到输入
        String toicp = request.getParameter("toicp");
        String spid = request.getParameter("spid");
        String linkid = request.getParameter("linkid");
        String innerid = request.getParameter("innerid");
        String channel = request.getParameter("channel");
        String fromobile = request.getParameter("fromobile");
        String content = request.getParameter("content");
        
        log.info("[kongzhong_notify]toicp[" + toicp + "]spid[" + spid + "]linkid[" + linkid + 
                "]innerid[" + innerid + "]channel[" + channel + "]fromobile[" + fromobile + "]content[" + content + "]");
        
        // 设置返回格式
        response.setCharacterEncoding("GBK");
        PrintWriter out = response.getWriter();
        
        // 返回
        out.print("Y");
    }
}
