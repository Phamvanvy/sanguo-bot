package com.pip.servermgr;

import java.io.*;

public class StreamRedirector extends Thread {
	private File file;
	private InputStream is;
	private OutputStream os;
	
	public StreamRedirector(InputStream is, File file) {
		this.is = is;
		this.file = file;
	}
	
	public void run() {
		try {
			os = new FileOutputStream(file);
			while (true) {
				int ch = is.read();
				if (ch == -1) {
					return;
				} else {
					os.write(ch);
				}
			}
		} catch (Exception e) {
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
