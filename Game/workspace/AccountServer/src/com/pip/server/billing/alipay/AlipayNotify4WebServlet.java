package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alipay.config.AlipayConfig;
import com.alipay.util.AlipayFunction;
import com.alipay.util.AlipayNotify;
import com.pip.server.account.bean.Fee;
import com.pip.server.billing.Server;
/**
 * 支付宝订单异步通知<WEB支付专用>
 * 
 * @author jyu
 *
 */
@SuppressWarnings("serial")
public class AlipayNotify4WebServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(AlipayNotify4WebServlet.class);
	private Server server;
	private Order_AlipayDAO dataDAO;
	
	
	public AlipayNotify4WebServlet(Server server,Order_AlipayDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("[alipay_notify_4web]");
		try {
			@SuppressWarnings("unchecked")
			Map params = getRequestParams(request);			
			//写日志记录
			String sWord = AlipayFunction.CreateLinkString(params);
			log.info("[alipay_notify_4web]："+sWord);
			
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			String out_trade_no = request.getParameter("out_trade_no");	    //获取订单号
			String total_fee = request.getParameter("total_fee");	        //获取总金额
			String tradeno = request.getParameter("trade_no");
			String trade_status = request.getParameter("trade_status");		//交易状态
			
			PrintWriter out = response.getWriter();
			
			boolean verified = validate(params,request);
			
			if(verified){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					int amount = new Double(total_fee).intValue();
		            
		            // 查找订单数据
			        Order_Alipay order = dataDAO.getBySeqID(out_trade_no);
			        if (order == null ) {
			            log.info("[alipay_notify_4web] 订单不存在");
			            return;
			        }
			        
			        //修改订单状态并为用户添加i币
			        if (order.getStatus() != 1) {
						try {
							if (order.getMoney() > amount) {// 订单金额大于实际扣费金额，核减
								order.setMoney(amount);
								order.setImoney(Tools.calcIMoney(amount * 100));
							}
							order.setTradeNo(tradeno);//保存支付宝交易号
							addIMoney(order);
						} catch (Exception e) {
							log.error(e, e);
							log.info("[alipay_notify_4web] 添加i币失败");
							return;
						}
						log.info("[alipay_notify_4web] ChargeOK");
					} else {
						log.info("[alipay_notify_4web] Ignored");
					}
					order.setStatus(1);
			        
			        order.setFinishTime(new java.util.Date());	       
			        dataDAO.update(order);
					
					out.println("success");	// 请不要修改或删除
				} else {
					log.info("[alipay_notify_4web] 交易未成功:"+trade_status);
					out.println("success");	//请不要修改或删除
				}
			}else{//验证失败
				log.info("[alipay_notify_4web] 验证失败:"+out_trade_no);
				out.println("fail");
			}				
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
        Fee fee = server.newFee(order.getUserName(), order.getImoney() * 100, "AlipayWEB_" + (order.getMoney()));
        
        // 完成订单，修改帐户余额
        if (!server.fulfillOrder(fee.getId())) {
            throw new Exception();
        }
        
        // 添加积分
        server.addCreditByMoney(order.getAccountID(), order.getMoney());
    }
    
    
    @SuppressWarnings("unchecked")
	private Map getRequestParams(HttpServletRequest request){
    	//获取支付宝POST过来反馈信息
		Map params = new HashMap();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
			params.put(name, valueStr);
		}
		return params;
    }
    
    @SuppressWarnings("unchecked")
    private boolean validate(Map params,HttpServletRequest request){
    	String key = AlipayConfig.key;
		
		String mysign = AlipayNotify.GetMysign(params,key);
		String responseTxt = AlipayNotify.Verify(request.getParameter("notify_id"));
		String sign = request.getParameter("sign");
		if(mysign.equals(sign) && responseTxt.equals("true")){//验证成功
			return true;
		}
		return false;
    }
}
