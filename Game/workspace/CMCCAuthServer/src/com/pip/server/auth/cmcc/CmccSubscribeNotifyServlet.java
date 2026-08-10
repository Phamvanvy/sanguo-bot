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
import com.pip.server.auth.bean.RecommendRecord;
import com.pip.server.auth.bean.RecommendRequest;
import com.pip.server.auth.dao.BaseDao;
import com.pip.server.auth.net.AccountConstants;
import com.pip.server.auth.net.UWAPSegment;

/**
 * 卓望平台订购业务结果通知接口。
 * GET方式，参数userId（用户ID），busiType（业务类型）和result（开通结果）
 * 合作方服务器正常应答（http状态200），并输出字符串0表示接收成功。
 */
public class CmccSubscribeNotifyServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CmccSubscribeNotifyServlet.class);
    private ConnectService connectService;

    public CmccSubscribeNotifyServlet(ConnectService cs) {
        super();
        connectService = cs;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 解析输入参数
        String userId = request.getParameter("userId");
        String busiType = request.getParameter("busiType");
        String result = request.getParameter("result");
        
        log.info("[CMCC_SUBSCRIBE_NOTIFY]userId[" + userId + "]busiType[" + busiType+ 
                "]result[" + result + "]");
        
        // 把这个消息广播给所有连接
        UWAPSegment seg = new UWAPSegment(AccountConstants.CMCC_SUBSCRIBE_NOTIFY);
        seg.writeString(userId);
        seg.writeInt(Integer.parseInt(busiType));
        seg.writeBoolean("1".equals(result));
        connectService.broadcast(seg);
        
        // 设置输出编码
        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("0");
    }
}
