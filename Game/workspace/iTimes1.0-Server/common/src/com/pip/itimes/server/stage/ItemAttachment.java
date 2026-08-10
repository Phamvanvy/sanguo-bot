package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ItemAttachment implements Attachment{

    private IItem item;
    private int count;

    private static final long EQU_TIME = 10*60*1000L;
    private static final long EXTENDED_TIME = 15*60*1000L;
    private static final long MATERIAL_TIME = 5*60*1000L;

    public ItemAttachment(IItem item,int count) {
        this.item = item;
        this.count = count;
    }

    public IItem getItem(){
        return item;
    }

    public int count(){
        return count;
    }

    public long getSendTime(){
        byte type = item.getType();
        if(type==IItem.TYPE_BASIC){
            int itemId = item.getItemId();
            if(itemId>=0&&itemId<=14){  //ºì£¬À¶
                return EXTENDED_TIME;
            }
            else if(itemId>=15&&itemId<=97){
                return MATERIAL_TIME;
            }
        }
        else if(type==IItem.TYPE_EXTENDED){
            return EXTENDED_TIME;
        }
        else if(type==IItem.TYPE_EQU){
            return EQU_TIME;
        }
        return 0;
    }

    public byte[] toDbBytes(){
        return ItemUtils.item2dbAttachment(item,count);
    }
}
