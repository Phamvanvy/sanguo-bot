package com.pip.servermgr;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pip.util.DataFetcher;

/**
 * 这个Servlet用来控制先前创建的数据提取线程，包括：查询状态、中止、下载文件、清理。
 * 参数：
 *		op - 操作类型：query查询、cancel中止、download下载、clean清理
 *      id - DataFetchServlet返回的ID
 *      password - DataFetchServlet返回的提取密码
 * 查询返回(UTF-8): 
 * 		第一行表示状态 0 - 正在执行、1 - 已经完成、2 - 发生错误
 * 		第二行：如果是正在执行，返回执行进度字符串；如果是已经完成，返回文件大小；如果是发生错误，发挥错误字符串；
 * 中止返回：无
 * 下载返回：文件内容byte[]
 * 清理返回：无
 * @author lighthu
 */
public class FetchDataControlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public FetchDataControlServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 验证参数
		String op = request.getParameter("op");
		int id = Integer.parseInt(request.getParameter("id"));
		int password = Integer.parseInt(request.getParameter("password"));
		if (!FetchDataServlet.allFetchers.containsKey(id) || !FetchDataServlet.fetchPassword.containsKey(id)) {
			throw new ServletException("invalid id");
		}
		if (password != FetchDataServlet.fetchPassword.get(id)) {
			throw new ServletException("invalid password");
		}
		DataFetcher fetcher = FetchDataServlet.allFetchers.get(id);
		
		if ("query".equals(op)) {
			// 查询状态
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			if (fetcher.isOver()) {
				String error = fetcher.getError();
				if (error != null) {
					pw.println(2);
					pw.println(error);
				} else {
					pw.println(1);
					pw.println(fetcher.getFile().length());
				}
			} else {
				pw.println(0);
				pw.println(fetcher.getProgress());
			}
		} else if ("cancel".equals(op)) {
			// 取消操作
			FetchDataServlet.allFetchers.remove(id);
			FetchDataServlet.fetchPassword.remove(id);
			if (fetcher.isOver()) {
				fetcher.clean();
			} else {
				fetcher.cancel();
			}
		} else if ("download".equals(op)) {
			// 下载结果文件
			
			// 读取文件内容
			byte[] data = new byte[(int)fetcher.getFile().length()];
			FileInputStream fis = new FileInputStream(fetcher.getFile());
			new DataInputStream(fis).readFully(data);
			fis.close();
			
			// 压缩
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(bos);
			gos.write(data);
			gos.flush();
			gos.close();
			data = bos.toByteArray();

			// 写出
			response.setContentType("application/octet-stream");
			response.setContentLength(data.length);
			ServletOutputStream os = response.getOutputStream();
			os.write(data);
			os.close();
		} else if ("clean".equals(op)) {
			// 清理并删除临时文件
			FetchDataServlet.allFetchers.remove(id);
			FetchDataServlet.fetchPassword.remove(id);
			if (fetcher.isOver()) {
				fetcher.clean();
			} else {
				fetcher.cancel();
			}
		} else {
			throw new ServletException("invalid request");
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
