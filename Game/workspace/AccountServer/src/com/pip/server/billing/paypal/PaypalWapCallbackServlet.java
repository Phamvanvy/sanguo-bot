package com.pip.server.billing.paypal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

@SuppressWarnings("serial")
public class PaypalWapCallbackServlet extends HttpServlet{
	
	private Server server;
	private Order_PaypalDAO dataDAO;
	
	static HashMap<String, String> returnURLMap = new HashMap<String, String>();
	
	private static Logger log = Logger.getLogger(PaypalWapCallbackServlet.class);
	
	public PaypalWapCallbackServlet(Server s,Order_PaypalDAO dao){
		server = s;
		dataDAO = dao;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {
      
		String tocken = request.getParameter("token");
		String payerID = request.getParameter("PayerID");
		
		boolean isOK = false;
		HashMap<String,String> result = null;
		String order_no = null;
		String transID = null;//paypal交易号
		double amount = 0;
		double feeamount = 0; //费用（手续费）
		String currencyCode = "";
		String errcode = "";
		try{
			result = requestCheckoutDetails(tocken);
			String act = result.get("ACK");
            if("success".equals(act.toLowerCase())|| "successwithwarning".equals(act.toLowerCase())){//执行成功
            	order_no = result.get("INVNUM");
            	result = doExpressCheckoutPayment(tocken,payerID);
            	
            	if("success".equals(act.toLowerCase())|| "successwithwarning".equals(act.toLowerCase())){//执行成功
            		transID = result.get("TRANSACTIONID");
            		amount = Double.parseDouble( result.get("AMT"));
            		currencyCode = result.get("CURRENCYCODE");
            		
            		if(result.get("FEEAMT")!=null){
            			feeamount = Double.parseDouble( result.get("FEEAMT"));
            		}
            		isOK = true;
            	}else{
            		errcode = result.get("L_ERRORCODE0");
            	}
            }else{
            	errcode = result.get("L_ERRORCODE0");
            }
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String returnhttp = returnURLMap.get(order_no);
		if(returnhttp==null || returnhttp.trim().equals("")){
        	returnhttp = ConstPaypal.callbackURL;
        } 
		if(returnhttp.endsWith("?")){
		}else if(returnhttp.indexOf("&")>0){
			returnhttp = returnhttp + "&" ;
		}else{
			returnhttp = returnhttp + "?" ;
		}
		if(!isOK){
			returnhttp = returnhttp+"&retcode=2&retmsg="+errcode;
			response.sendRedirect(returnhttp);
			return;
		}
		try {
			// 查找订单数据
			Order_Paypal order = dataDAO.getBySeqID(order_no);
			if (order == null) {
				log.info("[paypal_wap_callback] 订单不存在");
				returnhttp = returnhttp+"&retcode=2&retmsg=orderNotExist";
				response.sendRedirect(returnhttp);
				return;
			}
			if (order.getMoney() != (amount * 100)
					|| !order.getCurrencyCode().equals(currencyCode)) {
				log.info("[paypal_wap_callback] 货币，金额 同 订单不匹配，[" + order_no
						+ "]" + amount + currencyCode);
			}
			// 把结果保存在缓存中等待查询
			ConstPaypal.orderResults.put(order_no, new String[] { order_no,"结果参数" });

			// 修改订单状态并为用户添加i币
			if (order.getStatus() != 1) {
				try {
					int feeid = addIMoney(order);
					if (feeid > 0) {
						order.setFeeId(feeid);
					}
				} catch (Exception e) {
					log.error(e, e);
					log.info("[paypal_wap_callback] 添加i币失败");
					returnhttp = returnhttp + "&retcode=2&retmsg=addMoneyError";
					response.sendRedirect(returnhttp);
					return;
				}
				log.info("[paypal_wap_callback] ChargeOK");
				order.setStatus(1);
				order.setPaypalID(transID);
				order.setFeeamount((int)(feeamount*100));
			} else {
				log.info("[paypal_wap_callback] Ignored");
			}
			
			order.setFinishTime(new java.util.Date());
			dataDAO.update(order);
		} catch (Exception e) {
			e.printStackTrace();
		}
		returnhttp = returnhttp+"&retcode=0";
		response.sendRedirect(returnhttp);
	}
	
	private static HashMap<String,String> requestCheckoutDetails(String tocken) throws Exception {

        String url = ConstPaypal.WAP_URL_TOCKEN_REQUEST ;
        StringBuffer query = new StringBuffer("METHOD=GetExpressCheckoutDetails");
        query.append("&USER=").append(ConstPaypal.WAP_USER);
        query.append("&PWD=").append(ConstPaypal.WAP_PWD);
        query.append("&SIGNATURE=").append(ConstPaypal.WAP_SIGN);
        query.append("&VERSION=").append(ConstPaypal.WAP_VERSION);

        query.append("&TOKEN=").append(tocken);
        return requestData(url,query.toString());
    }
	
    private static HashMap<String,String> doExpressCheckoutPayment(String tocken,String playerID) throws Exception {
            String url = ConstPaypal.WAP_URL_TOCKEN_REQUEST ;
            StringBuffer query = new StringBuffer("METHOD=DoExpressCheckoutPayment");
            query.append("&USER=").append(ConstPaypal.WAP_USER);
            query.append("&PWD=").append(ConstPaypal.WAP_PWD);
            query.append("&SIGNATURE=").append(ConstPaypal.WAP_SIGN);
            query.append("&VERSION=").append(ConstPaypal.WAP_VERSION);

            query.append("&TOKEN=").append(tocken);
            query.append("&PAYERID=").append(playerID);
            query.append("&PAYMENTACTION=Sale");
            query.append("&AMT=1.5");
            return requestData(url,query.toString());
    }
    
    private int addIMoney(Order_Paypal order) throws Exception {
        // 在认证服务器创建订单
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, "Paypalwap_" + (order.getRmbmoney()/100));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getRmbmoney()/100);
        
        return fee.getId();
    }
    private static HashMap<String,String> requestData(String url,String queryStr) throws Exception {
       
    	HttpURLConnection conn = null;
        InputStream is = null;
        OutputStream out = null;

        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = conn.getOutputStream();
            out.write(queryStr.getBytes("utf-8"));
            out.flush();
            out.close();

            int retCode = conn.getResponseCode();
            if(retCode == 200){
                is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = br.readLine();
                //解析名值对
                HashMap<String,String> params = ConstPaypal.parseParams(line);
                Iterator it = params.keySet().iterator();
                String str = "";
                while (it.hasNext()) {
                    String paramName = (String) it.next();
                    String paramValue = params.get(paramName);
                    try {
                        str = str + "&" + paramName + "=" +
                              URLEncoder.encode(paramValue, "UTF-8");

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                log.info("[paypal_wap_callback]"+str);
                return params;
            }

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }

            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
}
