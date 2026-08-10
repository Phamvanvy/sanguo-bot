package log.define.processor.sango;

import log.define.Definings;
import log.define.processor.LogProcessor;

public class LogProcessorQuestType extends LogProcessor{

	public LogProcessorQuestType(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String process(String data) {
		String questName = Definings.getQuestName(data);
		if(questName!=""){
			return "任务Id："+data+"；任务名称："+questName;
		}
		return data;
	}

}
