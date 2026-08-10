package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
/**
 * 申请创建支付宝订单<Android客户端专用>
 * 
 * @author jyu
 *
 */
public class GetOrder4ClientAlipayServlet extends HttpServlet {
	private final Random rnd = new Random();
	private static Logger log = Logger.getLogger(GetOrderAlipayServlet.class);
	private Order_AlipayDAO dataDAO;
	private Server server;
	/**
	 * Constructor of the object.
	 */
	public GetOrder4ClientAlipayServlet(Server s,Order_AlipayDAO dao) {
		server = s;
		dataDAO = dao;
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	@Override
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request,response);
	}

	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
        // 取得参数
        String name = request.getParameter("name");
        int id = -1;
        if (name == null || name.length()==0) {
            id = Integer.parseInt(request.getParameter("id"));
        }
        int amount = Integer.parseInt(request.getParameter("amount"));
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));

        if (name == null || name.length()==0) {
            log.info("[alipay_order_4client]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + "]");
        } else {
            log.info("[alipay_order_4client]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + "]");
        }

        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // 验证用户名和密码
        Account acc;
        if (name != null && name.length()!=0) {
            acc = server.findAccountByName(name);
        } else {
            acc = server.findAccount(id);
        }
        if (acc == null) {
            log.info("[alipay_order_4client] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (amount<1&&amount>2000) {
            log.info("[alipay_order_4client] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Order_Alipay order = new Order_Alipay();
        order.setAccountID(acc.getId());
        order.setUserName(acc.getName());
        order.setCreateTime(new java.util.Date());
        order.setMoney(amount);
        order.setStatus(0);
        order.setGameCode(gameCode);
        
        String payseq = System.currentTimeMillis()+ Tools.getRandom(rnd);    	
        order.setPaySeq(payseq);
        
        order.setImoney(Tools.calcIMoney(amount*100));
       
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[alipay_order_4client] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
               
		//构造完整的URL
		String notifyurl =  server.getServerURL() + "/alipay_notify_4client?oid="+order.getId()+"&amp;sid="+payseq;
		out.println("0");
        out.println("元宝");
        out.println(payseq);
        out.println(Tools.partner_4Client);
        out.println(Tools.seller_4Client);
        out.println(notifyurl);
        out.println(Tools.publicKey_4Client);
        out.println(Tools.privateKey_4Client);
        out.println(Tools.validateKey_4Client);
	}


	@Override
	public void init() throws ServletException {
		// Put your code here
	}

}
