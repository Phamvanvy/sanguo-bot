package com.pip.itimes.server.util;

import java.io.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;
import org.apache.log4j.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordsUtil {
    private static Logger log = Logger.getLogger(KeywordsUtil.class);
    private static Pattern[] patterns = null;
    static{
        try {
            KeywordsUtil.loadKeywords(new File(System.getProperty("user.dir") +
                                               "/keywords.xml"));
            patterns = loadPatterns(new File(System.getProperty("user.dir")+"/invalidname.txt"));
        } catch (Exception ex) {
            log.error(ex,ex);
        }
    }

    public KeywordsUtil() {
    }

    private static Pattern[] loadPatterns(File file) throws Exception{
        FileReader fr =new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        ArrayList retList = new ArrayList();
        String line;
        while ((line = br.readLine()) != null) {
            retList.add(Pattern.compile(line));
        }
        fr.close();
        Pattern[] ret = new Pattern[retList.size()];
        retList.toArray(ret);
        return ret;
    }

    public static boolean isInvalidName(String name) {
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


    public static void loadKeywords(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        ArrayList list = new ArrayList();
        Iterator itor1 = root.elementIterator("keyword");
        //初始化keyWordsState
        KeyWordsState.root = new KeyWordsState();
        while (itor1.hasNext()) {
            Element keyElem = (Element)itor1.next();
            String keyword = keyElem.getText();
            String replace = keyElem.attributeValue("replacement");
            list.add(new Keyword(keyword, replace));
            KeyWordsState.addString(keyword);
        }
        KeyWordsState.init();
    }

    // 过滤字符串中出现的敏感关键词
    public static String filterKeywords(String str) {
//    //先转成小写,再进行检查
//    HashMap map = KeyWordsState.match(str.toLowerCase());
        HashMap map = KeyWordsState.match(str);
        Object[] keys = map.keySet().toArray();
        int size = keys.length;

        char[] chars = str.toCharArray();
        StringBuffer buffer = new StringBuffer();
        int length = chars.length;
        int j = 0;
        Integer index, strLength;
        //排序0
        if (size != 1) {
            List list = Arrays.asList(keys);
            Collections.sort(list);
            keys = list.toArray();
        }
        if (size > 0) {
            index = (Integer)keys[j];
            strLength = (Integer)map.get(index);
            for (int i = 0; i < length; i++) {
                if (i == index.intValue() && (j < size)) {
                    for (int m = 0; m < strLength.intValue(); m++) {
                        buffer.append('X');
                    }
                    i = i + strLength.intValue() - 1;
                    j++;
                    if (j < size) {
                        index = (Integer)keys[j];
                    }
                    strLength = (Integer)map.get(index);
                } else {
                    buffer.append(chars[i]);
                }
            }
            str = buffer.toString();
        }
        Pattern pattern = Pattern.compile("(http://|https://|www.|wap.){1}[\\w\\.\\-/:]+");
        Matcher matcher = pattern.matcher(str);
        while(matcher.find()){
        	str = str.replaceAll(matcher.group(),"wap.pipfit.com");
        }
        return str;
    }
    /**
     * @param content
     * @return 输入是否有效果
     */
    public static boolean isLegitimate(String content){
    	boolean flag = true;
    	HashMap map = KeyWordsState.match(content);
        Object[] keys = map.keySet().toArray();
        int size = keys.length;

        char[] chars = content.toCharArray();
        StringBuffer buffer = new StringBuffer();
        int length = chars.length;
        int j = 0;
        Integer index, strLength;
        //排序0
        if (size != 1) {
            List list = Arrays.asList(keys);
            Collections.sort(list);
            keys = list.toArray();
        }
        if (size > 0) {
            index = (Integer)keys[j];
            strLength = (Integer)map.get(index);
            for (int i = 0; i < length; i++) {
                if (i == index.intValue() && (j < size)) {
                  flag = false;
                  return flag;
                } 
            }
        }
        Pattern pattern = Pattern.compile("(http://|https://|www.|wap.){1}[\\w\\.\\-/:]+");
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()){
        	flag = false;
        	return flag;
        }
    	return flag;
    }
}
