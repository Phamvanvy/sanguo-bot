package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alipay.util.AlipayService;
import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;
/**
 * 申请创建支付宝订单<WEB支付专用>
 * 
 * @author jyu
 *
 */
public class GetOrder4WebAlipayServlet extends HttpServlet {
	private final Random rnd = new Random();
	private static Logger log = Logger.getLogger(GetOrderAlipayServlet.class);
	private Order_AlipayDAO dataDAO;
	private Server server;
	/**
	 * Constructor of the object.
	 */
	public GetOrder4WebAlipayServlet(Server s,Order_AlipayDAO dao) {
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
        int amount = Integer.parseInt(request.getParameter("amount"));//单位分
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        String returnhttp = request.getParameter("returnhttp");
        if (name == null || name.length()==0) {
            log.info("[alipay_order_4web]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + "]");
        } else {
            log.info("[alipay_order_4web]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + "]");
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
            log.info("[alipay_order_4web] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (amount<1&&amount>200000) {
            log.info("[alipay_order_4web] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Order_Alipay order = new Order_Alipay();
        order.setAccountID(acc.getId());
        order.setUserName(acc.getName());
        order.setCreateTime(new java.util.Date());
        order.setMoney(amount/100);
        order.setStatus(0);
        order.setGameCode(gameCode);
        
        String payseq = System.currentTimeMillis()+ Tools.getRandom(rnd);    	
        order.setPaySeq(payseq);
        
        order.setImoney(Tools.calcIMoney(amount));
       
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[alipay_order_4web] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
               
		//构造完整的URL
		String notifyurl =  server.getServerURL() + "/alipay_notify_4web";
		String callbackurl =  server.getServerURL() + "/alipay_callback_4web";
		String goodsName = "i币";
		String goodsIntr ="可以用来购买掌上明珠旗下游戏产品商城中的商品,或者使用一些便捷功能。";
		
		if(gameCode==6){//三国
			goodsName = "元宝";
		}
		
		//保存订单回调地址
		AlipayCallBack4WebServlet.orderMap.put(payseq, returnhttp==null?"":returnhttp);
		
		/**向支付宝网关发送支付请求**/
		StringBuffer sbufhtml = new StringBuffer();
		sbufhtml.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		sbufhtml.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
		
		//修改配置参数, 改为统一的账号配置信息
//		sbufhtml.append(AlipayService.BuildForm(AlipayConfig.partner, AlipayConfig.seller_email,
//				callbackurl, 
//				notifyurl, 
//				AlipayConfig.show_url, payseq, goodsName,goodsIntr, amount/100+"", 
//				"directPay", "", "", 
//				"", "", 
//				"", "", "", "", AlipayConfig.input_charset, AlipayConfig.key, AlipayConfig.sign_type));		
//		sbufhtml.append("</body></html>");
		sbufhtml.append(AlipayService.BuildForm(Tools.partner_4web, Tools.seller_4web,
				callbackurl, 
				notifyurl, 
				Tools.show_url_4web, payseq, goodsName,goodsIntr, amount/100+"", 
				"directPay", "", "", 
				"", "", 
				"", "", "", "", Tools.input_charset_4web, Tools.key_4web, Tools.sign_type_4web));		
		sbufhtml.append("</body></html>");
		out.println("0");
		out.println(sbufhtml.toString());
	}


	@Override
	public void init() throws ServletException {
		// Put your code here
	}

}
