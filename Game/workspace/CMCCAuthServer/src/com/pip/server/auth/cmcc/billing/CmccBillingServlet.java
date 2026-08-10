package com.pip.server.auth.cmcc.billing;

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
import com.pip.server.auth.bean.RecommendRecord;
import com.pip.server.auth.bean.RecommendRequest;
import com.pip.server.auth.cmcc.CmccException;
import com.pip.server.auth.dao.BaseDao;
import com.pip.server.auth.net.AccountConstants;
import com.pip.server.auth.net.UWAPSegment;

/**
 * 卓望待计费接口。
 * 参数：
 *     name - 渠道登录名
 *     password - 渠道密码
 *     userId - 用户ID
 *     code - 渠道消费代码
 * 返回（UTF-8编码文本）
 *     第一行是返回代码：0成功，1失败
 *     如果成功，第二行是消费成功的金额（分）
 *     如果失败，第二行是失败原因
 */
public class CmccBillingServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CmccBillingServlet.class);
    private BillingService billingService;

    public CmccBillingServlet(BillingService bs) {
        super();
        this.billingService = bs;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 解析输入参数
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String userId = request.getParameter("userId");
        String code = request.getParameter("code");
        String info = request.getParameter("info");
        if (info == null) {
            info = "";
        }
        String ip = request.getRemoteAddr();
        
        log.info("[TRY_BILLING]name[" + name + "]password[" + password + "]userid[" + 
                userId + "]code[" + code + "]ip[" + ip + "]");
        
        // 设置输出编码
        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int price = billingService.channelBuy(name, password, ip, userId, code, info);
            out.println("0");
            out.println(price);
        } catch (CmccException e) {
            out.println("1");
            out.println(e.getMessage());
        }
    }
}
