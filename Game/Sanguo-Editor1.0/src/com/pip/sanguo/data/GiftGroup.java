package com.pip.sanguo.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.item.Item;
import com.pip.util.Utils;

public class GiftGroup extends DataObject {
    /**
     * 定义周期的类。周期的类型包括：小时，天，周，月。
     */
    public static class CycleDef {
        public static final int CYCLE_HOUR = 0;
        public static final int CYCLE_DAY = 1;
        public static final int CYCLE_WEEK = 2;
        public static final int CYCLE_MONTH = 3;
        public int type = CYCLE_DAY;
        public int amount = 1;
    }
    
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 开始日期（包含）。空表示不限制。只有年月日字段有效。
     */
    public Date beginDate;
    /**
     * 结束日期（包含）。空表示不限制。只有年月日字段有效。
     */
    public Date endDate;
    /**
     * 是否有效。
     */
    public boolean valid = true;
    /**
     * 最大领取次数。-1表示不限制。
     */
    public int maxTimes = -1;
    /**
     * 领取周期。
     */
    public CycleDef cycle = new CycleDef();
    /**
     * 一个周期内最大领取次数。-1表示不限制。
     */
    public int repeatTimes = 1;
    /**
     * 一个周期内两次领取之间最小间隔时间（秒）。-1表示不限制。
     */
    public int timeSpace = -1;
    /**
     * 一个周期内允许领取的开始日。-1表示不限制，0表示第一天。如果周期小于或等于1天，此设定无意义。
     */
    public int beginDay = -1;
    /**
     * 一个周期内允许领取的结束日（包含）。-1表示不限制，0表示第一天。如果周期小于或等于1天，此设定无意义。
     */
    public int endDay = -1;
    /**
     * 一个周期内每天允许领取的开始时间（分，从0点开始计算）。
     */
    public int beginTime = 0;
    /**
     * 一个周期内每天允许领取的结束时间（分，从0点开始计算，不包含）。
     */
    public int endTime = 1440;
    
    // 下面的消息中可使用以下变量：beginlevel最小级别，endlevel最大级别，needitem需求物品，
    // giveitem兑换物品，acount已领取次数，rcount本期已领取次数，max最大领取次数，
    // repeat本期最大领取次数，needtime领取时间范围，timespace最小领取间隔。
    
    /**
     * 错误消息，在领取时发现没有对应的级别配置时报告此消息。
     */
    public String errorMessage = "没有你这个级别的兑换配置。";
    /**
     * 奖品组的标题。
     */
    public String groupMessage = "${beginlevel}到${endlevel}之间可以领取奖品哦！";
    /**
     * 奖品标题。
     */
    public String giftMessage = "用${needitem}可以兑换${giveitem}！";
    /**
     * 超出总领取次数的消息。
     */
    public String maxExceedMessage = "你已经领过${acount}次了，只能领${max}次，抱歉啦。";
    /**
     * 一个周期内超出次数限制的消息。
     */
    public String repeatExceedMessage = "今天你已经领过${rcount}次了，一天我只能给你${repeat}次，明天再来吧。";
    /**
     * 领取时间间隔没到的消息。
     */
    public String timeSpaceMessage = "每${timespace}只能领取一次哦。";
    /**
     * 没到领取时间的提示消息。
     */
    public String timeErrorMessage = "现在还没到领取时间哪，领取时间是${needtime}，到时再来吧。";
    /**
     * 需求物品不足的提示消息。
     */
    public String needItemMessage = "你没有足够的物品，需要${needitem}，找齐了再来找我吧。";
    /**
     * 需求变量条件不满足的提示消息。
     */
    public String needVarMessage = "你没有达到兑换条件：${needvar}。";
    /**
     * 发放到背包成功的提示消息。
     */
    public String giveOKMessage = "${giveitem}已经放到你的包包里了，赶快去装备上试试吧！";
    /**
     * 背包已满的提示消息。
     */
    public String bagFullMessage = "你包包满了啊，清一下包再来找我吧。";
    
    /**
     * 兑换组定义（按用户级别划分）。
     */
    public ArrayList<GiftDef> gifts = new ArrayList<GiftDef>();
    
