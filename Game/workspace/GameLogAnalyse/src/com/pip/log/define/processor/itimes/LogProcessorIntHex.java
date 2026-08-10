package com.pip.log.define.processor.itimes;

import com.pip.log.define.processor.LogProcessor;

public class LogProcessorIntHex extends LogProcessor{
    public LogProcessorIntHex(String id){
        super(id);
    }

    @Override
    public String process(String data){
        return "0x" + Integer.toHexString(Integer.parseInt(data));
    }
}
