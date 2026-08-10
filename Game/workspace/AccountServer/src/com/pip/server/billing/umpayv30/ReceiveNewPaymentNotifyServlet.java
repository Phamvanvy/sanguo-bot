package com.pip.server.billing.umpayv30;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Signature;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;
import com.umpay.SignEnc;
import com.umpay.SignEncException;

import com.pip.server.billing.umpay.ProductHandler;
import com.pip.server.billing.umpay.ProductManager;

/**
 * 接收从UMPay来的付款结果的Servlet。
 * 当用户使用手机钱包进行支付后，UMPay会调用这个类来通知支付结果，SP在这个类中用UMPay提供的
 * 方法来验证支付结果，并将验证后的结果返回给UMPay。返回的方式为直接在响应中写入包含特定META
 * 信息的字符串。
 *
 * 3.1版本协议
 *
 */
public class ReceiveNewPaymentNotifyServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(ReceiveNewPaymentNotifyServlet.class);
	private Server server;
	
	public ReceiveNewPaymentNotifyServlet(Server s) {
		server = s;
	}
	
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 取得付款结果的相关参数
        // SP平台代码 ,商户号
        String merId = request.getParameter("merId");  
        // 商品号	
        String goodsId = request.getParameter("goodsId"); 
        // 订单号
        String orderID = request.getParameter("orderId"); 
        // 商户订单日期
        String merDate = request.getParameter("merDate");
        // 支付日期
        String payDate = request.getParameter("payDate");
        // 交易金额
        String amount = request.getParameter("amount");
        // 付款类型
        String amtType = request.getParameter("amtType");
        // 银行类型
        String bankType = request.getParameter("bankType");
        // 手机号码
        String mobileId = request.getParameter("mobileId");
        // 交易类型
        String transType = request.getParameter("transType");
        // 清算日期
        String settleDate = request.getParameter("settleDate");
        // 商户私有域
        String merPriv = request.getParameter("merPriv");
        // 平台返回码
        String retCode = request.getParameter("retCode");
        // 版本
        String version = request.getParameter("version");
        // 平台签名
        String sign = request.getParameter("sign");

        StringBuffer paramesInfo = new StringBuffer();
        paramesInfo.append("merId=").append(merId);
        paramesInfo.append("&goodsId=").append(goodsId);
        paramesInfo.append("&orderId=").append(orderID);
        paramesInfo.append("&merDate=").append(merDate);
        paramesInfo.append("&payDate=").append(payDate);
        paramesInfo.append("&amount=").append(amount);
        paramesInfo.append("&amtType=").append(amtType);
        paramesInfo.append("&bankType=").append(bankType);
        paramesInfo.append("&mobileId=").append(mobileId);
        paramesInfo.append("&transType=").append(transType);
        paramesInfo.append("&settleDate=").append(settleDate);
        if(merPriv!=null){
        	paramesInfo.append("&merPriv=").append(merPriv);
        }
        paramesInfo.append("&retCode=").append(retCode);
        paramesInfo.append("&version=").append(version);
        
//      loginfo.append(" sign=").append(sign);
        
        log.info("Unipay(NewPaymentNotify):"+paramesInfo.toString()+"&sign="+sign);
        log.info("Unipay(TestRemoteServer:):"+request.getRemoteAddr());
        
        boolean trust = "222.128.28.171".equals(request.getRemoteAddr());
