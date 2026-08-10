package com.pip.server.billing.umpay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;
import com.pip.server.billing.umpayv30.ReceiveNewPaymentNotifyServlet;
import com.umpay.SignEnc;
import com.umpay.SignEncException;

/**
 * 接收从UMPay来的付款结果的Servlet。
 * 当用户使用手机钱包进行支付后，UMPay会调用这个类来通知支付结果，SP在这个类中用UMPay提供的
 * 方法来验证支付结果，并将验证后的结果返回给UMPay。返回的方式为直接在响应中写入包含特定META
 * 信息的字符串。
 *
 * @author Frank
 *
 */
public class ReceivePaymentNotifyServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(ReceivePaymentNotifyServlet.class);
	private Server server;
	ReceiveNewPaymentNotifyServlet servlet30 = null;
	public ReceivePaymentNotifyServlet(Server s) {
		server = s;
		servlet30 = new ReceiveNewPaymentNotifyServlet(s);
	}
	
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
    	
    	String version = request.getParameter("version"); // 定长3。3.0
        if(version!=null && version.equals("3.0")){
            servlet30.service(request,response);
            return ;
        }
         
        // 取得付款结果的相关参数
        // SP平台代码
        String spID = request.getParameter("SPID");
        // 订单号（按次支付传递的是ORDERID）
        String orderID = request.getParameter("ORDERID");
        boolean isMonth = false;
        if (orderID == null) {
        	// 商品ID（包月支付传递GOODSID）
        	orderID = request.getParameter("GOODSID");
        	isMonth = true;
        }
        // 支付流水号
        String payID = request.getParameter("PAYID");
        // 付款金额
        String amount = request.getParameter("AMOUNT");
        // 交易日期
        String dateTime = request.getParameter("DATETIME");
        // 付款手机号
        String mobileID = request.getParameter("MOBILEID");
        // 随机数
        String rdpwd = request.getParameter("RDPWD");
        // 平台返回码
        String retCode = request.getParameter("RETCODE");
        // 附加信息
        String remark = request.getParameter("REMARK");
        // 平台签名
        String sign = request.getParameter("SIGN");

        log.info("Unipay(PaymentNotify): SPID='" + spID + "'");
        log.info("Unipay(PaymentNotify): ORDERID='" + orderID + "'");
        log.info("Unipay(PaymentNotify): PAYID='" + payID + "'");
        log.info("Unipay(PaymentNotify): AMOUNT='" + amount + "'");
        log.info("Unipay(PaymentNotify): DATETIME='" + dateTime + "'");
        log.info("Unipay(PaymentNotify): MOBILEID='" + mobileID + "'");
        log.info("Unipay(PaymentNotify): RDPWD='" + rdpwd + "'");
        log.info("Unipay(PaymentNotify): RETCODE='" + retCode + "'");
        log.info("Unipay(PaymentNotify): REMARK='" + remark + "'");
        log.info("Unipay(PaymentNotify): SIGN='" + sign + "'");
        
        boolean trust = "222.128.28.171".equals(request.getRemoteAddr());
