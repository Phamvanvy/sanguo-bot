package peony.game.maintain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionCheck {
	
	private static final String FILE_NAME = "e:/logauction8";
	
	public static void main(String[] args) throws Exception{
		BufferedReader r = new BufferedReader(new FileReader(FILE_NAME));
		int linecount = 0;
		String s = null;
		List<String> l = new ArrayList<String>();
		while((s=r.readLine()) != null){
			if(isCreate(s))
				l.add(s);
			if(isCreated(s) || isFail(s)){
				for(int i=l.size()-1;i>=0;i--){
					String s1 = l.get(i);
					if(isOk(s,s1)){
						l.remove(i);
						break;
					}
				}
			}
				
		}
		for(String v:l){
			System.out.println(v);
		}
		System.out.println(l.size());
		Map<String,Map<Integer,Integer>> map = new HashMap<String,Map<Integer,Integer>>();
		for(String v:l){
			String id = getId(v);
			Map<Integer,Integer> m1 = map.get(id);
			if(m1==null){
				m1 = new HashMap<Integer,Integer>();
				map.put(id, m1);
			}
			int[] is = getItem(v);
			Integer i = m1.get(is[0]);
			if(i != null){
				i = i + is[1];
			}else{
				i = is[1];
			}
			m1.put(is[0], i);
		}
		for(String v:map.keySet()){
			System.out.println("ID["+v+"]");
			Map<Integer,Integer> m = map.get(v);
			for(int v1:m.keySet()){
				int count = m.get(v1);
				System.out.println("        ITEM["+v1+","+count+"]");
			}
		}
	}
	
	public static int[] getItem(String s){
		int index = s.indexOf("ITEM[");
		int end = s.indexOf("]", index);
		String sub = s.substring(index+5,end);
		String[] ss = sub.split(",");
		int[] ret = new int[2];
		ret[0] = Integer.parseInt(ss[0]);
		ret[1] = Integer.parseInt(ss[2]);
		return ret;
	}
	
	public static boolean isCreate(String s){
		return s.indexOf("AUCTIONCREATE") != -1 && s.indexOf("AUCTIONCREATED") == -1 && s.indexOf("AUCTIONCREATEFAILED") == -1;
	}
	
	public static boolean isCreated(String s){
		return s.indexOf("AUCTIONCREATED") != -1;
	}
	
	public static boolean isFail(String s){
		return s.indexOf("AUCTIONCREATEFAILED") != -1;
	}
	
	public static boolean isOk(String s1,String s2){
		String id1 = getId(s1);
		String id2 = getId(s2);
		return id1.equals(id2);
	}
	
	public static String getId(String s1){
		int index = s1.indexOf("ID[");
		int end = s1.indexOf("]", index);
		return s1.substring(index+3, end);
	}
}
