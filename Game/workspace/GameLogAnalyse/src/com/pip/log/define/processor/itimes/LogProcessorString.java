package com.pip.log.define.processor.itimes;

import com.pip.log.define.processor.LogProcessor;

public class LogProcessorString extends LogProcessor{
    public LogProcessorString(String id){
        super(id);
    }

    @Override
    public String process(String data){
        return data;
    }
}
