package peony.log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.emory.mathcs.backport.java.util.Collections;

public class ReachErrorLog {
	
	//2009-11-30|00:00:13[REACHERROR]ID[32539]ACCOUNT[5346455]]DEST[480,0,0]
	
	static Pattern pattern = Pattern.compile(".*(\\d+:\\d+:\\d+)\\[REACHERROR\\]ID\\[(\\d+)\\]ACCOUNT.+DEST\\[(.+)\\]");
	static Map<String,List<Position>> positions = new HashMap<String,List<Position>>();
	
	
	public static void main(String[] args) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader("d:\\logreach"));
		String s = null;
		while((s = reader.readLine())!=null){
			Matcher matcher = pattern.matcher(s);
			if(matcher.matches()){
				String time = matcher.group(1);
				String id = matcher.group(2);
				String map = matcher.group(3);
				Position p = new Position(time,id,map);
				List<Position> l = positions.get(id);
				if(l==null){
					l = new ArrayList<Position>(20);
					positions.put(p.id, l);
				}
				l.add(p);
			}
		}
		System.out.println("Total Count:"+positions.size());
//		Iterator<Map.Entry<String,List<Position>>> ite = positions.entrySet().iterator();
//		while(ite.hasNext()){
//			Map.Entry<String, List<Position>> entry = ite.next();
//			System.out.printf("ID[%s]COUNT[%d]\n", entry.getKey(),entry.getValue().size());
//		}
		List<String> ids = new ArrayList<String>(positions.keySet());
		Collections.sort(ids, new PositionComparator());
		for(String key:ids){
			System.out.printf("ID[%s]COUNT[%d]\n", key,positions.get(key).size());
		}
	}

}

class PositionComparator implements Comparator<String>{

	public int compare(String o1, String o2) {
		int size1 = ReachErrorLog.positions.get(o1).size();
		int size2 = ReachErrorLog.positions.get(o2).size();
		return size2 - size1;
	}
	
}

class Position{
	String time,id,map;
	Position(String time,String id,String map){
		this.time = time;
		this.map = map;
		this.id = id;
	}
}
