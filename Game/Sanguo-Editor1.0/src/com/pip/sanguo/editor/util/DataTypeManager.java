package com.pip.sanguo.editor.util;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.pip.sanguo.data.*;
import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.SuiteConfig;
import com.pip.sanguo.data.equipment.SuiteConfig_New;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.recast.Recast;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.*;
import com.pip.sanguo.editor.area.GameAreaEditor;
import com.pip.sanguo.editor.attendant.AttendantTypeEditor;
import com.pip.sanguo.editor.card.CardEditor;
import com.pip.sanguo.editor.clientevent.ClientEventEditor;
import com.pip.sanguo.editor.equipment.EquipmentEditor;
import com.pip.sanguo.editor.equipment.SuiteEditor;
import com.pip.sanguo.editor.equipment.SuiteEditor_New;
import com.pip.sanguo.editor.horse.HorseTypeEditor;
import com.pip.sanguo.editor.item.BookEditor;
import com.pip.sanguo.editor.item.DropGroupEditor;
import com.pip.sanguo.editor.item.FormulaEditor;
import com.pip.sanguo.editor.item.ItemEditor;
import com.pip.sanguo.editor.item.TalismanEditor;
import com.pip.sanguo.editor.quest.QuestEditor;
import com.pip.sanguo.editor.recast.RecastEditor;
import com.pip.sanguo.editor.shop.ShopEditor;
import com.pip.sanguo.editor.shop.TeleportSetEditor;
import com.pip.sanguo.editor.skill.BuffEditor;
import com.pip.sanguo.editor.skill.SkillEditor;
import com.pip.sanguo.editor.wizard.*;

/**
 * 数据类型相关信息，例如：编辑器类、创建Wizard类，等等。
 * @author lighthu
 */
public class DataTypeManager {
    // 支持编辑的对象类
    public static final Class[] editableClasses = { 
        GameArea.class, NPCTemplate.class, Animation.class, Quest.class, 
        GiftGroup.class, Shop.class, SuiteConfig.class, SuiteConfig_New.class,Title.class, Formula.class,
        Item.class, DropGroup.class, SkillConfig.class, Equipment.class, 
        BuffConfig.class, HorseType.class, TeleportSet.class, Sound.class,Card.class, Recast.class, 
        TalismanType.class,AttendantType.class,ClientEvent.class,DirectoryType.class,BookConfig.class
    };
    // 存储各数据类型对应的编辑器ID
    private static HashMap<Class, String> dataTypeEditors = new HashMap<Class, String>();
    static {
        dataTypeEditors.put(GameArea.class, GameAreaEditor.ID);
        dataTypeEditors.put(NPCTemplate.class, NPCTemplateEditor.ID);
        dataTypeEditors.put(Animation.class, AnimationEditor.ID);
        dataTypeEditors.put(Quest.class, QuestEditor.ID);
        dataTypeEditors.put(SuiteConfig.class, SuiteEditor.ID);
        dataTypeEditors.put(SuiteConfig_New.class, SuiteEditor_New.ID);
        dataTypeEditors.put(Title.class, TitleEditor.ID);
        dataTypeEditors.put(Formula.class, FormulaEditor.ID);
        dataTypeEditors.put(Item.class, ItemEditor.ID);
        dataTypeEditors.put(Equipment.class, EquipmentEditor.ID);
        dataTypeEditors.put(Item.class, ItemEditor.ID);
        dataTypeEditors.put(DropGroup.class, DropGroupEditor.ID);
        dataTypeEditors.put(SkillConfig.class, SkillEditor.ID);
        dataTypeEditors.put(Equipment.class, EquipmentEditor.ID);
        dataTypeEditors.put(Shop.class, ShopEditor.ID);
        dataTypeEditors.put(BuffConfig.class, BuffEditor.ID);
        dataTypeEditors.put(GiftGroup.class, GiftGroupEditor.ID);
        dataTypeEditors.put(HorseType.class, HorseTypeEditor.ID);
        dataTypeEditors.put(TeleportSet.class, TeleportSetEditor.ID);
        dataTypeEditors.put(Sound.class, SoundEditor.ID);
        dataTypeEditors.put(Card.class, CardEditor.ID);
        dataTypeEditors.put(Recast.class, RecastEditor.ID);
        dataTypeEditors.put(TalismanType.class, TalismanEditor.ID);
        dataTypeEditors.put(AttendantType.class, AttendantTypeEditor.ID);
        dataTypeEditors.put(ClientEvent.class, ClientEventEditor.ID);
        dataTypeEditors.put(DirectoryType.class, DirectoryEditor.ID);
        dataTypeEditors.put(BookConfig.class, BookEditor.ID);
    }
    
