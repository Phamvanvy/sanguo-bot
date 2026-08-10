package com.pip.sanguo.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Document;
import org.jdom.Element;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.Shop.ShopItem;
import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentPrefix;
import com.pip.sanguo.data.equipment.SuiteConfig;
import com.pip.sanguo.data.equipment.SuiteConfig_New;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.JewelConfig;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.pkg.PackageFile;
import com.pip.sanguo.data.pkg.PackageUtils;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.recast.Recast;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.Settings;
import com.pip.util.Utils;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;

/**
 * 项目数据集合。所有数据对象都继承DataObject接口。同一类型的数据对象保存在一个XML文件中。所有数据对象的
 * XML文件，以及数据对象引用的其他文件，都保存在一个项目目录的子目录中。
 * @author lighthu
 */
public class ProjectData {
    // 支持标记的对象类
    public static final Class[] supportDataClasses = { 
        GameArea.class, NPCTemplate.class, AttendantType.class, Animation.class, Quest.class, 
        GiftGroup.class, Shop.class, TeleportSet.class, SuiteConfig.class, SuiteConfig_New.class, 
        Title.class, Formula.class, Card.class, Item.class, DropGroup.class,
        SkillConfig.class, Equipment.class, BuffConfig.class, HorseType.class, 
        EquipmentPrefix.class, Sound.class,Recast.class, TalismanType.class,ClientEvent.class,DirectoryType.class,BookConfig.class
    };
    // 不同类型对象对应的XML根标签
    private static final String[] dataRootTags = { 
        "areas", "npcs", "attendanttype", "animations", "quests", 
        "giftgroups", "shops", "teleports", "suites","newsuites", "titles", "formulas", "cards",
        "items", "DropGroups", "skills", "equipments", 
        "buffs", "horsetypes", "prefixes", "sounds", "recasts", "talismen","events","directorys","books"
    };
    // 不同类型对象对应的XML标签
    private static final String[] dataTags = { 
        "area", "npc", "attendant", "animation", "quest", 
        "giftgroup", "shop", "teleportset", "suite","newsuite", "title", "formula","card",
        "item", "DropGroup", "skill", "equipment", 
        "buff", "horsetype", "prefix", "sound", "recast", "talisman", "event", "directory","book"
    };
    // 不同类型对象对应的XML文件相对路径
    private static final String[] dataFiles = { 
        "Areas/index.xml", "NPCTemplates/index.xml", "Attendant/attendanttype.xml", "Animations/index.xml", "Quests/index.xml", 
        "Items/giftgroups.xml", "Items/shop.xml", "Items/teleport.xml", "Items/suites.xml","Items/newsuites.xml", "Titles/index.xml", "Items/formula.xml","Cards/index.xml", 
        "Items/item.xml", "Items/dropgroup.xml", "Skill/skills.xml", "Items/equipment.xml",
        "Skill/buffs.xml", "Horse/horsetype.xml", "Items/prefix.xml", "Sounds/index.xml", "Recast/index.xml", 
        "Items/talisman.xml", "ClientEvent/index.xml", "directorys/index.xml","Items/books.xml"
    };
    
    // 字典对象类
    private static final Class[] dictDataClasses = { NPCType.class, Faction.class, Rank.class };
    // 字典对象类对应的XML标签
    private static final String[] dictDataTags =  { "npctype", "faction", "rank" };
    // 字典对象对应的XML文件相对路径
    private static final String[] dictDataFiles = { "npctypes.xml", "factions.xml", "ranks.xml" };
    
    // 所有编辑器支持的对象列表，和supportDataClasses顺序对应。
    private List<DataObject>[] dataLists = new List[supportDataClasses.length];
    // 对象分类列表
    private List<DataObjectCategory>[] dataCateLists = new List[supportDataClasses.length];
    // 所有字典对象列表，和dictDataClasses是顺序对应
    private List<DataObject>[] dictDataLists = new List[dictDataClasses.length];
    // 当前项目路径
    public java.io.File baseDir;
    // 保存动作监听
    private IProjectDataListener dataListener;
    // 配置参数
    public Properties config = new Properties();
    
    // 是否服务器模式。在服务器模式下，所有访问的内容被缓存起来。
    public boolean serverMode = false;
    // 是否需要创建寻路数据
    public boolean createPathFinder = true;
    // 分支版本，用于支持CMCC和CHINATEL版本，null表示PIP版本。仅用于服务器模式。
    public String branch = null;
    // 文件缓存，仅用于服务器模式
    protected Hashtable<String, byte[]> resourceCache = new Hashtable<String, byte[]>();
    // 所有文件的版本号（仅用于服务器模式）
    protected Hashtable<String, Integer> resourceVersion = new Hashtable<String, Integer>();
    // 所有文件名和实际文件的对应关系（仅用于服务器模式）
    protected Hashtable<String, String> downloadFileMapping = new Hashtable<String, String>();
    // 自动寻路工具（仅用于服务器模式）
    protected AutoPathFinder pathFinder;
    // 客户端数据配置文件
    protected ClientData clientData;
    
    /** 技能图标 */
    public PipImage skillIcon;
    /** 技能图标--新增 */
    public PipImage skillIcon2;
    /** 武器图标 */
    public PipImage weaponIcon;
    /** 法宝图标 */
    public PipImage talismanIcon;
    /** CMCC道具代码设置 */
    public CmccConfig cmccConfig;
    /** NPC缺省设置 */
    public NPCTemplateConfig npcTemplateConfig;
    /** 宝石配置 */
    public JewelConfig jewelConfig;
    /** 随从缺省设置 */
    public AttendantConfig attendantConfig;
    
    /**
     * 创建一个空的项目。
     */
    public ProjectData() {
        for (int i = 0; i < dataLists.length; i++) {
            dataLists[i] = new ArrayList<DataObject>();
        }
        for (int i = 0; i < dataCateLists.length; i++) {
            dataCateLists[i] = new ArrayList<DataObjectCategory>();
        }
        for (int i = 0; i < dictDataLists.length; i++) {
            dictDataLists[i] = new ArrayList<DataObject>();
        }
    }
    
    /**
     * 设置数据改变动作监听者。
     * @param l
     */
    public void setDataListener(IProjectDataListener l) {
        dataListener = l;
    }
    
