package com.pip.servermgr;

import java.io.*;

public class StreamTerminator extends Thread {
	private InputStream is;
	
	public StreamTerminator(InputStream is) {
		this.is = is;
	}
	
	public void run() {
		try {
			while (true) {
				int ch = is.read();
				if (ch == -1) {
					return;
				}
			}
		} catch (Exception e) {
		}
	}
}