    /**
     * 某用户级别兑换定义。
     * @author lighthu
     */
    public static class GiftDef {
        /**
         * 奖励ID。
         */
        public int id = 0;
        /**
         * 最小级别（包含）。
         */
        public int beginLevel = 1;
        /**
         * 最大级别（包含）。
         */
        public int endLevel = 100;
        /**
         * 需求物品。只支持金钱、战功或物品。
         */
        public ArrayList<Shop.BuyRequirement> needItems = new ArrayList<Shop.BuyRequirement>();
        /**
         * 给予物品。只支持金钱、战功或物品。
         */
        public ArrayList<Shop.BuyRequirement> giveItems = new ArrayList<Shop.BuyRequirement>();
        
        public GiftDef dup() {
            GiftDef ret = new GiftDef();
            ret.id = id;
            ret.beginLevel = beginLevel;
            ret.endLevel = endLevel;
            ret.needItems.clear();
            for (Shop.BuyRequirement item : needItems) {
                ret.needItems.add(item.dup());
            }
            ret.giveItems.clear();
            for (Shop.BuyRequirement item : giveItems) {
                ret.giveItems.add(item.dup());
            }
            return ret;
        }
        
        public String toString() {
            return "[" + beginLevel + "-" + endLevel + "级]";
        }
        
        public void switchProject(ProjectData prj) {
            for (Shop.BuyRequirement br : needItems) {
                br.switchProject(prj);
            }
            for (Shop.BuyRequirement br : giveItems) {
                br.switchProject(prj);
            }
        }
    }
    
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public GiftGroup(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void update(DataObject obj) {
        GiftGroup oo = (GiftGroup)obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        beginDate = oo.beginDate;
        endDate = oo.endDate;
        valid = oo.valid;
        maxTimes = oo.maxTimes;
        cycle.type = oo.cycle.type;
        cycle.amount = oo.cycle.amount;
        repeatTimes = oo.repeatTimes;
        timeSpace = oo.timeSpace;
        beginDay = oo.beginDay;
        endDay = oo.endDay;
        beginTime = oo.beginTime;
        endTime = oo.endTime;
        errorMessage = oo.errorMessage;
        groupMessage = oo.groupMessage;
        giftMessage = oo.giftMessage;
        maxExceedMessage = oo.maxExceedMessage;
        repeatExceedMessage = oo.repeatExceedMessage;
        timeSpaceMessage = oo.timeSpaceMessage;
        timeErrorMessage = oo.timeErrorMessage;
        needItemMessage = oo.needItemMessage;
        needVarMessage = oo.needVarMessage;
        giveOKMessage = oo.giveOKMessage;
        bagFullMessage = oo.bagFullMessage;
        
        gifts.clear();
        for (GiftDef item : oo.gifts) {
            GiftDef newItem = item.dup();
            gifts.add(newItem);
            
            // 项目切换，所有对象引用需要重算
            if (owner != oo.owner) {
                newItem.switchProject(owner);
            }
        }
    }
    
    public DataObject duplicate() {
        GiftGroup ret = new GiftGroup(owner);
        ret.update(this);
        return ret;
    }
    
    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        try {
            beginDate = dateFormat.parse(elem.getAttributeValue("begindate"));
        } catch (Exception e) {
            beginDate = null;
        }
        try {
            endDate = dateFormat.parse(elem.getAttributeValue("enddate"));
        } catch (Exception e) {
            endDate = null;
        }
        valid = "true".equals(elem.getAttributeValue("valid"));
        maxTimes = Integer.parseInt(elem.getAttributeValue("max"));
        
        // 周期字符串，最后一个字符表示类型：h,d,w,m
        String cycleStr = elem.getAttributeValue("cycle");
        char suffix = cycleStr.charAt(cycleStr.length() - 1);
        switch (suffix) {
        case 'h':
            cycle.type = CycleDef.CYCLE_HOUR;
            break;
        case 'd':
            cycle.type = CycleDef.CYCLE_DAY;
            break;
        case 'w':
            cycle.type = CycleDef.CYCLE_WEEK;
            break;
        case 'm':
            cycle.type = CycleDef.CYCLE_MONTH;
            break;
        default:
            throw new IllegalArgumentException();
        }
        cycle.amount = Integer.parseInt(cycleStr.substring(0, cycleStr.length() - 1));

        repeatTimes = Integer.parseInt(elem.getAttributeValue("repeat"));
        timeSpace = Integer.parseInt(elem.getAttributeValue("timespace"));
        beginDay = Integer.parseInt(elem.getAttributeValue("beginday"));
        endDay = Integer.parseInt(elem.getAttributeValue("endday"));
        beginTime = Integer.parseInt(elem.getAttributeValue("begintime"));
        endTime = Integer.parseInt(elem.getAttributeValue("endtime"));
        
        errorMessage = elem.getChildText("message_error");
        groupMessage = elem.getChildText("message_group");
        giftMessage = elem.getChildText("message_gift");
        maxExceedMessage = elem.getChildText("message_count");
        repeatExceedMessage = elem.getChildText("message_repeat");
        timeSpaceMessage = elem.getChildText("message_timespace");
        timeErrorMessage = elem.getChildText("message_time");
        needItemMessage = elem.getChildText("message_item");
        needVarMessage = elem.getChildText("message_var");
        giveOKMessage = elem.getChildText("message_give");
        bagFullMessage = elem.getChildText("message_bag");
        
        gifts.clear();
        List list = elem.getChildren("gift");
        for (int i = 0; i < list.size(); i++) {
            gifts.add(loadGift((Element)list.get(i)));
        }
    }
    