    /**
     * 取得一个数据类型在类型表中的位置。
     */
    public static int getIndexByType(Class cls) {
        for (int i = 0; i < supportDataClasses.length; i++) {
            if (supportDataClasses[i] == cls) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 取得一个类型的所有数据对象。
     * @param cls 数据对象类型
     * @return
     */
    public List<DataObject> getDataListByType(Class cls) {
        return (List<DataObject>)dataLists[getIndexByType(cls)];
    }
    
    /**
     * 取得一个类型的所有数据对象分类。
     * @param cls 数据对象类型
     */
    public List<DataObjectCategory> getCategoryListByType(Class cls) {
        return (List<DataObjectCategory>)dataCateLists[getIndexByType(cls)];
    }
    
    /**
     * 取得一个字典类型的所有数据对象。
     * @param cls 数据对象类型
     * @return
     */
    public List<DataObject> getDictDataListByType(Class cls) {
        for (int i = 0; i < dictDataClasses.length; i++) {
            if (dictDataClasses[i] == cls) {
                return (List<DataObject>)dictDataLists[i];
            }
        }
        return null;
    }
    
    /**
     * 查找指定ID，指定类型的数据对象。
     * @param cls 数据对象类型
     * @param id 对象ID
     * @return 如果没有找到，返回null
     */
    public DataObject findObject(Class cls, int id) {
        List<DataObject> list = getDataListByType(cls);
        
        // 如果是服务器模式，则数据是已经排序的，可使用二分查找
        if (this.serverMode) {
            int start = 0, end = list.size() - 1;
            while (start <= end) {
                int mid = (start + end) / 2;
                DataObject obj = list.get(mid);
                if (obj.id == id) {
                    return obj;
                } else if (obj.id < id) {
                    start = mid + 1;
                } else {
                    end = mid - 1;
                }
            }
        } else {
            for (DataObject obj : list) {
                if (obj.id == id) {
                    return obj;
                }
            }
        }
        return null;
    }
    
    /**
     * 查找指定类型数据的一个数据分类。
     * @param cls
     * @param name
     * @return
     */
    public DataObjectCategory findCategory(Class cls, String name) {
        int index = getIndexByType(cls);
        List<DataObjectCategory> list = dataCateLists[index];
        for (DataObjectCategory cate : list) {
            if (cate.name.equals(name)) {
                return cate;
            }
        }
        return null;
    }
    
    /**
     * 根据ID查找物品或装备
     * @param id
     * @return
     */
    public Item findItemOrEquipment(int id) {
        Item ret = findItem(id);
        if (ret == null) {
            ret = findEquipment(id);
        }
        return ret;
    }
    
    /**
     * 根据id查找物品
     * @param id
     * @return
     */
    public Item findItem(int id) {
        return (Item)findObject(Item.class, id);
    }
    
    /**
     * 根据id查找装备
     * @param id
     * @return
     */
    public Equipment findEquipment(int id) {
        return (Equipment)findObject(Equipment.class, id);
    }
    
    /**
     * 查找指定数据对象在总列表中的索引。
     * @param obj 数据对象
     * @return 如果没有找到，返回-1
     */
    public int getObjectIndex(DataObject obj) {
        List<DataObject> list = getDataListByType(obj.getClass());
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).id == obj.id) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 查找指定ID，指定类型的字典数据对象。
     * @param cls 数据对象类型
     * @param id 对象ID
     * @return 如果没有找到，返回null
     */
    public DataObject findDictObject(Class cls, int id) {
        List<DataObject> list = getDictDataListByType(cls);
        
        // 如果是服务器模式，则数据是已经排序的，可使用二分查找
        if (this.serverMode) {
            int start = 0, end = list.size() - 1;
            while (start <= end) {
                int mid = (start + end) / 2;
                DataObject obj = list.get(mid);
                if (obj.id == id) {
                    return obj;
                } else if (obj.id < id) {
                    start = mid + 1;
                } else {
                    end = mid - 1;
                }
            }
        } else {
            for (DataObject obj : list) {
                if (obj.id == id) {
                    return obj;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据id查找卡片
     * @param id
     * @return
     */
    public Card findCard(int id) {
        return (Card)findObject(Card.class, id);
    }
    
    /**
     * 根据id查找书籍
     * @param id
     * @return
     */
    public BookConfig findBook(int id) {
        return (BookConfig)findObject(BookConfig.class, id);
    }
    
    /**
     * 查找指定字典数据对象在总列表中的索引。
     * @param obj 数据对象
     * @return 如果没有找到，返回-1
     */
    public int getDictObjectIndex(DataObject obj) {
        List<DataObject> list = getDictDataListByType(obj.getClass());
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).id == obj.id) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 创建一个新的数据对象，并加入到列表中。
     * @param cls 数据对象类型
     * @return 新创建的数据对象 
     */
    public DataObject newObject(Class cls) throws Exception {
        Constructor cons = cls.getConstructor(ProjectData.class);
        DataObject newObj = (DataObject)cons.newInstance(this);
        newObj.id = 1;
        while (findObject(cls, newObj.id) != null) {
            newObj.id++;
        }
        addObjectToList(cls, newObj);
        return newObj;
    }
    
    public void addObjectToList(Class cls, DataObject newObj) {
        // 加入总列表
        int index = getIndexByType(cls);
        dataLists[index].add(newObj);
        
        // 加入分类列表
        boolean found = false;
        for (DataObjectCategory cate : dataCateLists[index]) {
            if (cate.name.equals(newObj.categoryName)) {
                cate.objects.add(newObj);
                found = true;
                break;
            }
        }
        if (!found) {
            DataObjectCategory cate = new DataObjectCategory(cls);
            cate.name = newObj.categoryName;
            cate.objects.add(newObj);
            dataCateLists[index].add(cate);
        }
    }
    
    /**
     * 创建一个新的数据分类。
     * @param cls 数据对象类型
     * @return
     */
    public DataObjectCategory newCategory(Class cls, String name) throws Exception {
        int index = getIndexByType(cls);
        for (DataObjectCategory cate : dataCateLists[index]) {
            if (cate.name.equals(name)) {
                throw new Exception("分类名称不能重复。");
            }
        }
        DataObjectCategory cate = new DataObjectCategory(cls);
        cate.name = name;
        dataCateLists[index].add(cate);
        return cate;
    }
    
    /**
     * 新建一个物品，并分配id，添加到类型列表中
     * @param itemType
     * @return
     */
    public Item newItem(DataObjectCategory category) {
        Item item = new Item(this);
        item.id = 1;
        while (findItemOrEquipment(item.id) != null) {
            item.id++;
        }
        if (category != null) {
            item.categoryName = category.name;
        }
        addObjectToList(Item.class, item);
        return item;
    }

    /**
     * 新建一个装备，并分配id，添加到类型列表中
     * @param equiType
     * @return
     */
    public Equipment newEquipment(DataObjectCategory category) {
        Equipment equi = new Equipment(this);
        
        // 装备和物品公用id，需要分段从百万开始
        equi.id = 1000000;
        while (findItemOrEquipment(equi.id) != null) {
            equi.id++;
        }
        if (category != null) {
            equi.categoryName = category.name;
        }
        addObjectToList(Equipment.class, equi);
        return equi;
    }
    
    /**
     * 更新对象。任何一个对象的更新都会触发XML文件存储。
     * @param src 新的数据
     * @param dest 需要更新的目标对象
     * @throws Exception
     */
    public void updateObject(DataObject src, DataObject dest) throws Exception {
        // 确保没有重复的ID
        DataObject searchResult = findObject(src.getClass(), src.id);
        if (searchResult != null && searchResult != dest) {
            throw new Exception("重复的ID。");
        }
        
        // 保存对象属性并更新XML文件
        dest.update(src);
        saveDataList(dest.getClass());
    }
    
    /**
     * 把一个数据对象从一个分类移动到另外一个分类。
     * @param obj
     * @param newCate
     */
    public void changeObjectCategory(DataObject obj, DataObjectCategory newCate) {
        DataObjectCategory oldCate = findCategory(obj.getClass(), obj.categoryName);
        oldCate.objects.remove(obj);
        obj.categoryName = newCate.name;
        newCate.objects.add(obj);
    }
    
    /**
     * 删除一个对象（本方法不会删除此数据对象的关联对象）。
     * @param obj
     */
    public void deleteObject(DataObject obj) {
        int index = getIndexByType(obj.getClass());
        if (index == -1) {
            return;
        }
        List<DataObject> list = dataLists[index];
        list.remove(obj);
        List<DataObjectCategory> clist = dataCateLists[index];
        for (DataObjectCategory cate : clist) {
            if (cate.name.equals(obj.categoryName)) {
                cate.objects.remove(obj);
            }
        }
    }
    
    /**
     * 保存所有类型的数据对象列表。
     */
    public void saveAll() throws Exception {
        for (int i = 0; i < supportDataClasses.length; i++) {
            saveDataList(supportDataClasses[i]);
        }
    }
    
    /**
     * 取得某类型数据对应的索引文件。
     * @param cls
     * @return
     */
    public File getDataFile(Class cls) {
        for (int i = 0; i < supportDataClasses.length; i++) {
            if (supportDataClasses[i] == cls) {
                return new File(baseDir, dataFiles[i]);
            }
        }
        return null;
    }
    
    /**
     * 保存一个类型的所有数据对象。
     * @param cls 数据对象类型
     * @throws Exception
     */
    public void saveDataList(Class cls) throws Exception {
        if (dataListener != null) {
            dataListener.saveStart(cls);
        }
        try {
            for (int i = 0; i < supportDataClasses.length; i++) {
                if (supportDataClasses[i] == cls) {
                    Element root = new Element(dataRootTags[i]);
                    Document doc = new Document(root);
                    for (DataObjectCategory cate : (List<DataObjectCategory>)dataCateLists[i]) {
                        for (DataObject obj : cate.objects) {
                            root.addContent(obj.save());
                        }
                    }
                    Utils.saveDOM(doc, new File(baseDir, dataFiles[i]));
                }
            }
        } finally {
            if (dataListener != null) {
                dataListener.saveEnd(cls);
            }
        }
    }
    
    /**
     * 找出依赖于指定对象的所有相关对象。
     * @param objs
     * @return
     */
    public List<DataObject> findRelateObjects(Object[] objs) {
        List<DataObject> ret = new ArrayList<DataObject>();
        for (int i = 0; i < dataLists.length; i++) {
            for (DataObject obj : dataLists[i]) {
                for (int j = 0; j < objs.length; j++) {
                    if (obj.depends((DataObject)objs[j])) {
                        ret.add(obj);
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 载入项目。项目支持的所有数据对象都会被载入。载入的顺序是supportDataClasses的反序，所以，如果一类
     * 数据依赖于另一类数据，则被依赖的数据要放在数组的后面。
     * @param dir
     * @throws Exception
     */
    public void load(File dir) throws Exception {
        baseDir = dir;
        
        // 载入配置
        config = new Properties();
        try {
            Document configDoc = Utils.loadDOM(new File(baseDir, "config.xml"));
            List l = configDoc.getRootElement().getChildren();
            for (int i = 0; i < l.size(); i++) {
                if (l.get(i) instanceof Element) {
                    Element elem = (Element)l.get(i);
                    String configName = elem.getName();
                    String configValue = elem.getTextTrim();
                    config.setProperty(configName, configValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 载入字典数据
        for (List<DataObject> l : dictDataLists) {
            l.clear();
        }
        for (int i = dictDataClasses.length - 1; i >= 0; i--) {
            Document doc = Utils.loadDOM(new File(baseDir, dictDataFiles[i]));
            List list = doc.getRootElement().getChildren(dictDataTags[i]);
            for (Object elem : list) {
                DataObject newObj = (DataObject)dictDataClasses[i].newInstance();
                newObj.load((Element)elem);
                dictDataLists[i].add(newObj);
            }
            
            // 如果是服务器模式，进行排序以便查找
            if (this.serverMode) {
                Collections.sort(dictDataLists[i]);
            }
        }
        npcTemplateConfig = new NPCTemplateConfig(new File(baseDir, "NPCTemplates/config.xml"));
        jewelConfig = new JewelConfig(this);
        
        attendantConfig = new AttendantConfig(new File(baseDir, "Attendant/config.xml"));
        
        // 载入可编辑数据
        for (List<DataObject> l : dataLists) {
            l.clear();
        }
        for (List<DataObjectCategory> l : dataCateLists) {
            l.clear();
        }
        for (int i = supportDataClasses.length - 1; i >= 0; i--) {
            Document doc = Utils.loadDOM(new File(baseDir, dataFiles[i]));
            List list = doc.getRootElement().getChildren(dataTags[i]);
            HashMap<String, DataObjectCategory> cateMap = new HashMap<String, DataObjectCategory>();
            
            // 创建缺省分类
            DataObjectCategory emptyCate = new DataObjectCategory(supportDataClasses[i]);
            emptyCate.name = "";
            cateMap.put("", emptyCate);
            dataCateLists[i].add(emptyCate);
            
            for (Object elem : list) {
                Constructor cons = supportDataClasses[i].getConstructor(ProjectData.class);
                DataObject newObj = (DataObject)cons.newInstance(this);
                newObj.load((Element)elem);
                dataLists[i].add(newObj);
                if (this.serverMode) {
                    newObj.editorIndex = dataLists[i].size() - 1;
                }
                
                // 加入分类列表中
                DataObjectCategory cate = cateMap.get(newObj.categoryName);
                if (cate == null) {
                    cate = new DataObjectCategory(supportDataClasses[i]);
                    cate.name = newObj.categoryName;
                    cateMap.put(cate.name, cate);
                    dataCateLists[i].add(cate);
                }
                cate.objects.add(newObj);
            }
            
            // 如果是服务器模式，进行排序以便查找
            if (this.serverMode) {
                Collections.sort(dataLists[i]);
            }
        }
        
        skillIcon = new PipImage();
        skillIcon.load(new File(baseDir, "client_res/ability.pip").getAbsolutePath());
        
        skillIcon2 = new PipImage();
        skillIcon2.load(new File(baseDir, "client_res/ability_new.pip").getAbsolutePath());
        
        weaponIcon = new PipImage();
        weaponIcon.load(new File(baseDir, "client_res/weapon.pip").getAbsolutePath());
        
        talismanIcon = new PipImage();
        talismanIcon.load(new File(baseDir, "client_res/talisman.pip").getAbsolutePath());
        
        cmccConfig = new CmccConfig(new File(baseDir, "cmcc_config.xml"));
        
        // 服务器模式下，所有嵌套的掉落组摊平到一层以提高效率
        if (this.serverMode) {
            // Light(20100724): 因为增加了掉落组的valid标志，并希望可以由活动来修改掉落组的valid状态，所以
            //   不能再使用摊平优化算法了。
//            List<DataObject> dgs = getDataListByType(DropGroup.class);
//            for (DataObject dobj : dgs) {
//                ((DropGroup)dobj).makeFlat();
//            }
        }
        
        // 服务器模式下，载入文件版本配置文件
        if (this.serverMode) {
            loadResourceVersions();
        }
        
        // 服务器模式下，构建所有场景的通达关系表以支持自动寻路功能
        if (this.serverMode && this.createPathFinder) {
            pathFinder = new AutoPathFinder(this);
        }
        
        // 服务器模式下读入客户端资源配置文件
        if(this.serverMode){
            clientData = new ClientData(this, baseDir, branch);
        }
        
        // 临时代码：比较Animations目录下的所有pip文件，消除重复文件引用
//        try {
//            File aniDir = new File(baseDir, "Animations/2x");
//            File[] arr = aniDir.listFiles();
//            HashMap<String, PipImage> standards = new HashMap<String, PipImage>();
//            HashMap<String, PipAnimateSet> animateSets = new HashMap<String, PipAnimateSet>();
//            HashSet<String> changedAnimates = new HashSet<String>();
//            for (File f : arr) {
//                String fname = f.getName();
//                if (fname.endsWith(".pip")) {
//                    if (!Character.isDigit(fname.charAt(0))) {
//                        PipImage img = new PipImage();
//                        img.load(f.getAbsolutePath());
//                        standards.put(fname, img);
//                    }
//                }
//                if (fname.endsWith(".cts")) {
//                    PipAnimateSet as = new PipAnimateSet();
//                    as.load(f);
//                    animateSets.put(f.getName(), as);
//                }
//            }
//            for (File f : arr) {
//                String fname = f.getName();
//                if (fname.endsWith(".pip")) {
//                    if (Character.isDigit(fname.charAt(0))) {
//                        // 比较
//                        PipImage img = new PipImage();
//                        img.load(f.getAbsolutePath());
//                        for (String key : standards.keySet()) {
//                            PipImage img2 = standards.get(key);
//                            double matchRate = img.compare(img2);
//                            if (matchRate > 0.95) {
//                                System.out.println(f.getName() + " matchs " + key + " " + matchRate);
//                                
//                                // 把所有引用的cts中引用此文件修改为引用匹配的文件
//                                for (String key2 : animateSets.keySet()) {
//                                    PipAnimateSet as = animateSets.get(key2);
//                                    for (int i = 0; i < as.getFileCount(); i++) {
//                                        if (as.getFileName(i).equals(f.getName())) {
//                                            as.setFileName(i, key);
//                                            changedAnimates.add(key2);
//                                        }
//                                    }
//                                }
//                                
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//            
//            for (String asname : changedAnimates) {
//                PipAnimateSet as = animateSets.get(asname);
//                as.save(new File(baseDir, "Animations/2x/" + asname), true);
//                asname = asname.replaceAll("\\.cts$", "\\.ctn");
//                as.save(new File(baseDir, "Animations/2x/" + asname), false);
//                System.out.println("save " + asname);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        
      // 临时代码：所有带半透明的PIP都不能使用合并模式
//      File aniDir = new File(baseDir, "Animations/2x");
//      File[] arr = aniDir.listFiles();
//      for (File f : arr) {
//          String fname = f.getName();
//          if (fname.endsWith(".pip")) {
//              PipImage pi = new PipImage();
//              pi.load(f.getAbsolutePath());
//              if (pi.isMergeMode() && pi.hasHalfTransparent()) {
//                  pi.setMergeMode(false);
//                  pi.save(f);
//                  System.out.println("process: " + fname);
//              }
//          }
//      }
        
        // 临时代码：删除所有没有用到的cts文件
//        File aniDir = new File(baseDir, "Animations/2x");
//        HashSet<String> ctss = new HashSet<String>();
//        List<DataObject> list = getDataListByType(Animation.class);
//        for (DataObject dobj : list) {
//            Animation ani = (Animation)dobj;
//            ctss.add(ani.largeSource.getName());
//        }
//        File[] arr = aniDir.listFiles();
//        for (File f : arr) {
//            String fname = f.getName();
//            if (fname.endsWith(".cts") && !ctss.contains(fname) && Character.isDigit(fname.charAt(0))) {
//                new File(aniDir, fname).delete();
//                new File(aniDir, fname.substring(0, fname.length() - 1) + "n").delete();
//                System.out.println("delete: " + fname);
//            }
//        }
        
        // 临时代码：删除所有没有用到的pip文件
//        HashSet<String> pips = new HashSet<String>();
//        for (DataObject dobj : list) {
//            Animation ani = (Animation)dobj;
//            PipAnimateSet pas = new PipAnimateSet();
//            pas.load(ani.largeSource);
//            for (int i = 0; i < pas.getFileCount(); i++) {
//                pips.add(pas.getFileName(i));
//            }
//        }
//        arr = aniDir.listFiles();
//        for (File f : arr) {
//            String fname = f.getName();
//            if (fname.endsWith(".pip") && !pips.contains(fname) && Character.isDigit(fname.charAt(0))) {
//                new File(aniDir, fname).delete();
//                System.out.println("delete: " + fname);
//            }
//        }
        
        // 临时代码：分目录导出所有NPC图片资源
//        List<DataObject> anis = getDataListByType(Animation.class);
//        File targetDir = new File("d:/temp");
//        Map<Integer, GameAreaInfo> areaInfos = new HashMap<Integer, GameAreaInfo>();
//        for (DataObject dobj : getDataListByType(GameArea.class)) {
//            GameArea ga = (GameArea)dobj;
//            if (ga.title.startsWith("(TEST)")) {
//                continue;
//            }
//            GameAreaInfo gai = new GameAreaInfo(ga);
//            gai.load();
//            areaInfos.put(ga.id, gai);
//        }
//        for (DataObject dobj: anis) {
//            Animation ani = (Animation)dobj;
//            
//            // 搜索这个动画最早被用到了什么地方
//            int firstUsage = -1;
//            for (int areaID = 1; areaID < 100; areaID++) {
//                GameAreaInfo gai = areaInfos.get(areaID);
//                if (gai == null) {
//                    continue;
//                }
//                for (GameMapInfo gmi : gai.maps) {
//                    for (GameMapObject gmo : gmi.objects) {
//                        if (gmo instanceof GameMapNPC) {
//                            GameMapNPC npc = (GameMapNPC)gmo;
//                            if (npc.template.image.id == ani.id) {
//                                firstUsage = areaID;
//                                break;
//                            }
//                        }
//                    }
//                    if (firstUsage != -1) {
//                        break;
//                    }
//                }
//                if (firstUsage != -1) {
//                    break;
//                }
//            }
//            if (firstUsage == -1) {
//                continue;
//            }
//            
//            // 创建一个新目录拷贝文件
//            GameArea ga = (GameArea)findObject(GameArea.class, firstUsage);
//            String path = ga.id + " - " + ga.title + "/" + ani.id + " - " + ani.title;
//            File path1 = new File(targetDir, path);
//            path1.mkdirs();
//            
//            PipAnimateSet as = new PipAnimateSet();
//            as.load(ani.source);
//            Utils.copyFile(ani.source, new File(path1, ani.source.getName()));
//            String ctnName = ani.source.getName().substring(0, ani.source.getName().length() - 1) + "n";
//            Utils.copyFile(new File(ani.source.getParentFile(), ctnName), new File(path1, ctnName));
//            for (int kk = 0; kk < as.getFileCount(); kk++) {
//                Utils.copyFile(new File(ani.source.getParentFile(), as.getFileName(kk)), new File(path1, as.getFileName(kk)));
//            }
//            
//            path1 = new File(path1, "2x");
//            path1.mkdirs();
//            as = new PipAnimateSet();
//            as.load(ani.largeSource);
//            Utils.copyFile(ani.largeSource, new File(path1, ani.largeSource.getName()));
//            ctnName = ani.largeSource.getName().substring(0, ani.largeSource.getName().length() - 1) + "n";
//            Utils.copyFile(new File(ani.largeSource.getParentFile(), ctnName), new File(path1, ctnName));
//            for (int kk = 0; kk < as.getFileCount(); kk++) {
//                Utils.copyFile(new File(ani.largeSource.getParentFile(), as.getFileName(kk)), new File(path1, as.getFileName(kk)));
//            }
//        }
//        NPCTemplate t = new NPCTemplate(this);
//        for (int clazz = 0; clazz < 4; clazz++) {
//            for (int level = 1; level <= 70; level++) {
//                t.clazz = clazz;
//                t.level = level;
//                System.out.print("\t<levelconfig class=\"" + clazz + "\" level=\"" + level + "\"");
//                System.out.print(" hp=\"" + t.getStandardHP() + "\"");
//                System.out.print(" mp=\"" + t.getStandardMP() + "\"");
//                System.out.print(" armor=\"" + t.getStandardArmor() + "\"");
//                System.out.print(" magicarmor=\"" + t.getStandardMagicArmor() + "\"");
//                System.out.print(" sta=\"" + t.getStandardSTA() + "\"");
//                System.out.print(" str=\"" + t.getStandardSTR() + "\"");
//                System.out.print(" agi=\"" + t.getStandardAGI() + "\"");
//                System.out.print(" int=\"" + t.getStandardINT() + "\"");
//                System.out.print(" ap1=\"" + t.getStandardWeaponAP1() + "\"");
//                System.out.print(" ap2=\"" + t.getStandardWeaponAP2() + "\"");
//                System.out.print(" magicap=\"" + t.getStandardWeaponMagicAP() + "\"");
//                System.out.print(" exp=\"" + t.getStandardExp() + "\"");
//                System.out.print(" money=\"" + t.getStandardMoney() + "\"");
//                System.out.println(" />");
//            }
//        }
        
        // 临时代码，整理所有NPC名称
//        List<DataObject> gameAreas = getDataListByType(GameArea.class);
//        HashSet<String> namesSet = new HashSet<String>();
//        for (DataObject dobj : gameAreas) {
//            GameArea ga = (GameArea)dobj;
//            GameAreaInfo gai = new GameAreaInfo(ga);
//            gai.load();
//            for (GameMapInfo gmi : gai.maps) {
//                for (GameMapObject gmo : gmi.objects) {
//                    if (!(gmo instanceof GameMapNPC)) {
//                        continue;
//                    }
//                    namesSet.add(((GameMapNPC)gmo).name);
//                }
//            }
//        }
//        for (String s : namesSet) {
//            System.out.println(s);
//        }
    }
    
    /**
     * 载入文件版本号配置文件，从数据目录的fileversion.xml里载入。
     */
    public void loadResourceVersions() throws Exception {
        Document doc = Utils.loadDOM(new File(baseDir, "fileversion.xml"));
        List list = doc.getRootElement().getChildren("file");
        Hashtable<String, Integer> ret = new Hashtable<String, Integer>();
        for (Object obj : list) {
            Element elem = (Element)obj;
            String fileName = elem.getAttributeValue("path");
            int fileVersion = Integer.parseInt(elem.getAttributeValue("version"));
            ret.put(fileName, fileVersion);
        }
        resourceVersion = ret;
        resourceCache.clear();
        downloadFileMapping.clear();
    }
    
    public void makeWorldMapPackages(File mapf){
        try {
            //载入世界地图数据文件
            MapFile mapFile = new MapFile();
            mapFile.load(mapf);
            //生成世界地图下载文件
            PackageFile wmtemp = new PackageFile();
            PackageUtils.makeClientPackage(mapFile, wmtemp);
            wmtemp.save(new File(mapf.getParent(), "worldMap.data"));
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 为所有关卡生成客户端下载文件。
     */
    public void makeClientPackages() {
        for (DataObject obj : getDataListByType(GameArea.class)) {
            try {
                System.out.println("导出地区" + obj.id);
                GameArea ga = (GameArea)obj;
                MapFile mapFile = new MapFile();
                File mapf = new File(ga.source, "game.map");
                mapFile.load(mapf);
                MapFile mapFileLarge = new MapFile();
                File mapfl = new File(ga.source, "game_l.map");
                if (mapfl.exists()) {
                    mapFileLarge.load(mapfl);
                } else {
                    mapFileLarge.load(mapf);
                    mapFileLarge.enlarge();
                }
                GameAreaInfo areaInfo = new GameAreaInfo(ga);
                if (new File(ga.source, "info.xml").exists()) {
                    areaInfo.load();
                } else {
                    areaInfo.save();
                }

                PackageFile pkgtemp = new PackageFile();
                pkgtemp.setName(String.valueOf(ga.id));
                pkgtemp.setVersion(0);
                PackageUtils.makeClientPackage(ga, mapFile, areaInfo, pkgtemp, 1.0f);
                pkgtemp.save(new File(ga.source, "client.pkg"));
                
                pkgtemp = new PackageFile();
                pkgtemp.setName(String.valueOf(ga.id));
                pkgtemp.setVersion(0);
                PackageUtils.makeClientPackage(ga, mapFileLarge, areaInfo, pkgtemp, 2.0f);
                pkgtemp.save(new File(ga.source, "client_l.pkg"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
        
    /**
     * 为所有BUFF对象生成Java Class文件。
     * @throws Exception
     */
    public void generateBuffClasses(String encoding) throws Exception {
        List<DataObject> buffs = getDataListByType(BuffConfig.class);
        File clsDir = new File(Settings.exportClassDir, Settings.buffPackage.replace('.', '/'));
        
        // 生成Java文件
        for (DataObject o : buffs) {
            BuffConfig bc = (BuffConfig)o;
            File jf = new File(clsDir, bc.getClassName(Settings.buffClassPrefix) + ".java");
            FileOutputStream fos = new FileOutputStream(jf);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, encoding));
            System.out.println("process buff : " + bc.id);
            bc.generateJava(pw, Settings.buffPackage, Settings.buffClassPrefix);
            pw.close();
        }
        
        // 保存buffs.xml
        this.saveDataList(BuffConfig.class);
    }
    
    /**
     * 为所有Skill对象生成Java Class文件。
     * @throws Exception
     */
    public void generateSkillClasses(String encoding) throws Exception {
        List<DataObject> skills = getDataListByType(SkillConfig.class);
        File clsDir = new File(Settings.exportClassDir, Settings.skillPackage.replace('.', '/'));
        
        // 生成Java文件
        for (DataObject o : skills) {
            try {
                SkillConfig bc = (SkillConfig)o;
                File jf = new File(clsDir, bc.getClassName(Settings.skillClassPrefix) + ".java");
                FileOutputStream fos = new FileOutputStream(jf);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, encoding));
                System.out.println("process skill : " + bc.id);
                bc.generateJava(pw, Settings.skillPackage, Settings.skillClassPrefix);
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        
        // 保存skills.xml
        this.saveDataList(SkillConfig.class);
    }
    
    /**
     * 生成所有场景的列表
     * @throws Exception
     */
    public String generateMapList() throws Exception {
        List<DataObject> areas = getDataListByType(GameArea.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < areas.size(); i++) {
            GameArea ga = (GameArea)areas.get(i);
            GameAreaInfo areaInfo = new GameAreaInfo(ga);
            areaInfo.load();
            MapFile mf = new MapFile();
            File mapf = new File(ga.source, "game.map");
            mf.load(mapf);
            for (GameMapInfo gmi : areaInfo.maps) {
                sb.append(gmi.getGlobalID());
                sb.append("\t");
                sb.append(gmi.name);
                sb.append("\t");
                sb.append(mf.getMaps().get(gmi.id).width);
                sb.append("\t");
                sb.append(mf.getMaps().get(gmi.id).height);
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * 更新所有任务文本中的NPC和地图引用。
     * @throws Exception
     */
    public void validateMixedText() throws Exception {
        validateMixedText(true);
    }
    public void validateMixedText(boolean reportException) throws Exception {
        List<DataObject> quests = getDataListByType(Quest.class);
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = (Quest)quests.get(i);
            try {
                quest.validateMixedText();
            } catch (Exception e) {
                if (reportException) {
                    throw e;
                } else {
                    System.err.println(e);
                }
            }
        }
        saveDataList(Quest.class);
    }
    
    /**
     * 根据公式自动更新所有装备的价格和耐久度属性。
     * @throws Exception
     */
    public void updateEquipmentPrices() throws Exception {
        // 更新装备表
        List<DataObject> equiList = getDataListByType(Equipment.class);
        for (DataObject equi : equiList) {
            ((Equipment)equi).recalcPriceAndDurability();
        }
        saveDataList(Equipment.class);
        
        // 更新商店中的引用
        List<DataObject> shops = getDataListByType(Shop.class);
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = (Shop)shops.get(i);
            for (ShopItem si : shop.items) {
                if (si.item instanceof Equipment) {
                    for (Shop.BuyRequirement br : si.requirements) {
                        if (br.type == Shop.TYPE_MONEY && br.deduct) {
                            br.amount = si.item.price * 2;
                        }
                    }
                }
            }
        }
        saveDataList(Shop.class);
    }
    
    /**
     * 加入fileversion是排序，这样方便比较和上次做fileversion的差异
     * @param list
     * @param elem
     */
    private void addToList(List<Element> list,Element elem){
        int start = -1;
        int end = list.size();
        int mid = end;
        while(end - start > 1){
            mid = (start + end) / 2;
            Element e = (Element)list.get(mid);
            if(elem.getAttributeValue("path").compareTo(e.getAttributeValue("path")) < 0){
                end = mid;
            } else {
                start = mid;
            }
        }
        list.add(end, elem);
    }
    
    public static void addToList(List<FileInfo> list,FileInfo file){
        int start = -1;
        int end = list.size();
        int mid = end;
        while(end - start > 1){
            mid = ((start + end) >> 1);
            FileInfo e = (FileInfo)list.get(mid);
            if(file.src.getName().compareTo(e.src.getName()) < 0){
                end = mid;
            } else {
                start = mid;
            }
        }
        list.add(end, file);
    }
    
    /**
     * 非废弃地图
     */
    private static void loadValidMapIds(){
        validMapIds.put(2176,2176);
        validMapIds.put(1442,1442);
        validMapIds.put(129,129);
        validMapIds.put(256,256);
        validMapIds.put(272,272);
        validMapIds.put(96,96);
        validMapIds.put(112,112);
        validMapIds.put(145,145);
        validMapIds.put(161,161);
        validMapIds.put(400,400);
        validMapIds.put(401,401);
        validMapIds.put(545,545);
        validMapIds.put(673,673);
        validMapIds.put(657,657);
        validMapIds.put(2177,2177);
        validMapIds.put(1410,1410);
        validMapIds.put(80,80);
        validMapIds.put(224,224);
        validMapIds.put(240,240);
        validMapIds.put(33,33);
        validMapIds.put(34,34);
        validMapIds.put(35,35);
        validMapIds.put(17,17);
        validMapIds.put(177,177);
        validMapIds.put(528,528);
        validMapIds.put(529,529);
        validMapIds.put(481,481);
        validMapIds.put(609,609);
        validMapIds.put(625,625);
        validMapIds.put(2178,2178);
        validMapIds.put(1426,1426);
        validMapIds.put(337,337);
        validMapIds.put(368,368);
        validMapIds.put(352,352);
        validMapIds.put(305,305);
        validMapIds.put(320,320);
        validMapIds.put(289,289);
        validMapIds.put(417,417);
        validMapIds.put(496,496);
        validMapIds.put(560,560);
        validMapIds.put(561,561);
        validMapIds.put(593,593);
        validMapIds.put(689,689);
        validMapIds.put(848,848);
        validMapIds.put(849,849);
        validMapIds.put(1008,1008);
        validMapIds.put(816,816);
        validMapIds.put(832,832);
        validMapIds.put(833,833);
        validMapIds.put(896,896);
        validMapIds.put(1024,1024);
        validMapIds.put(880,880);
        validMapIds.put(1056,1056);
        validMapIds.put(2032,2032);
        validMapIds.put(944,944);
        validMapIds.put(2016,2016);
        validMapIds.put(2000,2000);
        validMapIds.put(2001,2001);
        validMapIds.put(752,752);
        validMapIds.put(912,912);
        validMapIds.put(928,928);
        validMapIds.put(1072,1072);
        validMapIds.put(1088,1088);
        validMapIds.put(1104,1104);
        validMapIds.put(1152,1152);
        validMapIds.put(1232,1232);
        validMapIds.put(1248,1248);
        validMapIds.put(1168,1168);
        validMapIds.put(1184,1184);
        validMapIds.put(1200,1200);
        validMapIds.put(1216,1216);
        validMapIds.put(1728,1728);
        validMapIds.put(1729,1729);
        validMapIds.put(1730,1730);
        validMapIds.put(1731,1731);
        validMapIds.put(1732,1732);
        validMapIds.put(1733,1733);
        validMapIds.put(1734,1734);
        validMapIds.put(2064,2064);
        validMapIds.put(1840,1840);
        validMapIds.put(1616,1616);
        validMapIds.put(1712,1712);
        validMapIds.put(1776,1776);
        validMapIds.put(1792,1792);
        validMapIds.put(1936,1936);
        validMapIds.put(1952,1952);
        validMapIds.put(1966,1966);
        validMapIds.put(1984,1984);
        validMapIds.put(1872,1872);
        validMapIds.put(1888,1888);
        validMapIds.put(1904,1904);
        validMapIds.put(1920,1920);
        validMapIds.put(1680,1680);
        validMapIds.put(1696,1696);
        validMapIds.put(1760,1760);
        validMapIds.put(1763,1763);
        validMapIds.put(1857,1857);
        validMapIds.put(1858,1858);
        validMapIds.put(1859,1859);
        validMapIds.put(1633,1633);
        validMapIds.put(1648,1648);
        validMapIds.put(1650,1650);
        validMapIds.put(1664,1664);
        validMapIds.put(1665,1665);
        validMapIds.put(1744,1744);
        validMapIds.put(1745,1745);
        validMapIds.put(1808,1808);
        validMapIds.put(1809,1809);
        validMapIds.put(448,448);
        validMapIds.put(450,450);
        validMapIds.put(432,432);
        validMapIds.put(433,433);
        validMapIds.put(768,768);
        validMapIds.put(769,769);
        validMapIds.put(770,770);
        validMapIds.put(771,771);
        validMapIds.put(772,772);
        validMapIds.put(773,773);
        validMapIds.put(1137,1137);
        validMapIds.put(1138,1138);
        validMapIds.put(1139,1139);
        validMapIds.put(1140,1140);
        validMapIds.put(1141,1141);
        validMapIds.put(976,976);
        validMapIds.put(977,977);
        validMapIds.put(978,978);
        validMapIds.put(1344,1344);
        validMapIds.put(1345,1345);
        validMapIds.put(1346,1346);
        validMapIds.put(1328,1328);
        validMapIds.put(2048,2048);
        validMapIds.put(2080,2080);
        validMapIds.put(2096,2096);
        validMapIds.put(736,736);
        validMapIds.put(737,737);
        validMapIds.put(784,784);
        validMapIds.put(785,785);
        validMapIds.put(800,800);
        validMapIds.put(801,801);
        validMapIds.put(1264,1264);
        validMapIds.put(1280,1280);
        validMapIds.put(1296,1296);
        validMapIds.put(1361,1361);
        validMapIds.put(1488,1488);
        validMapIds.put(1504,1504);
        validMapIds.put(1520,1520);
        validMapIds.put(1536,1536);
        validMapIds.put(1568,1568);
        validMapIds.put(2160,2160);
        validMapIds.put(2192,2192);
        validMapIds.put(2208,2208);
        validMapIds.put(512,512);
        validMapIds.put(513,513);
    }
    
    /**
     * 30级前的非废弃地图
     */
    private static void loadValidMapIdsLevel(){
        validMapIdsLevel.put(2176,2176);
        validMapIdsLevel.put(1442,1442);
        validMapIdsLevel.put(129,129);
        validMapIdsLevel.put(256,256);
        validMapIdsLevel.put(272,272);
        validMapIdsLevel.put(96,96);
        validMapIdsLevel.put(112,112);
        validMapIdsLevel.put(145,145);
        validMapIdsLevel.put(161,161);
        validMapIdsLevel.put(400,400);
        validMapIdsLevel.put(401,401);
        validMapIdsLevel.put(545,545);
        validMapIdsLevel.put(673,673);
        validMapIdsLevel.put(657,657);
        validMapIdsLevel.put(2177,2177);
        validMapIdsLevel.put(1410,1410);
        validMapIdsLevel.put(80,80);
        validMapIdsLevel.put(224,224);
        validMapIdsLevel.put(240,240);
        validMapIdsLevel.put(33,33);
        validMapIdsLevel.put(34,34);
        validMapIdsLevel.put(35,35);
        validMapIdsLevel.put(17,17);
        validMapIdsLevel.put(177,177);
        validMapIdsLevel.put(528,528);
        validMapIdsLevel.put(529,529);
        validMapIdsLevel.put(481,481);
        validMapIdsLevel.put(609,609);
        validMapIdsLevel.put(625,625);
        validMapIdsLevel.put(2178,2178);
        validMapIdsLevel.put(1426,1426);
        validMapIdsLevel.put(337,337);
        validMapIdsLevel.put(368,368);
        validMapIdsLevel.put(352,352);
        validMapIdsLevel.put(305,305);
        validMapIdsLevel.put(320,320);
        validMapIdsLevel.put(289,289);
        validMapIdsLevel.put(417,417);
        validMapIdsLevel.put(496,496);
        validMapIdsLevel.put(560,560);
        validMapIdsLevel.put(561,561);
        validMapIdsLevel.put(593,593);
        validMapIdsLevel.put(689,689);
        validMapIdsLevel.put(2192,2192);
    }
    
    private static void loadValidMapIdsSmall(){
        validMapIdsSmall.put(2176,2176);
    }
    
    /**
     * 扫描项目目录，统计所有资源文件的版本号，写成一个XML文件。
     * @throws Exception
     */
    public void generateResourceVersionXML() throws Exception {
        //生成内置资源包
        serverMode = true;
        load(baseDir);
            //生成全资源包(大包)
        generatePackDotData(ProjectData.PACK_DATA_NPC|ProjectData.PACK_DATA_MAP|ProjectData.PACK_DATA_MUSIC|ProjectData.PACK_DATA_SCRIPTS,ProjectData.PACK_TYPE_BIG);
            //生成部分资源包(中包)
        generatePackDotData(ProjectData.PACK_DATA_NPC|ProjectData.PACK_DATA_MAP,ProjectData.PACK_TYPE_MIDIUM);
            //生成部分资源包(小包)
        generatePackDotData(ProjectData.PACK_DATA_NPC,ProjectData.PACK_TYPE_SMALL);
        //生成fileversion.xml
        Element root = new Element("files");
        Document doc = new Document(root);
        List<String> stack = new ArrayList<String>();
        stack.add("");
        List<Element> cache = new ArrayList<Element>();
        HashMap<File, Integer> allVersions = new HashMap<File, Integer>();
        while (stack.size() > 0) {
            String path = stack.remove(0);
            File dirFile = path.length() == 0 ? baseDir : new File(baseDir, path);
            File[] childs = dirFile.listFiles();
            for (File child : childs) {
                String cname = child.getName();
                String relatePath;
                if (path.length() > 0) {
                    relatePath = path + "/" + cname;
                } else {
                    relatePath = cname;
                }
                if (child.isDirectory()) {
                    stack.add(relatePath);
                } else if (cname.endsWith(".etf.gz") || cname.endsWith(".ctn") || 
                        cname.endsWith(".pip") || cname.endsWith(".png") ||
                        cname.endsWith(".pkg") || cname.endsWith(".data") ||
                        cname.endsWith(".mid") || cname.endsWith(".mp3")
                        ) {
                    // 如果是ETF、CTN或者PIP文件，直接取这个文件在CVS中的文件名
                    if(relatePath.indexOf("client_pkg") == -1 || cname.equals("pkg.data")){
                        Element elem = new Element("file");
                        elem.addAttribute("path", relatePath);
                        
                        // channel.data特殊处理，取CRC版本号
                        if (cname.equals("channel.data") || ((relatePath.indexOf("NewUI_AndroidLarge")!=-1 || relatePath.indexOf("NewUI_iOSLarge")!=-1) && (cname.endsWith(".etf.gz") || (cname.startsWith("pack_") && cname.endsWith(".data"))))) {
                            int crcVersion = getFileCRCVersion(child);
                            elem.addAttribute("version", String.valueOf(crcVersion));
                            allVersions.put(child, crcVersion);
                        } else {
                            int cvsVersion = getFileCVSVersion(child);
                            elem.addAttribute("version", String.valueOf(cvsVersion));
                            allVersions.put(child, cvsVersion);
                        }
                        addToList(cache, elem);
                    }
                }
            }
        }
        
        //把排序后的fileversion放入root
        for (int i = 0; i < cache.size(); i++) {
            Element e = cache.get(i);
            root.addContent(e);
        }

        // 生成所有branch的client_pkg文件
        ClientData clientData = new ClientData(this, baseDir, allVersions, null);
        clientData.makeClientData();
        clientData.makePkgData();
        if (new File(baseDir, "Branches") != null) {
            File[] brs = new File(baseDir, "Branches").listFiles();
            if (brs != null) {
                for (File br : brs) {
                    if (!br.isDirectory()) {
                        continue;
                    }
                    if (br.getName().equals("CVS")) {
                        continue;
                    }
                    clientData = new ClientData(this, baseDir, allVersions, br.getName());
                    clientData.makeClientData();
                    clientData.makePkgData();
                }
            }
        }
        
        Utils.saveDOM(doc, new File(baseDir, "fileversion.xml"));
    }
    
    /**
     * 在线程中做fileversion
     */
    public void generateResourceVersionXMLInThread(Shell shell) {
        ThreadFileversion tf = new ThreadFileversion(shell,this);
        Thread thread = new Thread(tf);
        thread.start();
    }
    
    /**
     * npc资源
     */
    public static final int PACK_DATA_NPC = 1;
    /**
     * 关卡资源
     */
    public static final int PACK_DATA_MAP = (1 << 1);
    /**
     * 音乐资源
     */
    public static final int PACK_DATA_MUSIC = (1 << 2);
    /**
     * 脚本资源
     */
    public static final int PACK_DATA_SCRIPTS = (1 << 3);
    
    /**
     * 资源包（大），包括npc,map,mp2,gz
     */
    public static final int PACK_TYPE_BIG = 0;
    /**
     * 资源包（中），包括npc,map,mp2,gz
     */
    public static final int PACK_TYPE_MIDIUM = 1;
    /**
     * 资源包（小），包括第一个关卡的npc
     */
    public static final int PACK_TYPE_SMALL = 2;
    
    /**
     * 有效地图
     */
    public static final HashMap<Integer,Integer> validMapIds = new HashMap<Integer,Integer>();
    
    /**
     * 前30级有效地图
     */
    public static final HashMap<Integer,Integer> validMapIdsLevel = new HashMap<Integer,Integer>();
    /**
     * 小包内置npc
     */
    public static final HashMap<Integer,Integer> validMapIdsSmall = new HashMap<Integer,Integer>();
    class FileInfo {
    	public FileInfo(File src,String name){
    		this.src = src;
    		this.name = name;
    	}
    	File src;
    	String name;
    	@Override
    	public String toString() {
    	    return this.src.getName()+this.name;
    	}
    }
    
    /**
     * 是否是client_pkg.xml定义的资源
     * @return
     */
    public boolean isClientData(String name,String uimodel){
        boolean ret = false;
        Hashtable<String, Boolean> clientNeedTable = getAllClientNeedTable(uimodel);
        if(clientNeedTable != null){
            ret = clientNeedTable.containsKey(name);
        }
        return ret;
    }
    
    public static final String UIMODEL_IOSLARGE = "NewUI_iOSLarge";
    public static final String UIMODEL_ANDROIDLARGE = "NewUI_AndroidLarge";
    
    /**
     * 生成新界面内置资源大包pack.data
     * @throws Exception
     */
    public void generatePackDotData(int dataTypes,int packType) throws Exception {
        File head = baseDir;
        HashMap mapIds = null;
        if(packType == PACK_TYPE_BIG){
            if(validMapIds.size() == 0){
                loadValidMapIds();
            }
            mapIds = validMapIds;
        } else if(packType == PACK_TYPE_MIDIUM){
            if(validMapIdsLevel.size() == 0){
                loadValidMapIdsLevel();
            }
            mapIds = validMapIdsLevel;
        } else {
            if(validMapIdsSmall.size() == 0){
                loadValidMapIdsSmall();
            }
            mapIds = validMapIdsSmall;
        }
        String[] uiModels = null;
            uiModels = new String[]{UIMODEL_IOSLARGE,UIMODEL_ANDROIDLARGE};

        for (String uiModel : uiModels) {
            //ios只做大包
            if(uiModel.equals(UIMODEL_IOSLARGE) && packType != PACK_TYPE_BIG){
                continue;
            }
            if(baseDir.getParentFile().getAbsolutePath().indexOf("zh_TW")!=-1){//台湾版Android大包
                if(packType != PACK_TYPE_BIG){
                    continue;
                }
            }
            
            System.out.println("pack.data ------------------------- "+uiModel);
            
            List<FileInfo> fileList = new ArrayList<FileInfo>();
            
            int mapCount = 0;
            HashMap<String,String> exist = new HashMap<String,String>();
            List<GameArea> list = (List)getDataListByType(GameArea.class);
            for (GameArea area : list) {
                GameAreaInfo gai = area.getAreaInfo();
                boolean hasValidMap = false;
                if((dataTypes & PACK_DATA_NPC) != 0){
                    for (GameMapInfo gmi : gai.maps) {
                        if(!mapIds.containsKey(gmi.getGlobalID())){
                            continue;
                        }
                        hasValidMap = true;
                        for (GameMapObject gmo : gmi.objects) {
                            if(gmo instanceof GameMapNPC){
                                GameMapNPC gmn = (GameMapNPC)gmo;
                                
                                Animation animation = gmn.template.image;
                                
                                //该地图的NPC资源(ctn及pip)
                                File realFile = new File(animation.largeSource.getParentFile(),animation.largeSource.getName().substring(0,animation.largeSource.getName().length()-1) + "n");
                                
                                FileInfo fi = new FileInfo(realFile,String.valueOf(animation.id)+".ctn");
                                if(exist.containsKey(fi.toString())){
                                	continue;
                                }
                                exist.put(fi.toString(), "");
                                
                                addToList(fileList,fi);
                                
                                PipAnimateSet pas = new PipAnimateSet();
                                pas.load(animation.largeSource);
                                
                                for (int i = 0; i < pas.getFileCount(); i++) {
                                    File f = pas.getSourceFile(i);
                                    FileInfo ff = new FileInfo(f,f.getName());
                                    if(exist.containsKey(ff.toString())){
                                        continue;
                                    }
                                    exist.put(ff.toString(), "");
                                    addToList(fileList,ff);
                                }
                            }
                        }
                    }
                }
                
                //地图资源
                if((dataTypes & PACK_DATA_MAP) != 0){
                    if(hasValidMap){
                        File file = new File(area.source ,  "client_l.pkg");
                        if(!isClientData(String.valueOf(area.id) + ".pkg", uiModel)){
                            addToList(fileList, new FileInfo(file,String.valueOf(area.id) + ".pkg"));
                            mapCount++;
                        }
                    }
                }
            }
                
            System.out.println("---------map count:"+mapCount);
            
            
            if((dataTypes & PACK_DATA_MUSIC) != 0){
                int mp3Count = 0;
                //音乐(.mp3)
                File dir3 = new File(head ,  "Sounds/iPhone");
                File [] files3 = dir3.listFiles();
                for (File file : files3) {
                    if(file.isFile() && file.getName().endsWith(".mp3") && !isClientData(file.getName(), uiModel)){
                        addToList(fileList, new FileInfo(file,file.getName()));
                        mp3Count++;
                    }
                }
                File dir4 = new File(head ,  "Sounds");
                File [] files4 = dir4.listFiles();
                for (File file : files4) {
                    if(file.isFile() && file.getName().endsWith(".mp3") && !isClientData(file.getName(), uiModel)){
                    	addToList(fileList, new FileInfo(file,file.getName()));
                    	mp3Count++;
                    }
                }
                System.out.println("---------mp3 count:"+mp3Count);
            }
            
            
            //分机型资源
            if((dataTypes & PACK_DATA_SCRIPTS) != 0){
                //脚本
                File dir5 = new File(head ,  "scripts/"+uiModel);
                File [] files5 = dir5.listFiles();
                for (File file : files5) {
                    if(file.isFile() && file.getName().endsWith(".gz")){
                    	String name = file.getName();
                        name = file.getName().substring(0, name.indexOf(uiModel) - 1)+".etf";
                        if(!isClientData(name, uiModel)){
                            addToList(fileList, new FileInfo(file,name));
                        }
                    }
                }
                System.out.println("---------scripts:"+files5.length);
            }
            
            //坐骑资源
            if(packType != PACK_TYPE_SMALL){
                File dir6 = new File(head ,  "client_res/character/2x");
                File [] files6 = dir6.listFiles();
                int horseFileCount = 0;
                for (File file : files6) {
                    if(file.isFile() && file.getName().startsWith("horse") && (file.getName().endsWith(".ctn") || file.getName().endsWith(".pip"))){
                        String name = file.getName();
                        if(!isClientData(name, uiModel)){
                            addToList(fileList, new FileInfo(file,name));
                            horseFileCount++;
                        }
                    }
                }
                System.out.println("---------horse files:"+horseFileCount);
            }
            
            //世界地图
            if(packType != PACK_TYPE_SMALL){
                File dir7 = null;
                if(uiModel.equals("NewUI_AndroidLarge")){
                    dir7 = new File(head ,  "client_res/NewUI/worldmap.png");
                } else {
                    dir7 = new File(head ,  "client_res/NewUI/worldmap.pip");
                }
                String name = dir7.getName();
                if(!isClientData(name, uiModel)){
                    addToList(fileList, new FileInfo(dir7,name));
                }
                System.out.println("---------worldmap files:"+name);
                
            }
                
            //VIP表情
            File dir8 = new File(head,"client_res/NewUI/vipemotions");
            File[] files = dir8.listFiles();
            for (File file : files) {
                if(file.getName().endsWith(".pip") || file.getName().endsWith(".ctn")){
                    addToList(fileList,new FileInfo(file,file.getName()));
                    System.out.println("---------add emotion file:"+ file.getName());
                }
            } 

            //新logo
            if(!uiModel.equals(UIMODEL_IOSLARGE)){
                File dir9 = new File(head,"client_res/NewUI/logo");
                files = dir9.listFiles();
                for (File file : files) {
                    if(file.getName().endsWith(".pip") || file.getName().endsWith(".ctn")){
                        addToList(fileList,new FileInfo(file,file.getName()));
                        System.out.println("---------add logo file:"+ file.getName());
                    }
                } 
            }
            
            //写如pack.data
            File packData = new File(head,  "client_res/NewUI/pack_"+uiModel+dataTypes+".data");
            try {
                DataOutputStream pw = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(packData))));
                pw.writeInt(fileList.size());
                File destDir = null;
                if(baseDir.getParentFile().getAbsolutePath().indexOf("zh_TW")!=-1){
                    destDir = new File(head.getParentFile().getParentFile() ,  "newui_pack_zh_TW/"+uiModel+dataTypes);
                } else {
                    destDir = new File(head.getParentFile() ,  "newui_pack/"+uiModel+dataTypes);
                }
                if(destDir.exists()){
                    File[] fs = destDir.listFiles();
                    for (File file : fs) {
                        if(file.isFile()){
                            file.delete();
                        }
                    }
                } else {
                    destDir.mkdirs();
                }
                
                for (int i = 0; i < fileList.size(); i++) {
                	FileInfo fileInfo = fileList.get(i);
                    File file = fileInfo.src;
                    int fileVersion;
                    fileVersion = getFileCRCVersion(file);
                    String name = fileInfo.name;
                    
                    String content = name+" "+fileVersion;
                    pw.writeUTF(name);
                    pw.writeInt(fileVersion);
                    
                    System.out.println(content);
                    if(name.endsWith(".etf")){
                    	Utils.copyFile(file, new File(destDir,file.getName()));
                    } else {
                    	Utils.copyFile(file, new File(destDir,name));
                    }
                    
                    
                }
            
                pw.close();
                Utils.copyFile(packData, new File(destDir,packData.getName()));
                
                System.out.println(uiModel+" total " +fileList.size() +" files.");
                
                File filever = new File(packData.getParent(),packData.getName()+".ver");
                pw = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filever))));
                int packVersion = getFileCRCVersion(packData);
                pw.writeInt(packVersion);
                pw.flush();
                pw.close();
                Utils.copyFile(filever, new File(destDir,filever.getName()));
                System.out.println("pack data version:"+packVersion);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 打印pack.data
     * @param path
     */
    public static void printPackDotData(String path){
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(new File(path)))));
            int count = dis.readInt();
            System.out.println("file count: " +count);
            for (int i = 0; i < count; i++) {
                String name = dis.readUTF();
                int version = dis.readInt();
                System.out.println(name + " " +version);
            }
            dis.close();
        }
        catch (Exception e) {
            // TODO: handle exception
        }
        
    }
    
    /*
     * 取得一个文件在CVS中的版本号。
     */
    public static int getFileCVSVersion(File file) {
        long fileTm = file.lastModified();

        File entryFile = new File(file.getParentFile(), "CVS/Entries");
        try {
            String content = Utils.loadFileContent(entryFile);
            BufferedReader br = new BufferedReader(new StringReader(content));
            String line;
            String fname = file.getName();
            String prefix = "/" + fname + "/";
            while ((line = br.readLine()) != null) {
                if (line.startsWith(prefix)) {
                    line = line.substring(prefix.length());
                    int pos = line.indexOf('/');
                    
                    String versionStr = line.substring(0, pos);
                    line = line.substring(pos + 1);
                    
                    pos = versionStr.indexOf('.');
                    versionStr = versionStr.substring(pos + 1);
                    int version = Integer.parseInt(versionStr);
                    
                    pos = line.indexOf('/');
                    String entryTimeStr = line.substring(0, pos);
                    DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", new Locale("en_US"));
                    long entryTime = 0;
                    
                    try{
                        entryTime = df.parse(entryTimeStr).getTime() + (long)3600 * 8 * 1000;
                    }catch(Exception e){
                        e.printStackTrace();
                        entryTime = fileTm;
                    }
                    
                    if(entryTime < fileTm - 10000){
                        System.out.println("warning: uncommited file: " + file);
                        // version++;
                    }
                    
                    return version;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
 
    /*
     * 把下载文件名转换为实际文件名。
     * @param name 客户端使用的下载文件名（见规范）
     * @param model 客户端机型
     */
    public String translateFileName(String name, String model) {
        String key = name + "\n" + model;
        String ret = downloadFileMapping.get(key);
        String cType = getCharacterDir(model);
        if (ret == null) {
            // 优先搜索客户端内置资源
            if (name.endsWith(".etf")) {
                // 脚本文件，固定到scripts目录下的机型子目录中取
                if (branch == null) {
                    ret = "scripts/" + model + "/" + (name.substring(0, name.length() - 4)) + "_" + model + ".etf.gz";
                } else {
                    ret = "Branches/" + branch + "/scripts/" + model + "/" + (name.substring(0, name.length() - 4)) + "_" + model + ".etf.gz";
                }
            } else if ((ret = clientData.getMatchPath(model, name)) != null) {
                // 内置文件，使用client_pkg.xml的配置 
                File f = new File(baseDir , ret);
                if(!f.exists()){//由于配置文件是手工维护，也出现过打包时大小写不一致等问题，所以这里再确认一下
                    ret = findFile("client_res/" + model, name);
                    if (!new File(baseDir, ret).exists()) {
                        ret = findFile("client_res", name);
                    }
                }
            } else if (name.endsWith(".ctn")) {
                if (Character.isDigit(name.charAt(0))) {
                    // 数字开头的CTN文件是NPC动画，到Animations目录下取
                    int id = Integer.parseInt(name.substring(0, name.length() - 4));
                    Animation nif = (Animation)findObject(Animation.class, id);
                    if ("2x".equals(cType)) {
                        String ctsName = nif.largeSource.getName();
                        String ctnName = ctsName.substring(0, ctsName.length() - 1) + "n";
                        ret = "Animations/2x/" + ctnName;
                    } else {
                        String ctsName = nif.source.getName();
                        String ctnName = ctsName.substring(0, ctsName.length() - 1) + "n";
                        ret = "Animations/" + ctnName;
                    }
                } else {
                    // 其他CTN文件，搜索次序是：client_res/model，client_res/character/1x，client_res
                    ret = findFile("client_res/" + model, name);
                    if (!new File(baseDir, ret).exists()) {
                        ret = findFile("client_res/character/" + getCharacterDir(model), name);
                        if (!new File(baseDir, ret).exists()) {
                            ret = findFile("client_res", name);
                        }
                    }
                }
            } else if (name.endsWith(".pip")) {
                // 非内置PIP文件，搜索顺序是：client_res/model, client_res/character/1x, client_res，Animations
                ret = findFile("client_res/" + model, name);
                if (!new File(baseDir, ret).exists()) {
                    ret = findFile("client_res/character/" + getCharacterDir(model), name);
                    if (!new File(baseDir, ret).exists()) {
                        ret = findFile("client_res", name);
                    }
                }
                if (!new File(baseDir, ret).exists()) {
                    if ("2x".equals(cType)) {
                        ret = findFile("Animations/2x/", name);
                    } else {
                        ret = findFile("Animations", name);
                    }
                }
            }  else if (name.endsWith(".png")) {
                if(name.startsWith("card")){//卡片图片
                    ret = "Cards/" + getCardDir(model) + "/" + name;
                } else {// 非内置PNG文件，搜索顺序是client_res/model, client_res
                	ret = findFile("client_res/" + model, name);
                    if (!new File(baseDir, ret).exists()) {
                        if(!new File(baseDir, ret).exists()) {
                            ret = findFile("client_res", name);
                        }
                    }
                }
            } else if (name.endsWith(".pkg")) {
                // 关卡文件，到Areas目录下取
                int id = Integer.parseInt(name.substring(0, name.length() - 4));
                GameArea area = (GameArea)findObject(GameArea.class, id);
                if (isUseLarge(model)) {
                    ret = "Areas/" + area.source.getName() + "/client_l.pkg";
                } else {
                    ret = "Areas/" + area.source.getName() + "/client.pkg";
                }
            } else if (name.equals("client.data")) {
                // client.data，到client_pkg子目录下取
                if (branch == null) {
                    ret = findFile("scripts/" + model, name);
                } else {
                    ret = findFile("Branches/" + branch + "/scripts/" + model, name);
                }
            } else if (name.endsWith(".mid") || name.endsWith(".mp3")) {
                // 声音文件，到Sounds目录下取
                ret = findFile("Sounds", name);
            } else {
                // 其他未知文件，搜索顺序是：client_res/model，client_res
                ret = findFile("client_res/" + model, name);
                if (!new File(baseDir, ret).exists()) {
                    ret = findFile("client_res", name);
                }
            }
            downloadFileMapping.put(key, ret);
        }
        return ret;
    }
    
    /**
     * 不同机型对应的卡片目录
     * @param model
     * @return
     */
    private String getCardDir(String model){
        String ret = "240x320";
        if(model.equals("Midp2Small") || model.equals("SEK750") || model.equals("NokiaS60V2")){
            ret = "176x208";
        } else {
            ret = "240x320";
        }
        return ret;
    }
    
    /*
     * 在一个子目录中搜索文件。
     * @param dir 相对子目录
     * @param name 文件名
     * @return 返回找到的文件的相对路径
     */
    private String findFile(String dir, String name) {
        if (!new File(baseDir, dir).exists()) {
            return dir + "/" + name;
        }
        File f = new File(baseDir, dir + "/" + name);
        if (f.exists()) {
            return dir + "/" + name;
        }
        ArrayList<String> subDir = new ArrayList<String>();
        File[] files = new File(baseDir, dir).listFiles();
        for (File ff : files) {
            if (ff.isDirectory()) {
                subDir.add(ff.getName());
            }
        }
        while (subDir.size() > 0) {
            String subDirName = subDir.remove(0);
            if (new File(baseDir, dir + "/" + subDirName + "/" + name).exists()) {
                return dir + "/" + subDirName + "/" + name;
            }
            files = new File(baseDir, dir + "/" + subDirName).listFiles();
            for (File ff : files) {
                if (ff.isDirectory()) {
                    subDir.add(subDirName + "/" + ff.getName());
                }
            }
        }
        return dir + "/" + name;
    }
    
    /**
     * 取得某个文件的当前版本号。
     * @param name 客户端使用的下载文件名（见规范）
     * @param model 客户端机型
     * @return 如果文件不存在，返回0。
     */
    public int getFileVersion(String name, String model) {
        // channel.data特殊处理
        if (name.equals("channel.data")) {
            String path = "client_res/channel.data";
            byte[] ret = findFile(path);
            return getFileCRCVersion(ret);
        }
        
        Integer obj = resourceVersion.get(translateFileName(name, model));
        if (obj == null) {
            return 0;
        } else {
            return obj.intValue();
        }
    }
    
    public boolean getIsClientNeed(String name, String model){
        return clientData.isClientNeedResource(name, model);
    }
    
    public Hashtable<String, Boolean> getAllClientNeedTable(String model){
        return clientData.getAllClientNeedTable(model);
    }
    
    public int getClientDataVersion(String model){
        Integer obj = resourceVersion.get(translateFileName("client.data", model));
        if (obj == null) {
            return 0;
        } else {
            return obj.intValue();
        }
    }
    
    public byte[] getClientData(String model){
        return clientData.getClientData(model);
    }
    
    /**
     * 下载文件。这个方法可以用来下载CTN，PIP和ETF文件。PKG文件还是通过PackageUtils.makeClientPackage来获得。
     * @param name 客户端使用的下载文件名（见规范）
     * @param model 客户端机型
     * @return
     */
    public byte[] downloadFile(String name, String model) {
        String path = translateFileName(name, model);
        byte[] ret = findFile(path);
        
        // 下载PIP图片时，7370和K750需要特殊处理：7370因为用halfbuffer，所以图片必须是
        // 非合并模式的；而K750为了减少图片对象，必须是合并模式的。其他机型尽量采用合并模式，
        // 但对带有半透明的图片不用合并模式。
        if (name.endsWith(".pip") && Character.isDigit(name.charAt(0))) {
            if ("Nokia7370".equals(model)) {
                path += "/Nokia7370";
                byte[] nret = resourceCache.get(path);
                if (nret != null) {
                    return nret;
                }
                try {
                    PipImage img = new PipImage();
                    img.load(new ByteArrayInputStream(ret));
                    if (img.isMergeMode()) {
                        img.setMergeMode(false);
                        img.setSupportColorOp(false);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(ret.length + 1000);
                        DataOutputStream dos = new DataOutputStream(bos);
                        img.save(dos, true);
                        dos.close();
                        nret = bos.toByteArray();
                    } else {
                        nret = ret;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    nret = ret;
                }
                resourceCache.put(path, nret);
                return nret;
            } else if ("SEK750".equals(model)) {
                path += "/SEK750";
                byte[] nret = resourceCache.get(path);
                if (nret != null) {
                    return nret;
                }
                try {
                    PipImage img = new PipImage();
                    img.load(new ByteArrayInputStream(ret));
                    if (!img.isMergeMode()) {
                        img.setMergeMode(true);
                        img.setSupportColorOp(false);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(ret.length + 1000);
                        DataOutputStream dos = new DataOutputStream(bos);
                        img.save(dos, true);
                        dos.close();
                        nret = bos.toByteArray();
                    } else {
                        nret = ret;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    nret = ret;
                }
                resourceCache.put(path, nret);
                return nret;
            } else {
                path += "/Other";
                byte[] nret = resourceCache.get(path);
                if (nret != null) {
                    return nret;
                }
                try {
                    PipImage img = new PipImage();
                    img.load(new ByteArrayInputStream(ret));
                    if (!img.isMergeMode() && !img.hasHalfTransparent()) {
                        img.setMergeMode(true);
                        img.setSupportColorOp(false);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(ret.length + 1000);
                        DataOutputStream dos = new DataOutputStream(bos);
                        img.save(dos, true);
                        dos.close();
                        nret = bos.toByteArray();
                    } else {
                        nret = ret;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    nret = ret;
                }
                resourceCache.put(path, nret);
                return nret;
            }
        }
        return ret;
    }
    
    /**
     * 查找一个文件的内容。
     * @param path 文件相对于项目根目录的路径
     * @return 文件内容，如果文件未找到，返回null。
     */
    public byte[] findFile(String name) {
    	if (!serverMode) {
    		throw new IllegalArgumentException();
    	}
    	byte[] ret = resourceCache.get(name);
    	if (ret != null) {
    		return ret;
    	}
    	try {
    		ret = Utils.loadFileData(new File(baseDir, name));
    	} catch (Exception e) {
    	}
    	if (ret != null) {
    		resourceCache.put(name, ret);
    	}
    	return ret;
    }
 
    /**
     * 返回全局寻路工具。
     * @return
     */
    public AutoPathFinder getPathFinder() {
        return pathFinder;
    }
    
    /**
     * 清理所有没有用到的文件。
     */
    public void cleanGabage(Shell shell) {
        List<File> toBeDelete = new ArrayList<File>();
        
        // Animations目录，删除没有被引用的cts, ctn，然后删除没有被引用的pip
        List<DataObject> anis = getDataListByType(Animation.class);
        HashSet<String> usedCTS = new HashSet<String>();
        HashSet<String> usedPIP = new HashSet<String>();
        for (DataObject obj : anis) {
            Animation ani = (Animation)obj;
            usedCTS.add(ani.source.getName());
            PipAnimateSet nset = new PipAnimateSet();
            try {
                nset.load(ani.source);
                String ctn = ani.source.getAbsolutePath();
                ctn = ctn.substring(0, ctn.length() - 1) + "n";
                nset.save(new File(ctn), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < nset.getFileCount(); i++) {
                usedPIP.add(nset.getFileName(i));
            }
        }
        File[] files = new File(baseDir, "Animations").listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            String n = f.getName();
            if (!Character.isDigit(n.charAt(0))) {
                continue;
            }
            if (n.endsWith(".cts")) {
                if (!usedCTS.contains(n)) {
                    toBeDelete.add(f);
                }
            } else if (n.endsWith(".ctn")) {
                if (!usedCTS.contains(n.substring(0, n.length() - 1) + "s")) {
                    toBeDelete.add(f);
                }
            } else if (n.endsWith(".pip")) {
                if (!usedPIP.contains(n)) {
                    toBeDelete.add(f);
                }
            }
        }
        
        // Areas，删除没有被引用的目录
        List<DataObject> areas = getDataListByType(GameArea.class);
        HashSet<String> usedAreaDir = new HashSet<String>();
        for (DataObject obj : areas) {
            GameArea area = (GameArea)obj;
            usedAreaDir.add(area.source.getName());
        }
        files = new File(baseDir, "Areas").listFiles();
        for (File f : files) {
            if (!f.isDirectory()) {
                continue;
            }
            String n = f.getName();
            if (n.toLowerCase().contains("cvs")) {
                continue;
            }
            if (!usedAreaDir.contains(n)) {
                toBeDelete.add(f);
            }
        }
        
        // Quests，删除没有被引用的xml
        List<DataObject> quests = getDataListByType(Quest.class);
        HashSet<String> usedQuestXML = new HashSet<String>();
        for (DataObject obj : quests) {
            Quest quest = (Quest)obj;
            usedQuestXML.add(quest.source.getName());
        }
        files = new File(baseDir, "Quests").listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            String n = f.getName();
            if (n.endsWith(".xml") && !n.equals("index.xml")) {
                if (!usedQuestXML.contains(n)) {
                    toBeDelete.add(f);
                }
            }
        }
        
        StringBuilder msgB = new StringBuilder();
        msgB.append("你将要删除以下文件/目录，请确认：\n");
        for (File f : toBeDelete) {
            msgB.append(f);
            msgB.append("\n");
        }
        if (MessageDialog.openConfirm(shell, "删除", msgB.toString())) {
            for (File f : toBeDelete) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    Utils.deleteDir(f);
                }
            }
        }
    }
    
    public void reload(Class[] types, Map<Class, DataChangeHandler> handlers) throws Exception {
        reload(null, types, handlers);
    }
    
    /**
     * 重新载入项目数据。新数据会和旧数据进行一一比对，只有修改后的数据才会通知对应的处理器
     * 进行处理。数据变化可能包含3种类型：添加、修改、删除。
     * 这个操作只在服务器模式有效。
     * @param proj 已经载入完成的数据，如果为null，则在本方法里加载
     * @param types 重载的数据类型
     * @param handlers 不同类型数据的变化处理器
     */
    public void reload(ProjectData proj, Class[] types, Map<Class, DataChangeHandler> handlers) throws Exception {
        if (!this.serverMode) {
            throw new IllegalArgumentException();
        }
        
        // 载入新版本数据
        ProjectData newPrj;
        if (proj == null) {
            newPrj = new ProjectData();
            newPrj.serverMode = true;
            newPrj.branch = branch;
            newPrj.load(baseDir);
        } else {
            newPrj = proj;
        }
        
        // 更新所有字典类数据
        for (int i = 0; i < dictDataClasses.length; i++) {
            updateDataList(dictDataClasses[i], dictDataLists[i], newPrj.dictDataLists[i], handlers.get(dictDataClasses[i]));
        }
        
        // 更新所有可编辑数据
        for (int i = types.length - 1; i >= 0; i--) {
            int ind = getIndexByType(types[i]);
            if(ind==9){
                System.out.println();
            }
            Class cls = supportDataClasses[ind];
            updateDataList(cls, dataLists[ind], newPrj.dataLists[ind], handlers.get(cls));
        }
    }
    
    public void reloadPathFinder() {
        // 重构寻路数据
        pathFinder = new AutoPathFinder(this);
    }
    
    public void reloadFile() throws Exception {
        // 清除文件缓存
        loadResourceVersions();
        
        // 读入客户端资源配置文件
        clientData = new ClientData(this, baseDir, branch);
    }
    
    /**
     * 强制从缓存中清除某个文件，下次使用时重新载入。
     * @param path
     */
    public void forceReloadFile(String path) {
        resourceCache.remove(path);
    }

    /*
     * 比较新旧两个列表的数据（服务器模式，已按ID排序）。
     */
    protected void updateDataList(Class dataClass, List<DataObject> oldList, List<DataObject> newList, DataChangeHandler handler) throws Exception {
        int i = 0;
        int j = 0;
        while (i < oldList.size() && j < newList.size()) {
            DataObject oldObj = oldList.get(i);
            DataObject newObj = newList.get(j);
            if (oldObj.id < newObj.id) {
                // 这种情况说明旧对象已被删除
                if (handler != null) {
                    handler.dataObjectRemoved(oldObj);
                }
                oldList.remove(i);
            } else if (oldObj.id > newObj.id) {
                // 这种情况说明有新建对象
                Constructor cons = dataClass.getConstructor(ProjectData.class);
                DataObject addObj = (DataObject)cons.newInstance(this);
                addObj.update(newObj);
                oldList.add(i, addObj);
                if (handler != null) {
                    handler.dataObjectAdded(addObj);
                }
                i++;
                j++;
            } else {
                // ID匹配，检查数据是否修改
                if (oldObj.changed(newObj)) {
                    if (handler != null) {
                        handler.dataObjectChanging(oldObj);
                    }
                    oldObj.update(newObj);
                    if (handler != null) {
                        handler.dataObjectChanged(oldObj);
                    }
                }
                i++;
                j++;
            }
        }
        
        // 收尾，删除旧队列中剩余对象，这些对象都是应该被删除的
        while (i < oldList.size()) {
            if (handler != null) {
                handler.dataObjectRemoved(oldList.get(i));
            }
            oldList.remove(i);
        }
        
        // 收尾，新队列中剩余对象加入旧队列
        while (j < newList.size()) {
            Constructor cons = dataClass.getConstructor(ProjectData.class);
            DataObject addObj = (DataObject)cons.newInstance(this);
            addObj.update(newList.get(j));
            oldList.add(addObj);
            if (handler != null) {
                handler.dataObjectAdded(addObj);
            }
            j++;
        }
    }
    
    /**
     * 判断一个进行是否使用放大版本关卡数据。
     * 新加LenovoU1， IPad， IPhone4
     * @param model
     * @return
     */
    public boolean isUseLarge(String model) {
        return "Lenovo".equals(model) || "AndroidLarge".equals(model) || "Flash".equals(model) || "Nokia5800".equals(model) || "LenovoU1".equals(model) || "IPad".equals(model) || "IPhone4".equals(model) || "AndroidLargeNew".equals(model) || "NokiaS60V5".equals(model) || "iOSNewUILarge".equals(model) || "Nokia5800New".equals(model) || "Nokia5800NewC".equals(model) || "NewUI_AndroidLarge".equals(model) || "NewUI_iOSLarge".equals(model);
    }
    
    /**
     * 取得某机型对应的形象目录。
     */
    public String getCharacterDir(String model) {
        if ("Lenovo".equals(model) || "AndroidLarge".equals(model) || "Flash".equals(model) || "Nokia5800".equals(model) || "LenovoU1".equals(model) || "IPad".equals(model) || "IPhone4".equals(model) || "AndroidLargeNew".equals(model) || "NokiaS60V5".equals(model) || "iOSNewUILarge".equals(model) || "Nokia5800New".equals(model) || "Nokia5800NewC".equals(model)|| "NewUI_AndroidLarge".equals(model) ||  "NewUI_iOSLarge".equals(model)) {
            // 640以上的机型用2倍大小
            return "2x";
        } else if ("Android".equals(model) || "iPhone".equals(model) || "AndroidNew".equals(model) || "iOSNewUI".equals(model)) {
            // 480以上机型有1.35倍大小
            // 暂时没有，还用1倍大小
            return "1x";
        } else {
            return "1x";
        }
    }
    
    /**
     * 载入小提示。
     * @return
     */
    public Map<Integer,List<Hint>> loadHints() throws Exception {
       Document doc = Utils.loadDOM(new File(baseDir, "hints.xml"));
       List list = doc.getRootElement().getChildren("hint");
       Map<Integer,List<Hint>> ret = new HashMap<Integer,List<Hint>>();
       for (int i = 0; i < list.size(); i++) {
           Element elem = (Element)list.get(i);
           Hint hint = new Hint();
           hint.load(elem);
           List<Hint> hintList = ret.get(hint.keyboardType);
           if(hintList == null){
               hintList = new ArrayList<Hint>();
           }
           hintList.add(hint);
           ret.put(hint.keyboardType, hintList);
       }
       return ret;
    }
   
    /**
     * 导出所有关卡文件。
     * @param useLarge
     * @param target
     * @throws Exception
     */
    public void exportStages(boolean useLarge, File target) throws Exception {
        List<DataObject> areas = getDataListByType(GameArea.class);
        for (DataObject dobj : areas) {
            GameArea ga = (GameArea)dobj;
            if (useLarge) {
                byte[] data = Utils.loadFileData(new File(ga.source, "client_l.pkg"));
                int[] lens = new int[4];
                lens[0] = data.length / 4;
                lens[1] = data.length / 4;
                lens[2] = data.length / 4;
                lens[3] = data.length - lens[0] - lens[1] - lens[2];
                int pos = 0;
                for (int i = 0; i < 4; i++) {
                    byte[] data1 = new byte[lens[i]];
                    System.arraycopy(data, pos, data1, 0, lens[i]);
                    pos += lens[i];
                    Utils.saveFileData(new File(target, ga.id + "_" + i + ".pkg"), data1);
                }
            } else {
                Utils.copyFile(new File(ga.source, "client.pkg"), new File(target, ga.id + ".pkg"));
            }
        }
    }
    
    /**
     * 取得文件的当前版本号。
     * 版本号编码规则：
     * 4字节整数，前2个字节表示文件大小的低16位，后两个字节表示CRC16。
     */
    public static int getFileCRCVersion(File file) {
        byte[] content = null;
        try {
            content = Utils.loadFileData(file);
        } catch (Exception e) {
            return 0;
        }
        return (content.length << 16) | (crc16(content) & 0xFFFF);
    }
    
    /**
     * 取得文件的当前版本号。
     * 版本号编码规则：
     * 4字节整数，前2个字节表示文件大小的低16位，后两个字节表示CRC16。
     */
    public static int getFileCRCVersion(byte[] data) {
        return (data.length << 16) | (crc16(data) & 0xFFFF);
    }

    /**
     * 字节流CRC值 （16位）
     */
    private static int crc16(byte[] data) {
        byte ret1 = 0;
        byte ret2 = 0;
        int len = data.length;
        for (int i = 0; i < len; i += 2) {
            ret1 ^= data[i];
            if (i + 1 < len) {
                ret2 ^= data[i + 1];
            }
        }
        return (ret1 << 8) | (ret2 & 0xFF);
    }
    
    class ThreadFileversion implements Runnable {
        ProjectData data;
        Shell shell;
        public ThreadFileversion(Shell shell,ProjectData data){
            this.shell = shell;
            this.data = data;
        }
        @Override
        public void run() {
            try {
                data.generateResourceVersionXML();
                shell.getDisplay().syncExec(new Runnable(){
                    public void run(){
                        MessageDialog.openInformation(shell, "成功", "操作成功！");
                    }
                }); 
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**/
    public static void main(String[] args) {
        try {
            ProjectData pd = new ProjectData();
            pd.serverMode = true;
            pd.load(new File("D:/workspace/Sanguo1.0-Data/data_zh_TW/data"));
            pd.generatePackDotData(ProjectData.PACK_DATA_NPC|ProjectData.PACK_DATA_MAP|ProjectData.PACK_DATA_MUSIC|ProjectData.PACK_DATA_SCRIPTS,ProjectData.PACK_TYPE_BIG);
//            pd.generatePackDotData(ProjectData.PACK_DATA_NPC|ProjectData.PACK_DATA_MAP|ProjectData.PACK_DATA_MUSIC|ProjectData.PACK_DATA_SCRIPTS,ProjectData.PACK_TYPE_BIG);
//              pd.generatePackDotData(ProjectData.PACK_DATA_NPC,ProjectData.PACK_TYPE_SMALL);
        
//            printPackDotData("D:/workspace/Sanguo1.0-Data/newui_pack/NewUI_AndroidLarge15/pack_NewUI_AndroidLarge15.data");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
