package com.pip.itimes.server.world;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ModelService {
    private static final Logger log = Logger.getLogger(ModelService.class);
    protected File configFile;

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
    	modelInputTypeMap.put("Dopod585", new ModelInputType(1, 0));
    	modelInputTypeMap.put("DopodS700", new ModelInputType(0, 1));
    	modelInputTypeMap.put("Lenovoi758", new ModelInputType(0, 1));
    	modelInputTypeMap.put("Midp2Small", new ModelInputType(1, 0));
    	modelInputTypeMap.put("Midp2Touch", new ModelInputType(0, 1));
    	modelInputTypeMap.put("MotoE2", new ModelInputType(1, 0));
    	modelInputTypeMap.put("MotoE680", new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia5200", new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia3250", new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia5500", new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia5800", new ModelInputType(0, 1));
        modelInputTypeMap.put("Nokia6681", new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia7370", new ModelInputType(1, 0));
        modelInputTypeMap.put("Nokia7610", new ModelInputType(1, 0));
        modelInputTypeMap.put("NokiaE62", new ModelInputType(2, 0));
        modelInputTypeMap.put("NokiaN73", new ModelInputType(1, 0));
        modelInputTypeMap.put("NokiaNGage", new ModelInputType(1, 0));
        modelInputTypeMap.put("PAX800", new ModelInputType(1, 0));
        modelInputTypeMap.put("SAMi458", new ModelInputType(1, 0));
        modelInputTypeMap.put("SAMi8510", new ModelInputType(1, 0));
        modelInputTypeMap.put("SAML288", new ModelInputType(1, 0));
        modelInputTypeMap.put("SEK300", new ModelInputType(1, 0));
        modelInputTypeMap.put("SEK500", new ModelInputType(1, 0));
        modelInputTypeMap.put("SEK750", new ModelInputType(1, 0));
        modelInputTypeMap.put("SEW958C", new ModelInputType(1, 1));
        modelInputTypeMap.put("SEK790", new ModelInputType(1, 0));
        modelInputTypeMap.put("TYA650", new ModelInputType(0, 1));
        modelInputTypeMap.put("ZTEU860", new ModelInputType(1, 1));
        modelInputTypeMap.put("ZTEU981", new ModelInputType(0, 1));
    }
    
    public static class Config {
        int id;
        String name;
        int minimap;
        int playershowmode;
        int textmode;
        int playercount;
        
        public byte[] toBytes() {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeShort(0);			// 免打扰模式
                dos.writeShort(0);			// 头像显示
                minimap = 2;
                dos.writeShort(minimap);	// 小地图选项
                dos.writeShort(0);			// 怪物图标下载
                dos.writeShort(0);			// NPC怪物图片下载
                dos.writeShort(0);			// 自动行走
                dos.writeShort(playershowmode);		// 人物显示选项
                dos.writeShort(0);			// 称号设置
                dos.writeShort(textmode);	// 字体显示
                dos.writeShort(playercount);	// 其他玩家显示数量
                //zxyu 2011年3月8日14:19:57 修改默认为不震动
                dos.writeShort(1);			// 不震动
                for (int i = 0; i < 9; i++) {
                    dos.writeShort(0);			// 保留
                }
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
		SAXReader reader = new SAXReader();
    	Document doc = reader.read(configFile);
        List list = doc.getRootElement().elements("config");
        List<Config> arr = new ArrayList<Config>();
        for (int i = 0; i < list.size(); i++) {
            Element elem = (Element)list.get(i);
            Config config = new Config();
            config.id = Integer.parseInt(elem.attributeValue("id"));
            config.name = elem.attributeValue("name");
            config.minimap = Integer.parseInt(elem.attributeValue("minimap"));
            config.playershowmode = Integer.parseInt(elem.attributeValue("playershowmode"));
            config.textmode = Integer.parseInt(elem.attributeValue("textmode"));
            config.playercount = Integer.parseInt(elem.attributeValue("playercount"));
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
                throw new Exception("机型配置错误，配置不存在：" + configID);
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
            elem.addAttribute("minimap", String.valueOf(config.minimap));
            elem.addAttribute("playershowmode", String.valueOf(config.playershowmode));
            elem.addAttribute("textmode", String.valueOf(config.textmode));
            elem.addAttribute("playercount", String.valueOf(config.playercount));
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
        saveDocument(doc, new FileWriter(configFile));
    }
    
    public static void saveDocument(Document doc, Writer w) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        XMLWriter writer = new XMLWriter(w, format);
        try {
			writer.write(doc);
		} catch (IOException e) {
			log.error(e, e);
		}finally{
			 try {
				writer.close();
			} catch (IOException e) {
			}
		}
       
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
        ModelInputType obj = modelInputTypeMap.get(model);
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
        ModelInputType obj = modelInputTypeMap.get(model);
        if (obj == null) {
            return 0;
        } else {
            return obj.mouseType;
        }
    }
}
