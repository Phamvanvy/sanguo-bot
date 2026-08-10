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
	protected Map<String, Integer> id2int = new HashMap<String, Integer>();
	protected Map<String, Integer> deprivedMaxVersions = new HashMap<String, Integer>();
	

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
        Map<String,Integer> verInts = new HashMap<String,Integer>();
        Map<String,Integer> verDeprived = new HashMap<String,Integer>();
        for(Iterator i = root.elementIterator("group"); i.hasNext();){
        	Element node = (Element) i.next();
        	List<Version> l = buildVersions(node);
        	for(Version v:l){
        		vers.put(v.id, v);
        		verInts.put(v.id, getVersionIntValue(v.id));
        	}
        }
        for(Iterator i = root.elementIterator("deprived"); i.hasNext();){
        	Element node = (Element) i.next();
        	for (Iterator iterator = node.elementIterator("model"); iterator.hasNext();) {
        		Element model = (Element) iterator.next();
        		String name = model.attributeValue("name");
            	String maxVer = model.attributeValue("maxversion");
            	int maxVerInt = getVersionIntValue(maxVer);
            	verDeprived.put(name, maxVerInt);
			}
        }
        id2version = vers;
        id2int = verInts;
        deprivedMaxVersions = verDeprived;
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
	
	public synchronized Version getVersion(String id){
		return id2version.get(id);
	}
	
	public synchronized int getVersionInt(String id){
		if(id2int.containsKey(id)){
			return id2int.get(id);
		} else {
			return 0;
		}
	}
	
	/**
	 * 获得版本号的int形式，保留两位小数,例如：2.7 ==> 270 ,2.8.1 ==> 281等
	 * @param versionStr
	 * @return
	 */
	private int getVersionIntValue(String versionStr){
		StringBuilder sb = new StringBuilder();
		char ch;

		int _dotCnt = 0;
		for(int i=0;i<versionStr.length();i++){
			ch = versionStr.charAt(i);
			if(ch != '.'){
				sb.append(ch);
			} else {
				_dotCnt++;
			}
		}
		
		String ret = sb.toString();
		if(_dotCnt == 1){
			ret = ret.concat("0");
		}
		int version = Integer.parseInt(ret);
		return version;
	}
	
	/**
	 * 检查此机型此版本是否已被废
	 * @param uimodel
	 * @param version
	 * @return
	 */
	public boolean isDeprivedVersion(String uimodel,String version){
		boolean ret = false;
		if(deprivedMaxVersions.containsKey(uimodel)){
			int maxVersion = deprivedMaxVersions.get(uimodel);
			int verInt = getVersionInt(version);
			if(verInt <= maxVersion){
				ret = true;
			}
		}
		
		return ret;
	}

}
