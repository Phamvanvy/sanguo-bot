package com.pip.server.billing.umpaybank;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.util.Const;
import com.pip.server.billing.Server;
import com.umpay.SignEnc;
import com.umpay.SignEncException;

/**
 * 手机钱包支付结果通知接口。
 * 参数和返回格式参见手机钱包文档。
 * @author Light Hu
 */
public class PaymentNotifyServlet_Umpay2 extends HttpServlet {
	private static Logger log = Logger.getLogger(PaymentNotifyServlet_Umpay2.class);
	private Server server;
	
	public PaymentNotifyServlet_Umpay2(Server s) {
		server = s;
	}
	
    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 取得付款结果的相关参数
        // SP编号
        String spID = request.getParameter("SPID");
        // 订单ID
        String orderID = request.getParameter("ORDERID");
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

        log.info("[umpay2_notify]orderid[" + orderID + "]payid[" + payID + "]amount[" + amount + "]phone[" + mobileID + "]");
        
        boolean trust = "222.128.28.171".equals(request.getRemoteAddr());
        String retResult;
        String retRemark = "";
        if (trust || verifyPaymentNotify(spID, orderID, payID, amount, dateTime, mobileID, rdpwd, retCode, remark, sign)) {
            // 验证成功
            retResult = "0000";
            log.info("[umpay2_notify] Verify payment success.");
            
            // 完成订单
            int feeId = Integer.parseInt(orderID.trim());
            Fee fee = server.findFee(feeId);
            if (fee == null || fee.isCharged()) {
                retResult = "1111";
                log.info("[umpay2_notify] 订单不存在");
            } else if (!server.fulfillOrder(feeId)) {
                retResult = "1111";
                log.info("[umpay2_notify] 完成订单失败");
            } else {
                Account acc = server.findAccount(fee.getAccountId());
                String accName = "";
                if (acc != null) {
                    accName = acc.getName();
                }
                retRemark = "您购买的" + (fee.getAmount() / 100) + "明珠i币已充入帐号" + acc.getName();  
            }
        } else {
            // 验证失败
            retResult = "1111";
            log.info("[umpay2_notify] Verify payment failure.");
        }

        // 得到SP返回给UMPay时的sign
        String retSign = this.getRetSign(retResult, dateTime, payID);

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
    private boolean verifyPaymentNotify(String spID, String orderID, 
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

        StringBuffer signParam = new StringBuffer();
        signParam.append("SPID=");
        signParam.append(spID);
    	signParam.append("&ORDERID=");
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
        signParam.append(retResult);
        signParam.append("|");
        signParam.append(dateTime);
        signParam.append("|");
        signParam.append(ConstUmpayBank.SPID);
        signParam.append("|");
        signParam.append(payID);

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
        contentBuffer.append(ConstUmpayBank.SPID);
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
