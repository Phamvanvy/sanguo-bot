package log.define.processor.sango;

import log.define.processor.LogProcessor;

public class LogProcessorMethodType extends LogProcessor{

	public LogProcessorMethodType(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
		int num = Integer.parseInt(data);
		String result = "";
		if(num == 0)
			result = "∆’Õ®œ‚«∂";
		else if(num == 1)
			result = "µÕº∂œ‚«∂∑˚";
		else if(num == 2)
			result = "∏ﬂº∂œ‚«∂∑˚";
		else 
			result = data;
		return result;
	}

}
