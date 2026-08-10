package com.pip.sanguo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.data.clientEvent.EventTrigger;

/**
 * 活动指引
 * @author dchen
 */
public class DirectoryType extends ClientEvent {

    /** 所属项目。*/
    public ProjectData owner;
    
    /** 目标位置 */
    public int[] location;
    
    /** 周 */
    public int[] weeks;
    
    /** 时间段 小时 */
    public int[] timeScheduleHour;
    
    /** 时间段 分钟 */
    public int[] timeScheduleMin;
    
    /** 活动描述 */
    public String description1 = "";
    
    /** 阵营 */
    public int faction;
    
    /** 阵营 */
    public int clazz;
    
    /** 级别对应的评分 */
    public Map<Integer, Integer> scores = new HashMap<Integer, Integer>();
    
    /** 条件集合 */
    public List<EventTrigger> triggers = new ArrayList<EventTrigger>();
    
    /** 经验 */
    public int exp;
    
    /** 金钱 */
    public int money;
    
    /** 战功 */
    public int rank;
    
    /** 珍珠 */
    public int pearl;
    
    /** 奖励物品 */
    public int item1, count1;
    
    /** 奖励物品 */
    public int item2, count2;
    
    /** 奖励物品 */
    public int item3, count3;
    
    /** 奖励物品 */
    public int item4, count4;
    
    /** 奖励描述 */
    public String rewardDesc = "";
    
    /** 级别下限 */
    public int minLevel;
    
    /** 级别上限 */
    public int maxLevel;
    
    /** 活动明细 */
    public String directoyList = "";
    
    /** 难度 */
    public int difficulty;
    
    /** 活动工资 */
    public int salary;
    
    public DirectoryType(ProjectData owner) {
        super(owner);
        this.owner = owner;
    }
    
    public boolean changed(DataObject obj) {
        return false;
    }

    public boolean depends(DataObject obj) {
        return false;
    }

    public DataObject duplicate() {
        DirectoryType d = new DirectoryType(owner);
        d.update(this);
        return d;
    }

