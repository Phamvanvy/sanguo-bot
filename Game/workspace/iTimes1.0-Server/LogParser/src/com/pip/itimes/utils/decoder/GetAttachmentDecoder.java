package com.pip.itimes.utils.decoder;

import java.util.*;
import java.util.regex.*;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.*;
import com.pip.itimes.utils.award.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class GetAttachmentDecoder
    extends RegexLineDecoder {
    public GetAttachmentDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[27\\],Attachment\\[(.+)\\]Money\\[(\\d+)\\]");
        //ID[50281],TYPE[27],Attachment[03 00 0F 43 55 00 5B 8B 1B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ]Money[23927]
    }


    public GetAttachmentAction createAction(Matcher matcher,Date date){
        int id = Integer.parseInt(matcher.group(1));
        String s = matcher.group(2);
        IAward award = Utils.getAwardFromAttachmentString(s);
        return new GetAttachmentAction(id,date,award);
    }



//    public boolean matchs(Matcher matcher) {
//        String src = matcher.group(1);
//        String s = matcher.group(2);
//        if("null".equals(s))
//            return "";
//        byte[] attachment = HexUtils.getBytes(s);
//        String attString = null;
//        if(attachment[0]==8){ //钱
//            attString = "["+Utils.getNumber(attachment,1,4)+"J]";
//        }
//        else{
//            int itemId = (int)Utils.getNumber(attachment,1,4);
//            IItemTemplate item = Items.getTemplate(itemId);
//
//            attString = "["+(item!=null?item.getName():"未知物品")+","+itemId+"]";
//        }
//        return "玩家["+src+"]收取了附件["+attString+"]";
//        return false;
//    }
}
