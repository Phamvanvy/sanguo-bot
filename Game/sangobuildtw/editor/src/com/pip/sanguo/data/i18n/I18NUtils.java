package com.pip.sanguo.data.i18n;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.gtl.etf.ETFFile;
import com.pip.gtl.etf.ETFUtil;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.JavaTokenizer;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.TeleportSet;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.TeleportSet.Teleport;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.SuiteConfig;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.ItemEffect;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.data.quest.QuestTrigger;
import com.pip.sanguo.data.quest.QuestVariable;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.ExpressionList;
import com.pip.sanguo.data.quest.pqe.FunctionCall;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.ai.AIRuleConfig;
import com.pip.sanguo.editor.ai.SkillAttackRuleConfig;
import com.pip.sanguo.editor.ai.SummonRuleConfig;
import com.pip.sanguo.editor.ai.WalkShoutRuleConfig;
import com.pip.util.Utils;

/**
 * 本类用于处理数据国际化/本地化问题。
 * 
 * 制作一个其他语言版本分为3步：国际化、翻译、本地化。
 * 1. 国际化：调用findI18NRelatedStrings方法找出项目中所有需要国际化的字符串。
 * 2. 翻译：把第一步找出的字符串给第三方翻译。
 * 3. 本地化：调用doI18N函数把项目中的字符串替换为其他语言的版本。
 * 
 * 注意在这3步完成后，还需要进行几个额外动作以保证数据完整性：
 * 1. 场景中带中文字的地图（例如新手村），需要用特殊规则替换为其他语言版本。
 * 2. 重新生成所有的client.pkg。
 * 3. 重新生成所有的Buff类和Skill类。
 * 
 * 需要国际化的内容包括：
 * GameArea
 *   GameMapInfo: name
 *   GameMapNPC: name, functionName, searchName
 * HorseType: showName
 * Equipment: title
 * Formula: title, description
 * GiftGroup: title, errorMessage, groupMessage, giftMessage, maxExceedMessage, 
 *   repeatExceedMessage, timeSpaceMessage, timeErrorMessage, needItemMessage, 
 *   needVarMessage, giveOKMessage, bagFullMessage, 
 * Item: title, description
 * Shop: title
 * Suite: title
 * TeleportSet:
 *   Teleport: name
 * NPCTemplate:
 *   Rule: message
 * Quest: preDescription, postDescription, unfinishDescription
 *   QuestTarget: condition, description
 *   QuestInfo: 
 *     QuestTrigger: condition, action
 * BuffConfig: title, description
 * SkillConfig : title, description
 * Title: title, description
 * hints.xml: 所有提示信息文本
 * Rank: title
 * scripts目录下的所有etf.gz脚本文件
 * questions.xml: 答题文件
 *
 * @author lighthu
 */
public class I18NUtils {
    /*
     * 国际化操作环境，记录操作模式，l10n数据，未处理字符串等。
     */
    private static class I18NContext {
        private boolean i18nMode;
        private MessageFile messageFile;
        private Map<String, String> existStrings;
        private Set<String> missingStrings;
        private Map<String, String> missingStringSources;
        private int foundReplaces;
        
        public I18NContext(boolean i18nMode, MessageFile mfile) {
            this.i18nMode = i18nMode;
            this.messageFile = mfile;
            existStrings = mfile.getMap();
            missingStrings = new HashSet<String>();
            missingStringSources = new HashMap<String, String>();
        }
        
        /**
         * 输入一个字符串，检查是否pool中是否有对应的本地化数据。
         * @param str
         * @return
         */
        public String input(String str, String source) {
            if (str == null) {
                return null;
            }
            if (existStrings.containsKey(str)) {
                if (i18nMode) {
                    return null;
                } else {
                    String ret = existStrings.get(str);
                    foundReplaces++;
                    return ret;
                }
            }
            if (!isI18NRelated(str)) {
                return null;
            }
            
            // 添加一个新字符串，并自动翻译替换
            missingStrings.add(str);
            missingStringSources.put(str, source);
            if (i18nMode) {
                return null;
            }
            foundReplaces++;
            return messageFile.autoTranslate(str);
        }
        