    private GiftDef loadGift(Element elem) {
        GiftDef ret = new GiftDef();
        ret.id = Integer.parseInt(elem.getAttributeValue("id"));
        ret.beginLevel = Integer.parseInt(elem.getAttributeValue("beginlevel"));
        ret.endLevel = Integer.parseInt(elem.getAttributeValue("endlevel"));
        List list = elem.getChildren("needitem");
        for (int i = 0; i < list.size(); i++) {
            Shop.BuyRequirement item = loadItem((Element)list.get(i));
            if (item != null) {
                ret.needItems.add(item);
            }
        }
        list = elem.getChildren("giveitem");
        for (int i = 0; i < list.size(); i++) {
            Shop.BuyRequirement item = loadItem((Element)list.get(i));
            if (item != null) {
                ret.giveItems.add(item);
            }
        }
        return ret;
    }
    
    private Shop.BuyRequirement loadItem(Element elem) {
        BuyRequirement ret = new BuyRequirement();
        ret.type = Shop.nameToType(elem.getAttributeValue("type"));
        ret.amount = Integer.parseInt(elem.getAttributeValue("amount"));
        if (ret.type == Shop.TYPE_ITEM) {
            int itemID = Integer.parseInt(elem.getAttributeValue("itemid"));
            ret.item = owner.findItemOrEquipment(itemID);
            if (ret.item == null) {
                return null;
            }
        }
        return ret;
    }
    
    public Element save() {
        Element ret = new Element("giftgroup");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        if (beginDate == null) {
            ret.addAttribute("begindate", "");
        } else {
            ret.addAttribute("begindate", dateFormat.format(beginDate));
        }
        if (endDate == null) {
            ret.addAttribute("enddate", "");
        } else {
            ret.addAttribute("enddate", dateFormat.format(endDate));
        }
        ret.addAttribute("valid", valid ? "true" : "false");
        ret.addAttribute("max", String.valueOf(maxTimes));
        
        String cycleStr = String.valueOf(cycle.amount);
        switch (cycle.type) {
        case CycleDef.CYCLE_HOUR:
            cycleStr += "h";
            break;
        case CycleDef.CYCLE_DAY:
            cycleStr += "d";
            break;
        case CycleDef.CYCLE_WEEK:
            cycleStr += "w";
            break;
        case CycleDef.CYCLE_MONTH:
            cycleStr += "m";
            break;
        }
        ret.addAttribute("cycle", cycleStr);
        
        ret.addAttribute("repeat", String.valueOf(repeatTimes));
        ret.addAttribute("timespace", String.valueOf(timeSpace));
        ret.addAttribute("beginday", String.valueOf(beginDay));
        ret.addAttribute("endday", String.valueOf(endDay));
        ret.addAttribute("begintime", String.valueOf(beginTime));
        ret.addAttribute("endtime", String.valueOf(endTime));

        appendElem(ret, "message_error", errorMessage);
        appendElem(ret, "message_group", groupMessage);
        appendElem(ret, "message_gift", giftMessage);
        appendElem(ret, "message_count", maxExceedMessage);
        appendElem(ret, "message_repeat", repeatExceedMessage);
        appendElem(ret, "message_timespace", timeSpaceMessage);
        appendElem(ret, "message_time", timeErrorMessage);
        appendElem(ret, "message_item", needItemMessage);
        appendElem(ret, "message_var", needVarMessage);
        appendElem(ret, "message_give", giveOKMessage);
        appendElem(ret, "message_bag", bagFullMessage);
        
        for (GiftDef gift : gifts) {
            ret.addContent(saveGift(gift));
        }
        return ret;
    }
    
