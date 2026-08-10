package com.pip.itimes.server.billing;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.itimes.server.auth.Const;

/**
 * 账单完成通知接口。用于新浪WAP续费新方式。对此接口的请求必须从新浪平台发出，其他来源的请求一律拒绝。
 */
class WapFeeOkServlet1 extends HttpServlet {
	private static Logger log = Logger.getLogger(SendOrderWapServlet.class);
	private Server server;
	
	public WapFeeOkServlet1(Server s) {
		server = s;
	}
	
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws
            ServletException,
            IOException {
    	log.info("WapFeeOkServlet1: " + request.getQueryString() + ", From: " + request.getRemoteAddr());
    	
        String address = request.getRemoteAddr();
        String s = request.getParameter("FeeID");
        int feeId = Integer.parseInt(s);
        
        // 检查是否合法来源地址
        if (!server.isTrustWapFeeIP(address)) {
            log.info("WapFeeOkServlet1: AddressRejected[" + address +
                     "]feeId[" + feeId + "]");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        // 检查MID是否在黑名单中，或者是否本月上限已达最大限额
        String mid = request.getParameter("MISC_MID");
        String result;
        if (mid != null && mid.length() > 0) {
        	server.loadWapBlackList();
        	if (server.inWapBlackList(mid) || server.getMonthPayment("MID_" + mid) >= Const.MONTH_WAPPAY_MAX) {
        		log.info("WapFeeFail[" + feeId + "]MISC_MID[" + mid + "]");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
        	}
        	result = server.fee(feeId, 0, "MID_" + mid);
        } else {
        	// result = server.fee(feeId, 0, null);
        	log.info("WapFeeFail[" + feeId + "]MISC_MID[]");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (result != null) {
        	response.setContentType("text/vnd.wap.wml;charset=utf-8");
            PrintWriter writer = response.getWriter();
            response.setStatus(HttpServletResponse.SC_OK);

            log.info("WapFeeOk[" + feeId + "]MISC_MID[" + mid + "]");
            StringBuffer buff = new StringBuffer(500);
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            buff.append("<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">\n");
            buff.append("<wml>\n");
            buff.append("<head>\n");
            buff.append(
                    "<meta http-equiv=\"Cache-Control\" content=\"max-age=0\" forua=\"true\"/>\n");
            buff.append("</head>\n");
            buff.append(
                    "<card id=\"home\" title=\"forward\" onenterforward=\"" +
                    Const.PORTAL_REDIRECT + Const.OK + "\">\n");
            buff.append("<p> </p>\n");
            buff.append("</card>\n");
            buff.append("</wml>\n");
            writer.write(buff.toString());
        } else {
            log.info("WapFeeFail[" + feeId + "]MISC_MID[" + mid + "]");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            StringBuffer buff = new StringBuffer(500);
//            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//            buff.append("<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">\n");
//            buff.append("<wml>\n");
//            buff.append("<head>\n");
//            buff.append(
//                    "<meta http-equiv=\"Cache-Control\" content=\"max-age=0\" forua=\"true\"/>\n");
//            buff.append("</head>\n");
//            buff.append(
//                    "<card id=\"home\" title=\"forward\" onenterforward=\"" +
//                    Const.PORTAL_REDIRECT + Const.BILL_NOT_FOUND + "\">\n");
//            buff.append("<p> </p>\n");
//            buff.append("</card>\n");
//            buff.append("</wml>\n");
//            writer.write(buff.toString());
        }    
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws
            ServletException,
            IOException {
        doPost(request, response);
    }
}
