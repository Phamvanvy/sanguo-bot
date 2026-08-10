package com.pip.server.auth.cmcc;

import javax.servlet.http.HttpServlet;
import java.io.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import com.pip.server.auth.AccountService;
import com.pip.server.auth.AccountState;
import com.pip.server.auth.ConnectService;
import com.pip.server.auth.FeeService;
import com.pip.server.auth.net.AccountConstants;
import com.pip.server.auth.net.UWAPSegment;

/**
 * 卓望平台短信购买道具结果通知接口。
 * POST方式
 * 请求数据：
 * <?xml version="1.0" encoding="UTF-8"?>
 * <request>
 *   <hRet>0</hRet>
 *   <status>1800</status>
 *   <transIDO>12345678901234567</transIDO>
 *   <versionId>100</versionId>
 *   <userId>12345678</userId>
 *   <cpServiceId>120123002000</cpServiceId>
 *   <consumeCode>120123002001</consumeCode>
 *   <cpParam>0000000000000000</cpParam>
 * </request>
 * 返回数据：
 * <?xml version="1.0" encoding="UTF-8"?>
 * <response>
 *   <transIDO>12345678901234567</transIdo>
 *   <hRet>0</hRet>
 *   <message>Successful</message>
 * </response>
 * 参数:
 *  userId 用户ID
 *  key 会话key
 */
public class CmccSmsBuyNotifyServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CmccSmsBuyNotifyServlet.class);
    private CmccService cmccService;
    private CmccUserCache cache;
    private AccountService accountService;
    private FeeService feeService;

    public CmccSmsBuyNotifyServlet(CmccService cmccService, CmccUserCache cache,
           AccountService accountService, FeeService feeService) {
        super();
        this.cmccService = cmccService;
        this.cache = cache;
        this.accountService = accountService;
        this.feeService = feeService;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 解析输入参数
        String userId = null;
        String cpServiceId = null;
        String consumeCode = null;
        String reqID = null;
        int hRet = 0;
        int status = 0;
        String transIDO = null;
        try {
            SAXReader reader = new SAXReader();
            InputStreamReader isr = new InputStreamReader(request.getInputStream(), "UTF-8");
            Document doc = reader.read(isr);
            // 临时测试代码
//            String testStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
// "<request>" +
//   "<hRet>0</hRet>" +
//   "<status>1800</status>" +
//   "<transIDO>12345678901234567</transIDO>" +
//   "<versionId>100</versionId>" +
//   "<userId>test001</userId>" +
//   "<cpServiceId>120123002000</cpServiceId>" +
//   "<consumeCode>120121916043</consumeCode>" +
//   "<cpParam>0000000001000011</cpParam>" +
// "</request>";
//            Document doc = reader.read(new StringReader(testStr));
            Element root = doc.getRootElement();
            userId = root.elementText("userId");
            cpServiceId = root.elementText("cpServiceId");
            consumeCode = root.elementText("consumeCode");
            reqID = root.elementText("cpParam");
            hRet = Integer.parseInt(root.elementText("hRet"));
            status = Integer.parseInt(root.elementText("status"));
            transIDO = root.elementText("transIDO");
            if (transIDO == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new ServletException("输入参数错误");
        }
        
        // 设置输出编码
        response.setContentType("text/xml;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        log.info("SMSBuyNotify ReqID[" + reqID + "]UserID[" + userId + "]SMSConsumeCode[" + 
                consumeCode + "]hRet[" + hRet + "]status[" + status + "]transIDO[" + transIDO + "]");
        
        // 如果是购买错误，直接返回
        if (hRet != 0) {
            succ(out, transIDO);
            return;
        }
        
        // 根据请求ID查找请求对象
        if (reqID.equals("0000000000000000")) {
            // 特殊处理，如果是旧充值请求，直接写数据库即可
            feeService.newChargedFee(-1, cmccService.getPrice(consumeCode), 
                    "CMCC_" + userId);
        } else {
            CmccSmsBuyReq reqObj = cache.findSmsBuyReq(reqID);
            if (reqObj == null) {
                log.info("SMSBuyNotify ReqID[" + reqID + "] Not Found");
                error(out, transIDO, "购买请求已失效");
                return;
            }
            
            // 验证消费代码一致，避免扣费和物品价格不一致
            if (!reqObj.consumeCode.equals(consumeCode)) {
                log.info("SMSBuyNotify ReqID[" + reqID + "] ConsumeCode Mismatch");
                error(out, transIDO, "购买请求已失效");
                return;
            }
            
            // 检查用户是否还在线
            AccountState account = accountService.getAccount(reqObj.accountId);
            if (account == null || account.getSession() == null || 
                    !reqObj.serverId.equals(account.getSession().getWorldID())) {
                log.info("SMSBuyNotify ReqID[" + reqID + "] Account Not Online");
                error(out, transIDO, "购买请求已失效");
                return;
            }
            
            // 发生购买成功的包
            UWAPSegment segment = new UWAPSegment(AccountConstants.CMCC_SMS_BUY_SUCC);
            segment.writeInt(reqObj.requestId);
            segment.writeInt(reqObj.accountId);
            segment.writeInt(reqObj.playerId);
            segment.writeString(reqObj.token);
            account.getSession().write(segment);
            
            // 在fee表中插入一条记录
            feeService.newChargedFee(reqObj.accountId, cmccService.getPrice(consumeCode), 
                    "CMCC_" + userId);
        }

        log.info("SMSBuyNotify ReqID[" + reqID + "] OK");
        
        succ(out, transIDO);
    }
    
    protected void error(PrintWriter out, String transIDO, String errorMsg) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<response>");
        out.println("<transIDO>" + transIDO + "</transIDO>");
        out.println("<hRet>1</hRet>");
        out.println("<message>" + errorMsg + "</message>");
        out.println("</response>");
    }
    
    protected void succ(PrintWriter out, String transIDO) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<response>");
        out.println("<transIDO>" + transIDO + "</transIDO>");
        out.println("<hRet>0</hRet>");
        out.println("<message>Successful</message>");
        out.println("</response>");
    }
}
