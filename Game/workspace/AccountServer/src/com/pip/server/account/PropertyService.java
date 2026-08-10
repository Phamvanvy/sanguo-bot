package com.pip.server.account;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.pip.server.account.SourceManager.SourceDef;

public class PropertyService implements Runnable{
	
	protected File f;
	protected boolean monitor;
	protected Thread thread;
	protected long stamp;
	
	protected Map<String,String> map;
	
	public PropertyService(File f,boolean monitor) throws Exception{
		this.f = f;
		map = load(f);
		stamp = f.lastModified();
		if(monitor){
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	protected Map<String,String> load(File file) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String s = null;
		Map<String,String> result = new HashMap<String,String>();
		while ((s = reader.readLine()) != null) {
			if (!s.startsWith("#")) {
				String[] ss = s.split(";");
				if(ss.length==2){
					result.put(ss[0], ss[1]);
				}
			}
		}
		return result;
	}
	
	public String get(String key){
		return map.get(key);
	}
	
	public void run(){
		while(true){
			long curStamp = f.lastModified();
			if(curStamp!=stamp){
				stamp = curStamp;
				try {
					map = load(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {
			}
		}
	}
}
