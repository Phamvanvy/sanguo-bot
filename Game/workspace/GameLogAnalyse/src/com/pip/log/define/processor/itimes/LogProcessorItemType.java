package com.pip.log.define.processor.itimes;

import com.pip.log.define.processor.LogProcessor;

public class LogProcessorItemType extends LogProcessor{
    private static final byte TYPE_BASIC = 0;
    private static final byte TYPE_TASK = 1;
    private static final byte TYPE_EXTENDED = 2;
    private static final byte TYPE_EQU = 3;
    private static final byte TYPE_PET = 4;

    public LogProcessorItemType(String id){
        super(id);
    }

    @Override
    public String process(String data){
        int type = Integer.parseInt(data);
        String result = data;

        switch(type){
            case TYPE_BASIC:
                result = "基本物品";
                break;
            case TYPE_TASK:
                result = "任务物品";
                break;
            case TYPE_EXTENDED:
                result = "扩展物品";
                break;
            case TYPE_EQU:
                result = "装备";
                break;
            case TYPE_PET:
                result = "宠物";
                break;
        }

        return result;
    }
}
