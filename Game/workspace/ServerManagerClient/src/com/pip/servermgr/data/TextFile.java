package com.pip.servermgr.data;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本类提供对基于行的文本文件的分页浏览支持。
 */
public class TextFile {
	private File source;
	private int[] pageStarts;
	private long lastModified;
	
	public TextFile(File f) throws IOException {
		source = f;
		load();
		lastModified = f.lastModified();
	}
	
	public File getSource() {
		return source;
	}
	
	protected void load() throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(source);
			BufferedInputStream bis = new BufferedInputStream(fis);
			int ch;
			int lines = 0;
			int chCount = 0;
			List<Integer> pages = new ArrayList<Integer>();   // 每10000行一页
			pages.add(0);
			while ((ch = bis.read()) != -1) {
				chCount++;
				if (ch == '\n') {
					lines++;
					if ((lines % 10000) == 0) {
						pages.add(chCount);
					}
				}
			}
			pageStarts = new int[pages.size()];
			for (int i = 0; i < pages.size(); i++) {
				pageStarts[i] = pages.get(i);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
	
	protected void checkLoad() {
		if (lastModified != source.lastModified()) {
			try {
				load();
				lastModified = source.lastModified();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getTotalPages() {
		checkLoad();
		return pageStarts.length;
	}
	
	/**
	 * 读取某一页的内容。
	 * @param pageNo 从1开始的页号
	 * @return
	 * @throws IOException
	 */
	public String getPage(int pageNo) throws IOException {
		checkLoad();
		if (pageNo > pageStarts.length) {
			pageNo = pageStarts.length;
		}
		int start = pageStarts[pageNo - 1];
		int next = pageNo < pageStarts.length ? pageStarts[pageNo] : (int)source.length();
		int len = next - start;
		byte[] buf = new byte[len];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(source);
			fis.skip(start);
			new DataInputStream(fis).readFully(buf);
			return new String(buf, "GBK");
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}
