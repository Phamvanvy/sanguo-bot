package peony.service.version;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Version;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;

public class VersionService implements Service, ServiceEventListener{

	protected Map<String, Version> id2version = new HashMap<String, Version>();

	

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_PLAYER_FIRSTLOAD, // 角色在进入地图以后发送load信息
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			playerFirstLoaded((Player) event.param1);
			break;
		}
	}
	
	protected void playerFirstLoaded(Player p){
		if(p!=null&&p.session!=null){
			Account account = (Account)p.session.getIdentity();
			if(account != null){
				Version v = account.getVersion();
				if(v != null&&v.status==Version.STATUS_CANCELED){
					if(v.script != null){
						Packet pt = new Packet(OpCode.OPENUI_SERVER);
						pt.putString(v.script);
						pt.putString("");
						p.send(pt);
					}
				}
			}
		}
	}
	
	public VersionService() throws Exception {
		reload();
	}
	
	@SuppressWarnings("unchecked")
	public void reload() throws Exception {
	    File f = new File(System.getProperty("user.dir") + "/version.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(f);
        Element root = doc.getRootElement();
        Map<String, Version> vers = new HashMap<String, Version>();
        for(Iterator i = root.elementIterator("group"); i.hasNext();){
        	Element node = (Element) i.next();
        	List<Version> l = buildVersions(node);
        	for(Version v:l){
        		vers.put(v.id, v);
        	}
        }
        id2version = vers;
	}
	
	@SuppressWarnings("unchecked")
	protected List<Version> buildVersions(Element group){
    	String url = group.attributeValue("obsoleteurl");
    	String script = group.attributeValue("canceledscript");
    	String model = group.attributeValue("model");
    	List<Version> ret = new ArrayList<Version>();
        for (Iterator i = group.elementIterator("version"); i.hasNext();) {
            Element node = (Element) i.next();
            String id = node.attributeValue("id");
            int status = Version.STATUS_CANCELED;
            String statusString = node.attributeValue("status");
            if ("current".equals(statusString)) {
                status = Version.STATUS_CURRENT;
            } else if ("obsolete".equals(statusString)) {
                status = Version.STATUS_OBSOLETE;
            } else if ("canceled".equals(statusString)) {
                status = Version.STATUS_CANCELED;
            }
            int maxLevel = Integer.parseInt(node.attributeValue("maxlevel"));
            String desc = node.attributeValue("description");
            String message = node.getText();
            Version version = new Version(id, status, maxLevel, desc, message,model,url,script);
            ret.add(version);
        }
        return ret;
	}

	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}
	
	public Version getVersion(String id){
		return id2version.get(id);
	}

}