    @SuppressWarnings("unchecked")
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        categoryName = elem.getAttributeValue("category");
        description1 = elem.getAttributeValue("description");
        description = elem.getAttributeValue("description");
        directoyList = elem.getAttributeValue("directorylist");
        faction = Integer.parseInt(elem.getAttributeValue("faction"));
        difficulty = Integer.parseInt(elem.getAttributeValue("difficulty"));
        try {
            minLevel = Integer.parseInt(elem.getAttributeValue("minlevel"));
        }catch (NumberFormatException e2) {
        }
        try {
            maxLevel = Integer.parseInt(elem.getAttributeValue("maxlevel"));
        }catch (NumberFormatException e2) {
        }
        try {
            salary = Integer.parseInt(elem.getAttributeValue("salary"));
        }catch (NumberFormatException e) {
        }
        List eventElems = elem.getChildren("eventtrigger");
        for (int i = 0; i < eventElems.size(); i++) {
            EventTrigger trigger = new EventTrigger(this);
            trigger.load((Element) eventElems.get(i));
            triggers.add(trigger);
        }
        Element el = elem.getChild("info");
        try {
            location = parseLocation(el.getAttributeValue("location"));
        }catch (Exception e1) {
        }
        Element el1 = elem.getChild("level");
        String scoresEl = el1.getAttributeValue("value");
        String[] strs = scoresEl.split(",");
        for(int i=0;i<strs.length;i+=2){
            try {
                scores.put(Integer.parseInt(strs[i]), Integer.parseInt(strs[i+1]));
            }catch (NumberFormatException e) {
            }
        }
        Element el2 = elem.getChild("time");
        try {
            weeks = parseWeeks(el2.getAttributeValue("week"));
        }catch (Exception e) {
        }
        try {
            timeScheduleHour = parseTime(el2.getAttributeValue("hour"), true);
            timeScheduleMin = parseTime(el2.getAttributeValue("hour"), false);
        }catch (Exception e) {
        }
        Element el3 = elem.getChild("bubble");
        try {
            exp = Integer.parseInt(el3.getAttributeValue("exp"));
        }catch (NumberFormatException e) {
        }
        try {
            money = Integer.parseInt(el3.getAttributeValue("money"));
        }catch (NumberFormatException e) {
        }
        try {
            rank = Integer.parseInt(el3.getAttributeValue("rank"));
        }catch (NumberFormatException e) {
        }
        try {
            pearl = Integer.parseInt(el3.getAttributeValue("pearl"));
        }catch (NumberFormatException e) {
        }
        Element el4 = elem.getChild("reward");
        try {
            item1 = Integer.parseInt(el4.getAttributeValue("item1"));
        }catch (NumberFormatException e) {
        }
        try {
            count1 = Integer.parseInt(el4.getAttributeValue("count1"));
        }catch (NumberFormatException e) {
        }
        try {
            item2 = Integer.parseInt(el4.getAttributeValue("item2"));
        }catch (NumberFormatException e) {
        }
        try {
            count2 = Integer.parseInt(el4.getAttributeValue("count2"));
        }catch (NumberFormatException e) {
        }
        try {
            item3 = Integer.parseInt(el4.getAttributeValue("item3"));
        }catch (NumberFormatException e) {
        }
        try {
            count3 = Integer.parseInt(el4.getAttributeValue("count3"));
        }catch (NumberFormatException e) {
        }
        try {
            item4 = Integer.parseInt(el4.getAttributeValue("item4"));
        }catch (NumberFormatException e) {
        }
        try {
            count4 = Integer.parseInt(el4.getAttributeValue("count4"));
        }catch (NumberFormatException e) {
        }
        try {
            rewardDesc = el4.getAttributeValue("desc");
        }catch (NumberFormatException e) {
        }
        try {
            clazz = Integer.parseInt(el4.getAttributeValue("clazz"));
        }catch (NumberFormatException e) {
        }
        
    }

    public Element save() {
        Element ret = new Element("directory");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description1);
        ret.addAttribute("directorylist", directoyList);
        ret.addAttribute("faction", String.valueOf(faction));
        ret.addAttribute("minlevel", String.valueOf(minLevel));
        ret.addAttribute("maxlevel", String.valueOf(maxLevel));
        ret.addAttribute("difficulty", String.valueOf(difficulty));
        ret.addAttribute("salary",String.valueOf(salary));
        if(categoryName != null){
            ret.addAttribute("category", categoryName);
        }else{
            ret.addAttribute("category", "");
        }
        for(EventTrigger trigger : triggers){
            ret.addContent(trigger.save());
        }
        Element infoEl = new Element("info");
        infoEl.addAttribute("location", parseLocation(location));
        Element scoreEl = new Element("level");
        scoreEl.addAttribute("value",parseLevelScore(scores));
        Element timeEl = new Element("time");
        timeEl.addAttribute("week", parseWeeks(weeks));
        timeEl.addAttribute("hour", parseTime(timeScheduleHour, timeScheduleMin));
        Element bubbleEl = new Element("bubble");
        bubbleEl.addAttribute("exp", String.valueOf(exp));
        bubbleEl.addAttribute("money", String.valueOf(money));
        bubbleEl.addAttribute("rank", String.valueOf(rank));
        bubbleEl.addAttribute("pearl", String.valueOf(pearl));
        Element rewardEl = new Element("reward");
        rewardEl.addAttribute("item1", String.valueOf(item1));
        rewardEl.addAttribute("count1", String.valueOf(count1));
        rewardEl.addAttribute("item2", String.valueOf(item2));
        rewardEl.addAttribute("count2", String.valueOf(count2));
        rewardEl.addAttribute("item3", String.valueOf(item3));
        rewardEl.addAttribute("count3", String.valueOf(count3));
        rewardEl.addAttribute("item4", String.valueOf(item4));
        rewardEl.addAttribute("count4", String.valueOf(count4));
        rewardEl.addAttribute("desc", String.valueOf(rewardDesc));
        rewardEl.addAttribute("clazz", String.valueOf(clazz));
        ret.addContent(infoEl);
        ret.addContent(scoreEl);
        ret.addContent(timeEl);
        ret.addContent(bubbleEl);
        ret.addContent(rewardEl);
        return ret;
    }

    public void update(DataObject obj) {
        DirectoryType oo = (DirectoryType)obj;
        id = oo.id;
        title = oo.title;
        description1 = oo.description1;
        description = oo.description;
        directoyList = oo.directoyList;
        categoryName = oo.categoryName;
        difficulty = oo.difficulty;
        location = oo.location;
        weeks = oo.weeks;
        timeScheduleHour = oo.timeScheduleHour;
        timeScheduleMin = oo.timeScheduleMin;
        faction = oo.faction;
        clazz = oo.clazz;
        scores = oo.scores;
        exp = oo.exp;
        money = oo.money;
        rank = oo.rank;
        pearl = oo.pearl;
        item1 = oo.item1;
        count1 = oo.count1;
        item2 = oo.item2;
        count2 = oo.count2;
        item3 = oo.item3;
        count3 = oo.count3;
        item4 = oo.item4;
        count4 = oo.count4;
        rewardDesc = oo.rewardDesc;
        minLevel = oo.minLevel;
        maxLevel = oo.maxLevel;
        salary = oo.salary;
        triggers.clear();
        for (EventTrigger trigger : oo.triggers) {
            EventTrigger newReward = trigger.duplicate();
            newReward.owner = this;
            triggers.add(newReward);
        }
    }
    
    public static String parseLocation(int[] location){
        try {
            return location[0]+","+location[1]+","+location[2];
        }
        catch (Exception e) {
            return "";
        }
    }
    
    public static int[] parseLocation(String location){
        String[] strs = location.split(",");
        int[] loca = new int[strs.length];
        for(int i=0;i<strs.length;i++){
            loca[i] = Integer.parseInt(strs[i]);
        }
        return loca;
    }
    
    public static String parseLevelScore(Map<Integer, Integer> map){
        try {
            StringBuffer sb = new StringBuffer();
            for(int key : map.keySet()){
                sb.append(key);
                sb.append(",");
                sb.append(map.get(key));
                sb.append(",");
            }
            return sb.substring(0, sb.length()-1);
        }
        catch (Exception e) {
            return "";
        }
    }
    
    public static int[] parseWeeks(String week){
        String[] strs = week.split(",");
        int[] arr = new int[strs.length];
        for(int i=0;i<arr.length;i++){
            arr[i] = Integer.parseInt(strs[i]);
        }
        return arr;
    }
    
    public static String parseWeeks(int[] week){
        try {
            StringBuffer sb = new StringBuffer();
            for(int w : week){
                sb.append(w);
                sb.append(",");
            }
            return sb.substring(0, sb.length()-1);
        }
        catch (Exception e) {
            return "";
        }
    }
    
    public static int[] parseTime(String time, boolean flag){
        String[] strs = time.split(",");
        int[] arr = new int[strs.length * 2];
        if(flag){
            for(int i=0;i<strs.length;i++){
                String[] s = strs[i].split("-");
                String[] str = s[0].split(":");
                String[] str2 = s[1].split(":");
                arr[i*2] = Integer.parseInt(str[0]);
                arr[i*2+1] = Integer.parseInt(str2[0]);
            }
        }else{
            for(int i=0;i<strs.length;i++){
                String[] s = strs[i].split("-");
                String[] str = s[0].split(":");
                String[] str2 = s[1].split(":");
                arr[i*2] = Integer.parseInt(str[1]);
                arr[i*2+1] = Integer.parseInt(str2[1]);
            }
        }
        return arr;
    }
    
    public static String parseTime(int[] hour, int[] min){
        try {
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<hour.length;i++){
                if(i%2==1)
                    sb.append("-");
                if(hour[i]<10)
                    sb.append("0" + hour[i]);
                else
                    sb.append(hour[i]);
                sb.append(":");
                if(min[i]<10)
                    sb.append("0" + min[i]);
                else
                    sb.append(min[i]);
                if(i%2==1)
                    sb.append(",");
            }
            return sb.substring(0, sb.length()-1);
        }
        catch (Exception e) {
            return "";
        }
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

}
