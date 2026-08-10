package com.pip.servermgr.remote;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 执行服务器上的一个命令或脚本。
 * 参数：
 * 		cmd 命令
 * 返回：无
 */
public class ExecuteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ExecuteServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cmd = request.getParameter("cmd");
        Process p = Runtime.getRuntime().exec(cmd);
        new StreamTerminator(p.getInputStream()).start();
        new StreamTerminator(p.getErrorStream()).start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new IOException();
        }
        p.destroy();
	}

    public class StreamTerminator extends Thread {
        private InputStream is;
        
        public StreamTerminator(InputStream is) {
            this.is = is;
        }
        
        public void run() {
            try {
                while (true) {
                    int ch = is.read();
                    if (ch == -1) {
                        return;
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
