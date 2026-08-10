package com.pip.server.auth.cmcc;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.pip.server.auth.ConnectService;
import com.pip.server.auth.ConnectSession;
import com.pip.server.auth.bean.RecommendRecord;
import com.pip.server.auth.bean.UserRegion;
import com.pip.server.auth.dao.BaseDao;

public class CmccBackdoorServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CmccBackdoorServlet.class);
    private ConnectService connectService;

    public CmccBackdoorServlet(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        int accountId = Integer.parseInt(request.getParameter("accountId"));
        int playerId = Integer.parseInt(request.getParameter("playerId"));
        int level = Integer.parseInt(request.getParameter("level"));
        
        CmccConnectSession cmccSess = null;
        for (ConnectSession sess : connectService.getAllConnects()) {
            if (sess != null && sess instanceof CmccConnectSession) {
                cmccSess = (CmccConnectSession)sess;
                break;
            }
        }
        try {
            if (level >= 30) {
                cmccSess.jilin_levelUpCheck(userId, accountId, playerId, 30);
            }
            if (level >= 20) {
                cmccSess.jilin_levelUpCheck(userId, accountId, playerId, 20);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
