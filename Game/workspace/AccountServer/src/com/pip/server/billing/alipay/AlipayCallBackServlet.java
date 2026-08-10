package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

public class AlipayCallBackServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(AlipayCallBackServlet.class);
	private Server server;
	private Order_AlipayDAO dataDAO;
	
	public AlipayCallBackServlet(Server server,Order_AlipayDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("[alipay_callback]");
		// 获得通知参数
		Map map = request.getParameterMap();
		try {
			// 获得通知签名
			String sign = (String) ((Object[]) map.get("sign"))[0];

			// 获得待验签名的数据
			String verifyData = getVerifyData(map);
			boolean verified = false;

			String oid = request.getParameter("oid");
			String sid = request.getParameter("sid");
			String result = request.getParameter("result");

			log.info("[alipay_callback]:oid=" + oid + ",sid=" + sid);
			
			PrintWriter out = response.getWriter();
			
			// 查找订单数据
	        Order_Alipay order = dataDAO.getByID(Integer.parseInt(oid));
	        if (order == null || !order.getPaySeq().equals(sid)) {
	            log.info("[alipay_callback] 订单不存在");
	            response.sendRedirect(Tools.CALLBACK_URL + "?gamecode=1&code=2");
	            return;
	        }

			// 验签名			
			verified = Tools.securityManager.verify(Tools.clientConfig
					.getSignAlgo(), verifyData, sign, Tools.clientConfig
					.getAlipayPubKey());			

			System.out.println("Veri:"+verified);
			System.out.println("Result:"+result);
			//修改订单状态并为用户添加i币
	        if (verified && result.equals("success") ) {
	            if (order.getStatus() != 1) {
	                try {
	                    addIMoney(order);
	                } catch (Exception e) {
	                    log.error(e, e);
	                    log.info("[alipay_callback] 添加i币失败");
	                    out.print("N");
	                    return;
	                }
	                log.info("[alipay_callback] ChargeOK");
	            } else {
	                log.info("[alipay_callback] Ignored");
	            }
	            order.setStatus(1);
	        } else {
	            order.setStatus(2);
	        }
	        order.setFinishTime(new java.util.Date());	       
	        dataDAO.update(order);
	        if (verified) {
	            response.sendRedirect(Tools.CALLBACK_URL + "?gamecode=" + order.getGameCode() + "&code=0");
	        } else {
	            response.sendRedirect(Tools.CALLBACK_URL + "?gamecode=" + order.getGameCode() + "&code=3");
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }
    
	/**
     * 获得验签名的数据
     * @param map
     * @return
     */
    private String getVerifyData(Map map) {
    	 String result = (String) ((Object[]) map.get("result"))[0];
         String request_token = (String) ((Object[]) map.get("request_token"))[0];
       
         return "request_token=" + request_token + "&result=" + result;
//        // 按照key做排序
//        List<String> keys = new ArrayList<String>(map.keySet());
//        Collections.sort(keys);
//        StringBuffer content = new StringBuffer();
//        for (int i = 0; i < keys.size(); i++) {
//            String key = (String) keys.get(i);
//            String value = (String)((Object[]) map.get(key))[0];
//            if ("sign".equals(key)) {
//                continue;
//            }else if("notify_data".equals(key)){
//            	try {
//            		value = decryptNotifyData(value);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }            
//            if (value != null) {
//            	content.append((i == 0 ? "" : "&") + key + "=" + value);
//            } else {
//                content.append((i == 0 ? "" : "&") + key + "=");
//            }
//        }
//        return content.toString();
    }
    
    /**
     * 使用自己的私钥解密返回的结果，只需要对notify_data的内容解密
     * @param resData
     * @return
     * @throws Exception
     */
    private String decryptNotifyData(String notifyData) throws Exception {
        String data = "";
        data = Tools.securityManager.decrypt(Tools.clientConfig.getEncryptAlgo(), notifyData, Tools.clientConfig
            .getPrikey());
        return data;
    }
    
    private void addIMoney(Order_Alipay order) throws Exception {
        // 在认证服务器创建订单
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, "Alipay_" + (order.getMoney()));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getMoney());
    }
}
