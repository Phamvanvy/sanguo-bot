package com.pip.itimes.utils.award;

import com.pip.itimes.utils.IAward;

/**
 * 不能识别，保留byte流，因为可能是删除的物品，也可能是当前本地的资源文件没有同步
 * @author Jeffrey
 * @version 1.0
 */
public class UnknowAward implements IAward{

    private byte[] bytes;

    public UnknowAward(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes(){
        return bytes;
    }

    public String toString(){
        return "{UnKnow}";
    }
}
