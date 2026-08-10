package com.pip.itimes.server.connect;

import java.io.*;
import java.util.*;

import com.pip.gtl.etf.ETFFile;
import com.pip.gtl.etf.ETFUtil;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
//import com.pip.itimes.server.question.QuestionLoader;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.suit.SuitLoader;
import com.pip.itimes.server.util.Utils;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class StageService {

    private File pkgDir;
    private PngResources npcPngs;
    private PngResources mgPngs;
    private PngResources monsterPngs;
    private Map stages = new HashMap();
    private NpcPool npcPool = new NpcPool();
    private MonsterGroupPool mgPool = new MonsterGroupPool();
    private ResourcePool resourcePool = new ResourcePool();
    private StageBuilder stageBuilder = new StageBuilder();
    private TaskService taskService;
    private AbilitiesLoader abilitiesLoader;
    private ItemLoader itemLoader;
    private RecipesLoader recipesLoader;
    private TaskNpcLoader taskNpcLoader;
    private TaskAwardLoader taskAwardLoader;
    private TaskDefinitionLoader taskDefinitionLoader;
    private ChatFavoriteLoader chatFavoriteLoader;
    private MaterialTypeLoader materialTypeLoader;
    private StoreGroupLoader storeGroupLoader;
    private RoleFaceLoader faceLoader;
    private ClientService clientService = null;
    private BbsService bbsService;
    private SuitLoader suitLoader;
//    private QuestionLoader questionLoader;

    public StageService(File stageDir,BbsService bbsService) throws Exception {
        this.pkgDir = stageDir;
        this.bbsService = bbsService;
        load(stageDir);
    }

    public void load(File file) throws Exception {

        loadImages();

        loadAbilities();

        loadMaterialType();

        loadItems();

        loadRecipes();

        loadNpcs();

        loadStages();

        loadTasks();

        loadChatFavorites();

        loadCommodityGroups();

        loadForbidenBbs();

        loadFaces();
//        loadDropGroup();
        
        loadSuits();

//        loadQuestions();
    }

    private void loadSuits() throws Exception{
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(), "Items/suit.xml");
        suitLoader = new SuitLoader(new File(dirName));
    }
    
    private void loadQuestions() throws Exception{
//    	String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),"Areas/questions.xml");
//    	questionLoader = new QuestionLoader(pkgDir);
    }
    
    private void loadFaces() throws Exception{
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),
                                              "RoleImages/index.xml");
        faceLoader = new RoleFaceLoader(new File(dirName));
    }

    private void loadForbidenBbs() throws Exception{
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),
                                              "Areas/bbs.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        Set ids = new HashSet();
        for(Iterator i=root.elementIterator("bbs");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            ids.add(new Integer(id));
        }
        bbsService.setForbidenBbs(ids);
    }

    private void loadChatFavorites() throws Exception {
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),
                                              "Areas/chatfavorites.xml");
        chatFavoriteLoader = new ChatFavoriteLoader(new File(dirName));
    }

    private void loadAbilities() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String abilitiesDirName = FilenameUtils.concat(stageDirName,
                "Skill/index.xml");
        abilitiesLoader = new AbilitiesLoader(new File(abilitiesDirName));
//        Skill.addSkills();
    }

