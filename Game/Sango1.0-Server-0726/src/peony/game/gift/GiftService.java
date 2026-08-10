package peony.game.gift;

import java.util.Iterator;
import java.util.List;

import peony.game.Server;
import peony.service.Service;
import ch.javasoft.util.intcoll.IntHashMap;

import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GiftGroup;

public class GiftService implements Service, DataChangeHandler {
	
	protected IntHashMap<GiftGroup> groups = new IntHashMap<GiftGroup>();
	
	
	public void shutdown() {
		
	}
	
	@SuppressWarnings("unchecked")
	public void startup() throws Exception {
		List groups = Server.server.getServiceRegistry().getDataService().data
				.getDataListByType(GiftGroup.class);
		Iterator ite = groups.iterator();
		while (ite.hasNext()) {
			GiftGroup g = (GiftGroup)ite.next();
			addGiftGroup(g);
		}
	}

	public void addGiftGroup(GiftGroup group){
		groups.put(group.getID(), group);
	}
	
	public GiftGroup getGiftGroup(int id){
		return groups.get(id);
	}

    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof GiftGroup) {
            addGiftGroup((GiftGroup)obj);
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof GiftGroup) {
            groups.remove(obj.id);
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
    }
}
