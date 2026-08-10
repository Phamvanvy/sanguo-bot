package com.pip.util;

import java.io.*;
import java.util.*;

public class PropertiesEx {
	protected HashMap<String, String> properties = new HashMap<String, String>();
	
	public void load(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		String line;
		while ((line = br.readLine()) != null) {
			int pos = line.indexOf('=');
			if (pos == -1) {
				continue;
			}
			properties.put(line.substring(0, pos), line.substring(pos + 1));
		}
	}
	
	public void load(InputStream is, String encoding) throws IOException {
		load(new InputStreamReader(is, encoding));
	}
	
	public void save(Writer w) throws IOException {
		PrintWriter pw = new PrintWriter(w);
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = properties.get(key);
			pw.println(key + "=" + value);
		}
		pw.flush();
	}
	
	public void save(OutputStream os, String encoding) throws IOException {
		save(new OutputStreamWriter(os, encoding));
	}
	
	public void clear() {
		properties.clear();
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		String ret = getProperty(key);
		if (ret == null) {
			ret = defaultValue;
		}
		return ret;
	}
	
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
}
