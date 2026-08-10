package com.pip.j0ide;

import java.io.*;
import java.util.*;

import com.pip.util.Utils;

public class Settings {
	public static File workingDir = new File(".");
	public static File polishDir = new File("C:/Program Files/J2ME-Polish");
	public static String imageEditor = "mspaint.exe";
	public static String imageEditorArg = "\"{0}\"";
	public static File tileLibDir = new File(".");
	public static File npcImageDir = new File(".");
	public static String compileThreadCount = "1";
	
	// 历史项目目录
	public static List<String> projectHistory = new ArrayList<String>();
	
	// 项目的输入目录历史，最新的在最前
	public static List<String> projectOutputPath = new ArrayList<String>();
	
	public static void loadSetting() {
		String home = System.getProperty("user.home");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(home, "j0ide.dat"));
			DataInputStream dis = new DataInputStream(fis);
			String path = dis.readUTF();
			workingDir = new File(path);
			path = dis.readUTF();
			polishDir = new File(path);
			path = dis.readUTF();
			tileLibDir = new File(path);
			path = dis.readUTF();
			npcImageDir = new File(path);
		} catch (IOException e) {
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		try {
		    // 新版本配置文件
		    String config = Utils.loadFileContent(new File(home, "j0ide.cfg"));
            Properties prop = new Properties();
            prop.load(new StringReader(config));
            workingDir = new File(prop.getProperty("working_dir"));
            polishDir = new File(prop.getProperty("polish_dir"));
            tileLibDir = new File(prop.getProperty("tilelib_dir"));
            npcImageDir = new File(prop.getProperty("npcimage_dir"));
            compileThreadCount = prop.getProperty("compileThreadCount");
            if(compileThreadCount == null) {
            	compileThreadCount = "1";
            }
            for (int i = 1; i < 100000; i++) {
                String path = prop.getProperty("project" + i);
                if (path == null) {
                    break;
                }
                projectHistory.add(path);
            }
		} catch (Exception e) {
		}
	}

	public static void saveSetting() {
		if (workingDir == null) {
			return;
		}
		String home = System.getProperty("user.home");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(home, "j0ide.dat"));
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(fos));
			dos.writeUTF(workingDir.getAbsolutePath());
			dos.writeUTF(polishDir.getAbsolutePath());
			dos.writeUTF(tileLibDir.getAbsolutePath());
			dos.writeUTF(npcImageDir.getAbsolutePath());
			dos.writeUTF(compileThreadCount);
			dos.flush();
		} catch (IOException e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		
		// 新版本配置文件
		try {
		    Properties prop = new Properties();
		    prop.setProperty("working_dir", workingDir.getAbsolutePath());
		    prop.setProperty("polish_dir", polishDir.getAbsolutePath());
		    prop.setProperty("tilelib_dir", tileLibDir.getAbsolutePath());
		    prop.setProperty("npcimage_dir", npcImageDir.getAbsolutePath());
		    prop.setProperty("compileThreadCount", compileThreadCount);
		    for (int i = 0; i < projectHistory.size(); i++) {
		        String key = "project" + (i + 1);
		        prop.setProperty(key, projectHistory.get(i));
		    }
            StringWriter sw = new StringWriter();
            prop.store(sw, "");
            Utils.saveFileContent(new File(home, "j0ide.cfg"), sw.toString());
		} catch (Exception e) {
		}
	}
	
	public static void changeWorkingDir(String path) {
	    workingDir = new File(path);
	    projectHistory.remove(path);
	    projectHistory.add(0, path);
	}
	
	public static void loadProjectSetting() {
	    try {
            projectOutputPath.clear();
	        String config = Utils.loadFileContent(new File(workingDir, "project.cfg"));
	        Properties prop = new Properties();
	        prop.load(new StringReader(config));
	        for (int i = 1; i < 10000; i++) {
	            String path = prop.getProperty("output_path" + i);
	            if (path == null) {
	                break;
	            }
	            projectOutputPath.add(path);
	        }
	    } catch (Exception e) {
	    }
	}
	
	public static void saveProjectSetting() {
	    try {
            Properties prop = new Properties();
            for (int i = 0; i < projectOutputPath.size(); i++) {
                String key = "output_path" + (i + 1);
                prop.setProperty(key, projectOutputPath.get(i));
            }
            StringWriter sw = new StringWriter();
            prop.store(sw, "");
            Utils.saveFileContent(new File(workingDir, "project.cfg"), sw.toString());
        } catch (Exception e) {
        }
	}
}
