package com.pip.itimes.server.auth;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class CmccUserNotifyServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CmccUserNotifyServlet.class);

    private CmccUserCache cache;

    public CmccUserNotifyServlet(CmccUserCache cache) {
        super();
        this.cache = cache;
    }

    public void service(HttpServletRequest request,
                       HttpServletResponse response) throws
           ServletException, IOException {
       log.info("Notify");
       String userId = request.getParameter("userId");
       log.info("UserId:"+userId);
       String key = request.getParameter("key");
       log.info("Key:"+key);
       CmccUserKey u = new CmccUserKey(userId,key);
       cache.addUserKey(u);
       response.setCharacterEncoding("GBK");
       response.getWriter().print("0");
   }

}