//        boolean trust = "192.168.0.126".equals(request.getRemoteAddr());
        String retResult;
        String retRemark = "";
        if (/*trust ||*/ verifyPaymentNotify(merId, orderID, goodsId, retCode, paramesInfo, sign)) {
            // 验证成功
            retResult = "0000";
            log.info("Unipay(PaymentNotify): Verify payment success.");
            
            // 根据订单类型查找产品
            ProductHandler handler = null;
            int feeId = Integer.parseInt(orderID.trim());
          
            Fee fee = server.findFee(feeId);
            if (fee != null) {
            	handler = ProductManager.instance.findHandlerByOrderType(fee.getChannel());
            }
            if(goodsId.startsWith("021")){//特殊处理（单机游戏）没有计费单的情况
            	handler = ProductManager.instance.findHandlerByProduct("021#");
            }
          
            if (handler != null) {
            	StringBuffer remarkBuf = new StringBuffer();
            	if (handler.fulfilOrder(feeId, mobileId, remarkBuf)) {
            		retRemark = remarkBuf.toString();
            	} else {
                	log.info("Unipay(PaymentNotify): Fulfill order failed.");
                	retResult = "1111";
            	}
            } else {
            	log.info("Unipay(PaymentNotify): Invalid order.");
            	retResult = "1111";
            }
        } else {
            // 验证失败
            retResult = "1111";
            log.info("Unipay(PaymentNotify): Verify payment failure.");
        }

        // 得到SP返回给UMPay时的sign
        String retSign = this.getRetSign(merId,goodsId,orderID,merDate,retResult,retRemark,version);

        // 如果支付成功，计数
        if ("0000".equals(retResult)) {
            server.incrementBillingCount(mobileId);
        }	

        // 设置响应的字符集，如果不设置，会产生乱码。
        response.setCharacterEncoding("GBK");
        // 向response中写入返回的内容
        PrintWriter writer = response.getWriter();
        writer.print(this.getReturnContents(merId,goodsId,orderID,merDate,retResult,retRemark,version,retSign));
        writer.flush();
        writer.close();
    }

    /**
     * 验证付款结果。
     *
     * @return true:付款成功，false:付款失败。
     */
    private boolean verifyPaymentNotify(String merId, String orderID, 
                                        String goodsID, String retCode, StringBuffer signParam,
                                        String sign) {
        boolean result = false;

        // 依次检查参数合法性
        if (!Const.UNIPAY_SPID.equals(merId)) {
            return false;
        }
        try {
            Integer.parseInt(orderID);
        } catch (Exception e) {
            return false;
        }
        if (goodsID == null || goodsID.length() == 0) {
            return false;
        }
        if (!"0000".equals(retCode)) {
            return false;
        }

        // 根据文档3.1定义组合签名对象(外部以构造）

        log.info("Unipay(NewPaymentNotify): verify sign is '" +
                 signParam.toString() + "'");

        try {
            // 验证签名对象
            result = SignEnc.verify(signParam.toString(), sign);
        } catch (SignEncException e) {
            log.error(e, e);
        }
        return result;
    }

    /**
     * 根据文档3.1组装返回给UMPay时用的签名对象。
     *
     * @param retResult SP返回码
     * @return
     */
    private String getRetSign(String merId,String goodsId,String orderID,String merDate,String retResult,String retMsg,String version) {
        String tmpSign = "";

        StringBuffer signParam = new StringBuffer();
    	signParam.append(merId);	
        signParam.append("|");
        signParam.append(goodsId);
        signParam.append("|");
        signParam.append(orderID);
        signParam.append("|");
        signParam.append(merDate);
        signParam.append("|");
        signParam.append(retResult);
        signParam.append("|");
        try
        {
            BASE64Encoder base64 = new BASE64Encoder();
            retMsg=base64.encode(retMsg.getBytes("gbk"));
        }catch(Exception e){
        }
        signParam.append(retMsg);
        signParam.append("|");
        signParam.append(version);

        log.info("Unipay(NewPaymentNotify): The payment return sign is '" +
                 signParam.toString() + "'");

        try {
            // 得到签名对象
            tmpSign = SignEnc.sign(signParam.toString());
        } catch (SignEncException e) {
            log.error(e, e);
        }
        log.info("Unipay(NewPaymentNotify): The payment return signresult is '" +
        		tmpSign + "'");
        return tmpSign;
    }

    /**
     * 根据文档2.2的5.2组装返回给UMPay的内容。
     *
     * @return 返回给UMPay的内容。
     */
    private String getReturnContents(String merId,String goodsId,String orderID,String merDate,String retResult,String retMsg,String version,String retSign) {
        StringBuffer contentBuffer = new StringBuffer();
        contentBuffer.append("<html>\n");
        contentBuffer.append("<head>\n");
        // 组装META内容
        contentBuffer.append("<META NAME=\"MobilePayPlatform\" CONTENT =\"");
        contentBuffer.append(Const.UNIPAY_SPID);
        contentBuffer.append("|");
        contentBuffer.append(goodsId);
        contentBuffer.append("|");
        contentBuffer.append(orderID);
        contentBuffer.append("|");
        contentBuffer.append(merDate);
        contentBuffer.append("|");
        contentBuffer.append(retResult);
        contentBuffer.append("|");
        try
        {
            BASE64Encoder base64 = new BASE64Encoder();
            retMsg=base64.encode(retMsg.getBytes("gbk"));
        }catch(Exception e){
        }
        contentBuffer.append(retMsg);
        contentBuffer.append("|");
        contentBuffer.append(version);
        contentBuffer.append("|");
        contentBuffer.append(retSign);
        contentBuffer.append("\">\n");
        contentBuffer.append("</head>\n");
        contentBuffer.append("</html>");

        return contentBuffer.toString();
    }
}