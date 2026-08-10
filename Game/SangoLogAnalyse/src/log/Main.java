package log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import log.define.Definings;
import log.define.Log;
import log.define.LogDefine;

public class Main {
	public static void losdFile() throws IOException {
		try {
			Definings.loadDefine();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		if(SangoLogApplication.orderFile.exists()){
//			SangoLogApplication.orderFile.delete();
//			SangoLogApplication.orderFile.createNewFile();
//		} else {
//			SangoLogApplication.orderFile.createNewFile();
//		}
		BufferedReader br = null;
		try {
			LogDefine define = Definings.getLogDefine("sango");
			br = new BufferedReader(new FileReader("cici.txt"));
			String line = br.readLine();
			while (line != null) {
				try {
					Log log = new Log(define, line);
					log.process();
					System.out.println(log.toString());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					line = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		losdFile();
	}
}
