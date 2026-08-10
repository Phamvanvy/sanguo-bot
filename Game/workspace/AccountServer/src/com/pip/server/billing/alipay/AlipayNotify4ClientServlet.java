package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;

public class AlipayNotify4ClientServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(AlipayNotify4ClientServlet.class);
	private Server server;
	private Order_AlipayDAO dataDAO;
	
	
	public AlipayNotify4ClientServlet(Server server,Order_AlipayDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("[alipay_notify_client]");
		try {
			
			//获得参数
//			String service = request.getParameter("service");
//			String v = request.getParameter("v");
//			String sec_id = request.getParameter("sec_id");
			String sign = request.getParameter("sign");
			String notify_data = request.getParameter("notify_data");//RSA
			
			//组装验证数据
			StringBuffer verifyDataBuf = new StringBuffer();
//			verifyDataBuf.append("service=").append(service).append("&");
//			verifyDataBuf.append("v=").append(v).append("&");
//			verifyDataBuf.append("sec_id=").append(sec_id).append("&");
			verifyDataBuf.append("notify_data=").append(notify_data);
			
			log.info("[alipay_notify_4client]:sign="+sign + " verfifydata=" + verifyDataBuf.toString());
			
			boolean verified = false;
			
			PrintWriter out = response.getWriter();
			// 验签名			
			verified = Tools.securityManager.verify(Tools.clientConfig
					.getSignAlgo(), verifyDataBuf.toString(), sign, Tools.validateKey_4Client);			

			if (verified) {
				out.print("success");
			} else {
				out.print("false");
				return;
			}
					
		
			SAXReader reader = new SAXReader();
            Reader r = new StringReader(notify_data);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            
            
            String out_trade_no = root.elementText("out_trade_no");
            String trade_status  =root.elementText("trade_status");
            String total_fee = root.elementText("total_fee");
            int amount = new Double(total_fee).intValue();
            if(!"TRADE_FINISHED".equals(trade_status))
            { 
            	log.info("[alipay_notify_4client] 交易未成功:"+trade_status);
	            return;
            }
            // 查找订单数据
	        Order_Alipay order = dataDAO.getBySeqID(out_trade_no);
	        if (order == null ) {
	            log.info("[alipay_notify_4client] 订单不存在");
	            return;
	        }

			//修改订单状态并为用户添加i币
	        if (verified) {
	            if (order.getStatus() != 1) {
	                try {
	                	if(order.getMoney()>amount){//订单金额大于实际扣费金额，核减
	                		order.setMoney(amount);
	                		order.setImoney(Tools.calcIMoney(amount*100));
	                	}
	                    addIMoney(order);
	                } catch (Exception e) {
	                    log.error(e, e);
	                    log.info("[alipay_notify_4client] 添加i币失败");
	                    return;
	                }
	                log.info("[alipay_notify_4client] ChargeOK");
	            } else {
	                log.info("[alipay_notify_4client] Ignored");
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
