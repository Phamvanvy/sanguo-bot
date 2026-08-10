package com.pip.j0ide;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

import com.pip.uieditor.editor.MacroManager;
import com.pip.util.Utils;

public class Settings {
	public static File workingDir = new File(".");
	public static File polishDir = new File("C:/Program Files/J2ME-Polish");
	public static String imageEditor = "mspaint.exe";
	public static String imageEditorArg = "\"{0}\"";
	public static File tileLibDir = new File(".");
	public static File npcImageDir = new File(".");
	public static String compileThreadCount = "1";
	public static File uiResourceDir = new File(".");
	public static File uiAnimateDir = new File(".");
	public static File numberImageFile = null;
	public static File uiDefineFile = null;
	public static int[] numberImageMaps = {0, 1, 2, 3, 4, 5, 6, 7 ,8, 9};
	public static int textStyle = 0;
	
	public static FontData defaultFont = new FontData("Courier New", 16, SWT.NORMAL);
	
	public static Map<String, FontData> fonts = new LinkedHashMap<String, FontData>();
	
	
	
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
			compileThreadCount = dis.readUTF();
			path = dis.readUTF();
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
            if(prop.getProperty("ui_resource_dir") != null) {
            	uiResourceDir = new File(prop.getProperty("ui_resource_dir"));
            }
            if(prop.getProperty("ui_animate_dir") != null) {
            	uiAnimateDir = new File(prop.getProperty("ui_animate_dir"));
            }
            if(prop.getProperty("default_font") != null) {
            	FontData font = createFontData(prop.getProperty("default_font"));
            	if(font != null) {
            		defaultFont = font;
            	}
            }
            for(int i = 0; i < 10 ; i++) {
            	String f = "font" + i;
            	if(prop.getProperty(f) != null) {
            		FontData font = createFontData(prop.getProperty(f));
            		String fontName = getFontName(prop.getProperty(f));
            		fonts.put(fontName, font);
            	}
            }
            if(prop.getProperty("number_image_file_name") != null) {
            	numberImageFile = new File(prop.getProperty("number_image_file_name"));
            }
            if(prop.getProperty("number_image_map") != null) {
            	String s = prop.getProperty("number_image_map");
            	String[] ss = s.split(",");
            	int[] tmp = new int[10];
            	if(ss.length == 10) {
            		boolean success = true;
            		for(int i = 0; i < 10;i ++) {
            			tmp[i] = ss[i].charAt(0) - '0';
            			if(tmp[i] < 0 || tmp[i] > 9) {
            				success = false;
            				break;
            			}
            		}
            		if(success) {
            			numberImageMaps = tmp;
            		}
            	}
            }
            if(prop.getProperty("ui_define_file_name") != null) {
            	uiDefineFile = new File(prop.getProperty("ui_define_file_name"));
            	if(uiDefineFile.exists()) {
            		MacroManager mm =  new MacroManager();
            		try {
						mm.load(uiDefineFile);
						MacroManager.setInstance(mm);
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}
            }
            if(prop.getProperty("text_style") != null) {
            	textStyle = Integer.parseInt(prop.getProperty("text_style"));
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
	
	public static FontData createFontData(String setting) {
		String[] ss = setting.split(",");
		try {
			return new FontData(ss[0], Integer.parseInt(ss[1]), SWT.NORMAL);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getFontName(String setting) {
		String[] ss = setting.split(",");
		return ss[2];
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
		    prop.setProperty("ui_resource_dir", uiResourceDir.getAbsolutePath());
		    prop.setProperty("ui_animate_dir", uiAnimateDir.getAbsolutePath());
		    prop.setProperty("number_image_file_name", numberImageFile.getAbsolutePath());
		    if(uiDefineFile != null) {
		    	prop.setProperty("ui_define_file_name", uiDefineFile.getAbsolutePath());
		    }
		    prop.setProperty("default_font", defaultFont.getName() + "," + defaultFont.getHeight());
		    int fontIndex = 0;
		    for(Map.Entry<String, FontData> entry : fonts.entrySet()) {
		    	String f = "font" + fontIndex;
		    	FontData font = entry.getValue();
		    	String fontName = entry.getKey();
		    	prop.setProperty(f, font.getName()+","+font.getHeight()+","+fontName);
		    	fontIndex++;
		    }

		    StringBuilder sb = new StringBuilder(30);
		    for(int i = 0; i < numberImageMaps.length - 1; i++) {
		    	sb.append(numberImageMaps[i]);
		    	sb.append(',');
		    }
		    sb.append(numberImageMaps[numberImageMaps.length - 1]);
		    prop.setProperty("number_image_map", sb.toString());
		    prop.setProperty("text_style", String.valueOf(textStyle));
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
