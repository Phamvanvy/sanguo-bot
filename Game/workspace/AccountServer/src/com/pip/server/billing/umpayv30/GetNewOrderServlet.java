package com.pip.server.billing.umpayv30;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

import com.pip.server.account.util.*;
import com.pip.server.billing.Server;
import com.umpay.SignEnc;
import com.umpay.SignEncException;
import com.pip.server.billing.umpay.ProductHandler;
import com.pip.server.billing.umpay.ProductManager;

/**
 * 手机钱包平台向商户下订单（UMPay-->SP）的Servlet。
 *
 * 3.1版本协议
 *
 */
public class GetNewOrderServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(GetNewOrderServlet.class);
	private Server server;

	public GetNewOrderServlet(Server s) {
		server = s;
	}

    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // 取得订单的相关参数
        String merId = request.getParameter("merId"); // 商户ID
        String goodsId = request.getParameter("goodsId"); // 商品号
        String goodsInf = request.getParameter("goodsInf"); // 商品信息
        String mobileID = request.getParameter("mobileId"); // 付款手机号
        String amtType = request.getParameter("amtType"); // 付款类型
        String bankType = request.getParameter("bankType"); // 银行卡
        String version = request.getParameter("version"); // 定长3。3.0
        String sign = request.getParameter("sign"); // 平台签名

        String callbackURL = server.getServerURL() + "/receivePaymentNotify";
        
        StringBuffer loginfo = new StringBuffer("Unipay(GetNewOrder):");
        loginfo.append(" merId=").append(merId);
        loginfo.append(" goodsId=").append(goodsId);
        loginfo.append(" goodsInf=").append(goodsInf);
        loginfo.append(" mobileID=").append(mobileID);
        loginfo.append(" amtType=").append(amtType);
        loginfo.append(" bankType=").append(bankType);
        loginfo.append(" version=").append(version);
        loginfo.append(" sign=").append(sign);
        loginfo.append(" remoteip=").append(request.getRemoteAddr());
        log.info(loginfo.toString());

        String retResult;
        int payAmount = 0;
        String orderID = "000000";
        String retRemark = "";
        boolean trust = "222.128.28.171".equals(request.getRemoteAddr());
