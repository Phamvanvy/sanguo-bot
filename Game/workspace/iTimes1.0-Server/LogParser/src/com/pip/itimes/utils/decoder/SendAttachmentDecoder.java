package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.Utils;
import com.pip.itimes.utils.award.EmptyAward;
import com.pip.itimes.utils.IAward;
import com.pip.itimes.utils.action.SendAttachmentAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class SendAttachmentDecoder
    extends RegexLineDecoder {

    public SendAttachmentDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[24\\],Dest\\[(\\d+)\\]Attachment\\[(.+)\\]Price\\[(\\d+)\\]Money\\[\\d+\\]");
        //ID[27412],TYPE[24],Dest[46311]Attachment[00 00 00 00 03 00 50 ]Price[0]Money[510]

    }

    protected SendAttachmentAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int dest = Integer.parseInt(matcher.group(2));
        String sAtt = matcher.group(3);
        IAward award = Utils.getAwardFromAttachmentString(sAtt);
        int price = Integer.parseInt(matcher.group(4));
        return new SendAttachmentAction(source,dest,price,date,award);
    }


        //        String src = matcher.group(1);
//        String dest = matcher.group(2);
//        String s = matcher.group(3);
//        if("null".equals(s))
//            return "";
//        byte[] attachment = Utils.getBytes(matcher.group(3));
//        String attString = null;
//        if(attachment[0]==8){ //钱
//            attString = "["+Utils.getNumber(attachment,1,4)+"J]";
//        }
//        else{
//            int itemId = (int)Utils.getNumber(attachment,1,4);
//            IItemTemplate item = Items.getTemplate(itemId);
//            attString = "["+(item!=null?item.getName():"未知物品")+","+itemId+"]";
//        }
//        return "玩家["+src+"]向玩家["+dest+"]邮寄了"+attString;
//    }
}
