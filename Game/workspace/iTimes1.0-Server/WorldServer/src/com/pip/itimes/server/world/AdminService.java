package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.Iterator;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Admin;
import com.pip.itimes.server.dao.AdminDao;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.dao.DataAccessException;

import java.util.*;
/**
 * @author Jeffrey
 * @version 1.0
 */
public class AdminService {

    private AdminDao dao;

    private ConcurrentHashMap id2session = new ConcurrentHashMap();

    public AdminService(AdminDao dao) {
        this.dao = dao;
    }

    public Admin getAdmin(String name,String password){
        return dao.getAdmin(name,password);
    }

    public List getAdminList() {
    	return dao.getAdminList();
    }
    public void addAdmin(Admin adm){
        dao.addAdmin(adm);
    }
    public void deleteAdmin(Admin adm) {
    	try {
			dao.makeTransient(adm);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
    }
    public void receiveSegment(int playerId,UWAPSegment seg){

    }

    public void receiveData(int playerId,UWAPData data){
    	
    }

    public void onChatMessage(int type, int id, int toId, UWAPSegment data) {
    	Iterator ite = id2session.values().iterator();
        while(ite.hasNext()){
            AdminSession session = (AdminSession)ite.next();
            session.onChatMessage(type, id, toId, data);
        }
    }

    public boolean receiveChatMessage(int srcId,String srcName,String destName,String msg){
        boolean ret = false;
        if(destName.toLowerCase().equals("gm")){
            Iterator ite = id2session.values().iterator();
            while(ite.hasNext()){
                AdminSession session = (AdminSession)ite.next();
//                if(session.isKeepWatch())
                    session.receiveMessage(srcId, srcName, msg);
                    ret = true;

            }
        }else{
            String name = destName.substring(3,destName.length()-1);
            Iterator ite = id2session.values().iterator();
            while(ite.hasNext()){
                AdminSession session = (AdminSession)ite.next();
                if(session.getAdmin().getName().equals(name)){
                    session.receiveMessage(srcId, srcName, msg);
                    ret = true;
                }
            }
        }
        return ret;
    }

    public void broadcast(UWAPSegment seg){
        Iterator ite = id2session.values().iterator();
        while(ite.hasNext()){
            AdminSession session = (AdminSession)ite.next();
            session.write(seg);
        }
    }

    public void registry(AdminSession session){
        AdminSession s = (AdminSession)id2session.get(new Integer(session.getId()));
        if(s!=null){
            s.close();
        }
        id2session.put(new Integer(session.getId()),session);
    }
    public void unRegistry(AdminSession session){
        id2session.remove(new Integer(session.getId()));
    }

    public void updateSosMessageStatus(int id, int status, int sourceSessionId) {
    	for (Object key : id2session.keySet()) {
    		Integer sid = (Integer)key;
    		if (sid.intValue() != sourceSessionId) {
    			((AdminSession)id2session.get(sid)).updateSosMessageStatus(id, status);
    		}
    	}
    }
    public void broadcastSosMessage(com.pip.itimes.server.bean.Mail mail ) {
    	Iterator ite = id2session.values().iterator();
        while(ite.hasNext()){
            AdminSession session = (AdminSession)ite.next();
            session.sendSosMessage(mail);
        }
    }
    public ArrayList<AdminSession> listOnlines() {
    	ArrayList<AdminSession> ret = new ArrayList<AdminSession>();
    	Iterator ite = id2session.values().iterator();
        while(ite.hasNext()){
            ret.add((AdminSession)ite.next());
        }
    	return ret;
    }
}
