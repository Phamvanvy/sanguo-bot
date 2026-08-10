package peony.patchs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CreateUpdate {
	public static final void main(String[] args) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(new File("d:/money.txt")));
		String line = null;
		while((line=reader.readLine())!=null){
			StringBuilder sb = new StringBuilder(200);
//			update player set money = money - 10000 where id = 44502;
			sb.append("update player set money = money - ");
			String[] s = line.split("\t");
			sb.append(s[1]);
			sb.append(" where id = ");
			sb.append(s[0]);
			sb.append(';');
			System.out.println(sb.toString());
		}
	}
}
