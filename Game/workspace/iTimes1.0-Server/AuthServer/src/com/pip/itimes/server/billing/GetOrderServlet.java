package com.pip.itimes.server.billing;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.itimes.server.auth.Const;
import com.umpay.SignEnc;
import com.umpay.SignEncException;

/**
 * 手机钱包平台向商户下订单（UMPay-->SP）的Servlet。
 *
 * @author Frank
 *
 */
public class GetOrderServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(GetOrderServlet.class);
	private Server server;
	
	public GetOrderServlet(Server s) {
		server = s;
	}
	
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 取得订单的相关参数
        String spID = request.getParameter("SPID"); // SP平台代码
        String payID = request.getParameter("PAYID"); // 流水号
        String dateTime = request.getParameter("DATETIME"); // 交易日期
        String mobileID = request.getParameter("MOBILEID"); // 付款手机号
        String rdpwd = request.getParameter("RDPWD"); // 随机数
        String productID = request.getParameter("ProductID"); // 商品编号（格式为：商品号#用户帐号）
        String sign = request.getParameter("SIGN"); // 平台签名
        String remark = request.getParameter("REMARK"); // 商品描述

        log.info("Unipay(GetOrder): SPID=" + spID);
        log.info("Unipay(GetOrder): PAYID=" + payID);
        log.info("Unipay(GetOrder): DATETIME=" + dateTime);
        log.info("Unipay(GetOrder): MOBILEID=" + mobileID);
        log.info("Unipay(GetOrder): RDPWD=" + rdpwd);
        log.info("Unipay(GetOrder): ProductID=" + productID);
        log.info("Unipay(GetOrder): SIGN=" + sign);
        log.info("Unipay(GetOrder): REMARK=" + remark);

        String retResult;
        int payAmount = 0;
        String orderID = "000000";
        String retRemark = "";
        boolean trust = "222.128.28.171".equals(request.getRemoteAddr());

        // 验证手机号是否在黑名单中
        server.loadBlackList();
        if (server.inBlackList(mobileID)) {
            retResult = "0003";
            log.info("Unipay(GetOrder): Found in black list.");
        } else if (server.getBillingCount(mobileID) >= 10) {
        	retResult = "0003";
        	log.info("Unipay(GetOrder): Too many actions.");
        } else if (trust || verifyOrder(spID, payID, dateTime, mobileID, rdpwd,
                               productID, sign)) {
            // 验证成功
            retResult = "0000";

            // 查找产品处理程序
            ProductHandler handler = ProductManager.instance.findHandlerByProduct(productID);
            if (handler == null) {
            	// 验证失败
                retResult = "0002";
                log.info("Unipay(GetOrder): Unknown product.");
            } else {
            	StringBuffer remarkBuf = new StringBuffer();
            	int feeId = handler.placeOrder(productID.substring(handler.getProductID().length()), 
            			mobileID, remarkBuf);
            	payAmount = handler.getPayAmount();
            	retRemark = remarkBuf.toString();
            	log.info("Unipay(GetOrder): feeId[" + feeId + "]");
                // 验证订单号
                if (feeId == -1) {
                    // 帐号不存在
                    retResult = "0002";
                } else if (feeId == -2) {
                    // 余额过多
                    retResult = "0003";
                } else {
                    //订单号合法
                    // 保存订单号
                    orderID = Integer.toString(feeId);
                }
            }
        } else {
            // 验证失败
            retResult = "0002";
            log.info("Unipay(GetOrder): Get order failure.");
        }

        // 得到SP返回给UMPay时的sign
        String retSign = this.getRetSign(retResult, dateTime, payAmount,
                                         orderID);

        // 设置响应的字符集，如果不设置，会产生乱码。
        response.setCharacterEncoding("GBK");
        // 向response中写入返回的内容
        PrintWriter writer = response.getWriter();
        writer.print(this.getReturnContents(retResult, dateTime, payAmount,
                                            orderID, retSign, retRemark));
        writer.flush();
        writer.close();
    }

    /**
     * 验证订单的合法性。
     *
     * @return true:合法订单，false:非法订单。
     */
    private boolean verifyOrder(String spID, String payID, String dateTime,
                                String mobileID, String rdpwd,
                                String productID, String sign) {
        boolean result = false;

        // 依次验证参数合法性
        if (!Const.UNIPAY_SPID.equals(spID)) {
            return false;
        }
        if (payID == null || payID.length() == 0) {
            return false;
        }

        // 根据文档2.2的5.1.3的定义组合签名对象
        StringBuffer signParam = new StringBuffer();
        signParam.append("SPID=");
        signParam.append(spID);
        signParam.append("&PAYID=");
        signParam.append(payID);
        signParam.append("&DATETIME=");
        signParam.append(dateTime);
        signParam.append("&MOBILEID=");
        signParam.append(mobileID);
        signParam.append("&RDPWD=");
        signParam.append(rdpwd);
        signParam.append("&ProductID=");
        signParam.append(productID);

        log.info("Unipay(GetOrder): The verify order's signParam is '" +
                 signParam.toString() + "'");
        log.info("Unipay(GetOrder): The verify order's sign is '" + sign +
                 "'");

        try {
            // 验证签名对象
            result = SignEnc.verify(signParam.toString(), sign);
        } catch (SignEncException e) {
            log.error(e, e);
        }

        log.info("Unipay(GetOrder): The verify order's result is '" +
                 result + "'");

        return result;
    }

    /**
     * 根据文档2.2的5.1.3组装返回给UMPay时用的签名对象。
     *
     * @param retResult SP返回码
     * @return
     */
    private String getRetSign(String retResult, String dateTime,
                              int payAmount, String orderID) {
        String tmpSign = "";

        StringBuffer signParam = new StringBuffer();
        signParam.append(retResult);
        signParam.append("|");
        signParam.append(dateTime);
        signParam.append("|");
        signParam.append(Const.UNIPAY_SPID);
        signParam.append("|");
        signParam.append(payAmount);
        signParam.append("|");
        signParam.append(orderID);

        log.info("Unipay(GetOrder): The order return signParam is '" +
                 signParam.toString() + "'");

        try {
            // 得到签名对象
            tmpSign = SignEnc.sign(signParam.toString());
        } catch (SignEncException e) {
            log.error(e, e);
        }

        log.info("Unipay(GetOrder): The order return sign is " + tmpSign);

        return tmpSign;
    }

    /**
     * 根据文档2.2的5.1.3组装返回给UMPay的内容。
     *
     * @return 返回给UMPay的内容。
     */
    private String getReturnContents(String retResult, String dateTime,
                                     int payAmount, String orderID,
                                     String retSign, String retRemark) {
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
        contentBuffer.append(payAmount);
        contentBuffer.append("|");
        contentBuffer.append(orderID);
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
