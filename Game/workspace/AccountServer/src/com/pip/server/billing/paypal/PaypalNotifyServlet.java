package com.pip.server.billing.paypal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;
/**
 * Paypal充值结果通知
 * 
 */
@SuppressWarnings("serial")
public class PaypalNotifyServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(PaypalNotifyServlet.class);
	private Server server;
	private Order_PaypalDAO dataDAO;
	
	static HashMap<String, String> orderMap = new HashMap<String, String>(); //订单地址
	
	public PaypalNotifyServlet(Server server,Order_PaypalDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("[paypal_notify]");
		
		boolean validate = doValidate(request,response);

		if(!validate){
			return;
		}

		// 将 POST 信息分配给本地变量，可以根据您的需要添加
		// 该付款明细所有变量可参考：
		// https://www.paypal.com/IntegrationCenter/ic_ipn-pdt-variable-reference.html
		String itemName = request.getParameter("item_name");
		String itemNumber = request.getParameter("item_number");//传递的是订单号
		String paymentStatus = request.getParameter("payment_status");
		String paymentAmount = request.getParameter("mc_gross");
		String paymentCurrency = request.getParameter("mc_currency");
		String txnId = request.getParameter("txn_id");
		String receiverEmail = request.getParameter("receiver_email");
		String payerEmail = request.getParameter("payer_email");
		
		String mc_fee = request.getParameter("mc_fee");
		String receipt_id = request.getParameter("receipt_id");
		
		StringBuffer sbuf  = new StringBuffer();
		sbuf.append("[paypal_notify]")
		.append("item_name[").append(itemName).append("]")
		.append("item_number[").append(itemNumber).append("]")
		.append("payment_status[").append(paymentStatus).append("]") 
		.append("mc_gross[").append(paymentAmount).append("]")
		.append("mc_currency[").append(paymentCurrency).append("]")
		.append("txn_id[").append(txnId).append("]") 
		.append("receiver_email[").append(receiverEmail).append("]")
		.append("payer_email[").append(payerEmail).append("]")
		.append("mc_fee[").append(mc_fee).append("]")
		.append("receipt_id[").append(receipt_id).append("]");
		log.info(sbuf.toString());
		
		try {
			// 获得通知参数	
			String order_no = itemNumber;
			
			// 查找订单数据
			Order_Paypal order = dataDAO.getBySeqID(order_no);
	        if (order == null ) {
	            log.info("[paypal_notify] 订单不存在");
	            return;
	        }
	        if(order.getMoney()!=(Double.parseDouble(paymentAmount)*100)||!order.getCurrencyCode().equals(paymentCurrency)){
	        	log.info("[paypal_notify] 货币，金额 同 订单不匹配，["+order_no+"]"+paymentAmount+paymentCurrency);
	        }
	        // 把结果保存在缓存中等待查询
	        ConstPaypal.orderResults.put(order_no, new String[] { order_no, "结果参数" });
	        
	        //修改订单状态并为用户添加i币
	        if (validate) {
	            if (order.getStatus() != 1 ) {
	                try {
	                    int feeid = addIMoney(order);
	                    if(feeid > 0){
	                    	order.setFeeId(feeid);
	                    }
	                } catch (Exception e) {
	                    log.error(e, e);
	                    log.info("[paypal_notify] 添加i币失败");
	                    return;
	                }
	                log.info("[paypal_notify] ChargeOK");
	                order.setStatus(1);
	                order.setPaypalID(receipt_id);
	                if(mc_fee!=null){
	                	order.setFeeamount((int)(Double.parseDouble(mc_fee)*100));
	                }
	            } else {
	                log.info("[paypal_notify] Ignored");
	            }	            
	        } else {
	            order.setStatus(2);
	        }
	        
	        order.setFinishTime(new java.util.Date());	       
	        dataDAO.update(order);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }
    
    private int addIMoney(Order_Paypal order) throws Exception {
        // 在认证服务器创建订单
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, "Paypal_" + (order.getRmbmoney()/100));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getRmbmoney()/100);
        
        return fee.getId();
    }
    
    //接收paypal信息，并验证信息是否正确
    @SuppressWarnings("unchecked")
	public boolean doValidate(HttpServletRequest request, HttpServletResponse response){
    	
    	//从PayPal出读取POST信息同时添加变量
		Enumeration en = request.getParameterNames();
		String str = "cmd=_notify-validate";
		while (en.hasMoreElements()) {
			String paramName = (String) en.nextElement();
			String paramValue = request.getParameter(paramName);
			try {
				str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue,"UTF-8"); //"iso-8859-1"
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		// 将信息POST回给PayPal进行验证，接收IPN回复信息
		URL u = null;
		URLConnection uc = null;
		PrintWriter pw = null;
		BufferedReader in = null;
		String res = null;
		try {
			u = new URL(ConstPaypal.ORDER_URL);
			uc = u.openConnection();
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			pw = new PrintWriter(uc.getOutputStream());
			pw.println(str);
			pw.close();
			
			in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			res = in.readLine();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			
			try {
				pw.close();
				in.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取 PayPal 对回发信息的回复信息，判断刚才的通知是否为 PayPal 发出的
		if (res.equals("VERIFIED")) {
			log.info("[paypal_notify]VERIFIED:"+str);
			return true;
		} else if (res.equals("INVALID")) {
			// 非法信息，可以将此记录到您的日志文件中以备调查
			log.info("[paypal_notify]INVALID:"+str);
		} 
		return false;
    }
}
