package peony.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeMap;

public class StarLog {
	
	
	public static final void main(String[] args) throws Exception{
		File file = new File("d:\\logstar");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String s = null;
		TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer>();
		int count = 0;
		while((s=reader.readLine())!=null){
			int index = s.indexOf("STAR[");
			if(index != -1){
				String cv = s.substring(index+5,index+6);
				int v = Integer.parseInt(cv);
				Integer i = map.get(v);
				if (i == null) {
					i = 0;
				}
				i++;
				map.put(v, i);
				count++;
			}
		}
		int total = 0;
		for (int star : map.keySet()) {
			total += map.get(star);
			System.out.println("star:" + star + "  count:" + map.get(star)
					+ " percent:" + map.get(star) * 100f / count);
		}
		
		System.out.println("total:"+total);
	}
}
