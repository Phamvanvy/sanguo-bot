package log.define.processor.sango;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import log.define.Definings;
import log.define.LogDefine;
import log.define.LogType;
import log.define.processor.LogProcessor;

public class LogProcessorItemType extends LogProcessor {

	public LogProcessorItemType(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
		BufferedReader br = new BufferedReader(new StringReader(data));
		String line;
		List<String> str = new ArrayList<String>();
		try{
			while((line = br.readLine())!=null){
				String[] ss = line.split(",");
				for(int i=0;i<ss.length;i++){
					str.add(ss[i]);
				}
			}
			if(str.size()==3){
				String id = str.get(0);
				String instanceId = str.get(1);
				String count = str.get(2);
				return "物品ID："+id+"; 物品名称："+Definings.getItemName(id)+"; instanceId："+instanceId+"; 物品个数："+count+"个";
			} else if(str.size()==5){
				String id = str.get(0);
				String instanceId = str.get(1);
				String count = str.get(2);
				String cno = str.get(3).substring(4);
				String value = str.get(4).substring(4);
				return "物品ID："+id+"; 物品名称："+Definings.getItemName(id)+"; instanceId："+instanceId+"; 物品个数："+count+"个；"+"元宝卡号："+cno+"；i币数："+value;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	

	//
	// @Override
	// public String process(String data) {
	// String result = data;
	// LogDefine define = Definings.getLogDefine("sango");
	// LogType type = define.findLogType(result);
	// if(type != null){
	// result = type.process(result);
	// }
	// return result;
	// }

//	@Override
//	public String process(String data) {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(bos);
//
//		try {
//			Pattern pattern = Pattern.compile(" ");
//			String[] tmp = pattern.split(data);
//
//			for (String s : tmp) {
//				dos.writeUTF(s);
//			}
//			return print(bos.toByteArray());
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				dos.close();
//			} catch (Exception e) {
//			}
//		}
//
//		return data;
//	}
//
//	private String print(byte[] data) {
//		StringBuffer sb = new StringBuffer();
//
//		ByteArrayInputStream bis = new ByteArrayInputStream(data);
//		DataInputStream dis = new DataInputStream(bis);
//
//		try {
//			while (dis.available() > 0) {
//				String type = dis.readUTF();
//				int count = dis.readByte();
//
//				for (int i = 0; i < count; i++) {
//					if (i > 0) {
//						sb.append(' ');
//					}
//					if (type != null) {
//						System.out.println(type);
//					}
//
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return sb.toString();
//	}
}
