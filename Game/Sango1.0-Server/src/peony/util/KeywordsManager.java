package peony.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import peony.game.Server;

/**
 * 管理敏感词汇，提供配置文件载入和自动更新功能以及敏感词汇过滤接口。
 * @author lighthu
 */
public class KeywordsManager extends Thread {
	/*
	 * 非法名字配置文件，名字为invalidname.txt
	 */
	protected File patternFile;
	/*
	 * 敏感词汇配置文件，名字为keywords.xml
	 */
	protected File keywordFile;
	/*
	 * 所有非法名字模式
	 */
	protected Pattern[] patterns;
	/*
	 * 所有无通配符的非法名字的集合
	 */
	protected Set<String> invalidStrings; 
	/*
	 * 敏感词汇过滤树的根节点
	 */
	protected KeyWordsState root = new KeyWordsState();
	/*
	 * invalidname.txt的最后修改时间，用于判断自动更新
	 */
	protected long patterFileStamp;
	/*
	 * keywords.xml的最后修改时间，用于判断自动更新
	 */
	protected long keywordFileStamp;
	/*
	 * 附加非法名字表。
	 */
	protected List<String> strings;
	
	/**
	 * 创建实例。系统中应该只有一个KeywordsManager的实例。构造后文件更新检查线程自动启动。
	 * @param patternFile
	 * @param keywordFile
	 * @throws Exception
	 */
	public KeywordsManager(File patternFile, File keywordFile, List<String> strings) throws Exception {
		patterFileStamp = patternFile.lastModified();
		Set<String> iss = new HashSet<String>();
		patterns = loadPatterns(patternFile, strings, iss);
		invalidStrings = iss;
		keywordFileStamp = keywordFile.lastModified();
		this.strings = strings;
		loadKeywords(keywordFile,new ArrayList<String>(0));
		setDaemon(true);
		start();
	}
	