//    private void loadDropGroup() throws Exception{
//        String stageDirName = pkgDir.getAbsolutePath();
//        String dropGroupDirName = FilenameUtils.concat(stageDirName,"Items/dropGroup.xml");
//        dropGroupLoader = new DropGroupLoader(new File(dropGroupDirName));
//    }

    private void loadCommodityGroups() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String storeGroupDirName = FilenameUtils.concat(stageDirName,
                "Areas/CommodityGroups.xml");
        storeGroupLoader = new StoreGroupLoader(new File(storeGroupDirName));
    }

    private void loadNpcs() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String taskNpcDirName = FilenameUtils.concat(stageDirName,
                "Areas/npc.xml");
        taskNpcLoader = new TaskNpcLoader(new File(taskNpcDirName));
    }

    private void loadRecipes() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String recipesDirName = FilenameUtils.concat(stageDirName,
                "Skill/Recipes.xml");
        recipesLoader = new RecipesLoader(new File(recipesDirName));
    }

    private void loadItems() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String equDirName = FilenameUtils.concat(stageDirName,
                                                 "Items/equ.xml");
        String itemDirName = FilenameUtils.concat(stageDirName,
                                                  "Items/item.xml");
        itemLoader = new ItemLoader(new File(equDirName), new File(itemDirName));
    }

    private void loadMaterialType() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String materialDirName = FilenameUtils.concat(stageDirName,
                "Areas/MaterialType.xml");
        materialTypeLoader = new MaterialTypeLoader(new File(materialDirName));
    }

    private void loadImages() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String npcPngsDirName = FilenameUtils.concat(stageDirName, "NpcImages");
        PngResources newNpcPngs = new PngResources(new File(npcPngsDirName));
        npcPngs = newNpcPngs;
        String mgPngsDirName = FilenameUtils.concat(stageDirName,
                "MonsterIcons");
        PngResources newMgPngs = new PngResources(new File(mgPngsDirName));
        mgPngs = newMgPngs;
        String monsterPngsDirName = FilenameUtils.concat(stageDirName,
                "MonsterImages");
        PngResources newMonsterPngs = new PngResources(new File(
                monsterPngsDirName));
        monsterPngs = newMonsterPngs;
    }

    public void loadTasks() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String taskDirName = FilenameUtils.concat(stageDirName, "Tasks");
        taskService = new TaskService(new File(taskDirName));
        String awardDirName = FilenameUtils.concat(stageDirName,
                "Tasks/items.xml");
        taskAwardLoader = new TaskAwardLoader(new File(awardDirName));
        String relationDirName = FilenameUtils.concat(stageDirName,
                "Tasks/index.xml");
        taskDefinitionLoader = new TaskDefinitionLoader(new File(
                relationDirName));
        stageBuilder.setTaskService(taskService);
    }


