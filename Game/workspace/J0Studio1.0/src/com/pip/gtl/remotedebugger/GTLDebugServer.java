package com.pip.gtl.remotedebugger;

import javax.swing.*;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

import com.pip.gtl.compiler.GTLCompiler;
import com.pip.gtl.decompiler.*;
import com.pip.gtl.remotedebugger.ui.*;
import com.pip.gtl.etf.*;
import javax.swing.event.*;

public class GTLDebugServer implements Runnable {
	// Server Socket
    private ServerSocket listenSocket;
    // Server Thread
    private Thread serverThread;
    // Debug manager
    private GTLDebugManager debugManager;
    // singleton
    private static GTLDebugServer instance;
    
    private GTLDebugServer() {
    	debugManager = new GTLDebugManager();
    }
    
    public static GTLDebugServer getInstance() {
    	if (instance == null) {
    		instance = new GTLDebugServer();
    	}
    	return instance;
    }
    
    public boolean isActive() {
    	return listenSocket != null;
    }
    
    public void start() throws IOException {
    	if (listenSocket != null) {
    		return;
    	}
    	listenSocket = new ServerSocket(32167);
    	serverThread = new Thread(this);
    	serverThread.start();
    }
    
    public void stop() {
    	if (listenSocket != null) {
    		try {
    			Thread oldThread = serverThread;
    			serverThread = null;
    			listenSocket.close();
    			listenSocket = null;
				oldThread.interrupt();
    		} catch (Exception e) {
    		}
    	}
    }

    public void run() {
    	while (listenSocket != null && serverThread == Thread.currentThread()) {
	    	try {
	    		Socket newSock = listenSocket.accept();
	    		debugManager.newSession(newSock);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
    	}
    }
    
    public GTLDebugManager getDebugManager() {
    	return debugManager;
    }
}