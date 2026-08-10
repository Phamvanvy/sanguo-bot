package peony.game;

import java.util.LinkedList;
import java.util.List;

import peony.game.changed.ChangedItem;
import peony.game.changed.PacketChangedItemVisitor;
import peony.net.ClientSession;
import peony.net.Packet;


/**
 * 用于计算用户一个cycle的属性，背包以及其他的改变量，这些改变量分为需要客户端提醒用户的部分以及不需要客户端提醒用户的部分
 * 此类是单线程类，不能用于多线程操作
 * @author Jeffrey
 *
 */
public class Changed {
	
	protected boolean isChanged;
	
	protected List<ChangedItem>[] noNotifies = new List[ChangedItem.TYPE_MAX+1];
	protected List<ChangedItem>[] notifies = new List[ChangedItem.TYPE_MAX+1];
	
	public Changed(){
		for(int i=0;i<noNotifies.length;i++){
			noNotifies[i] = new LinkedList<ChangedItem>();
		}
		for(int i=0;i<notifies.length;i++){
			notifies[i] = new LinkedList<ChangedItem>();
		}
	}
	
	public synchronized void addChangedItem(ChangedItem changedItem){
		isChanged = true;
		List<ChangedItem> l = null;
		if(changedItem.isNotif()){
			l = notifies[changedItem.getType()];
		}else{
			l = noNotifies[changedItem.getType()];
		}
		for(ChangedItem ci:l){
			if(ci.merge(changedItem))
				return;
		}
		l.add(changedItem);
	}
	
	public synchronized boolean isChanged(){
		return isChanged;
	}
	
	public synchronized void sendAndClean(ClientSession session, Player owner){
		Packet pt = new Packet(OpCode.SYNC_PLAYER_SERVER);
		owner.pv.init(pt, owner);
		pt.put(getCount(noNotifies));
		visitAll(owner, noNotifies);
		pt.put(getCount(notifies));
		visitAll(owner, notifies);
		isChanged = false;
		owner.pv.init(null, null);
		if(session!=null)
			session.send(pt);
	}
	
	public synchronized void clean(){
		for(List<ChangedItem> l:noNotifies){
			l.clear();
		}
		for(List<ChangedItem> l:notifies){
			l.clear();
		}
	}
	
	protected void visitAll(Player owner, List<ChangedItem>[] cs){
		for(List<ChangedItem> l:cs){
			for(ChangedItem changedItem:l){
				changedItem.accept(owner.pv);
			}
			l.clear();
		}
	}
	
	protected int getCount(List<ChangedItem>[] cs){
		int ret = 0;
		for(List<ChangedItem> l:cs){
			ret += l.size();
		}
		return ret;
	}
}
