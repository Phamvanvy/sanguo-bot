package com.pip.servermgr;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.SecurityUtils;
import com.pip.servermgr.data.Server;
import com.pip.util.DBConfig;
import com.pip.util.DataFetcher;

/**
 * 这个Servlet在服务器启动一个数据获取线程来读取服务器（例如数据库）里的一些数据，并保存到文件中。进度可以用
 * FetchDataControlServlet来查询。
 * 参数：通过post二进制流传递，格式为：用户名，密码，服务器路径，参数数量，(参数名、字符串参数值)*n，执行类名，类长度，执行类内容。
 * 返回(UTF-8): 
 * 		第一行操作ID
 * 		第二行提取密码
 * @author lighthu
 */
public class FetchDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static AtomicInteger idGen = new AtomicInteger();
	public static Hashtable<Integer, DataFetcher> allFetchers = new Hashtable<Integer, DataFetcher>();
	public static Hashtable<Integer, Integer> fetchPassword = new Hashtable<Integer, Integer>();
	static {
		try {
			DBConfig.init("");
		} catch (Exception e) {
		}
	}
	
    public FetchDataServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 读取上传数据
		DataInputStream dis = new DataInputStream(request.getInputStream());
		String userName = dis.readUTF();
		String password = dis.readUTF();
		try {
			password = SecurityUtils.decryptPassword(password);
		} catch (Exception e) {
			throw new ServletException();
		}
		String path = dis.readUTF();
		int paramCount = dis.readInt();
		Map<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < paramCount; i++) {
			params.put(dis.readUTF(), dis.readUTF());
		}
		String clsName = dis.readUTF();
		byte[] clsData = new byte[dis.readInt()];
		dis.readFully(clsData);
		dis.close();
		
		// 检查权限
		if (!Configuration.authenticate(userName, password, path, false, request.getRemoteAddr())) {
			throw new ServletException("权限错误");
		}
		
		// 查找对应的服务器
		Server server = Configuration.findServer(path);
		if (server == null || server.dbInfo == null) {
			throw new ServletException("服务器不存在");
		}

		try {
			// 创建DataFetcher
			SingleClassLoader cl = new SingleClassLoader(getClass().getClassLoader(), clsName, clsData);
			Class cls = cl.loadClass(clsName);
			DataFetcher fetcher = (DataFetcher)cls.newInstance();
			int newID = idGen.incrementAndGet();
			fetcher.setDBInfo(server.dbInfo.slaveURL, server.dbInfo.user, server.dbInfo.password);
			params.put("productName", server.parent.parent.name);
			params.put("serverName", server.parent.name);
			fetcher.setParams(params);
			allFetchers.put(newID, fetcher);
			int pass = new Random().nextInt();
			fetchPassword.put(newID, pass);
			new Thread(fetcher).start();
			
			// 返回ID
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			pw.println(newID);
			pw.println(pass);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
