package com.pip.server.billing.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
 * 支付宝支付回调接口<WEB支付专用>
 * 
 * @author jyu
 *
 */
@SuppressWarnings("serial")
public class AlipayCallBack4WebServlet  extends HttpServlet{
	private static Logger log = Logger.getLogger(AlipayCallBack4WebServlet.class);
	private Server server;
	private Order_AlipayDAO dataDAO;

	static HashMap<String, String> orderMap = new HashMap<String, String>();
	/*通知回调地址*/
	static String callbackURL = "http://news.pipgame.cn/webpg/pay_callback.do";
	
	public AlipayCallBack4WebServlet(Server server,Order_AlipayDAO dao){
		this.server=server;
		this.dataDAO = dao;
	}
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("[alipay_callback_4web]");
		
		request.setCharacterEncoding("UTF-8");
		
		@SuppressWarnings("unchecked")
		Map params = getRequestParams(request);			
		//写日志记录
		String sWord = AlipayFunction.CreateLinkString(params);
		log.info("[alipay_callback_4web]："+sWord);
				
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		String trade_no = request.getParameter("trade_no");				//支付宝交易号
		String out_trade_no = request.getParameter("out_trade_no");	    //获取订单号
		String total_fee = request.getParameter("total_fee");	        //获取总金额
		String subject = request.getParameter("subject");//商品名称、订单名称
		String body = "";
		if(request.getParameter("body") != null){
			body = request.getParameter("body");//商品描述、订单备注、描述
		}
		String buyer_email = request.getParameter("buyer_email");		//买家支付宝账号
		String trade_status = request.getParameter("trade_status");		//交易状态
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
			
		PrintWriter out = response.getWriter();
		boolean verified = validate(params,request);
		
		//重定向请求参数构造
		Map<String, String> transferparams = new HashMap<String, String>();
		transferparams.put("total_fee", total_fee);
		transferparams.put("subject", URLEncoder.encode(subject,"utf-8"));
		transferparams.put("body", URLEncoder.encode(body,"utf-8"));
		transferparams.put("buyer_email", buyer_email);
		transferparams.put("trade_no", trade_no);
//		transferparams.put("trade_status", trade_status);
		int amount = 0;
		if(verified){
			//////////////////////////////////////////////////////////////////////////////////////////
			// 查找订单数据
			Order_Alipay order = dataDAO.getBySeqID(out_trade_no);
	        if (order == null ) {
	            log.info("[alipay_callback_4web] 订单不存在");
	            response.sendRedirect(Tools.CALLBACK_URL + "?gamecode=1&code=2");
	            return;
	        }
	       
			//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				if (order.getStatus() != 1) {
					amount = new Double(total_fee).intValue();
	                try {
	                	if (order.getMoney() > amount) {// 订单金额大于实际扣费金额，核减
							order.setMoney(amount);
							order.setImoney(Tools.calcIMoney(amount * 100));
						}
	                    addIMoney(order);
	                } catch (Exception e) {
	                    log.error(e, e);
	                    log.info("[alipay_callback_4web] 添加i币失败");
	                    out.print("N");
	                    return;
	                }
	                log.info("[alipay_callback_4web] ChargeOK :" + out_trade_no);
	            } else {
	                log.info("[alipay_callback_4web] Ignored :" + out_trade_no);
	            }
	            order.setStatus(1);
	            transferparams.put("trade_status", URLEncoder.encode("交易成功","utf-8"));
			}else{
				order.setStatus(2);
				transferparams.put("trade_status", URLEncoder.encode("交易失败","utf-8"));
			}
			order.setFinishTime(new java.util.Date());	       
	        dataDAO.update(order);
		}else{
			transferparams.put("trade_status", URLEncoder.encode("交易失败","utf-8"));
		}
		String returnHttp = orderMap.get(out_trade_no);
     	if(returnHttp==null){
     		returnHttp = callbackURL + "?code=0&name=&amount="+ (amount*100);
     	}else{
     		orderMap.remove(out_trade_no);
     	}
     	if(returnHttp.indexOf("?")>0){
     		returnHttp=returnHttp+"&"+AlipayFunction.CreateLinkString(transferparams);
     	}else{
     		returnHttp=returnHttp+"?"+AlipayFunction.CreateLinkString(transferparams);
     	}     	
     	response.sendRedirect(returnHttp);
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
