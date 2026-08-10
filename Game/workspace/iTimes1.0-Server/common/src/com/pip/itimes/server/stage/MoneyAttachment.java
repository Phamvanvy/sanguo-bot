package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class MoneyAttachment implements Attachment{

    private int count;

    public MoneyAttachment(int count) {
        this.count = count;
    }

    public int getCount(){
        return count;
    }

    public long getSendTime(){
        return 0;
    }

    public byte[] toDbBytes(){
        return ItemUtils.money2dbAttachment(count);
    }
}
