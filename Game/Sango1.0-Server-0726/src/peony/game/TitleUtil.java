package peony.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import ch.javasoft.util.intcoll.IntHashMap;


public class TitleUtil implements DataChangeHandler {
	
	public static final IntHashMap<Title> titles = new IntHashMap<Title>();
	
	public static final List<Title> weiTitles = new ArrayList<Title>();
	public static final List<Title> shuTitles = new ArrayList<Title>();
	public static final List<Title> wuTitles = new ArrayList<Title>();
	public static final List<Title> neturalTitles = new ArrayList<Title>(); //朝廷官职
	public static final List<Title> otherTitles = new ArrayList<Title>();

	@SuppressWarnings("unchecked")
	public static void load() {
		List gas = Server.server.getServiceRegistry().getDataService().data
				.getDataListByType(com.pip.sanguo.data.Title.class);
		Iterator ite = gas.iterator();
		while (ite.hasNext()) {
			com.pip.sanguo.data.Title t = (com.pip.sanguo.data.Title) ite
					.next();
			translateTitle(t);
		}
	}
	
	private static void translateTitle(com.pip.sanguo.data.Title t) {
	    Title title = new Title();
	    title.create(t.getID(), t.level, t.getTitle(), t.type,
                t.faction.id, t.salary, t.price, t.buffID, t.buffLevel,t.description);
        titles.put(title.id, title);
        if(title.type==Title.TYPE_OFFICIAL){
            neturalTitles.add(title);
        }
        else if(title.type==Title.TYPE_COUNTRY){
            if(title.faction==GameObject.FACTION_WEI){
                weiTitles.add(title);
            }
            else if(title.faction==GameObject.FACTION_SHU){
                shuTitles.add(title);
            }
            else if(title.faction==GameObject.FACTION_WU){
                wuTitles.add(title);
            }
        }
        else if(title.type==Title.TYPE_OTHER){
            otherTitles.add(title);
        }
	}
	
	public static List<Title> getOtherTitles(){
		return otherTitles;
	}
	
	public static List<Title> getNeturalTitles(){
		return neturalTitles;
	}
	
	public static List<Title> getCountryTitles(int faction){
		if(faction==GameObject.FACTION_WEI){
			return weiTitles;
		}
		else if(faction==GameObject.FACTION_SHU){
			return shuTitles;
		}
		else if(faction==GameObject.FACTION_WU){
			return wuTitles;
		}
		throw new IllegalArgumentException();
	}
	
	public static Title getTitle(int id){
		return titles.get(id);
	}

    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof com.pip.sanguo.data.Title) {
            translateTitle((com.pip.sanguo.data.Title)obj);
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof com.pip.sanguo.data.Title) {
            titles.remove(obj.id);
            for (int i = 0; i < weiTitles.size(); i++) {
                if (weiTitles.get(i).id == obj.id) {
                    weiTitles.remove(i);
                    break;
                }
            }
            for (int i = 0; i < shuTitles.size(); i++) {
                if (shuTitles.get(i).id == obj.id) {
                    shuTitles.remove(i);
                    break;
                }
            }
            for (int i = 0; i < wuTitles.size(); i++) {
                if (wuTitles.get(i).id == obj.id) {
                    wuTitles.remove(i);
                    break;
                }
            }
            for (int i = 0; i < neturalTitles.size(); i++) {
                if (neturalTitles.get(i).id == obj.id) {
                    neturalTitles.remove(i);
                    break;
                }
            }
            for (int i = 0; i < otherTitles.size(); i++) {
                if (otherTitles.get(i).id == obj.id) {
                    otherTitles.remove(i);
                    break;
                }
            }
        }
    }
    
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    public void dataObjectChanging(DataObject obj) {
    }
    
    /**
     * 对象被修改通知。
     * @param newobj 修改后的新对象
     */
    public void dataObjectChanged(DataObject newobj) {
        if (newobj instanceof com.pip.sanguo.data.Title) {
            com.pip.sanguo.data.Title t = (com.pip.sanguo.data.Title)newobj;
            Title oldTitle = titles.get(newobj.id);
            int oldType = oldTitle.type;
            int oldFaction = oldTitle.faction;
            if (oldTitle != null) {
                oldTitle.create(t.getID(), t.level, t.getTitle(), t.type,
                    t.faction.id, t.salary, t.price, t.buffID, t.buffLevel,t.description);
            }
            if(oldType!=oldTitle.type){
            	if(oldType==Title.TYPE_OFFICIAL){
            		neturalTitles.remove(oldTitle);
            	}
            	else if(oldType==Title.TYPE_OTHER){
            		otherTitles.remove(oldTitle);
            	}
            	else if(oldType==Title.TYPE_COUNTRY){
            		if(oldFaction==GameObject.FACTION_WEI){
            			weiTitles.remove(oldTitle);
            		}
            		else if(oldFaction==GameObject.FACTION_SHU){
            			shuTitles.remove(oldTitle);
            		}
            		else if(oldFaction==GameObject.FACTION_WU){
            			wuTitles.remove(oldTitle);
            		}
            	}
            	if(oldTitle.type==Title.TYPE_OFFICIAL){
            		neturalTitles.add(oldTitle);
            	}
            	else if(oldTitle.type==Title.TYPE_OTHER){
            		otherTitles.add(oldTitle);
            	}
            	else if(oldTitle.type==Title.TYPE_COUNTRY){
            		if(oldTitle.faction==GameObject.FACTION_WEI){
            			weiTitles.add(oldTitle);
            		}
            		else if(oldTitle.faction==GameObject.FACTION_SHU){
            			shuTitles.add(oldTitle);
            		}
            		else if(oldTitle.faction==GameObject.FACTION_WU){
            			wuTitles.add(oldTitle);
            		}
            	}
            }
        }
    }
}
