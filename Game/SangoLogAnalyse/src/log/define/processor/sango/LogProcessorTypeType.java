package log.define.processor.sango;

import log.define.processor.LogProcessor;

public class LogProcessorTypeType extends LogProcessor{

	public LogProcessorTypeType(String id) {
		super(id);
		
	}

	@Override
	public String process(String data) {
		String result = "";
		if(data.equals("SHOP")){
			result = "商店";
		} else if(data.equals("0")){
			result = "防守方";
		} else if(data.equals("1")){
			result = "进攻方";
		} else {
			result = data;
		}
		return result;
	}

}
