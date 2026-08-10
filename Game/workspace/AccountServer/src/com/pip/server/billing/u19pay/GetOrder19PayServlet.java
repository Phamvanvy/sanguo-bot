package com.pip.server.billing.u19pay;

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
 * 19Pay渠道下单。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     amount = 金额(分)
 *     gamecode = 游戏代码
 *     channel = 用户渠道号
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果成功，第二行是重定向地址，第三行往后每行表示一个提交给此地址的参数，格式是key=value
 *     如果失败，第二行是错误信息
 */
public class GetOrder19PayServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(GetOrder19PayServlet.class);
	private Order_19PayDAO dataDAO;
	private Server server;
	
	public GetOrder19PayServlet(Server s, Order_19PayDAO dao) {
		server = s;
		dataDAO = dao;
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
        int amount = Integer.parseInt(request.getParameter("amount"));
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        String channel = request.getParameter("channel");
        
        if (name == null) {
            log.info("[19pay_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + "]");
        } else {
            log.info("[19pay_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + "]");
        }

        // 设置返回格式
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
            log.info("[19pay_order] 帐号不存在");
            out.println("1");
            out.println("帐号不存在");
            return;
        }
        
        // 验证金额
        if (!Const19Pay.IMONEY_MAP.containsKey(amount)) {
            log.info("[19pay_order] 金额错误");
            out.println("2");
            out.println("金额错误");
            return;
        }
        
        // 创建新的订单
        Order_19Pay order = new Order_19Pay();
        order.setAccountID(acc.getId());
        order.setUserName(acc.getName());
        order.setCreateTime(new java.util.Date());
        order.setMoney(amount);
        order.setStatus(0);
        order.setGameCode(gameCode);
        order.setImoney(Const19Pay.IMONEY_MAP.get(amount));
        order.setChannel(channel);
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[19pay_order] 创建订单失败");
            out.println("3");
            out.println("创建订单失败");
            return;
        }
        
        // 返回信息
        String callbackURL = server.getServerURL() + "/19pay_callback";
        String orderDate = Const19Pay.DATE_FORMAT.format(order.getCreateTime());
        String verifyStr = "version_id=" + Const19Pay.VERSION + "&merchant_id=" + 
            Const19Pay.MERCHANT_ID + "&order_date=" + orderDate + "&order_id=" +
            order.getId() + "&amount=" + (amount / 100) + ".00&currency=" +
            Const19Pay.CURRENCY_RMB + "&returl=" + callbackURL + "&pm_id=" + 
            Const19Pay.METHOD_UNICOM + "&pc_id=&merchant_key=" + Const19Pay.MERCHANT_KEY;
        verifyStr = Const19Pay.getMD5(verifyStr);
        out.println("0");
        out.println(Const19Pay.ORDER_URL);
        out.println("version_id=" + Const19Pay.VERSION);
        out.println("merchant_id=" + Const19Pay.MERCHANT_ID);
        out.println("verifystring=" + verifyStr);
        out.println("order_date=" + orderDate);
        out.println("order_id=" + order.getId());
        out.println("amount=" + (amount / 100) + ".00");
        out.println("currency=" + Const19Pay.CURRENCY_RMB);
        out.println("returl=" + callbackURL);
        out.println("pm_id=" + Const19Pay.METHOD_UNICOM);
        out.println("pc_id=" + "");
        
        log.info("[19pay_order]accountid[" + acc.getId() + "]amount[" + amount + "]gamecode[" + gameCode + "]result[ok]");
    }
}
