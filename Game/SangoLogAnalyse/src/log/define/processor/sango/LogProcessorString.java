package log.define.processor.sango;

import log.define.processor.LogProcessor;


public class LogProcessorString extends LogProcessor{

	public LogProcessorString(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
		return data;
	}

}
