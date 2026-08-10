package peony.game.maintain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ClickExpBug {
	
	private static final String FILE_NAME = peony.Messages.STRING_00013;
	
	static Map<Integer,Integer> count = new HashMap<Integer,Integer>();
	
	public static void main(String[] args) throws Exception{
		BufferedReader r = new BufferedReader(new FileReader(FILE_NAME));
		String line = null;
		while((line=r.readLine())!=null){
			int beginIndex = line.indexOf("ID[");
			int endIndex = line.indexOf(']', beginIndex);
			int id = Integer.parseInt(line.substring(beginIndex+3,endIndex));
			add(id);
		}
		for(int id:count.keySet()){
			int c = count.get(id);
			if(c>3){
				System.out.println(String.format("ID[%d]COUNT[%d]", id,c));
			}
		}
	}
	
	static void add(int id){
		int value = 0;
		if(count.containsKey(id)){
			value = count.get(id);
		}
		count.put(id, ++value);
	}
}