	/**
	 * 判断一个字符串是否包含invalidname.txt中规定不允许使用的模式。
	 * @param name
	 * @return 如果包含，返回true，否则返回false。
	 */
	public boolean isInvalidName(String name) {
	    if (invalidStrings.contains(name)) {
	        return true;
	    }
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

	/**
	 * 判断一个字符串中是否包含keywords.xml中规定不许使用的关键字。
	 * @param name
	 * @return 如果包含，返回true，否则返回false。
	 */
	public boolean containsBadWord(String name) {
		return match(name).size() > 0;
	}
	
	/**
	 * 过滤掉字符串中出现的keywords.xml中规定不允许使用的关键字，如果发现，用X替换。
	 * @param str
	 * @return 返回替换后的字符串。
	 */
	@SuppressWarnings("unchecked")
    public String filterBadWords(String str) {
        HashMap<Integer, Integer> map = match(str);
        Object[] keys = map.keySet().toArray();
        int size = keys.length;

        char[] chars = str.toCharArray();
        StringBuffer buffer = new StringBuffer();
        int length = chars.length;
        int j = 0;
        Integer index, strLength;
        
        //排序
        if (size != 1) {
            List list = Arrays.asList(keys);
            Collections.sort(list);
            keys = list.toArray();
        }
        if (size > 0) {
            index = (Integer)keys[j];
            strLength = map.get(index);
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
                    strLength = map.get(index);
                } else {
                    buffer.append(chars[i]);
                }
            }
            str = buffer.toString();
        }
        return str;
    }

	/*
	 * 从invalidname.txt中载入非法名字模式。
	 */
	protected Pattern[] loadPatterns(File f, List<String> strings, Set<String> invalidStrs) throws Exception {
		BufferedReader br = null;
		if(!Server.server.revision.equals(Server.REVISION_TYPE_PIP)){//外文版用utf8格式读取
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
		} else {
			br = new BufferedReader(new FileReader(f));
		}
		
		
		List<Pattern> retList = new ArrayList<Pattern>();
		String line;
		while ((line = br.readLine()) != null) {
		    line = line.trim();
		    if (line.length() == 0) {
		        continue;
		    }
		    if (line.contains(".") || line.contains("*") || line.contains("+")) {
		        retList.add(Pattern.compile(line));
		    } else {
		        invalidStrs.add(line);
		    }
		}
		
		br.close();
		
		for (String s:strings) {
		    if (s.contains(".") || s.contains("*") || s.contains("+")) {
		        retList.add(Pattern.compile(s));
		    } else {
		        invalidStrs.add(s);
		    }
		}
		Pattern[] ret = new Pattern[retList.size()];
		retList.toArray(ret);
		return ret;
	}

	/*
	 * 从keywords.xml中载入敏感词汇表。
	 */
	@SuppressWarnings("unchecked")
	protected void loadKeywords(File f, List<String> strings) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(f);
		Iterator itor1 = doc.getRootElement().elementIterator("keyword");
		root = new KeyWordsState();
		while (itor1.hasNext()) {
			Element keyElem = (Element) itor1.next();
			String keyword = keyElem.getText();
			String replace = keyElem.attributeValue("replacement");
			addString(keyword);
		}
		if (strings != null) {
			for (String s : strings) {
				addString(s);
			}
		}
		init(root);
	}
	
	/*
	 * 把一个字符串加入到敏感词汇过滤状态树中去。
	 */
	private void addString(String s) {
		KeyWordsState state = root;
		for (int i = 0; i < s.length(); i++) {
			state = state.addState(new Character(s.charAt(i)));
		}
		state.finalState = true;
	}

	/*
	 * 初始化过滤状态树数据结构。
	 */
	@SuppressWarnings("unchecked")
	private void init(KeyWordsState state) {
		Iterator ite = state.nextState.values().iterator();
		while (ite.hasNext()) {
			KeyWordsState s1 = (KeyWordsState) ite.next();
			KeyWordsState s2 = state.failState;
			while (true) {
				if (s2 == null) {
					s1.failState = root;
					break;
				}
				KeyWordsState s3 = s2.getState(s1.character);
				if (s3 != null) {
					s1.failState = s3;
					break;
				}
				s2 = s2.failState;
			}
			init(s1);
		}
	}
	
	/*
	 * 状态树过滤算法。
	 */
    private int reg(HashMap<Integer, Integer> map, KeyWordsState state, int n) {
        int t = 0;
        while (state != root) {
            if (state.finalState) {
                t = n;
                String s = "";
                while (state != root) {
                    s = state.character + s;
                    state = state.parent;
                    n--;
                }
                map.put(n, s.length());
                return t;
            }
            state = state.parent;
            n--;
        }
        return -1;
    }
    
	/*
	 * 状态树过滤算法。
	 */
    private KeyWordsState findNextNode(HashMap<Integer, Integer> map, KeyWordsState state,
			Character c, int i) {
		KeyWordsState tempState = state.getState(c);
		if (tempState == null) {
			if (state == root) {
				return state;
			}
			reg(map, state, i);
			state = state.failState;
			return findNextNode(map, state, c, i);
		} else {
			return tempState;
		}
	}   
    
	/*
	 * 查找字符串中的所有敏感词汇。
	 * @return 返回所有查找结果，key是起始位置，value是长度
	 */
    private HashMap<Integer, Integer> match(String target) {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        int i = 0;
        int n = target.length();
        KeyWordsState state = root;
        int count = 0;

        while (i < n) {
            count++;
            Character c = new Character(target.charAt(i));
            KeyWordsState nS = state.getState(c);
            if (nS == null) {
                if (state == root) {
                    i++;
                } else {
                    int temp = reg(map, state, i);
                    if (temp == -1) {
                        state = state.failState;
                    } else {
                        i = temp;
                        state = root;
                        c = new Character(target.charAt(i));
                    }
                    i++;
                    state = findNextNode(map, state, c, i);
                }
            } else {
                i++;
                state = nS;
            }
        }
        reg(map, state, n);
        return map;
    }    

    /**
     * 文件更新检查线程。定时检查invalidname.txt和keywords.xml两个文件是否改变。如果改变了则自动载入。
     */
	@Override
	public void run() {
		while (true) {
			if (patternFile != null) {
				long currentStamp = patternFile.lastModified();
				if (currentStamp != patterFileStamp) {
					patterFileStamp = currentStamp;
					try {
					    Set<String> invalidStrs = new HashSet<String>();
						patterns = loadPatterns(patternFile, strings, invalidStrs);
						invalidStrings = invalidStrs;
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
			if (keywordFile != null) {
				long currentStamp = keywordFile.lastModified();
				if (currentStamp != keywordFileStamp) {
					keywordFileStamp = currentStamp;
					try {
						loadKeywords(keywordFile,new ArrayList<String>(0));
					} catch (Exception e) {
						e.printStackTrace();
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
