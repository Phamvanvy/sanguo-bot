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

public class AlipayNotifyServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(AlipayNotifyServlet.class);
	private Server server;
	private Order_AlipayDAO dataDAO;
	
	public AlipayNotifyServlet(Server server,Order_AlipayDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("[alipay_notify]");
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

			log.info("[alipay_notify]:oid=" + oid + ",sid=" + sid+",data="+verifyData);
			
			PrintWriter out = response.getWriter();
			
			// 查找订单数据
	        Order_Alipay order = dataDAO.getByID(Integer.parseInt(oid));
	        if (order == null || !order.getPaySeq().equals(sid)) {
	            log.info("[alipay_notify] 订单不存在");
	            out.print("false");
	            return;
	        }

			// 验签名			
			verified = Tools.securityManager.verify(Tools.clientConfig
					.getSignAlgo(), verifyData, sign, Tools.clientConfig
					.getAlipayPubKey());			

			if (verified) {
				out.print("success");
			} else {
				out.print("false");
			}
			
			//修改订单状态并为用户添加i币
	        if (verified) {
	            if (order.getStatus() != 1) {
	                try {
	                    addIMoney(order);
	                } catch (Exception e) {
	                    log.error(e, e);
	                    log.info("[alipay_notify] 添加i币失败");
	                    out.print("N");
	                    return;
	                }
	                log.info("[alipay_notify] ChargeOK");
	            } else {
	                log.info("[alipay_notify] Ignored");
	            }
	            order.setStatus(1);
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
    
	/**
     * 获得验签名的数据
     * @param map
     * @return
     */
    private String getVerifyData(Map map) {
    	 String service = (String) ((Object[]) map.get("service"))[0];
         String v = (String) ((Object[]) map.get("v"))[0];
         String sec_id = (String) ((Object[]) map.get("sec_id"))[0];
         String notify_data = (String) ((Object[]) map.get("notify_data"))[0];
         try {
             notify_data = decryptNotifyData(notify_data);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "service=" + service + "&v=" + v + "&sec_id=" + sec_id + "&notify_data="
                + notify_data;
        
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