//        boolean trust = "192.168.0.126".equals(request.getRemoteAddr());
        // 验证手机号是否在黑名单中
        server.loadBlackList();
        if (server.inBlackList(mobileID)) {
            retResult = "0003";
            log.info("Unipay(GetNewOrder): Found in black list.");
        } else if (server.getBillingCount(mobileID) >= 10) {
        	retResult = "0003";
        	log.info("Unipay(GetNewOrder): Too many actions.");
        } else if ( /*trust ||*/ verifyOrder(merId, goodsId, goodsInf, mobileID, amtType,
        		bankType,version, sign)) {
            // 验证成功
            retResult = "0000";

            // 查找产品处理程序
            ProductHandler handler = ProductManager.instance.findHandlerByProduct(goodsInf);
            if (handler == null) {
            	// 验证失败
                retResult = "0002";
                log.info("Unipay(GetNewOrder): Unknown product.");
            } else {
            	StringBuffer remarkBuf = new StringBuffer();
            	int feeId = handler.placeOrder(goodsInf.substring(handler.getProductID().length()),
            			mobileID, remarkBuf);
            	payAmount = handler.getPayAmount();
            	retRemark = remarkBuf.toString();
            	log.info("Unipay(GetNewOrder): feeId[" + feeId + "]");
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
            log.info("Unipay(GetNewOrder): Get order failure.");
        }

        // 得到SP返回给UMPay时的sign
        Date dateTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      
        String retSign = this.getRetSign(merId, goodsId, orderID, sdf.format(dateTime), payAmount, callbackURL,retResult, retRemark, version);

        // 设置响应的字符集，如果不设置，会产生乱码。
        response.setCharacterEncoding("GBK");
        // 向response中写入返回的内容
        PrintWriter writer = response.getWriter();
        writer.print(this.getReturnContents(merId, goodsId, orderID, sdf.format(dateTime), payAmount,callbackURL,retResult, retRemark, version,retSign));
        writer.flush();
        writer.close();
    }

    /**
     * 验证订单的合法性。
     *
     * @return true:合法订单，false:非法订单。
     */
    private boolean verifyOrder(String merId, String goodsId, String goodsInf,
                                String mobileID, String amtType,
                                String bankType,String version, String sign) {
        boolean result = false;

        // 依次验证参数合法性
        if (!Const.UNIPAY_SPID.equals(merId)) {
            return false;
        }
        if (goodsId == null || goodsId.length() == 0) {
            return false;
        }

        // 根据文档3.1的3.8 1 b)的定义组合签名对象
        StringBuffer signParam = new StringBuffer();
        signParam.append("merId=");
        signParam.append(merId);
        signParam.append("&goodsId=");
        signParam.append(goodsId);
        if(goodsInf!=null){
        	signParam.append("&goodsInf=");
//        	try {
//				signParam.append(URLEncoder.encode(goodsInf,"UTF-8"));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
				signParam.append(goodsInf);
//			};
        }
        signParam.append("&mobileId=");
        signParam.append(mobileID);
        signParam.append("&amtType=");
        signParam.append(amtType);
        signParam.append("&bankType=");
        signParam.append(bankType);
        signParam.append("&version=");
        signParam.append(version);

        log.info("Unipay(GetNewOrder): The verify order's signParam is '" +
                 signParam.toString() + "'");
        log.info("Unipay(GetNewOrder): The verify order's sign is '" + sign +
                 "'");

        try {
            // 验证签名对象
            result = SignEnc.verify(signParam.toString(), sign);
        } catch (SignEncException e) {
            log.error(e, e);
        }

        log.info("Unipay(GetNewOrder): The verify order's result is '" +
                 result + "'");

        return result;
    }

    /**
     * 根据文档3.1的5.1.3组装返回给UMPay时用的签名对象。
     *
     * @param retResult SP返回码
     * @return
     */
    private String getRetSign(String merId, String goodsId,String orderId, String merDate, int amount,String callbackURL,String retResult, 
                              String retMsg,String version) {
        String tmpSign = "";
        StringBuffer signParam = new StringBuffer();
    	signParam.append(merId);	
        signParam.append("|");
        signParam.append(goodsId);
        signParam.append("|");
        signParam.append(orderId);
        signParam.append("|");
        signParam.append(merDate);
        signParam.append("|");
        signParam.append(amount);
        signParam.append("|");
        signParam.append(callbackURL);
        signParam.append("|");//merPriv
        signParam.append("|");//expand
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

        log.info("Unipay(GetNewOrder): The order return signParam is '" +
                 signParam.toString() + "'");

        try {
            // 得到签名对象
            tmpSign = SignEnc.sign(signParam.toString());
        } catch (SignEncException e) {
            log.error(e, e);
        }

        log.info("Unipay(GetNewOrder): The order return sign is " + tmpSign);

        return tmpSign;
    }

    /**
     * 根据文档2.2的5.1.3组装返回给UMPay的内容。
     *
     * @return 返回给UMPay的内容。
     */
    private String getReturnContents(String merId, String goodsId,String orderId, String merDate, int amount,String callbackURL, String retResult, 
            String retMsg,String version,String retSign) {
        StringBuffer contentBuffer = new StringBuffer();
        contentBuffer.append("<html>\n");
        contentBuffer.append("<head>\n");
        // 组装META内容
        contentBuffer.append("<META NAME=\"MobilePayPlatform\" CONTENT =\"");
        contentBuffer.append(Const.UNIPAY_SPID);
        contentBuffer.append("|");
        contentBuffer.append(goodsId);
        contentBuffer.append("|");
        contentBuffer.append(orderId);
        contentBuffer.append("|");
        contentBuffer.append(merDate);
        contentBuffer.append("|");
        contentBuffer.append(amount);
        contentBuffer.append("|");
        contentBuffer.append(callbackURL);
        contentBuffer.append("|");//merPriv
        contentBuffer.append("|");//expand
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
