package com.pip.server.billing.appstore;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

/**
 * Appstore订单结果验证接口。如果验证通过，直接加元宝并返回。一个transactionid只能充值一次。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     gamecode = 游戏代码
 *     bid = 产品bundle id
 *     receipt = BASE64编码的订单收据
 *     channel = 用户渠道号
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果失败，第二行是错误信息，如果成功，第二行是成功添加的i币数量（单位1/100i）
 */
public class AppStoreOrderServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(AppStoreOrderServlet.class);
    private Server server;
    
    public AppStoreOrderServlet(Server s) {
        server = s;
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
    	// 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 取得参数
        String name = request.getParameter("name");
        int id = -1;
        if (name == null) {
            id = Integer.parseInt(request.getParameter("id"));
        }
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        String receipt = request.getParameter("receipt");
        String channel = request.getParameter("channel");
        String bid = request.getParameter("bid");
        
        if (name == null) {
            log.info("[appstore_order]accountid[" + id + "]gamecode[" + gameCode + 
                    "]bid[" + bid + "]receipt[" + receipt + "]channel[" + channel + "]");
        } else {
            log.info("[appstore_order]accountname[" + name + "]gamecode[" + gameCode + 
                    "]bid[" + bid + "]receipt[" + receipt + "]channel[" + channel + "]");
        }

        // 设置返回格式
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证用户名和密码
        Account acc;
        if (name != null) {
            acc = server.findAccountByName(name);
        } else {
            acc = server.findAccount(id);
        }
        if (acc == null) {
            log.info("[appstore_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证应用是否支持
        if (!ConstAppStore.isAppValid(bid)) {
        	log.info("[appstore_order] 不支持此应用");
            out.println("1");
            out.println("不支持此应用");
        	return;
        }
        
        int ret = AppStoreOrderManager.tryPlaceOrder(bid, gameCode, channel, acc, receipt);
        if (ret >= 0) {
        	out.println("0");
        	out.println(ret);
        } else if (ret == -1) {
        	out.println("1");
        	out.println("订单无效");
        } else {
        	out.println("1");
        	out.println("抱歉，iTunes平台暂时无法连接，稍后我们会自动为您重试。");
        }
    }
}
