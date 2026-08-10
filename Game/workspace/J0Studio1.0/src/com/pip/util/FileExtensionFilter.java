package com.pip.util;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter {
	private String[] extension;
	private boolean allowDir;
	
	public FileExtensionFilter(String[] ext, boolean ad) {
		extension = ext;
		allowDir = ad;
	}
	
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
		    if(pathname.getName().toUpperCase().equals("CVS")){
		        return false;
		    }else{
		        return allowDir;
		    }
		}
		for (int i = 0; i < extension.length; i++) {
			if (pathname.getName().toLowerCase().endsWith("." + extension[i])) {
				return true;
			}
		}
		return false;
	}	
}
