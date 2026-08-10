package com.pip.uieditor.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MacroManager {
	
	private static MacroManager instance = new MacroManager();
//	static {
//		try {
//			instance.load(new File("C:/workspaces/iTimes2/iTimes2-Script/touchui/gtl/itimes2/ui/ui_define.h"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
	private HashMap<String, String> macro2file = new HashMap<String, String>();
	private HashMap<String, ArrayList<String>> file2macro = new HashMap<String, ArrayList<String>>();
	
	public MacroManager() {
		
	}
	
	public void load(File file) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String s = null;
		while((s = reader.readLine()) != null) {
			if(s.startsWith("#define")) {
				processDefine(s);
			}
		}
	}
	
	private void processDefine(String s) {
		String[] ss = s.split("\\s+");
		if(ss.length == 3) {
			if(ss[2].startsWith("\"") && ss[2].endsWith(".pip\"") || ss[2].endsWith(".ctn\"")) {
				ss[2] = ss[2].substring(1, ss[2].length() - 1);
				macro2file.put(ss[1], ss[2]);
				ArrayList<String> macros = file2macro.get(ss[2]);
				if(macros == null) {
					macros = new ArrayList<String>(3);
					file2macro.put(ss[2], macros);
				}
				macros.add(ss[1]);
			}
		}
	}
	
	public static MacroManager instance() {
		return instance;
	}
	
	public static void setInstance(MacroManager manager) {
		instance = manager;
	}
	
	public String findMacro(String fileName) {
		ArrayList<String> l = file2macro.get(fileName);
		if(l != null) {
			return l.get(0);
		}
		return null;
	}
	
	public ArrayList<String> findMacros(String fileName) {
		return file2macro.get(fileName);
	}
	
	public String findFileName(String macro) {
		return macro2file.get(macro);
	}
	
	
	public boolean isMacro(String value) {
		return value.indexOf(".") == -1;
	}
}
