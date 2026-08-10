package com.pip.servermgr.report;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.pip.servermgr.StreamRedirector;
import com.pip.servermgr.StreamTerminator;
import com.pip.util.DataFetcher;
import com.pip.util.Utils;

/**
 * 通用玩家数据提取器，这个类使用mysqldump命令来从数据库中直接提取数据。
 * @author lighthu
 */
public class GenericPlayerFetcher implements DataFetcher {
	protected String dbURL;
	protected String dbUser;
	protected String dbPass;
	protected String playerTable;
	protected int playerID;
	protected String encoding;
	protected boolean canceled;
	protected boolean over;
	protected String errorMessage;
	protected String progressText;
	protected File resultFile;

	/**
	 * 设置数据库参数。
	 * @param url 数据库URL
	 * @param name 数据库登录名
	 * @param pass 数据库密码
	 */
	public void setDBInfo(String url, String name, String pass) {
		dbURL = url;
		dbUser = name;
		dbPass = pass;
	}
	
	/**
	 * 设置参数。
	 * @param params
	 */
	public void setParams(Map<String, String> params) {
		playerTable = params.get("playerTable");
		playerID = Integer.parseInt(params.get("playerID"));
		encoding = params.get("encoding");
	}
	
	/**
	 * 判断是否获取完成。
	 * @return
	 */
	public boolean isOver() {
		return over;
	}
	
	/**
	 * 判断是否发生错误。
	 * @return 如果发生错误，返回错误内容，否则返回null。
	 */
	public String getError() {
		return errorMessage;
	}
	
	/**
	 * 获得当前进度字符串。
	 * @return
	 */
	public String getProgress() {
		return progressText;
	}
	
	/**
	 * 取消操作，并删除所有临时文件。
	 */
	public void cancel() {
		canceled = true;
		over = true;
		errorMessage = "已取消";
	}
	
	/**
	 * 获取完成后，取得保存数据的文件。
	 * @return
	 */
	public File getFile() {
		return resultFile;
	}
	
	/**
	 * 执行清理工作，删除临时文件。
	 */
	public void clean() {
		resultFile.delete();
	}
	
	public void run() {
		try {
			// 从数据库JDBC地址中解析出服务器地址、端口号和数据库名
			progressText = "正在提取";
			String hostName;
			String portName;
			String dbName;
			int pos = dbURL.indexOf("//") + 2;
			int pos2 = dbURL.indexOf("/", pos);
			String sub1 = dbURL.substring(pos, pos2);
			if (sub1.contains(":")) {
				int pos3 = sub1.indexOf(':');
				hostName = sub1.substring(0, pos3);
				portName = sub1.substring(pos3 + 1);
			} else {
				hostName = sub1;
				portName = "3306";
			}
			int pos3 = dbURL.indexOf("?", pos2);
			if (pos3 == -1) {
				dbName = dbURL.substring(pos2 + 1);
			} else {
				dbName = dbURL.substring(pos2 + 1, pos3);
			}
			
			// 生成命令行并执行
			String cmd = "/usr/bin/mysqldump -h " + hostName + " -P " + portName + " -u " + dbUser + " -p" + dbPass + " --where=id=" +
				playerID + " --no-create-info --hex-blob --default-character-set=" + encoding + " --complete-insert " + dbName + " " + playerTable;
			File tempFile = execCmd(cmd);
			
			// 如果是幻想和武林，那么还要倒出tbl_task表
			if ("tbl_userdata".equals(playerTable)) {
				cmd = "/usr/bin/mysqldump -h " + hostName + " -P " + portName + " -u " + dbUser + " -p" + dbPass + " --where=id=" +
					playerID + " --no-create-info --hex-blob --default-character-set=" + encoding + " --complete-insert " + dbName + " tbl_task";
				File tempFile2 = execCmd(cmd);
				File tempFile3 = File.createTempFile("_report_data", ".dat");
				byte[] data1 = loadFileData(tempFile);
				byte[] data2 = loadFileData(tempFile2);
				byte[] data3 = new byte[data1.length + data2.length];
				System.arraycopy(data1, 0, data3, 0, data1.length);
				System.arraycopy(data2, 0, data3, data1.length, data2.length);
				saveFileData(tempFile3, data3);
				tempFile.delete();
				tempFile2.delete();
				tempFile = tempFile3;
			}
			
			// 提取成功
			progressText = "提取完成。";
			resultFile = tempFile;
			over = true;
		} catch (Exception e) {
			e.printStackTrace();
			over = true;
			errorMessage = e.toString();
		}
	}
	
	private File execCmd(String cmd) throws Exception {
		// 生成临时文件
		File tempFile = File.createTempFile("_report_data", ".dat");
		File tempFile2 = File.createTempFile("_report_data", ".dat");
		
		// 生成命令行并执行
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			new StreamRedirector(p.getInputStream(), tempFile2).start();
			new StreamRedirector(p.getErrorStream(), tempFile).start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				throw new IOException();
			}
			Thread.sleep(3000);
			p.destroy();
		} catch (Exception e) {
			tempFile.delete();
			tempFile2.delete();
			throw e;
		}
		
		if (tempFile2.length() > 0) {
			tempFile.delete();
			return tempFile2;
		} else {
			tempFile2.delete();
			return tempFile;
		}
	}
	
	/**
     * 载入文件内容到字符数组。
     */
    public static byte[] loadFileData(File src) throws IOException{
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] data = new byte[256];
            int len;
            while((len = bis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                bos.write(data, 0, len);
            }
            return bos.toByteArray();
        }catch(IOException e){
            throw e;
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException e){
                }
            }
        }
    }

    /**
     * 保存数据到文件。
     */
    public static void saveFileData(File dest, byte[] data) throws IOException{
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(dest);
            fos.write(data);
        }catch(IOException e){
            throw e;
        }finally{
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }
}
