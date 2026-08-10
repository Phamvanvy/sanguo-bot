package log.define.processor.sango;

import log.define.processor.LogProcessor;

public class LogProcessorReType extends LogProcessor{

	public LogProcessorReType(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
		// 0 成功 1 包格不够 2 没有指定分支 3 没有指定任务 4 不能完成任务
			String message = "";
			if (data.equals("0")) {
				message = "任务成功";
			}else if(data.equals("1")){
			    message = "包格不够";
			} else if (data.equals("2")) {
				message = "没有指定分支";
			} else if (data.equals("3")) {
				message = "没有指定任务";
			} else if (data.equals("4")) {
				message = "不能完成任务";
			} else if (data.equals("OK")) {
				message = "成功";
			} else if (data.equals("FAIL")) {
				message = "失败";
			} else if(data.equals("NOKEY")){
				message = "鉴定符不够";
			} else if(data.equals("NOMONEY")){
				message = "金钱不够";
			} else 
				message = data;
			
		return message;
	}

}
