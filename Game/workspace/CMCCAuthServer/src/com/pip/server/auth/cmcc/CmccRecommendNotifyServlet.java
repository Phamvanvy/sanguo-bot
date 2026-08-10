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
 * 卓望平台推荐用户通知接口。
 * POST方式
 * 参数commandId（用户A的userid），presenteeId（用户B的userid），cDattribution（用户A的归
 * 属地），pDattribution（用户B的归属地）。
 * 合作方服务器正常应答（http状态200），并输出字符串0表示接收成功。
 */
public class CmccRecommendNotifyServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CmccRecommendNotifyServlet.class);

    public CmccRecommendNotifyServlet() {
        super();
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 解析输入参数
        String sourceId = request.getParameter("commandId");
        String targetId = request.getParameter("presenteeId");
        String sourceRegion = request.getParameter("cDattribution");
        String targetRegion = request.getParameter("pDattribution");
        
        log.info("[CMCC_RECOMMEND] sourceId[" + sourceId + "]targetId[" + targetId + "]sourceRegion[" + 
                sourceRegion + "]targetRegion[" + targetRegion + "]");
        
        RecommendRecord rr = new RecommendRecord();
        rr.setSource(sourceId);
        rr.setTarget(targetId);
        rr.setSourceRegion(sourceRegion);
        rr.setTargetRegion(targetRegion);
        rr.setFinishTime(new java.util.Date());
        try {
            new BaseDao().makePersistent(rr);
        } catch (Exception e) {
            log.error(e, e);
        }

        // 设置输出编码
        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("0");
    }
}