//        boolean trust = "192.168.0.126".equals(request.getRemoteAddr());
        String retResult;
        String retRemark = "";
        if (trust || verifyPaymentNotify(spID, orderID, isMonth, payID, amount, dateTime,
                                mobileID, rdpwd, retCode, remark, sign)) {
            // 验证成功
            retResult = "0000";
            log.info("Unipay(PaymentNotify): Verify payment success.");
            
            // 根据订单类型查找产品
            ProductHandler handler = null;
            int feeId = Integer.parseInt(orderID.trim());
            if (isMonth) {
            	// 包月产品特殊处理
            	handler = ProductManager.instance.findHandlerByProduct(orderID + "#");
            } else {
            	Fee fee = server.findFee(feeId);
            	if (fee != null) {
            		handler = ProductManager.instance.findHandlerByOrderType(fee.getChannel());
            	}
            }
            if (handler != null) {
            	StringBuffer remarkBuf = new StringBuffer();
            	if (handler.fulfilOrder(feeId, mobileID, remarkBuf)) {
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
        String retSign = this.getRetSign(retResult, dateTime, payID);

        // 如果支付成功，计数
        if ("0000".equals(retResult)) {
            server.incrementBillingCount(mobileID);
        }

        // 设置响应的字符集，如果不设置，会产生乱码。
        response.setCharacterEncoding("GBK");
        // 向response中写入返回的内容
        PrintWriter writer = response.getWriter();
        writer.print(this.getReturnContents(retResult, dateTime, payID,
                                            retSign, retRemark));
        writer.flush();
        writer.close();
    }

    /**
     * 验证付款结果。
     *
     * @return true:付款成功，false:付款失败。
     */
    private boolean verifyPaymentNotify(String spID, String orderID, boolean isMonth,
                                        String payID, String amount,
                                        String dateTime, String mobileID,
                                        String rdpwd,
                                        String retCode, String remark,
                                        String sign) {
        boolean result = false;

        // 依次检查参数合法性
        if (!Const.UNIPAY_SPID.equals(spID)) {
            return false;
        }
        try {
            Integer.parseInt(orderID);
        } catch (Exception e) {
            return false;
        }
        if (payID == null || payID.length() == 0) {
            return false;
        }
        if (!"Y".equals(retCode)) {
            return false;
        }

        // 根据文档2.2的5.2的定义组合签名对象
        StringBuffer signParam = new StringBuffer();
        signParam.append("SPID=");
        signParam.append(spID);
        if (!isMonth) {
        	signParam.append("&ORDERID=");
        } else {
        	signParam.append("&GOODSID=");
        }
        signParam.append(orderID);
        signParam.append("&PAYID=");
        signParam.append(payID);
        signParam.append("&AMOUNT=");
        signParam.append(amount);
        signParam.append("&DATETIME=");
        signParam.append(dateTime);
        signParam.append("&MOBILEID=");
        signParam.append(mobileID);
        signParam.append("&RDPWD=");
        signParam.append(rdpwd);
        signParam.append("&RETCODE=");
        signParam.append(retCode);
        signParam.append("&REMARK=");
        signParam.append(remark);

        log.info("Unipay(PaymentNotify): verify sign is '" +
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
     * 根据文档2.2的5.2组装返回给UMPay时用的签名对象。
     *
     * @param retResult SP返回码
     * @return
     */
    private String getRetSign(String retResult, String dateTime,
                              String payID) {
        String tmpSign = "";

        StringBuffer signParam = new StringBuffer();
        signParam.append("");
        signParam.append(retResult);
        signParam.append("|");
        signParam.append(dateTime);
        signParam.append("|");
        signParam.append(Const.UNIPAY_SPID);
        signParam.append("|");
        signParam.append(payID);

        log.info("Unipay(PaymentNotify): The payment return sign is '" +
                 signParam.toString() + "'");

        try {
            // 得到签名对象
            tmpSign = SignEnc.sign(signParam.toString());
        } catch (SignEncException e) {
            log.error(e, e);
        }

        return tmpSign;
    }

    /**
     * 根据文档2.2的5.2组装返回给UMPay的内容。
     *
     * @return 返回给UMPay的内容。
     */
    private String getReturnContents(String retResult, String dateTime,
                                     String payID, String retSign,
                                     String retRemark) {
        StringBuffer contentBuffer = new StringBuffer();
        contentBuffer.append("<html>\n");
        contentBuffer.append("<head>\n");
        // 组装META内容
        contentBuffer.append("<META NAME=\"MobilePayPlatform\" CONTENT =\"");
        contentBuffer.append(retResult);
        contentBuffer.append("|");
        contentBuffer.append(dateTime);
        contentBuffer.append("|");
        contentBuffer.append(Const.UNIPAY_SPID);
        contentBuffer.append("|");
        contentBuffer.append(payID);
        contentBuffer.append("|");
        contentBuffer.append(retSign);
        contentBuffer.append("|");
        contentBuffer.append(retRemark);
        contentBuffer.append("\">\n");
        contentBuffer.append("</head>\n");
        contentBuffer.append("</html>");

        return contentBuffer.toString();
    }
}
