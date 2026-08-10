package com.pip.servermgr.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 代理访问一个HTTP地址。
 * 参数：
 * 		url 代理地址
 */
public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getParameter("url");
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			int code = conn.getResponseCode();
			if (code != 200) {
				response.sendError(code);
			} else {
				String type = conn.getContentType();
				String encoding = conn.getContentEncoding();
				int length = conn.getContentLength();
				response.setContentType(type);
				response.setCharacterEncoding(encoding);
				response.setContentLength(length);
				InputStream is = conn.getInputStream();
				OutputStream os = response.getOutputStream();
				byte[] buf = new byte[1024];
				while (true) {
					int len = is.read(buf);
					if (len < 0) {
						break;
					}
					if (len > 0) {
						os.write(buf, 0, len);
					}
				}
				is.close();
				os.close();
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 读取参数
		String url = request.getParameter("url");
		InputStream is = request.getInputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[256];
		int len;
		while ((len = is.read(buf)) != -1) {
			if (len > 0) {
				bos.write(buf, 0, len);
			}
		}
		byte[] data = bos.toByteArray();
		
		// 建立远程连接
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(data);
			os.close();
			int code = conn.getResponseCode();
			if (code != 200) {
				response.sendError(code);
			} else {
				String type = conn.getContentType();
				String encoding = conn.getContentEncoding();
				int length = conn.getContentLength();
				response.setContentType(type);
				response.setCharacterEncoding(encoding);
				response.setContentLength(length);
				is = conn.getInputStream();
				os = response.getOutputStream();
				while ((len = is.read(buf)) != -1) {
					if (len > 0) {
						os.write(buf, 0, len);
					}
				}
				is.close();
				os.close();
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