        /**
         * 取得当前记录的所有没有本地化的国际化相关地字符串。
         */
        public String[][] getMissingStrings() {
            String[] ret = new String[missingStrings.size()];
            missingStrings.toArray(ret);
            String[][] ret2 = new String[ret.length][2];
            for (int i = 0; i < ret.length; i++) {
                ret2[i][0] = ret[i];
                ret2[i][1] = missingStringSources.get(ret[i]);
            }
            return ret2;
        }
        
        /**
         * 生成统计报告。
         */
        public void report() {
            System.out.println("替换：" + foundReplaces + "，发现新文本：" + missingStrings.size());
        }
        
        /**
         * 判断是否提取模式。
         * @return
         */
        public boolean isI18NMode() {
            return this.i18nMode;
        }
    }
    
    /**
     * 判断一个字符串是否需要国际化。
     * @param str 字符串内容
     * @return 如果此字符串中包含中文字符，返回true。
     */
    public static boolean isI18NRelated(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch >= 0x4E00 && ch <= 0x9FA5) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 增量查找项目中需要国际化的字符串。
     * @param proj 项目
     * @param mfile 已有的翻译文件
     * @return 本次处理新发现的需要国际化的字符串
     */
    public static String[][] findI18NRelatedStrings(ProjectData proj, MessageFile mfile) {
        I18NContext context = new I18NContext(true, mfile);
        processProject(proj, context);
        context.report();
        return context.getMissingStrings();
    }
    
    /**
     * 对项目中所有国际化相关文件进行本地化。
     * @param proj 项目
     * @param mfile 已有的翻译文件
     * @return 本次处理发现没有本地化数据的字符串
     */
    public static String[][] doI18N(ProjectData proj, MessageFile mfile) {
        I18NContext context = new I18NContext(false, mfile);
        processProject(proj, context);
        context.report();
        return context.getMissingStrings();
    }
    
