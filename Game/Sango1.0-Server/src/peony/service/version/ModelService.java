package peony.service.version;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.service.Service;

public class ModelService implements Service {
    private static final Logger log = Logger.getLogger(ModelService.class);
    protected File configFile;
    public static final int CONFIG_VERSION = 3;
    protected static class ModelInputType {
        // 键盘类型：0 - 无键盘，1 - 数字键盘，2 - 全键盘
        int keyboardType;
        // 鼠标类型：0 - 无指点设备，1 - 触摸屏，2 - 鼠标
        int mouseType;
        
        public ModelInputType(int kt, int mt) {
            keyboardType = kt;
            mouseType = mt;
        }
    }
    protected static Map<String, ModelInputType> modelInputTypeMap = new HashMap<String, ModelInputType>();
    static {
        modelInputTypeMap.put("GenericMidp2".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia7610".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia6681".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia3250".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("NokiaN73".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia7370".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("NokiaE62".toLowerCase(), new ModelInputType(2, 0));
        modelInputTypeMap.put("SEK750".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("SEK790".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("MotoE2".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("Midp2Touch".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("Lenovo".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("NokiaS60V3".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("NokiaS60V2".toLowerCase(), new ModelInputType(1, 0));
        modelInputTypeMap.put("Android".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("AndroidLarge".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("AndroidSmall".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("AndroidAuto".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("AndroidPSP".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("PocketPC".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("WindowsMobile".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("iPhone".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("iPhone4".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("IPad".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("LenovoU1".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia5800".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia5800II".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia5800New".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia5800NewC".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia5800Portrait".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("iOSNewUI".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("iOSNewUILarge".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("NewUI_AndroidLarge".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("NewUI_Android".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("NewUI_iOSLarge".toLowerCase(), new ModelInputType(0, 1));
        modelInputTypeMap.put("NewUI_iOS".toLowerCase(), new ModelInputType(0, 1));
    }
    
    public static class Config {
        int id;
        String name;
        int playercount;
        int envanimation;
        int network;
        int textmode;
        int chattransmode;
        int minimaptransmode;
        int downloadnpcmode;	//下载npc模式：0抢占主线程CPU时间片1不抢占主线程CPU时间片
        int trade;//是否允许交易    0：允许      1：不允许
        int pk;//pk
        int guild;//军团邀请
        int guildChat;//军团聊天
        int factionChat;     //国家聊
        int party;//组队
        
        public byte[] toBytes() {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(CONFIG_VERSION);
                dos.writeInt(playercount);
                dos.writeInt(envanimation);
                dos.writeInt(textmode);
                dos.writeInt(0);    // 是否播放聊天
                dos.writeInt(network);
                dos.writeInt(0);    // 是否显示小地图
                dos.writeInt(0);    // 自己头顶文字
                dos.writeInt(0);    // 其他玩家头顶文字
                dos.writeInt(chattransmode);
                dos.writeInt(minimaptransmode);
                for (int i = 0; i < 10; i++) {
                    dos.writeInt(0);
                }
                dos.writeInt(downloadnpcmode);
                dos.writeInt(trade);
                dos.writeInt(pk);
                dos.writeInt(guild);
                dos.writeInt(guildChat);
                dos.writeInt(factionChat);
                dos.writeInt(party);
                dos.flush();
                return bos.toByteArray();
            } catch (Exception e) {
                return new byte[0];
            }
        }
    }
    
    private static class Model {
        String name;
        String[] patternStrs;
        Pattern[] patterns;
        Config config;
    }
    
    private List<Config> configs;
    private List<Model> models;
    private HashMap<String, Model> uaModelMap;
    
    public void startup() throws Exception {
        configFile = new File("model.xml");
        loadConfig();
    }
    
    public void shutdown() {
        try {
            saveConfig();
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    public void reload() {
        try {
            loadConfig();
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    /**
     * 根据UserAgent决定用什么配置。
     * @param ua
     * @return
     */
    public Config getModelConfig(String ua) {
        Model m = uaModelMap.get(ua);
        if (m != null) {
            return m.config;
        }
        for (Model mm : models) {
            for (Pattern p : mm.patterns) {
                if (p.matcher(ua).matches()) {
                    uaModelMap.put(ua, mm);
                    return mm.config;
                }
            }
        }
        
        // 没有找到匹配的用缺省配置
        return configs.get(0);
    }
    
    protected void loadConfig() throws Exception {
    	FileInputStream fis = new FileInputStream(configFile);
    	Document doc = CommonUtil.getDocument(fis);
    	fis.close();
        List list = doc.getRootElement().elements("config");
        List<Config> arr = new ArrayList<Config>();
        for (int i = 0; i < list.size(); i++) {
            Element elem = (Element)list.get(i);
            Config config = new Config();
            config.id = Integer.parseInt(elem.attributeValue("id"));
            config.name = elem.attributeValue("name");
            config.playercount = Integer.parseInt(elem.attributeValue("playercount"));
            config.envanimation = Integer.parseInt(elem.attributeValue("envanimation"));
            config.network = Integer.parseInt(elem.attributeValue("network"));
            config.textmode = Integer.parseInt(elem.attributeValue("textmode"));
            config.chattransmode = Integer.parseInt(elem.attributeValue("chattransmode"));
            config.minimaptransmode = Integer.parseInt(elem.attributeValue("minimaptransmode"));
            config.downloadnpcmode = Integer.parseInt(elem.attributeValue("downloadnpcmode"));
            arr.add(config);
        }
        configs = arr;
        
        list = doc.getRootElement().elements("model");
        List<Model> marr = new ArrayList<Model>();
        HashMap<String, Model> map = new HashMap<String, Model>();
        for (int i = 0; i < list.size(); i++) {
            Element elem = (Element)list.get(i);
            Model model = new Model();
            model.name = elem.attributeValue("name");
            model.patternStrs = elem.attributeValue("pattern").split(",");
            model.patterns = new Pattern[model.patternStrs.length];
            for (int j = 0; j < model.patterns.length; j++) {
                model.patterns[j] = Pattern.compile(model.patternStrs[j]);
            }
            int configID = Integer.parseInt(elem.attributeValue("config"));
            boolean found = false;
            for (Config cfg : configs) {
                if (cfg.id == configID) {
                    found = true;
                    model.config = cfg;
                    break;
                }
            }
            if (!found) {
                throw new Exception(MessageFormat.format(peony.Messages.STRING_01332, configID));
            }
            marr.add(model);
            
            List list2 = elem.elements("ua");
            for (int j = 0; j < list2.size(); j++) {
                Element elem2 = (Element)list2.get(j);
                map.put(elem2.getText(), model);
            }
        }
        models = marr;
        uaModelMap = map;
    }
    
    protected void saveConfig() throws Exception {
    	Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("models");
        for (Config config : configs) {
            Element elem = root.addElement("config");
            elem.addAttribute("id", String.valueOf(config.id));
            elem.addAttribute("name", config.name);
            elem.addAttribute("playercount", String.valueOf(config.playercount));
            elem.addAttribute("envanimation", String.valueOf(config.envanimation));
            elem.addAttribute("network", String.valueOf(config.network));
            elem.addAttribute("textmode", String.valueOf(config.textmode));
            elem.addAttribute("chattransmode", String.valueOf(config.chattransmode));
            elem.addAttribute("minimaptransmode", String.valueOf(config.minimaptransmode));
            elem.addAttribute("downloadnpcmode", String.valueOf(config.downloadnpcmode));
        }
        for (Model model : models) {
            Element elem = root.addElement("model");
            elem.addAttribute("name", model.name);
            StringBuilder sb = new StringBuilder();
            for (String str : model.patternStrs) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(str);
            }
            elem.addAttribute("pattern", sb.toString());
            elem.addAttribute("config", String.valueOf(model.config.id));
            for (String ua : uaModelMap.keySet()) {
                Model m = uaModelMap.get(ua);
                if (m == model) {
                    Element elem2 = elem.addElement("ua");
                    elem2.setText(filter(ua));
                }
            }
        }
        CommonUtil.saveDocument(doc, new FileWriter(configFile));
    }
    
    /*
     * 过滤掉非ASCII字符
     */
    private String filter(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 0x20 && ch <= 0x7F) {
                sb.append(ch);
            } else if (ch >= 0x4E00 && ch <= 0x9FA5) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    /**
     * 取得某机型的键盘类型。
     * @return 0 - 无键盘，1 - 数字键盘，2 - 全键盘
     */
    public static int getKeyboardType(String model) {
        if (model == null) {
            return 1;
        }
        ModelInputType obj = modelInputTypeMap.get(model.toLowerCase().trim());
        if (obj == null) {
            return 1;
        } else {
            return obj.keyboardType;
        }
    }
    
    /**
     * 取得某机型的鼠标类型。
     * @return 0 - 无指点设备，1 - 触摸屏，2 - 鼠标
     */
    public static int getMouseType(String model) {
        if (model == null) {
            return 0;
        }
        ModelInputType obj = modelInputTypeMap.get(model.toLowerCase().trim());
        if (obj == null) {
            return 0;
        } else {
            return obj.mouseType;
        }
    }
}
