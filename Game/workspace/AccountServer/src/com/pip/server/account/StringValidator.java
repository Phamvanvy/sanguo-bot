package com.pip.server.account;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.server.account.util.KeyWords;

public class StringValidator implements IStringValidator, Runnable {

	protected int minLimit;
	protected int maxLimit;
	protected boolean monitor;
	protected File patternFile;
	protected File keywordFile;
	protected Pattern[] patterns;
	protected KeyWords keyWords;
	protected long patterFileStamp;
	protected long keywordFileStamp;
	protected Thread thread;
	
	protected Logger log = Logger.getLogger(StringValidator.class);

	public StringValidator(int minLimit, int maxLimit) throws Exception {
		this(minLimit, maxLimit, false, null, null);
	}

	public StringValidator(int minLimit, int maxLimit, boolean monitor,
			File patternFile, File keywordFile) throws Exception {
		this.minLimit = minLimit;
		this.maxLimit = maxLimit;
		this.monitor = monitor;
		this.patternFile = patternFile;
		this.keywordFile = keywordFile;
		if (patternFile != null) {
			patterFileStamp = patternFile.lastModified();
			patterns = loadPatterns(patternFile);
		}
		if (keywordFile != null) {
			keywordFileStamp = keywordFile.lastModified();
			keyWords = loadKeywords(keywordFile);
		}
		if(monitor){
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	protected Pattern[] loadPatterns(File f) throws Exception {
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		List<Pattern> retList = new ArrayList<Pattern>();
		String line;
		while ((line = br.readLine()) != null) {
			retList.add(Pattern.compile(line));
		}
		fr.close();
		Pattern[] ret = new Pattern[retList.size()];
		retList.toArray(ret);
		return ret;
	}

	protected KeyWords loadKeywords(File f) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(f);
		Element root = doc.getRootElement();
		ArrayList list = new ArrayList();
		Iterator itor1 = root.elementIterator("keyword");
		//≥ı ºªØkeyWordsState
		KeyWords ret = new KeyWords();
		List<String> l = new LinkedList<String>();
		while (itor1.hasNext()) {
			Element keyElem = (Element) itor1.next();
			String keyword = keyElem.getText();
			String replace = keyElem.attributeValue("replacement");
			//            list.add(new Keyword(keyword, replace));
			ret.addString(keyword);
			l.add(keyword);
		}
		ret.init();
		return ret;
	}

	public int valid(String value) {
		if(value==null)
			return IStringValidator.NULL;
		int length = value.length();
		try {
			length = value.getBytes("GBK").length;
		} catch (UnsupportedEncodingException e) {
		}
		if(length<minLimit)
			return IStringValidator.MIN_LIMIT;
		if(length>maxLimit)
			return IStringValidator.MAX_LIMIT;
		if(!checkString(value,false))
			return IStringValidator.ILLEGAL_CHAR;
		if(patterns!=null){
			if(isInvalidName(value)){
				return IStringValidator.ERROR_PATTERN;
			}
		}
		if(keyWords!=null){
			if(keyWords.match(value).size()>0){
				return IStringValidator.ILLEGAL_CHAR;
			}
		}
		return IStringValidator.OK;
	}

    public boolean checkString(String s, boolean allowColon){
        if(s == null){
            return false;
        }
        for(int i = 0; i < s.length(); i++){
            char ch = s.charAt(i);
            boolean isValid = false;
            if(ch >= 'a' && ch <= 'z'){
                isValid = true;
            }else if(ch >= 'A' && ch <= 'Z'){
                isValid = true;
            }else if(ch >= '0' && ch <= '9'){
                isValid = true;
            }else if(ch == '_'){
                isValid = true;
            }else if(ch >= 0x4E00 && ch <= 0x9FA5){
                isValid = true;
            }else if(allowColon && ch == ':'){
                isValid = true;
            }
            if(!isValid){
                return false;
            }
        }
        return true;
    }
	
    private boolean isInvalidName(String name) {
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].matcher(name).matches()) {
                return true;
            }
            if (patterns[i].equals(name)) {
                return true;
            }
        }
        return false;
    }	
	
	public void run() {
		
		while (true) {
			if (patternFile != null) {
				long currentStamp = patternFile.lastModified();
				if (currentStamp != patterFileStamp) {
					patterFileStamp = currentStamp;
					try {
						patterns = loadPatterns(patternFile);
					} catch (Exception e) {
						log.error(e, e);
					}

				}
			}
			if (keywordFile != null) {
				long currentStamp = keywordFile.lastModified();
				if (currentStamp != keywordFileStamp) {
					keywordFileStamp = currentStamp;
					try {
						keyWords = loadKeywords(keywordFile);
					} catch (Exception e) {
						log.error(e, e);
					}
				}
			}
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {
			}
		}
	}
}