    private void appendElem(Element parent, String name, String text) {
        Element elem = new Element(name);
        elem.setText(text);
        parent.addContent(elem);
    }
    
    private Element saveGift(GiftDef gift) {
        Element ret = new Element("gift");
        ret.addAttribute("id", String.valueOf(gift.id));
        ret.addAttribute("beginlevel", String.valueOf(gift.beginLevel));
        ret.addAttribute("endlevel", String.valueOf(gift.endLevel));
        for (Shop.BuyRequirement br : gift.needItems) {
            ret.addContent(saveItem(br, "needitem"));
        }
        for (Shop.BuyRequirement br : gift.giveItems) {
            ret.addContent(saveItem(br, "giveitem"));
        }
        return ret;
    }
    
    private Element saveItem(BuyRequirement req, String name) {
        Element ret = new Element(name);
        ret.addAttribute("type", Shop.TYPE_NAMES[req.type]);
        ret.addAttribute("amount", String.valueOf(req.amount));
        if (req.type == Shop.TYPE_ITEM) {
            ret.addAttribute("itemid", String.valueOf(req.item.id));
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        if (obj instanceof Item) {
            for (GiftDef g : gifts) {
                for (Shop.BuyRequirement br : g.giveItems) {
                    if (br.item == obj) {
                        return true;
                    }
                }
                for (Shop.BuyRequirement br : g.needItems) {
                    if (br.item == obj) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
 
    /**
     * 查找指定的玩家级别对应的礼品定义。
     * @param level 玩家级别
     * @return 所有符合条件的礼品兑换项。
     */
    public GiftDef[] findGifts(int level) {
        List<GiftDef> list = new ArrayList<GiftDef>();
        for (GiftDef gd : gifts) {
            if (gd.beginLevel <= level && level <= gd.endLevel) {
                list.add(gd);
            }
        }
        GiftDef[] ret = new GiftDef[list.size()];
        list.toArray(ret);
        return ret;
    }
    
    /**
     * 根据ID查找礼品兑换项。
     * @param id 礼品项ID
     * @return 找到的礼品兑换项，如果没有找到返回null
     */
    public GiftDef findGift(int id) {
        for (GiftDef gd : gifts) {
            if (gd.id == id) {
                return gd;
            }
        }
        return null;
    }
    
    /**
     * 替换一个文本消息内的变量，返回替换后的文本。
     * @param pattern 文本消息模板（包含变量）
     * @param gift 玩家级别对应的礼品定义
     * @param acount 此玩家总共领取次数
     * @param rcount 此玩家本周期内领取次数
     * @return 替换后的文本。
     */
    public String translateText(String pattern, GiftDef gift, int acount, int rcount) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        while (true) {
            int cur = pattern.indexOf("${", start);
            if (cur == -1) {
                sb.append(pattern.substring(start));
                break;
            }
            int next = pattern.indexOf('}', cur);
            if (next == -1) {
                sb.append(pattern.substring(start));
                break;
            }
            sb.append(pattern.substring(start, cur));
            String token = pattern.substring(cur + 2, next);
            start = next + 1;
            sb.append(translateVar(token, gift, acount, rcount));
        }
        return sb.toString();
    }
    
    /*
     * 解析文本消息中出现的变量。可用的变量包括：beginlevel最小级别，endlevel最大级别，needitem需求物品，
     * giveitem兑换物品，acount已领取次数，rcount本期已领取次数，max最大领取次数，repeat本期最大领取次数，
     * needtime领取时间范围。
     */
    private String translateVar(String var, GiftDef gift, int acount, int rcount) {
        if ("beginlevel".equals(var)) {
            return String.valueOf(gift.beginLevel);
        } else if ("endlevel".equals(var)) {
            return String.valueOf(gift.endLevel);
        } else if ("needitem".equals(var)) {
            return itemsToText(gift.needItems);
        } else if ("needvar".equals(var)) {
            return varsToText(gift.needItems);
        } else if ("giveitem".equals(var)) {
            return itemsToText(gift.giveItems);
        } else if ("acount".equals(var)) {
            return String.valueOf(acount);
        } else if ("rcount".equals(var)) {
            return String.valueOf(rcount);
        } else if ("max".equals(var)) {
            return String.valueOf(maxTimes);
        } else if ("repeat".equals(var)) {
            return String.valueOf(repeatTimes);
        } else if ("needtime".equals(var)) {
            return timeToText();
        } else if ("timespace".equals(var)) {
            int h = timeSpace / 3600;
            int m = (timeSpace  % 3600) / 60;
            if (h == 0) {
                return m + "分钟";
            } else {
                return h + "小时" + m + "分钟";
            }
        }
        return "";
    }
    
    /*
     * 物品列表转换为字符串。
     */
    private String itemsToText(ArrayList<Shop.BuyRequirement> items) {
        StringBuilder sb = new StringBuilder();
        for (Shop.BuyRequirement br : items) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            switch (br.type) {
            case Shop.TYPE_MONEY:
                sb.append(br.amount + "钱");
                break;
            case Shop.TYPE_HONOR:
                sb.append(br.amount + "战功");
                break;
            case Shop.TYPE_ITEM:
                sb.append(br.item.title + " x" + br.amount);
                break;
            case Shop.TYPE_EXP:
                sb.append(br.amount + "经验");
                break;
            }
        }
        return sb.toString();
    }
    
    /*
     * 变量需求转换为字符串。
     */
    private String varsToText(ArrayList<Shop.BuyRequirement> items) {
        StringBuilder sb = new StringBuilder();
        for (Shop.BuyRequirement br : items) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            switch (br.type) {
            case Shop.TYPE_VARIABLE:
                sb.append(br.varDesc);
                break;
            }
        }
        return sb.toString();
    }
    
    /*
     * 时间限制转换为字符串。
     */
    private String timeToText() {
        if (cycle.type == CycleDef.CYCLE_HOUR) {
            // 如果周期是小时，则时间限制是每天的时间
            if (beginTime == -1) {
                if (endTime == -1) {
                    return "全天";
                } else {
                    return "每天" + minuteToText(endTime) + "前";
                }
            } else {
                if (endTime == -1) {
                    return "每天" + minuteToText(beginTime) + "后";
                } else {
                    return "每天" + minuteToText(beginTime) + "到" + minuteToText(endTime);
                }
            }
        }
        if (cycle.type == CycleDef.CYCLE_DAY) {
            // 周期是天
            String text = cycle.amount == 1 ? "每天" : "每" + cycle.amount + "天";
            if (cycle.amount > 1) {
                if (beginDay == -1) {
                    if (endDay != -1) {
                        text += "中的第" + (beginDay + 1) + "天(含)后";
                    }
                } else {
                    if (endDay == -1) {
                        text += "中的第" + (beginDay + 1) + "天(含)前";
                    } else {
                        text += "中的第" + (beginDay + 1) + "到" + (endDay + 1) + "天(含)";
                    }
                }
            }
            if (beginTime == -1) {
                if (endTime == -1) {
                    return text;
                } else {
                    return "的" + text + minuteToText(endTime) + "前";
                }
            } else {
                if (endTime == -1) {
                    return "的" + text + minuteToText(beginTime) + "后";
                } else {
                    return "的" + text + minuteToText(beginTime) + "到" + minuteToText(endTime);
                }
            }
        }
        String weekdays = "日一二三四五六";
        if (cycle.type == CycleDef.CYCLE_WEEK) {
            // 周期是周
            String text = cycle.amount == 1 ? "每周" : "每" + cycle.amount + "周";
            if (cycle.amount > 1) {
                if (beginDay == -1) {
                    if (endDay != -1) {
                        text += "中的第" + (beginDay + 1) + "天(含)后";
                    }
                } else {
                    if (endDay == -1) {
                        text += "中的第" + (beginDay + 1) + "天(含)前";
                    } else {
                        text += "中的第" + (beginDay + 1) + "到" + (endDay + 1) + "天(含)";
                    }
                }
            } else {
                if (beginDay == -1) {
                    if (endDay != -1) {
                        text += "日到周" + weekdays.charAt(endDay);
                    }
                } else {
                    if (endDay == -1) {
                        text += weekdays.charAt(beginDay) + "到周六";
                    } else {
                        text += weekdays.charAt(beginDay) + "到周" + weekdays.charAt(endDay);
                    }
                }
            }
            if (beginTime == -1) {
                if (endTime == -1) {
                    return text;
                } else {
                    return "的" + text + minuteToText(endTime) + "前";
                }
            } else {
                if (endTime == -1) {
                    return "的" + text + minuteToText(beginTime) + "后";
                } else {
                    return "的" + text + minuteToText(beginTime) + "到" + minuteToText(endTime);
                }
            }
        }
        if (cycle.type == CycleDef.CYCLE_MONTH) {
            // 周期是月
            String text = cycle.amount == 1 ? "每月" : "每" + cycle.amount + "月";
            if (cycle.amount > 1) {
                if (beginDay == -1) {
                    if (endDay != -1) {
                        text += "中的第" + (beginDay + 1) + "天(含)后";
                    }
                } else {
                    if (endDay == -1) {
                        text += "中的第" + (beginDay + 1) + "天(含)前";
                    } else {
                        text += "中的第" + (beginDay + 1) + "到" + (endDay + 1) + "天(含)";
                    }
                }
            } else {
                if (beginDay == -1) {
                    if (endDay != -1) {
                        text += "1日到" + (endDay + 1) + "日";
                    }
                } else {
                    if (endDay == -1) {
                        text += (beginDay + 1) + "日到月末";
                    } else {
                        text += (beginDay + 1) + "日到" + (endDay + 1) + "日";
                    }
                }
            }
            if (beginTime == -1) {
                if (endTime == -1) {
                    return text;
                } else {
                    return "的" + text + minuteToText(endTime) + "前";
                }
            } else {
                if (endTime == -1) {
                    return "的" + text + minuteToText(beginTime) + "后";
                } else {
                    return "的" + text + minuteToText(beginTime) + "到" + minuteToText(endTime);
                }
            }
        }
        return "";
    }
    
    private String minuteToText(int min) {
        int hour = min / 60;
        min = min % 60;
        if (min < 10) {
            return hour + ":0" + min;
        } else {
            return hour + ":" + min;
        }
    }
    
    public boolean isValid(Date time){
        if(!valid)
            return valid;
        if(beginDate==null&&endDate==null)
            return true;
        if(beginDate==null)
            return time.getTime()<endDate.getTime();
        if(endDate==null)
            return time.getTime()>beginDate.getTime();
        return time.getTime()>beginDate.getTime()&&time.getTime()<endDate.getTime();
    }
    
    /**
     * 检查周期设置是否支持天设置。
     * @return
     */
    public boolean supportDaySetting() {
        return supportDaySetting(cycle.type, cycle.amount);
    }
    
    
    
    public static boolean supportDaySetting(int type, int amount) {
        if (type == CycleDef.CYCLE_HOUR) {
            return false;
        }
        if (type == CycleDef.CYCLE_DAY) {
            return amount > 1;
        }
        return true;
    }
}
