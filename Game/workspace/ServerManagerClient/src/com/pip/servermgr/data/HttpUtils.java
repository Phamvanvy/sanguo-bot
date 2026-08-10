package com.pip.servermgr.data;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * 执行服务器通讯操作的类。
 * @author lighthu
 */
public class HttpUtils {
	public static final String SERVER1 = "http://221.179.216.54:7001/ServerManager/";
	public static final String SERVER2 = "http://211.151.99.70:7001/ServerManager/";
	public static final String PROXY_URL = "http://211.151.99.70:7070/redir/rpx?url=";
	public static String baseURL = "http://211.151.99.70:7001/ServerManager/";
	public static String proxyURL = null;
	
	/**
	 * 向服务器请求同步时间。
	 */
	public static void syncTime() throws Exception {
		String url = baseURL + "gettime";
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = br.readLine().trim();
            long timestamp = Long.parseLong(line);
            SecurityUtils.updateServerTime(timestamp);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 登录服务器，返回产品配置XML文件。
	 */
	public static String login() throws Exception {
		String url = baseURL + "login?username=" + URLEncoder.encode(Configuration.userName, "UTF-8") + "&password=" +
			URLEncoder.encode(Configuration.getEncryptPassword(), "UTF-8");
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
            	int ch = is.read();
            	if (ch == -1) { 
            		break;
            	}
            	bos.write(ch);
            }
            return new String(bos.toByteArray(), "UTF-8");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 修改密码。
	 */
	public static void modifyPassword(String newpass) throws Exception {
		String url = baseURL + "modifypass?username=" + URLEncoder.encode(Configuration.userName, "UTF-8") + "&password=" +
			URLEncoder.encode(Configuration.getEncryptPassword(), "UTF-8") + "&newpass=" + 
			URLEncoder.encode(SecurityUtils.encryptPassword(newpass), "UTF-8");
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            Configuration.password = newpass;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}

	/**
	 * 执行一个服务器脚本。 
	 * @param cmd 脚本相对路径
	 * @param param 脚本参数
	 * @param hasRet 是否需要返回内容
	 * @param forceUpdate 是否强制刷新
	 * @return 如果有返回内容，返回结果文本
	 * @throws Exception
	 */
	public static String executeShell(String cmd, String param, boolean hasRet, boolean forceUpdate) throws Exception {
		String url = baseURL + "execute?username=" + URLEncoder.encode(Configuration.userName, "UTF-8") + "&password=" +
			URLEncoder.encode(Configuration.getEncryptPassword(), "UTF-8") + "&cmd=" + URLEncoder.encode(cmd, "UTF-8") + "&param=" +
			URLEncoder.encode(param, "UTF-8") + "&hasret=" + (hasRet ? "1" : "0") + "&forceupdate=" + 
			(forceUpdate ? "1" : "0");
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
            	int ch = is.read();
            	if (ch == -1) { 
            		break;
            	}
            	bos.write(ch);
            }
            return new String(bos.toByteArray(), "UTF-8");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 列出服务器一个目录的文件。
	 * @param path 目录相对路径
	 * @return 每个文件一行，各字段用空格分隔，格式为：文件名 文件大小 最后修改时间
	 * @throws Exception
	 */
	public static String listFile(String path) throws Exception {
		String url = baseURL + "listfile?username=" + URLEncoder.encode(Configuration.userName, "UTF-8") + "&password=" +
			URLEncoder.encode(Configuration.getEncryptPassword(), "UTF-8") + "&path=" + URLEncoder.encode(path, "UTF-8");
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
            	int ch = is.read();
            	if (ch == -1) { 
            		break;
            	}
            	bos.write(ch);
            }
            return new String(bos.toByteArray(), "UTF-8");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 上传文件的一部分。
	 * @param path 目标文件相对路径
	 * @param totalSize 文件总大小
	 * @param startPos 本段数据开始位置
	 * @param fileData 本段文件数据
	 * @throws Exception
	 */
	public static void uploadFile(String path, int totalSize, int startPos, byte[] fileData) throws Exception {
		String url = baseURL + "uploadfile";
		url = wrapURL(url);
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeUTF(Configuration.userName);
            dos.writeUTF(Configuration.getEncryptPassword());
            dos.writeUTF(path);
            dos.writeInt(totalSize);
            dos.writeInt(startPos);
            dos.writeInt(fileData.length);
            dos.write(fileData);
            dos.close();
            
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 启动下载文件任务。
	 * @param path 目标文件相对路径
	 * @return 返回的对象数组中，第一个是HttpURLConnection对象，第二个是InputStream对象，第三个是表示总长度的Integer
	 */
	public static Object[] downloadFile(String path) throws Exception {
		String url = baseURL + "downloadfile?username=" + URLEncoder.encode(Configuration.userName, "UTF-8") + "&password=" +
			URLEncoder.encode(Configuration.getEncryptPassword(), "UTF-8") + "&path=" + URLEncoder.encode(path, "UTF-8");
		return downloadFile(new URL(url));
	}
	
	/**
	 * 启动下载文件任务。
	 * @param url 目标文件URL
	 * @return 返回的对象数组中，第一个是HttpURLConnection对象，第二个是InputStream对象，第三个是表示总长度的Integer
	 */
	public static Object[] downloadFile(URL urlObj) throws Exception {
		urlObj = new URL(wrapURL(urlObj.toString()));
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            conn = (HttpURLConnection)urlObj.openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            int length = conn.getContentLength();
            is = conn.getInputStream();
            return new Object[] { conn, is, length };
        } catch (Exception ee) {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
            throw ee;
        }
	}
	
	/**
	 * 对文件列表进行排序。
	 * @param files 每个String[]的第一个元素是文件名。
	 */
	public static void sortFiles(List<String[]> files) {
		int len = files.size();
		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				if (compareFileName(files.get(i)[0], files.get(j)[0]) > 0) {
					String[] temp = files.get(i);
					files.set(i, files.get(j));
					files.set(j, temp);
				}
			}
		}
	}
	
	/**
	 * 比较两个文件名，比较时要把前面的路径名去掉。
	 * @param name1
	 * @param name2
	 * @return 如果name1大于name2，返回1，小于返回-1，等于返回0
	 */
	public static int compareFileName(String name1, String name2) {
		int pos = name1.lastIndexOf('/');
		if (pos >= 0) {
			name1 = name1.substring(pos + 1);
		}
		pos = name2.lastIndexOf('/');
		if (pos >= 0) {
			name2 = name2.substring(pos + 1);
		}
		return name1.compareTo(name2);
	}
	
	/**
	 * 访问一个URL，返回返回码。
	 * @param url
	 * @return 
	 * @throws Exception
	 */
	public static int httpGet(String url, String userName, String password) throws Exception {
		HttpClient httpclient = new HttpClient();
		httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
		httpclient.getParams().setSoTimeout(30000);
		Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
		httpclient.getState().setCredentials(AuthScope.ANY, defaultcreds);
		GetMethod method = new GetMethod(url);
        try {
        	method.addRequestHeader("Connection", "close");
        	return httpclient.executeMethod(method);
        } finally {
        	method.releaseConnection();
        }
	}
	
	/**
	 * 连接服务器启动数据提取线程。
	 * @param path 服务器shell路径，服务器用来判断是哪一个服务器
	 * @param params 参数
	 * @param clsName 提取类名
	 * @return int[2]，提取ID+提取密码
	 * @throws Exception
	 */
	public static int[] startFetchData(String path, Map<String, String> params, String clsName) throws Exception {
		String url = baseURL + "fetchdata";
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            // 参数：用户名、密码、服务器shell路径、参数个数、（参数名+参数值）*n、类名、类长度、类内容
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeUTF(Configuration.userName);
            dos.writeUTF(Configuration.getEncryptPassword());
            dos.writeUTF(path);
            dos.writeInt(params.size());
            for (String key : params.keySet()) {
            	dos.writeUTF(key);
            	dos.writeUTF(params.get(key));
            }
            dos.writeUTF(clsName);
            byte[] clsData = readClass(clsName);
            dos.writeInt(clsData.length);
            dos.write(clsData);
            dos.close();
            
            // 连接服务器
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            
            // 读取返回数据
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            int id = Integer.parseInt(br.readLine());
            int pass = Integer.parseInt(br.readLine());
            br.close();
            return new int[] { id, pass };
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	// 读取一个类的内容
	private static byte[] readClass(String name) {
		InputStream is = null;
		try {
			String path = "/" + name.replace('.', '/') + ".class";
			is = HttpUtils.class.getResourceAsStream(path);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			while (true) {
				int len = is.read(buf);
				if (len == -1) {
					break;
				} else if (len > 0) {
					bos.write(buf, 0, len);
				}
			}
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				is.close();
			} catch (Exception e1) {
			}
		}
	}
	
	/**
	 * 取消一个服务器数据提取线程。
	 * @param id 提取ID
	 * @param pass 提取密码
	 */
	public static void cancelFetchData(int id, int pass) {
		String url = baseURL + "fetchdatactl?op=cancel&id=" + id + "&password=" + pass;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.getResponseCode();
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 清理一个服务器数据提取线程。
	 * @param id 提取ID
	 * @param pass 提取密码
	 */
	public static void cleanFetchData(int id, int pass) {
		String url = baseURL + "fetchdatactl?op=clean&id=" + id + "&password=" + pass;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            conn.getResponseCode();
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 查询一个服务器数据提取线程的进度。
	 * @param id 提取ID
	 * @param pass 提取密码
	 * @return 第一个对象是Integer表示返回代码；第二个对象是错误信息。
	 */
	public static Object[] queryFetchData(int id, int pass) throws Exception {
		String url = baseURL + "fetchdatactl?op=query&id=" + id + "&password=" + pass;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            int code = conn.getResponseCode();
            if (code != 200) {
            	throw new Exception("错误码: " + code);
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            int ret = Integer.parseInt(br.readLine());
            String info = br.readLine();
            return new Object[] { new Integer(ret), info };
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
	
	/**
	 * 下载已经提取好的文件。
	 * @param id 提取ID
	 * @param pass 提取密码
	 * @return 返回的对象数组中，第一个是HttpURLConnection对象，第二个是InputStream对象，第三个是表示总长度的Integer
	 */
	public static Object[] downloadFetchData(int id, int pass) throws Exception {
		String url = baseURL + "fetchdatactl?op=download&id=" + id + "&password=" + pass;
		return downloadFile(new URL(url));
	}
	
	/*
	 * 把某个URL转换为通过代理服务器访问的URL。
	 */
	private static String wrapURL(String url) throws Exception {
		if (proxyURL == null) {
			return url;
		}
		return proxyURL + URLEncoder.encode(url, "GBK");
	}
	
	/**
	 * 访问一个URL，并返回内容。
	 * @param url 目标文件URL
	 * @return byte[]
	 */
	public static byte[] httpGet(String url) throws Exception {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            conn = (HttpURLConnection)new URL(url).openConnection();
            int retCode = conn.getResponseCode();
            if (retCode != 200) {
            	throw new Exception("错误码" + retCode);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[2048];
            is = conn.getInputStream();
            while (true) {
            	int len = is.read(buf);
            	if (len > 0) {
            		bos.write(buf, 0, len);
            	} else if (len < 0) {
            		break;
            	}
            }
            return bos.toByteArray();
        } finally {
        	try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
            }
        }
	}
}