    // 存储各数据类型对应的标题名称
    private static HashMap<Class, String> dataTypeNames = new HashMap<Class, String>();
    static {
        dataTypeNames.put(GameArea.class, "关卡");
        dataTypeNames.put(NPCTemplate.class, "NPC模板");
        dataTypeNames.put(Animation.class, "动画");
        dataTypeNames.put(Quest.class, "任务");
        dataTypeNames.put(SuiteConfig.class, "套装");
        dataTypeNames.put(SuiteConfig_New.class, "新套装");
        dataTypeNames.put(Title.class, "称号");
        dataTypeNames.put(Formula.class, "打造配方");
        dataTypeNames.put(Item.class, "物品");
        dataTypeNames.put(DropGroup.class, "掉落组");
        dataTypeNames.put(SkillConfig.class, "技能");
        dataTypeNames.put(Equipment.class, "装备");
        dataTypeNames.put(Shop.class, "商店");
        dataTypeNames.put(BuffConfig.class, "BUFF");
        dataTypeNames.put(GiftGroup.class, "奖励组");
        dataTypeNames.put(HorseType.class, "坐骑类型");
        dataTypeNames.put(TeleportSet.class, "驿站");
        dataTypeNames.put(Sound.class, "声音");
        dataTypeNames.put(Card.class, "卡片");
        dataTypeNames.put(Recast.class, "重铸");
        dataTypeNames.put(TalismanType.class, "法宝");
        dataTypeNames.put(AttendantType.class, "随从类型");
        dataTypeNames.put(ClientEvent.class, "事件");
        dataTypeNames.put(DirectoryType.class, "活动引导");
        dataTypeNames.put(BookConfig.class, "书籍");
    }
    
    // 存储各数据类型对应的创建Wizard类
    private static HashMap<Class, Class> dataTypeCreateWizards = new HashMap<Class, Class>();
    static {
        dataTypeCreateWizards.put(GameArea.class, NewGameAreaWizard.class);
        dataTypeCreateWizards.put(NPCTemplate.class, NewNPCTemplateWizard.class);
        dataTypeCreateWizards.put(Animation.class, NewAnimationWizard.class);
        dataTypeCreateWizards.put(Quest.class, NewQuestWizard.class);
        dataTypeCreateWizards.put(Title.class, NewTitleWizard.class);
        dataTypeCreateWizards.put(Formula.class, NewFormulaWizard.class);
        dataTypeCreateWizards.put(SuiteConfig.class, NewSuiteWizard.class);
        dataTypeCreateWizards.put(SuiteConfig_New.class, NewSuiteWizard_New.class);
        dataTypeCreateWizards.put(Item.class, NewItemWizard.class);
        dataTypeCreateWizards.put(DropGroup.class, NewDropGroupWizard.class);
        dataTypeCreateWizards.put(SkillConfig.class, NewSkillWizard.class);
        dataTypeCreateWizards.put(Equipment.class, NewEquipmentWizard.class);
        dataTypeCreateWizards.put(Shop.class, NewShopWizard.class);
        dataTypeCreateWizards.put(BuffConfig.class, NewBuffWizard.class);
        dataTypeCreateWizards.put(GiftGroup.class, NewGiftGroupWizard.class);
        dataTypeCreateWizards.put(HorseType.class, NewHorseTypeWizard.class);
        dataTypeCreateWizards.put(TeleportSet.class, NewTeleportSetWizard.class);
        dataTypeCreateWizards.put(Sound.class, NewSoundWizard.class);
        dataTypeCreateWizards.put(Card.class, NewCardWizard.class);
        dataTypeCreateWizards.put(Recast.class, NewRecastWizard.class);
        dataTypeCreateWizards.put(TalismanType.class, NewTalismanWizard.class);
        dataTypeCreateWizards.put(AttendantType.class, NewAttendantTypeWizard.class);
        dataTypeCreateWizards.put(ClientEvent.class, NewClientEventWizard.class);
        dataTypeCreateWizards.put(DirectoryType.class, NewDirectoryWizard.class);
        dataTypeCreateWizards.put(BookConfig.class, NewBookWizard.class);
    }
    
    
    /**
     * 查找一个数据类型的编辑器ID。
     * @param cls
     * @return
     */
    public static String getEditorID(Class cls) {
        return dataTypeEditors.get(cls);
    }
    
    /**
     * 查找一个数据类型的标题。
     */
    public static String getTypeName(Class cls) {
        return dataTypeNames.get(cls);
    }
    
    /**
     * 得到一个数据类型对应的Wizard对象。
     */
    public static Runnable getCreateWizard(Class cls) throws Exception {
        Class cls2 = dataTypeCreateWizards.get(cls);
        return (Runnable)cls2.newInstance();
    }
}
