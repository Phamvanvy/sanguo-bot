package com.pip.log.define.processor.itimes;

import com.pip.log.define.processor.LogProcessor;

public class LogProcessorInt extends LogProcessor{
    public LogProcessorInt(String id){
        super(id);
    }

    @Override
    public String process(String data){
        return Integer.toString(Integer.parseInt(data));
    }
}