    /*
     * 处理一个项目中的所有文件。如果指定的I18N模式是国际化，则不改变文件，否则改变文件。
     */
    private static void processProject(ProjectData proj, I18NContext context) {
        String tmp;
        boolean changed;
        
        // 处理所有关卡的GameMapInfo以及其中的GameMapNPC
        // GameArea
        //   GameMapInfo: name
        //   GameMapNPC: name, functionName, searchName
        //   GameMapExit: positionVarName
        for (DataObject dobj : proj.getDataListByType(GameArea.class)) {
            System.out.println("process GameArea " + dobj.id);
            GameArea gameArea = (GameArea)dobj;
            try {
                GameAreaInfo areaInfo = new GameAreaInfo(gameArea);
                areaInfo.load();
                changed = false;
                for (GameMapInfo gmi : areaInfo.maps) {
                    tmp = context.input(gmi.name, "场景:" + gmi.name);
                    if (tmp != null){ 
                        gmi.name = tmp;
                        changed = true;
                    }
                    for (GameMapObject gmo : gmi.objects) {
                        if (gmo instanceof GameMapNPC) {
                            GameMapNPC npc = (GameMapNPC)gmo;
                            tmp = context.input(npc.name, "场景:" + gmi.name);
                            if (tmp != null) {
                                npc.name = tmp;
                                if (tmp.length() == 0) {
                                    npc.visible = false;
                                }
                                changed = true;
                            }
                            tmp = context.input(npc.functionName, "场景:" + gmi.name);
                            if (tmp != null) {
                                npc.functionName = tmp;
                                changed = true;
                            }
                            tmp = context.input(npc.searchName, "场景:" + gmi.name);
                            if (tmp != null) {
                                npc.searchName = tmp;
                                changed = true;
                            }
                        } else if (gmo instanceof GameMapExit) {
                            GameMapExit exit = (GameMapExit)gmo;
                            tmp = context.input(exit.positionVarName, "场景:" + gmi.name);
                            if (tmp != null) {
                                exit.positionVarName = tmp;
                                changed = true;
                            }
                        }
                    }
                }
                if (changed) {
                    areaInfo.save();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有坐骑类型
        // HorseType: showName
        changed = false;
        for (DataObject dobj : proj.getDataListByType(HorseType.class)) {
            System.out.println("process HorseType " + dobj.id);
            HorseType horseType = (HorseType)dobj;
            tmp = context.input(horseType.showName, "坐骑类型");
            if (tmp != null) {
                horseType.showName = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(HorseType.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有装备
        // Equipment: title
        changed = false;
        for (DataObject dobj : proj.getDataListByType(Equipment.class)) {
            System.out.println("process Equipment " + dobj.id);
            Equipment equipment = (Equipment)dobj;
            tmp = context.input(equipment.title, "装备");
            if (tmp != null) {
                equipment.title = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(Equipment.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有打造配方
        // Formula: title, description
        changed = false;
        for (DataObject dobj : proj.getDataListByType(Formula.class)) {
            System.out.println("process Formula " + dobj.id);
            Formula formula = (Formula)dobj;
            tmp = context.input(formula.title, "配方");
            if (tmp != null) {
                formula.title = tmp;
                changed = true;
            }
            tmp = context.input(formula.description, "配方");
            if (tmp != null) {
                formula.description = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(Formula.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有奖励组
        // GiftGroup: title, errorMessage, groupMessage, giftMessage, maxExceedMessage, 
        //   repeatExceedMessage, timeSpaceMessage, timeErrorMessage, needItemMessage, 
        //   needVarMessage, giveOKMessage, bagFullMessage
        changed = false;
        for (DataObject dobj : proj.getDataListByType(GiftGroup.class)) {
            System.out.println("process GiftGroup " + dobj.id);
            GiftGroup giftGroup = (GiftGroup)dobj;
            tmp = context.input(giftGroup.title, "奖励兑换");
            if (tmp != null) {
                giftGroup.title = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.errorMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.errorMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.groupMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.groupMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.giftMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.giftMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.maxExceedMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.maxExceedMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.repeatExceedMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.repeatExceedMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.timeSpaceMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.timeSpaceMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.timeErrorMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.timeErrorMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.needItemMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.needItemMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.needVarMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.needVarMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.giveOKMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.giveOKMessage = tmp;
                changed = true;
            }
            tmp = context.input(giftGroup.bagFullMessage, "奖励兑换");
            if (tmp != null) {
                giftGroup.bagFullMessage = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(GiftGroup.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有物品
        // Item: title, description
        // 坐骑类效果的坐骑名称
        changed = false;
        for (DataObject dobj : proj.getDataListByType(Item.class)) {
            System.out.println("process Item " + dobj.id);
            Item item = (Item)dobj;
            tmp = context.input(item.title, "物品");
            if (tmp != null) {
                item.title = tmp;
                changed = true;
            }
            tmp = context.input(item.description, "物品");
            if (tmp != null) {
                item.description = tmp;
                changed = true;
            }
            for (ItemEffect eff : item.effects) {
                if (eff.effectType == ItemEffect.TYPE_GETHORSE) {
                    tmp = context.input((String)eff.param.get("name"), "物品");
                    if (tmp != null) {
                        eff.param.put("name", tmp);
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            try {
                proj.saveDataList(Item.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有商店
        // Shop: title
        changed = false;
        for (DataObject dobj : proj.getDataListByType(Shop.class)) {
            System.out.println("process Shop " + dobj.id);
            Shop shop = (Shop)dobj;
            tmp = context.input(shop.title, "商店");
            if (tmp != null) {
                shop.title = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(Shop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有套装
        // Suite: title
        changed = false;
        for (DataObject dobj : proj.getDataListByType(SuiteConfig.class)) {
            System.out.println("process SuiteConfig " + dobj.id);
            SuiteConfig suite = (SuiteConfig)dobj;
            tmp = context.input(suite.title, "套装");
            if (tmp != null) {
                suite.title = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(SuiteConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有驿站
        // TeleportSet:
        //   Teleport: name
        changed = false;
        for (DataObject dobj : proj.getDataListByType(TeleportSet.class)) {
            System.out.println("process TeleportSet " + dobj.id);
            TeleportSet tset = (TeleportSet)dobj;
            for (Teleport teleport : tset.items) {
                tmp = context.input(teleport.name, "驿站");
                if (tmp != null) {
                    teleport.name = tmp;
                    changed = true;
                }
            }
        }
        if (changed) {
            try {
                proj.saveDataList(TeleportSet.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有NPC模板中AI里的汉化
        // NPCTemplate:
        //   Rule: message
        changed = false;
        for (DataObject dobj : proj.getDataListByType(NPCTemplate.class)) {
            System.out.println("process NPCTemplate " + dobj.id);
            NPCTemplate npct = (NPCTemplate)dobj;
            for (AIRuleConfig rule : npct.aiRules) {
                if (rule instanceof SkillAttackRuleConfig) {
                    SkillAttackRuleConfig rc = (SkillAttackRuleConfig)rule;
                    tmp = context.input(rc.message, "NPC模板");
                    if (tmp != null) {
                        rc.message = tmp;
                        changed = true;
                    }
                } else if (rule instanceof SummonRuleConfig) {
                    SummonRuleConfig rc = (SummonRuleConfig)rule;
                    tmp = context.input(rc.message, "NPC模板");
                    if (tmp != null) {
                        rc.message = tmp;
                        changed = true;
                    }
                } else if (rule instanceof WalkShoutRuleConfig) {
                    WalkShoutRuleConfig rc = (WalkShoutRuleConfig)rule;
                    tmp = context.input(rc.message, "NPC模板");
                    if (tmp != null) {
                        rc.message = tmp;
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            try {
                proj.saveDataList(NPCTemplate.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有任务：任务描述、任务目标文字、任务内容中出现的文本
        // Quest: title, description, preDescription, postDescription, unfinishDescription, finishcondition
        //   QuestTarget: condition, description
        //   QuestInfo: 
        //     QuestTrigger: condition, action
        changed = false;
        for (DataObject dobj : proj.getDataListByType(Quest.class)) {
            System.out.println("process Quest " + dobj.id);
            Quest quest = (Quest)dobj;
            tmp = context.input(quest.title, "任务");
            if (tmp != null) {
                quest.title = tmp;
                changed = true;
            }
            tmp = context.input(quest.description, "任务");
            if (tmp != null) {
                quest.description = tmp;
                changed = true;
            }
            tmp = context.input(quest.preDescription, "任务");
            if (tmp != null) {
                quest.preDescription = tmp;
                changed = true;
            }
            tmp = context.input(quest.postDescription, "任务");
            if (tmp != null) {
                quest.postDescription = tmp;
                changed = true;
            }
            tmp = context.input(quest.unfinishDescription, "任务");
            if (tmp != null) {
                quest.unfinishDescription = tmp;
                changed = true;
            }
            ExpressionList exprList = ExpressionList.fromString(quest.finishCondition);
            if (processExpressionList(exprList, context)) {
                quest.finishCondition = exprList.toString();
                changed = true;
            }
            for (QuestTarget target : quest.targets) {
                exprList = ExpressionList.fromString(target.condition);
                if (processExpressionList(exprList, context)) {
                    target.condition = exprList.toString();
                    changed = true;
                }
                tmp = context.input(target.description, "任务");
                if (tmp != null) {
                    target.description = tmp;
                    changed = true;
                }
                tmp = context.input(target.hint, "任务");
                if (tmp != null) {
                    target.hint = tmp;
                    changed = true;
                }
            }
            
            // 处理任务脚本
            try {
                QuestInfo qinfo = new QuestInfo(quest);
                qinfo.load();
                boolean changed2 = false;
                for (QuestVariable var : qinfo.variables) {
                    tmp = context.input(var.name, "任务");
                    if (tmp != null) {
                        var.name = tmp;
                        changed = true;
                    }
                }
                for (QuestTrigger trigger : qinfo.triggers) {
                    exprList = ExpressionList.fromString(trigger.condition);
                    if (processExpressionList(exprList, context)) {
                        trigger.condition = exprList.toString();
                        changed2 = true;
                    }
                    exprList = ExpressionList.fromString(trigger.action);
                    if (processExpressionList(exprList, context)) {
                        trigger.action = exprList.toString();
                        changed2 = true;
                    }
                }
                if (changed2) {
                    qinfo.save();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (changed) {
            try {
                proj.saveDataList(Quest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有BUFF
        // BuffConfig: title, description
        changed = false;
        for (DataObject dobj : proj.getDataListByType(BuffConfig.class)) {
            System.out.println("process BuffConfig " + dobj.id);
            BuffConfig buffConfig = (BuffConfig)dobj;
            tmp = context.input(buffConfig.title, "技能");
            if (tmp != null) {
                buffConfig.title = tmp;
                changed = true;
            }
            tmp = context.input(buffConfig.description, "技能");
            if (tmp != null) {
                buffConfig.description = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(BuffConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有技能
        // SkillConfig : title, description
        changed = false;
        for (DataObject dobj : proj.getDataListByType(SkillConfig.class)) {
            System.out.println("process SkillConfig " + dobj.id);
            SkillConfig skillConfig = (SkillConfig)dobj;
            tmp = context.input(skillConfig.title, "技能");
            if (tmp != null) {
                skillConfig.title = tmp;
                changed = true;
            }
            tmp = context.input(skillConfig.description, "技能");
            if (tmp != null) {
                skillConfig.description = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(SkillConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有称号
        // Title: title, description
        changed = false;
        for (DataObject dobj : proj.getDataListByType(Title.class)) {
            System.out.println("process Title " + dobj.id);
            Title title = (Title)dobj;
            tmp = context.input(title.title, "称号");
            if (tmp != null) {
                title.title = tmp;
                changed = true;
            }
            tmp = context.input(title.description, "称号");
            if (tmp != null) {
                title.description = tmp;
                changed = true;
            }
        }
        if (changed) {
            try {
                proj.saveDataList(Title.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 处理所有小提示
        // hints.xml: 所有提示信息文本
        try {
            System.out.println("process hints.xml");
            changed = false;
            Document doc = Utils.loadDOM(new File(proj.baseDir, "hints.xml"));
            Element root = doc.getRootElement();
            List list = root.getChildren("hint");
            for (int i = 0; i < list.size(); i++) {
                Element elem = (Element)list.get(i);
                tmp = context.input(elem.getText(), "小提示");
                if (tmp != null) {
                    elem.setText(tmp);
                    changed = true;
                }
            }
            if (changed) {
                Utils.saveDOM(doc, new File(proj.baseDir, "hints.xml"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 处理所有军衔
        // Rank: title
        List<DataObject> rankList = (List<DataObject>)proj.getDictDataListByType(Rank.class);
        for (DataObject dobj : rankList) {
            System.out.println("process Rank " + dobj.id);
            Rank rank = (Rank)dobj;
            tmp = context.input(rank.title, "军衔");
            if (tmp != null) {
                rank.title = tmp;
                changed = true;
            }
        }
        if (changed) {
            Element root = new Element("ranks");
            Document doc = new Document(root);
            for (DataObject dobj : rankList) {
                root.addContent(dobj.save());
            }
            try {
                Utils.saveDOM(doc, new File(proj.baseDir, "ranks.xml"));
            } catch (Exception e) {
            }
        }
        
        // 处理所有脚本文件
        // scripts目录下的所有etf.gz文件
        List<File> scriptFiles = findFiles(new File(proj.baseDir, "scripts"), ".etf.gz");
        for (File sf : scriptFiles) {
            System.out.println("process " + sf);
            try {
                FileInputStream fis = new FileInputStream(sf);
                GZIPInputStream gis = new GZIPInputStream(fis);
                ETFFile etf = ETFFile.load(gis);
                fis.close();
                changed = false;
                for (int i = 0; i < etf.stringTable.length; i++) {
                    tmp = context.input(etf.stringTable[i], "脚本");
                    if (tmp != null) {
                        etf.stringTable[i] = tmp;
                        changed = true;
                    }
                }
                if (changed) {
                    FileOutputStream fos = new FileOutputStream(sf);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    GZIPOutputStream zos = new GZIPOutputStream(bos);
                    ETFUtil.save(etf, zos);
                    zos.flush();
                    zos.close();
                    bos.flush();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 处理答题系统问题
        // questions.xml: 所有问题的提问和答案
        try {
            System.out.println("process questions.xml");
            changed = false;
            Document doc = Utils.loadDOM(new File(proj.baseDir, "questions.xml"));
            Element root = doc.getRootElement();
            List list = root.getChildren("question");
            for (int i = 0; i < list.size(); i++) {
                Element elem = (Element)list.get(i);
                tmp = context.input(elem.getAttributeValue("desc"), "答题");
                if (tmp != null) {
                    elem.getAttribute("desc").setValue(tmp);
                    changed = true;
                }
                tmp = context.input(elem.getAttributeValue("answer"), "答题");
                if (tmp != null) {
                    elem.getAttribute("answer").setValue(tmp);
                    changed = true;
                }
            }
            if (changed) {
                Utils.saveDOM(doc, new File(proj.baseDir, "questions.xml"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
}
    
    /*
     * 处理一个PQE表达式。如果表达式处理后有改变，返回true。
     */
    private static boolean processExpressionList(Object exprObj, I18NContext context) {
        boolean ret = false;
        if (exprObj instanceof ExpressionList) {
            ExpressionList list = (ExpressionList)exprObj;
            for (int i = 0; i < list.getExprCount(); i++) {
                if (processExpressionList(list.getExpr(i), context)) {
                    ret = true;
                }
            }
        } else if (exprObj instanceof Expression) {
            Expression expr = (Expression)exprObj;
            if (processExpressionList(expr.getLeftExpr(), context)) {
                ret = true;
            }
            if (expr.getRightExpr() != null) {
                if (processExpressionList(expr.getRightExpr(), context)) {
                    ret = true;
                }
            }
        } else if (exprObj instanceof Expr0) {
            Expr0 expr0 = (Expr0)exprObj;
            if (expr0.type == Expr0.TYPE_IDENTIFIER) {
                String tmp = context.input(expr0.value, "任务");
                if (tmp != null) {
                    expr0.value = tmp;
                    ret = true;
                }
            } else if (expr0.type == Expr0.TYPE_STRING) {
                String str = PQEUtils.translateStringConstant(expr0.value);
                String tmp = context.input(str, "任务");
                if (tmp != null) {
                    expr0.value = "\"" + PQEUtils.reverseConv(tmp) + "\"";
                    ret = true;
                }
            } else if (expr0.type == Expr0.TYPE_FUNC) {
                FunctionCall fc = expr0.getFunctionCall();
                for (int i = 0; i < fc.getParamCount(); i++) {
                    if (processExpressionList(fc.getParam(i), context)) {
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }
    
    /*
     * 处理脚本中需要国际化的一个字符串常量。
     */
    private static boolean processSingleExprString(Expression expr, I18NContext context) {
        if (expr.getRightExpr() == null && expr.getLeftExpr().type == Expr0.TYPE_STRING) {
            String str = PQEUtils.translateStringConstant(expr.getLeftExpr().value);
            String tmp = context.input(str, "任务");
            if (tmp != null) {
                expr.getLeftExpr().value = "\"" + PQEUtils.reverseConv(tmp) + "\"";
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * 增量查找Java文件中需要国际化的字符串。
     * @param root 根目录
     * @param mfile 翻译文件
     * @return 本次处理新发现的需要国际化的字符串
     */
    public static String[][] findI18NRelatedJavaStrings(File root, MessageFile mfile, String encoding1, String encoding2) {
        I18NContext context = new I18NContext(true, mfile);
        processJava(root, context, encoding1, encoding2);
        context.report();
        return context.getMissingStrings();
    }
    
    /**
     * 对Java文件中所有国际化相关文件进行本地化。
     * @param root 根目录
     * @param mfile 翻译文件
     * @return 本次处理发现没有本地化数据的字符串
     */
    public static String[][] doI18NJava(File root, MessageFile mfile, String encoding1, String encoding2) {
        I18NContext context = new I18NContext(false, mfile);
        processJava(root, context, encoding1, encoding2);
        context.report();
        return context.getMissingStrings();
    }
    
    /*
     * 处理一个目录下的所有Java文件。如果指定的I18N模式是国际化，则不改变文件，否则改变文件。
     */
    private static void processJava(File root, I18NContext context, String encoding1, String encoding2) {
        List<File> javaFiles = findFiles(root, ".java");
        for (File jf : javaFiles) {
            if (jf.getName().startsWith("AutoGenerated")) {
                continue;
            }
            try {
                List<String> tokens = JavaTokenizer.parse(jf, encoding1);
                boolean changed = false;
                for (int i = 0; i < tokens.size(); i++) {
                    String tk = tokens.get(i);
                    if (tk.startsWith("\"") && tk.endsWith("\"")) {
                        String oldStr = PQEUtils.translateStringConstant(tk);
                        String newStr = context.input(oldStr, jf.getName());
                        if (newStr != null) {
                            tokens.set(i, "\"" + PQEUtils.reverseConv(newStr) + "\"");
                            changed = true;
                        }
                    }
                }
                if (!context.isI18NMode()) {
                    JavaTokenizer.save(jf, encoding2, tokens);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 找出目录中所有的Java文件。
     */
    public static List<File> findFiles(File root, String suffix) {
        List<File> retList = new ArrayList<File>();
        List<File> pendingList = new ArrayList<File>();
        pendingList.add(root);
        while (pendingList.size() > 0) {
            File ff = pendingList.remove(0);
            File[] ffs = ff.listFiles();
            for (File af : ffs) {
                if (af.isDirectory()) {
                    pendingList.add(af);
                } else if (af.getName().endsWith(suffix)) {
                    retList.add(af);
                }
            }
        }
        return retList;
    }
    
    /**
     * 增量查找ActionScript文件中需要国际化的字符串。
     * @param root 根目录
     * @param mfile 翻译文件
     * @return 本次处理新发现的需要国际化的字符串
     */
    public static String[][] findI18NRelatedActionScriptStrings(File root, MessageFile mfile, String encoding1, String encoding2) {
        I18NContext context = new I18NContext(true, mfile);
        processActionScript(root, context, encoding1, encoding2);
        context.report();
        return context.getMissingStrings();
    }
    
    /**
     * 对ActionScript文件中所有国际化相关文件进行本地化。
     * @param root 根目录
     * @param mfile 翻译文件
     * @return 本次处理发现没有本地化数据的字符串
     */
    public static String[][] doI18NActionScript(File root, MessageFile mfile, String encoding1, String encoding2) {
        I18NContext context = new I18NContext(false, mfile);
        processActionScript(root, context, encoding1, encoding2);
        context.report();
        return context.getMissingStrings();
    }
    
    /*
     * 处理一个目录下的所有ActionScript文件。如果指定的I18N模式是国际化，则不改变文件，否则改变文件。
     */
    private static void processActionScript(File root, I18NContext context, String encoding1, String encoding2) {
        List<File> actionScriptFiles = findFiles(root, ".as");
        for (File jf : actionScriptFiles) {
            if (jf.getName().startsWith("AutoGenerated")) {
                continue;
            }
            try {
                List<String> tokens = JavaTokenizer.parse(jf, encoding1);
                boolean changed = false;
                for (int i = 0; i < tokens.size(); i++) {
                    String tk = tokens.get(i);
                    if (tk.startsWith("\"") && tk.endsWith("\"")) {
                        String oldStr = PQEUtils.translateStringConstant(tk);
                        String newStr = context.input(oldStr, jf.getName());
                        if (newStr != null) {
                            tokens.set(i, "\"" + PQEUtils.reverseConv(newStr) + "\"");
                            changed = true;
                        }
                    }
                }
                if (!context.isI18NMode()) {
                    JavaTokenizer.save(jf, encoding2, tokens);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
