package com.pip.server.billing.u19pay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.server.account.bean.Account;
import com.pip.server.billing.Server;

/**
 * 19PAY渠道下单（直连方式）。
 * 请求参数：
 *     id = 帐号ID
 *     name = 帐号名
 *     上面2个参数任意指定一个即可
 *     amount = 金额(分)
 *     gamecode = 游戏代码
 *     cardno = 卡号
 *     cardpass = 密码
 *     cardtype = 卡类型（0 - 全国联通一卡充，1 - 全国移动充值卡，2 - 辽宁移动电话交费卡，3 - 江苏移动充值卡，4 - 浙江移动缴费券，5 - 福建移动呱呱通充值卡）
 *     channel = 用户渠道号
 *     returnhttp = 结果通知回调地址
 * 返回(UTF-8编码)：
 *     第一行是返回代码，0表示成功，1表示失败
 *     如果失败，第二行是错误信息，如果成功，第二行是订单ID
 */
public class GetOrderServlet_19PayD extends HttpServlet {
    private static Logger log = Logger.getLogger(GetOrderServlet_19PayD.class);
    private Order_19PayDAO dataDAO;
    private Server server;
    private AtomicInteger idGenerator = new AtomicInteger(10000);
    static ConcurrentHashMap<String, String> callbacks = new ConcurrentHashMap<String, String>();
    
    public GetOrderServlet_19PayD(Server s, Order_19PayDAO dao) {
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
        String idStr = request.getParameter("id");
        int id = -1;
        if (idStr != null) {
        	try {
        		id = Integer.parseInt(idStr);
        	} catch (Exception e) {
        	}
        }
        String name = null;
        if (id == -1) {
        	name = request.getParameter("name");
        }
        int amount = Integer.parseInt(request.getParameter("amount"));
        int gameCode = Integer.parseInt(request.getParameter("gamecode"));
        String cardNo = request.getParameter("cardno").trim();
        String cardPass = request.getParameter("cardpass").trim();
        int cardType = Integer.parseInt(request.getParameter("cardtype"));
        String channel = request.getParameter("channel");
        String callbackURL = request.getParameter("returnhttp");
        String[] ret = placeOrder(id, name, amount, gameCode, cardNo, cardPass, cardType, channel, callbackURL);
        
        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(ret[0]);
        out.println(ret[1]);
    }
    
