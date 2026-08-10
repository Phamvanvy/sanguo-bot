package com.pip.server.auth.cmcc;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.pip.server.auth.bean.RecommendRecord;
import com.pip.server.auth.bean.UserRegion;
import com.pip.server.auth.dao.BaseDao;

/**
 * 卓望平台用户信息同步接口。
 * GET方式
 * 参数:
 *  userId 用户ID
 *  key 会话key
 */
public class CmccUserNotifyServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CmccUserNotifyServlet.class);
    private CmccUserCache cache;
    private String forwardURL;

    public CmccUserNotifyServlet(CmccUserCache cache, String furl) {
        super();
        this.cache = cache;
        this.forwardURL = furl;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        String key = request.getParameter("key");
        String region = request.getParameter("region");
        if (region == null) {
            region = "";
        }
        log.info("CMCC User Login, UserID[" + userId + "]Key[" + key + "]Region[" + region + "]");
        CmccUserKey u = new CmccUserKey(userId, key, System.currentTimeMillis(), region);
        cache.addUserKey(u);
        response.setCharacterEncoding("GBK");
        response.getWriter().print("0");
        
        // 如果区域信息不为空，则保存到数据库中
        if (region.length() > 0) {
            try {
                String hql = "from UserRegion ur where ur.userID = '" + userId + "'";
                BaseDao dao = new BaseDao();
                UserRegion oldRecord = (UserRegion)dao.uniqueResult(hql);
                if (oldRecord == null) {
                    UserRegion newRecord = new UserRegion();
                    newRecord.setUserID(userId);
                    newRecord.setRegion(region);
                    dao.makePersistent(newRecord);
                }
            } catch (Exception e) {
                log.error(e, e);
            }
        }
        
        // 如果设置了转发，则调用转发url
        if (forwardURL != null && forwardURL.length() > 0) {
            String url = forwardURL + "?userId=" + userId + "&key=" + key + "&region=" + region;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)new URL(url).openConnection();
                conn.getResponseCode();
            } catch (Exception e) {
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }
}