//    private void loadInstanceDefinition() throws Exception{
//        String stageDirName = pkgDir.getAbsolutePath();
//        String instanceDirName = FilenameUtils.concat(stageDirName,"Areas/instance.xml");
//        File file = new File(instanceDirName);
//        SAXReader reader = new SAXReader();
//        Document doc = reader.read(file);
//        instances.clear();
//        Element root = doc.getRootElement();
//        for(Iterator i=root.elementIterator("instance");i.hasNext();){
//            Element e = (Element)i.next();
//            int id = Integer.parseInt(e.attributeValue("id"));
//            short map = Short.parseShort(e.attributeValue("map"));
//            short x = Short.parseShort(e.attributeValue("x"));
//            short y = Short.parseShort(e.attributeValue("y"));
//            int maxPlayer = Integer.parseInt(e.attributeValue("maxplayer"));
//            int refreshSecond = Integer.parseInt(e.attributeValue("refreshsecond"));
//            InstanceDefinition idf = new InstanceDefinition(id,map,x,y);
//            idf.setMaxPlayer(maxPlayer);
//            idf.setRefreshSecond(refreshSecond);
//            for(Iterator j = e.elementIterator("map");j.hasNext();){
//                Element m = (Element)j.next();
//                short mapId = Short.parseShort(m.attributeValue("id"));
//                idf.addMap(mapId);
//            }
//            Element entrance = e.element("entrance");
//            short entranceMapId = Short.parseShort(entrance.attributeValue("map"));
//            short entranceX = Short.parseShort(entrance.attributeValue("x"));
//            short entranceY = Short.parseShort(entrance.attributeValue("y"));
//            short pixelX = Short.parseShort(entrance.attributeValue("pixel_x"));
//            short pixelY = Short.parseShort(entrance.attributeValue("pixel_y"));
//            idf.setEntrance(entranceMapId);
//            idf.setEntranceX(entranceX);
//            idf.setEntranceY(entranceY);
//            idf.setEntrancePixelX(pixelX);
//            idf.setEntrancePixelY(pixelY);
//            instances.add(idf);
//        }
//    }


    public void reload() throws Exception {
//        loadInstanceDefinition();

        loadImages();

        loadAbilities();

        loadMaterialType();

        loadItems();

        loadRecipes();

        loadNpcs();

        loadStages();

        loadTasks();

        loadChatFavorites();

        loadCommodityGroups();

        loadForbidenBbs();
        
        loadSuits();
//        loadDropGroup();

//        loadQuestions();
    }


    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    public void setBbsService(BbsService bbsService){
        this.bbsService = bbsService;
    }

    private void loadStages() throws Exception {
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),
                                              "Areas/Export");
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        Map newStages = new HashMap();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            String ext = FilenameUtils.getExtension(fileName);
            if ("pkg".equals(ext)) {
                Stage stage = StageLoader.getStage(files[i]);
//                loadDynamicObjects(stage);
                packMonsters(stage);
                newStages.put(new Short(stage.getId()), stage);
            }
        }
        stages = newStages;
    }

    public Stage getStage(short id) {
        return (Stage) stages.get(new Short(id));
    }

    public Stage[] getStages() {
        Stage[] ret = new Stage[stages.size()];
        stages.values().toArray(ret);
        return ret;
    }


    private void packMonsters(Stage stage) throws IOException {
        Monster[] monsters = stage.getMonsters();
        List l = new ArrayList(monsters.length);
        for (int i = 0; i < monsters.length; i++) {
            byte type = monsters[i].getType();
            if ((type & 2) == 0) {
                l.add(monsters[i]);
            } else {
                break;
            }
        }
        Set abilities = new HashSet();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dis = new DataOutputStream(bos);
        dis.writeByte((byte) l.size());
        for (int i = 0; i < l.size(); i++) {
            Monster monster = (Monster) l.get(i);
            dis.writeShort(monster.getPngId());
            dis.writeUTF(monster.getName());
            dis.writeByte(monster.getType());
            dis.writeShort(monster.getLevel());
            dis.writeShort(monster.getVit());
            dis.writeShort(monster.getStr());
            dis.writeShort(monster.getInt());
            dis.writeShort(monster.getAgi());
            dis.writeShort(monster.getPMinAttack());
            dis.writeShort(monster.getPMaxAttack());
            dis.writeShort(monster.getPDef());
            dis.writeShort(monster.getMMinAttack());
            dis.writeShort(monster.getMMaxAttack());
            dis.writeShort(monster.getMDef());
            dis.writeShort(monster.getParry());
            dis.writeShort(monster.getHit());
            dis.writeShort(monster.getPCritial());
            dis.writeShort(monster.getMCritial());
            dis.writeInt(monster.getHp());
            dis.writeInt(monster.getMp());
            dis.writeByte(monster.getPetType());
            int[] abis = monster.getAbilities();
            dis.writeByte(abis.length);
            for (int j = 0; j < abis.length; j++) {
                dis.writeShort(abis[j]);
            }
            for (int j = 0; j < abis.length; j++) {
                abilities.add(Ability.getAbility(abis[j]));
            }
        }
        InPkgFile pkgFile = new InPkgFile();
        pkgFile.setData(bos.toByteArray());
        pkgFile.setName("m.d");
        stage.addInPkgFile(pkgFile);
        pkgFile = new InPkgFile();
        pkgFile.setData(getAbilitiesBytes(abilities));
        pkgFile.setName("ms.d");
        stage.addInPkgFile(pkgFile);
    }

    private byte[] getAbilitiesBytes(Set abilities) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort((short) abilities.size());
        Iterator ite = abilities.iterator();
        while (ite.hasNext()) {
            Ability ability = (Ability) ite.next();
            dos.writeByte(ability.getType());
            dos.writeUTF(ability.getName());
            dos.writeByte(ability.getEffect());
            dos.writeByte(ability.getStatus());
            dos.writeByte(ability.getPosition());
            dos.writeByte(ability.getCD());
            dos.writeByte(ability.getCDTime());
            dos.writeByte(1);
            dos.writeShort(ability.getId());
            dos.writeByte(ability.getLevel());
            dos.writeInt(ability.getValue1());
            dos.writeInt(ability.getValue2());
            dos.writeByte(ability.getEffectTime());
            dos.writeShort(ability.getMana());
            dos.writeByte(ability.getArithmetic());
        }
        return bos.toByteArray();
    }

    public byte[] getAllAbilitiesBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Ability[] abilities = Ability.getAbilitites();
        Map map = new HashMap();
        for (int i = 0; i < abilities.length; i++) {
            int effect = abilities[i].getEffect();
            List l = (List) map.get(new Integer(effect));
            if (l == null) {
                l = new ArrayList();
                map.put(new Integer(effect), l);
            }
            l.add(abilities[i]);
        }
        dos.writeShort(map.size());
        Collection c = map.values();
        Iterator ite = c.iterator();
        while (ite.hasNext()) {
            List sub = (List) ite.next();
            for (int j = 0; j < sub.size(); j++) {
                Ability ability = (Ability) sub.get(j);
                if (j == 0) {
                    dos.writeByte(ability.getType());
                    dos.writeUTF(ability.getName());
                    dos.writeByte(ability.getEffect());
                    dos.writeByte(ability.getStatus());
                    dos.writeByte(ability.getPosition());
                    dos.writeByte(ability.getCD());
                    dos.writeByte(ability.getCDTime());
                    dos.writeByte(sub.size());
                }
                dos.writeShort(ability.getId());
                dos.writeByte(ability.getLevel());
                dos.writeInt(ability.getValue1());
                dos.writeInt(ability.getValue2());
                dos.writeByte(ability.getEffectTime());
                dos.writeShort(ability.getMana());
                dos.writeByte(ability.getArithmetic());
            }
        }
        return bos.toByteArray();
    }


    private void loadDynamicObjects(Stage stage) {
        Scene[] scenes = stage.getScenes();
        for (int i = 0; i < scenes.length; i++) {
            Npc[] npcs = scenes[i].getDynNpcs();
            for (int j = 0; j < npcs.length; j++) {
                npcPool.addNpc(npcs[j], false);
            }
            MonsterGroup[] mgs = scenes[i].getDynMonsterGroups();
            for (int j = 0; j < mgs.length; j++) {
                mgPool.addMonsterGroup(mgs[j], false);
            }
            Resource[] resources = scenes[i].getResources();
            for (int j = 0; j < resources.length; j++) {
                resourcePool.addResource(resources[j], false);
            }
        }
    }

    public byte[] getStageBytes(short id, Map parameters) throws Exception {
        Stage stage = (Stage) stages.get(new Short(id));
        if (stage == null)
            return null;
        return stageBuilder.toBytes(stage, parameters);
    }

    public PngResourceData getPng(short type, short id) {
        String prefix = "";
        PngResources resource = null;
        if (type == 3) {
            resource = mgPngs;
        } else if (type == 4) {
            prefix = "n";
            resource = npcPngs;
        } else if (type == 5) {
            prefix = "m";
            resource = monsterPngs;
        }
        String name = prefix + id;
        PngResourceData data = resource.getPngResourceData(name);
        return data;
    }

    public void syncRefresh(int[] ids) {
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            byte type = Utils.getType(id);
            if (type == 0) {
                npcPool.setVisible(id, true);
            } else if (type == 1) {
                mgPool.setVisible(id, true);
            } else if (type == 2) {
                resourcePool.setVisible(id, true);
            }
        }
    }

    public void refreshAdd(int id) {
        byte type = Utils.getType(id);
        if (type == 0) { //npc
            npcPool.setVisible(id, true);
            Npc npc = npcPool.getNpc(id);
            if (npc != null) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
                seg.writeShort((short) 1);
                seg.write((byte) 1);
                seg.writeInt(id);
                clientService.broadcastToMap(seg, Utils.getMapIdById(id));
            }
        } else if (type == 1) { //monstergroup
            mgPool.setVisible(id, true);
            MonsterGroup mg = mgPool.getMonsterGroup(id);
            if (mg != null) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
                seg.writeShort((short) 1);
                seg.write((byte) 1);
                seg.writeInt(id);
                clientService.broadcastToMap(seg, Utils.getMapIdById(id));
            }
        } else if (type == 2) { //resource
            resourcePool.setVisible(id, true);
            Resource resource = resourcePool.getResource(id);
            if (resource != null) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
                seg.writeShort((short) 1);
                seg.write((byte) 1);
                seg.writeInt(id);
                clientService.broadcastToMap(seg, Utils.getMapIdById(id));
            }
        }
    }

    public void refreshDelete(int id) {
        byte type = Utils.getType(id);
        if (type == 0) { //npc
            npcPool.setVisible(id, false);
            Npc npc = npcPool.getNpc(id);
            if (npc != null) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
                seg.writeShort((short) 1);
                seg.write((byte) 0);
                seg.writeInt(id);
                clientService.broadcastToMap(seg, Utils.getMapIdById(id));
            }
        } else if (type == 1) { //monstergroup
            mgPool.setVisible(id, false);
            MonsterGroup mg = mgPool.getMonsterGroup(id);
            if (mg != null) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
                seg.writeShort((short) 1);
                seg.write((byte) 0);
                seg.writeInt(id);
                clientService.broadcastToMap(seg, Utils.getMapIdById(id));
            }
        } else if (type == 2) { //resource
            resourcePool.setVisible(id, false);
            Resource resource = resourcePool.getResource(id);
            if (resource != null) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
                seg.writeShort((short) 1);
                seg.write((byte) 0);
                seg.writeInt(id);
                clientService.broadcastToMap(seg, Utils.getMapIdById(id));
            }
        }
    }

    public int[] getVisibleObjects(short sceneId) {
        short stageId = Utils.getStageId(sceneId);
        byte mapId = (byte) (sceneId & 0xF);
        Stage stage = (Stage) stages.get(new Short(stageId));
        Scene[] scenes = stage.getScenes();
        IntList list = new ArrayIntList();
        for (int i = 0; i < scenes.length; i++) {
            if (scenes[i].getId() == mapId) {
                Npc[] npcs = scenes[i].getDynNpcs();
                for (int j = 0; j < npcs.length; j++) {
                    int id = npcs[j].getId();
                    if (npcPool.isVisible(id))
                        list.add(id);
                }
                MonsterGroup[] mgs = scenes[i].getDynMonsterGroups();
                for (int j = 0; j < mgs.length; j++) {
                    int id = mgs[j].getId();
                    if (mgPool.isVisible(id))
                        list.add(id);
                }
                Resource[] resources = scenes[i].getResources();
                for (int j = 0; j < resources.length; j++) {
                    int id = resources[j].getId();
                    if (resourcePool.isVisible(id)) {
                        list.add(id);
                    }
                }
            }
        }
        return list.toArray();
    }

    public byte[] getTaskBytes(short id) {
        try {
            ETFFile etfFile = taskService.findETF(id);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ETFUtil.save(etfFile, bos);
                return bos.toByteArray();
            }
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public byte[] getTaskBytes(short id, String[] args) {
        ETFFile etfFile = taskService.fineETF(id, args);
        try {
            if (etfFile != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ETFUtil.save(etfFile, bos);
                return bos.toByteArray();
            }
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public String[] getTasksName(short[] id) {
        try {
            String[] ret = new String[id.length];
            for (int i = 0; i < id.length; i++) {
                ETFFile etfFile = taskService.findETF(id[i]);
                if (etfFile != null) {
                    ret[i] = etfFile.taskName;
                }
            }
            return ret;
        } catch (Exception ex) {
            return null;
        }
    }
}