    public String[] placeOrder(int id, String name, int amount, int gameCode, String cardNo, 
    		String cardPass, int cardType, String channel, String callbackURL) { 
    	// 检查更新配置
    	Const19Pay.checkParam();

    	if (name == null) {
            log.info("[19payd_order]accountid[" + id + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardtype[" + cardType + "]returnurl[" + callbackURL + "]");
        } else {
            log.info("[19payd_order]accountname[" + name + "]amount[" + amount + "]gamecode[" + gameCode + 
                    "]cardno[" + cardNo + "]cardpass[" + cardPass + "]cardtype[" + cardType + "]returnurl[" + callbackURL + "]");
        }
        
        // 2次检查卡号和密码长度，判断卡类型
        if (cardType == 0) {
	        if (cardNo.length() == 17 && cardPass.length() == 18) {
	        	// 移动全国标准卡
	        	cardType = 1;
	        } else if (cardNo.length() == 16 && cardPass.length() == 17) {
	        	// 福建呱呱通充值卡
	        	cardType = 5;
	        } else if (cardNo.length() == 10 && cardPass.length() == 8) {
	        	// 浙江移动电子缴费券
	        	cardType = 4;
	        } else if (cardNo.length() == 16 && cardPass.length() == 21) {
	        	// 辽宁移动充值卡
	        	cardType = 2;
	        }
        }

        // 验证用户名和密码
        Account acc;
        if (name != null) {
            acc = server.findAccountByName(name);
        } else {
            acc = server.findAccount(id);
        }
        if (acc == null) {
            log.info("[19payd_order] 帐号不存在");
            return new String[] { "1", "账号不存在" };
        }
        
        // 验证金额
        if (!Const19Pay.IMONEY_MAP.containsKey(amount)) {
            log.info("[19payd_order] 金额错误");
            return new String[] { "2", "金额错误" };
        }

        // 检查卡号密码
        if (!Const19Pay.checkInput(cardNo, cardPass)) {
        	log.info("[19payd_order] 卡号密码格式错误");
        	return new String[] { "2", "卡号密码格式错误" };
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
        order.setCardNo(cardNo);
        order.setCardPass(cardPass);
        order.setCardType(cardType);
        dataDAO.create(order);
        if (order.getId() == 0) {
            log.info("[19payd_order] 创建订单失败");
            return new String[] { "3", "创建订单失败" };
        }
        
        // 向如意付平台发起支付请求
        String notifyURL = server.getServerURL() + "/19payd_notify";
        PostMethod method = new PostMethod(Const19Pay.ORDER_URL_D);
        method.addRequestHeader( "Connection", "close");
        String[] ret = new String[] { "4", "访问支付平台失败" };
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            method.addParameter("version_id", Const19Pay.VERSION);
            method.addParameter("merchant_id", Const19Pay.MERCHANT_ID);
            String dateStr = Const19Pay.DATE_FORMAT.format(order.getCreateTime());
            method.addParameter("order_date", dateStr);
            method.addParameter("order_id", String.valueOf(order.getId()));
            String amountStr = (order.getMoney() / 100) + ".00";
            method.addParameter("amount", amountStr);
            String cardStr = CipherUtil.encryptData(cardNo, Const19Pay.MERCHANT_KEY);
            method.addParameter("cardnum1", cardStr);
            String passStr = CipherUtil.encryptData(cardPass, Const19Pay.MERCHANT_KEY);
            method.addParameter("cardnum2", passStr);
            method.addParameter("currency", "RMB");
            String pm_id = Const19Pay.CARD_TYPES[cardType][1];
            method.addParameter("pm_id", pm_id);
            String pc_id = Const19Pay.CARD_TYPES[cardType][0];
            method.addParameter("pc_id", pc_id);
            method.addParameter("notify_url", notifyURL);

            String verifyStr = "version_id=" + Const19Pay.VERSION + "&merchant_id=" + Const19Pay.MERCHANT_ID + 
            	"&order_date=" + dateStr + "&order_id=" + order.getId() + "&amount=" + amountStr + 
            	"&currency=RMB&cardnum1=" + cardStr + "&cardnum2=" + passStr + "&pm_id=" + pm_id + 
            	"&pc_id=" + pc_id + "&merchant_key=" + Const19Pay.MERCHANT_KEY;
            log.info(verifyStr);
            method.addParameter("verifystring", Const19Pay.getMD5(verifyStr));

            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String s = method.getResponseBodyAsString().trim();
                log.info("[19payd_order] " + s);
                
            	SAXReader reader = new SAXReader();
                Reader r = new StringReader(s);
                Document doc = reader.read(r);
                Element root = doc.getRootElement();
                
                String paySeq = root.elementText("pay_sq");
                String result = root.elementText("result");
                int resultCode = Integer.parseInt(root.elementText("resultstr"));
                
                order.setPaySeq(paySeq);
                if ("P".equals(result)) {
                	// 成功
                	ret = new String[] { "0", String.valueOf(order.getId()) };
                	if (callbackURL != null) {
                        callbacks.put(String.valueOf(order.getId()), callbackURL);
                    }
                } else {
                	ret = new String[] { "5", Const19Pay.getErrorMessage(resultCode) };
                	order.setStatus(2);
                }
            } else {
                log.info("[19payd_order] code=" + code);
                ret = new String[] { "4", "访问支付平台失败" };
                order.setStatus(2);
            }
            dataDAO.update(order);
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        } finally {
            method.releaseConnection();
        }
        return ret;
    }
}
