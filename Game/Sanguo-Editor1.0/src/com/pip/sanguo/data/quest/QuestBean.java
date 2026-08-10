package com.pip.sanguo.data.quest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pip.sanguo.editor.quest.QuestExportToExcel;

public class QuestBean {
    /**任务ID*/
    private int questID;
    /**任务名*/
    private String questTitle;
    /**任务级别*/
    private int questLevel;
    /**任务类型*/
    private String questType;
    /**起始NPC*/
    private String questStartNpc;
    /**领取任务提示*/
    private String questPreDescription;
    /**任务描述*/
    private String questDescription;
    /**完成提示*/
    private String questFinishDescription;
    /**未完提示*/
    private String questUnfinishDescription;
    /**任务目标*/
    private String questTargets;
    /**任务奖励*/
    private String questRewardsItem;
    /**前置任务*/
    private String questCondition;
    /**领取提示-正则表达式*/
    private final String REGEX_PREDESCRIPTION = "<c\\w+>|</[cl]>|</?i>|<n>\\d+,|\\(.+\\)</n>|<l>\\d+,";
    
    private final String REGEX_QUESTREWARDSITEM = "物品\\s*\\d+:";
    
    /**正则转换
     * str--原始字符串
     * regexStr--转换规则
     * replaceStr--替换文本
     * */
    public String regexString(String str, String regexStr, String replaceStr){
        if (true) {
            return str;
        }
        if(str == null || str == ""){
            return "";
        }
        String resultStr = str;
        Pattern pattern = null;
        pattern = Pattern.compile(regexStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);

        if(matcher.find()){
            resultStr = matcher.replaceAll(replaceStr);
//            if(regexStr == REGEX_TARGETS) System.out.println(resultStr);
        }
        return resultStr;
    }
    
    private String regexTargetsString(String str, QuestExportToExcel q){
        if(str == null || str == ""){
            return "";
        }
        String resultStr = str;
        Pattern pattern = null;
        pattern = Pattern.compile("hasitem\\((\\d+),\\s+(\\d+)\\)", Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            int id = Integer.parseInt(matcher.group(1));
            int num = Integer.parseInt(matcher.group(2));
            String itemTitle = q.itemTable.get(id);
            resultStr = matcher.replaceFirst("拥有" + num + "个" + "“" + itemTitle + "”");
//            System.out.println(resultStr);
            return regexTargetsString(resultStr, q);
        }
        else{
            return resultStr;
        }
    }
    
    private String regexTargetsString(String str){
        if(str == null || str == ""){
            return "";
        }
        String resultStr = str;
        Pattern pattern = null;
        pattern = Pattern.compile("\\s+==\\s+(1\\d+|[2-9]\\d*)");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            resultStr = matcher.replaceFirst(matcher.group(1) + "个");
//            System.out.println(resultStr);
            return regexTargetsString(resultStr);
        }
        else{
            return resultStr;
        }
    }
    
    private String regexQuestCondition(String str, QuestExportToExcel q){
        if(str == null || str == ""){
            return "";
        }
        String resultStr = str;
        Pattern pattern = null;
        pattern = Pattern.compile(".*TaskFinished\\((\\d+)\\).*");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            resultStr = matcher.replaceFirst(q.beforeQuest.get(Integer.parseInt(matcher.group(1))));
//            System.out.println(resultStr);
            return regexQuestCondition(resultStr, q);
        }
        else{
            return resultStr;
        }
    }
    
    public String getQuestRewardsItem() {
        return regexString(questRewardsItem, REGEX_QUESTREWARDSITEM, "");
//        return questRewardsItem;
    }
    public void setQuestRewardsItem(String questRewardsItem) {
        this.questRewardsItem = questRewardsItem;
    }
    public String getQuestCondition(QuestExportToExcel q) {
        return regexQuestCondition(questCondition, q);
//        return questCondition;
    }
    public void setQuestCondition(String questCondition) {
        this.questCondition = questCondition;
    }
    public String getQuestTargets(QuestExportToExcel q) {
        questTargets = regexTargetsString(questTargets, q);//替换hasitem(id,num)
        questTargets = regexString(questTargets, "1\\s+==\\s+1", "完成对话");//替换1 == 1
        questTargets = regexTargetsString(questTargets);//替换杀敌数量
        questTargets = regexString(questTargets, "\\s+==\\s+1", "");//替换 == 1
        questTargets = regexString(questTargets, "_LEVEL", "等级");
        return questTargets;
    }
    public int getQuestLevel() {
        return questLevel;
    }
    public void setQuestLevel(int questLevel) {
        this.questLevel = questLevel;
    }
    public void setQuestTargets(String questTargets) {
        this.questTargets = questTargets;
    }
    public String getQuestPreDescription() {
        return regexString(questPreDescription, REGEX_PREDESCRIPTION, "");
    }
    public String getQuestDescription() {
        return regexString(questDescription, REGEX_PREDESCRIPTION, "");
    }
    public String getQuestFinishDescription() {
        return regexString(questFinishDescription, REGEX_PREDESCRIPTION, "");
    }
    public String getQuestUnfinishDescription() {
        return regexString(questUnfinishDescription, REGEX_PREDESCRIPTION, "");
    }
    public void setQuestPreDescription(String questPreDescription) {
        this.questPreDescription = questPreDescription;
    }
    public void setQuestDescription(String questDescription) {
        this.questDescription = questDescription;
    }
    public void setQuestFinishDescription(String questFinishDescription) {
        this.questFinishDescription = questFinishDescription;
    }
    public void setQuestUnfinishDescription(String questUnfinishDescription) {
        this.questUnfinishDescription = questUnfinishDescription;
    }
    public String getQuestStartNpc() {
        return questStartNpc;
    }
    public void setQuestStartNpc(String questStartNpc) {
        this.questStartNpc = questStartNpc;
    }
    public String getQuestType() {
        return questType;
    }
    public void setQuestType(String questType) {
        this.questType = questType;
    }
    public int getQuestID() {
        return questID;
    }
    public void setQuestID(int questID) {
        this.questID = questID;
    }
    public String getQuestTitle() {
        return questTitle;
    }
    public void setQuestTitle(String questTitle) {
        this.questTitle = questTitle;
    }

}
