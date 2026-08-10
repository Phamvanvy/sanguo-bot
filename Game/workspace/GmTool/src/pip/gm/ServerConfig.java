package pip.gm;

import java.util.Vector;

import javax.swing.ImageIcon;

import pip.gm.fw.AbstractGmForm;
import pip.gm.fw.BaseConfig;
import pip.util.UiUtil;

/**
 * 游戏服务器配置列表.包含配置文件,游戏服务器名称,启动引擎等.
 * 每个游戏服务器最多挂接一个GM客户端实例(单连接).
 */
public class ServerConfig {
	/** 以XML形式存在的配置文件.在启动连接时初始化更多参数选用 */
    public String configFile; 
    /** 游戏服务器显示的名称 */
    public String name; 
    /** 驱动游戏连接的GM客户端的启动类名 */
    public String gmFormClass; // class for loading
    /** 基本的配置文件 */
    public BaseConfig cfg = new BaseConfig();
    /** 游戏服务器GM客户端实例 */
    public AbstractGmForm ins;
    /** GM客户端名称,用来在服务器列表中显示 */
   
    public static ImageIcon closeIcon;
	static {
		closeIcon   =   UiUtil.getIcon("/closedWorld.png");
		
	}
    public String toString() {
    	if (ins != null) {
    		return name + ins.getServerState();
    	}
    	return name + "(Not Active)";
    }
    public ImageIcon getIcon() {
    	if (ins == null) {
    		return closeIcon;
    	}
    	return ins.getIcon();
    }
    public Class getGmFormClass() {
    	try {
			return Class.forName(gmFormClass);
		} catch (ClassNotFoundException e) {
		}
		return null;
    }
    public ServerConfig(String name) {
    	 this.name = name;
    	 subConfigs = new Vector<ServerConfig>();
    }
    private Vector<ServerConfig> subConfigs;
    public Vector<ServerConfig> getSubConfigs() {
    	return subConfigs;
    }
    public void addConfig(ServerConfig c) {
    	if (subConfigs != null) {
    		subConfigs.add(c);
    	}
    }
    public ServerConfig(String name, String xml) {
        this.configFile = xml;
        this.name = name;
        try {
            cfg.init(xml);
            if (cfg.title != null) {
                this.name = cfg.title;
            }
            if (cfg.game.equals("FIT") || cfg.game.equals("幻想")) {
            	gmFormClass = "fit.gm.ui.GameForm";
            } else if (cfg.game.equals("WLL2") || cfg.game.equals("武林")) {
            	gmFormClass = "wll.gm.ui.GameForm";
            } else if (cfg.game.equals("xkx-sky") || cfg.game.equals("侠客行")) {
            	gmFormClass = "xkx.gm.ui.GameForm";
            } else {
            	gmFormClass = cfg.game + ".gm.ui.GameForm";
            }
        } catch (Exception ex) {
        }

    }

}
