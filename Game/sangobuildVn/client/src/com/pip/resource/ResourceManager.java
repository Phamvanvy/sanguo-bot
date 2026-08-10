package com.pip.resource;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimateCache;
import com.pip.image.ImageSet;
import com.pip.io.UASegment;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameWorld;
import com.pip.ui.VM;
import com.pip.ui.VMGame;
import com.pip.util.SortHashtable;


/**
 * 资源管理器
 * @author leo
 *
 */
public class ResourceManager{
    private SortHashtable imageCache = new SortHashtable();
    private int clientDataVersion = 0;
    /**
     * mapping local resources' (client file cache and jar file resources) name to version<br/>
     * Version of resources in client cache are marked |{@link #DB_FILE_MARK}
     */
    private Hashtable localTable = new Hashtable();
    private Hashtable pkgTable = new Hashtable();
    private ResourceDatabaseRMS clientFileDb;
    private ResourceDatabaseRMS dataDb;
    private Vector remainUpdateList = new Vector();
    public SortHashtable currentUpdateTable = new SortHashtable();//此次更新的内容

    private int mode;
    private byte dbCount;
    private int dbLimit;
    private int configSaveTicks;
    private int nextSaveTick;
    
    private byte[] clientDataUpdate = null;

  //#ifdef buildtest
    private boolean debugMode = true;
  //#endif
    
    private static final int MODE_NORMAL = 0;
    private static final int MODE_UPDATING = 1;
    private static final int MODE_UPDATED = 2;
    private static final int MODE_NEED_INIT = 3;

    private static final String RES_CLIENT_PREFIX = "sanguo_clt_";
    private static final String RES_DATA_PREFIX = "sanguo_db_";

    /**
     * 关卡数据文件
     */
    public static final String POSTFIX_PKG = ".pkg";
    /**
     * 动画文件
     */
    public static final String POSTFIX_CTN = ".ctn";
    /**
     * 图片文件
     */
    public static final String POSTFIX_PIP = ".pip";
    /**
     * 脚本文件
     */
    public static final String POSTFIX_ETF = ".etf";
    /**
     * png文件
     */
    public static final String POSTFIX_PNG = ".png";

    /**
     * 关卡类型
     */
    public static final byte TYPE_PKG = 0;
    /**
     * 动画类型
     */
    public static final byte TYPE_CTN = 1;
    /**
     * 图片类型
     */
    public static final byte TYPE_PIP = 2;
    /**
     * 脚本类型
     */
    public static final byte TYPE_ETF = 3;
    /**
     * png类型
     */
    public static final byte TYPE_PNG = 4;

    public ResourceManager(){
        mode = MODE_NORMAL;
      //#ifdef buildtest
        dbCount = 16;
        dbLimit = 200 * 1024;
      //#endif
        
        //#= dbCount = ${Resource-Database-Count};
        //#= dbLimit = ${Resource-Database-Size} * 1024;
                
        configSaveTicks = 100;
        nextSaveTick = configSaveTicks;

        clientFileDb = new ResourceDatabaseRMS((byte)1, Integer.MAX_VALUE, RES_CLIENT_PREFIX, true);
        dataDb = new ResourceDatabaseRMS(dbCount, dbLimit, RES_DATA_PREFIX, false);
    }
    
    public void clearWholeData(){
        Tool.deleteRMSFile("clientdata.db");
        clientFileDb.clearDatabase();
        dataDb.clearDatabase();
    }
    
    public void clear(){
        saveResourceInfo(true);
        
        if(clientDataUpdate != null){
            VM.saveRMSFile("clientdata.db", clientDataUpdate);
            clientDataUpdate = null;
        }        
        
        int count = currentUpdateTable.size();
        if(count > 0) {
            Object[] keys = currentUpdateTable.keys();
            for(int i=0; i<keys.length; i++) {
            	imageCache.remove(keys[i]);
            }
        } else {
        	imageCache.clear();
        }
        pkgTable.clear();
    }
    
    public void initManager(UASegment segment){
        mode = MODE_NORMAL;
        segment.handled = true;
    }
    /**
     * 解析客户端的资源名称和版本号
     * @see {@link #loadLocalTable()}
     */
    public void loadResourceInfo(){
    	if(clientFileDb.loadInformation()) {
    		Tool.deleteRMSFile("clientdata.db");
    	}        
        loadLocalTable();
        dataDb.loadInformation();
        
      //#ifdef buildtest
        System.out.println("Resource Info Loaded");
      //#endif
    }

    public void saveResourceInfo(boolean saveWholeData){
        if(saveWholeData){
            clientFileDb.saveInformation(saveWholeData);
            dataDb.saveInformation(saveWholeData);
        }else{
            if(clientFileDb.isDirty()){
                GameMain.resourceAsynLoader.addDatabaseAction(ResourceAsynLoader.AYSN_LOAD_TYPE_SAVE_DATABASE_INFOMATION, clientFileDb, null, null, saveWholeData? VM.TRUE: VM.FALSE);
            }
            
            if(dataDb.isDirty()){
                GameMain.resourceAsynLoader.addDatabaseAction(ResourceAsynLoader.AYSN_LOAD_TYPE_SAVE_DATABASE_INFOMATION, dataDb, null, null, saveWholeData? VM.TRUE: VM.FALSE);
            }
        }
    }

    public void cycle(){
        if(GameMain.tick >= nextSaveTick){
            nextSaveTick = GameMain.tick + configSaveTicks;
            //saveResourceInfo(false);
        }
    }

    public byte[] findResource(String name){
        byte[] result = null;

        if(isLocalResource(name)){
            result = clientFileDb.loadFile(name);

            if(result == null){
                result = Tool.loadLocalResource(getLocalName(name));
            }
            
          //#ifdef buildtest
            if(debugMode && name.endsWith(POSTFIX_ETF)){
                result = Tool.loadLocalResource(getLocalName(name));
            }

            System.out.println("load Resource from client database : " + name);
          //#endif
        }else{
            if(name.endsWith(".pkg")) {
            	result = dataDb.loadFile(name);
            		
            	if(result == null && pkgTable.containsKey(name)) {
            		result = Tool.loadLocalResource(getLocalName(name));
            	}
            } else {
            	result = dataDb.loadFile(name);
            }
          //#ifdef buildtest
            if(debugMode && name.endsWith(POSTFIX_ETF)){
                result = Tool.loadLocalResource(getLocalName(name));
            }
            
            System.out.println("load Resource from data database : " + name);
          //#endif
        }
        
        return result;
    }

    public ImageSet findImageSet(String name, boolean cache){
        ImageSet result = (ImageSet)imageCache.get(name);

        if(result == null){
            byte[] data = findResource(name);
            try {
                result = new ImageSet(data);
    
                if(cache){
                    imageCache.put(name, result);
                }
            } catch (Exception e) {
            	//#ifdef buildtest
                e.printStackTrace();
              //#endif
            }

          //#ifdef buildtest
            System.out.println("load ImageSet from Resource : " + name);
          //#endif
        }
        //#ifdef buildtest
        else{        	
            System.out.println("load ImageSet from ImageCache : " + name);          
        }
      //#endif

        return result;
    }

    public void requestResource(String name){
        byte[] data = findResource(name);

        if(data == null){
            Tool.sendGetFile(name);

          //#ifdef buildtest
            System.out.println("download new Resource : " + name);
          //#endif
        }

        if(data != null){
            notifyGame(name, -1, data);
        }
    }

    public void recvResource(String name, int version, byte[] data){
        boolean isUpdate = false;
        
        if(remainUpdateList.size() > 0){
            if(remainUpdateList.contains(name)){
                remainUpdateList.removeElement(name);
                isUpdate = true;
            }

            if(remainUpdateList.size() == 0){
                mode =  MODE_UPDATED;
            }
        }

        if(isUpdate){
            clientFileDb.saveFile(name, data, version);
            
            if(remainUpdateList.size() == 0){
                saveResourceInfo(true);
            }
        }else{
            if(isLocalResource(name)){
                GameMain.resourceAsynLoader.addDatabaseAction(ResourceAsynLoader.AYSN_LOAD_TYPE_SAVE_DATABASE_FILE, clientFileDb, name, data, version);
            }else{
                GameMain.resourceAsynLoader.addDatabaseAction(ResourceAsynLoader.AYSN_LOAD_TYPE_SAVE_DATABASE_FILE, dataDb, name, data, version);
            }
        }

        if(!isUpdate){
            notifyGame(name, version, data);
        }
    }

    public void syncVersion(boolean whole){
        try{
            Vector dataList = dataDb.getFileNameList();
           
            // 计算一共要上传多少个文件版本信息给服务器比对
            
            // localTable 的文件全部上传服务器比对
            int realcount = localTable.size();
            Enumeration emu;            
            if(whole){
            	// 统计内置文件中有多少要上传服务器比对
                emu = pkgTable.keys();
                while(emu.hasMoreElements()){
                    String name = (String)emu.nextElement(); 
                    if(!dataDb.hasFile(name)){
                    	realcount++;
                    }
                }

                // 缓存文件全部上传服务器比对
                realcount += dataList.size();
            }
            
            // 构造协议包
            UASegment segment = new UASegment(Tool.CONN_VERSION_COMPARE_CLIENT);
            segment.writeString(GameMain.getUIModel());
            segment.writeString(GameMain.getClientVersion());
            segment.writeString(GameMain.getModel());
            segment.writeInt(clientDataVersion);

            segment.writeShort((short)realcount);
            emu = localTable.keys();
            
            while(emu.hasMoreElements()){
                String name = (String)emu.nextElement();
                segment.writeString(name);
                segment.writeInt(((Integer)localTable.get(name)).intValue() & 0x7FFFFFFF);   
            }

            if(whole){
            	
                emu = pkgTable.keys();
                
                while(emu.hasMoreElements()){
                    String name = (String)emu.nextElement();
                    
                    if(!dataDb.hasFile(name)){
                    	segment.writeString(name);
                    	segment.writeInt(((Integer)pkgTable.get(name)).intValue() & 0x7FFFFFFF);
                    }
                }

                for(int i = 0; i < dataList.size(); i++){
                    String name = (String)dataList.elementAt(i);
                    segment.writeString(name);
                    segment.writeInt(dataDb.getFileVersion(name));
                }
            }
            Utilities.sendRequest(segment);
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }
    }
    /**
     * 接受服务器发来的版本比较结果；<br/>
     * 包含多个条目，条目可能是client.data数据,或者要删除的资源
     * @param segment
     */
    public void recvSyncVersion(UASegment segment){
        int count = segment.readShort();
        Vector updateList = new Vector();
        currentUpdateTable.clear();
        
        for(int i = 0; i < count; i++){
            String name = segment.readString();
            int version = segment.readInt();

            if (name.equals("client.data")) {
                // 更新本地client.data文件
                byte[] data = segment.readBytes();
                byte[] data2 = new byte[data.length + 4];
                Tool.setInt(data2, 0, version);
                System.arraycopy(data, 0, data2, 4, data.length);
                clientDataUpdate = data2;
                continue;
            }
            
//            //#if ModelID == Lenovo || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad
//            //2010年6月1日之前不更新
//            if(System.currentTimeMillis() < 1275324528484L) {
//                if(name.startsWith("ui_gamemenu") || name.startsWith("ui_mainmenu") || name.startsWith("game_init") || name.startsWith("ui_menuitems.pip")) {
//                	continue;
//                }
//            }
//            //#endif
            
            if((version & 0x80000000) != 0){
                localTable.put(name, new Integer(version));
                clientFileDb.updateFile(name);
                updateList.addElement(name);
                currentUpdateTable.put(name, name);

              //#ifdef buildtest
                System.out.println("new resource Version : " + name);
              //#endif
            }else{
            	if(!dataDb.hasFile(name) && pkgTable.containsKey(name)){
            		pkgTable.remove(name);
            	}else{
            		dataDb.updateFile(name);
            	}

              //#ifdef buildtest
                System.out.println("new resource Version : " + name);
              //#endif
            }
        }

        remainUpdateList = updateList;

        //启动强制更新
        if(remainUpdateList.size() > 0){
            mode = MODE_UPDATING;
            //VMGame.loadVMGame("ui_update", VMGame.VM_TYPE_UI, true);
        }
    }
    
    /**
     * 清理客户端存储的资源文件，将需要更新的资源去除掉。
     */
    public void clearClientFileDb(){
        if(remainUpdateList.size() > 0){
            Vector savedFileNames = clientFileDb.getFileNameList();
            Vector cacheVersion = new Vector();
            Vector cacheData = new Vector();
            
            for(int i = 0; i < savedFileNames.size(); i++){
                String name = (String)savedFileNames.elementAt(i);
                
                if(remainUpdateList.contains(name)){
                    cacheData.addElement(new byte[0]);
                }else{
                    cacheData.addElement(clientFileDb.loadFile(name));
                }
                
                cacheVersion.addElement(new Integer(clientFileDb.getFileVersion(name)));
            }
            
            clientFileDb.truncateDatabase();
            
            for(int i = 0; i < savedFileNames.size(); i++){
                String name = (String)savedFileNames.elementAt(i);
                byte[] data = (byte[])cacheData.elementAt(i);
                int version = ((Integer)cacheVersion.elementAt(i)).intValue();
                
                if(data.length > 0){
                    clientFileDb.saveFile(name, data, version);
                }
            }
        }
    }

    public int getUpdateMode(){
        return mode;
    }

    public String[] getRemainUpdateList(){
        String[] result = new String[remainUpdateList.size()];
        remainUpdateList.copyInto(result);

        return result;
    }

    /**
     * 加载客户端资源<b>名称</b>和<b>版本</b>的映射<br/>
     * @see localTabel {@link #localTable}
     */
    private void loadLocalTable(){
        localTable.clear();
        
        byte[] clientData = VM.loadRMSFile("clientdata.db");
        if (clientData != null) {
            clientDataVersion = Tool.getInt(clientData, 0);
            byte[] arr = new byte[clientData.length - 4];
            System.arraycopy(clientData, 4, arr, 0, clientData.length - 4);
            clientData = arr;
        } else {
            clientData = Tool.loadLocalResource("client.data");
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(clientData);
        DataInputStream dis = new DataInputStream(bis);
        
        try{
            int count = dis.readInt();
            
            for(int i = 0; i < count; i++){
                String name = Tool.readUTF(dis);
                int version = dis.readInt();
                localTable.put(name, new Integer(version));
            }
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            try{
                dis.close();
            }catch(Exception e){
            }
        }
        
        byte[] pkgData = Tool.loadLocalResource("pkg.data");
        bis = new ByteArrayInputStream(pkgData);
        dis = new DataInputStream(bis);
        
        try{
            int count = dis.readInt();
            
            for(int i = 0; i < count; i++){
                String name = Tool.readUTF(dis);
                int version = dis.readInt();
                pkgTable.put(name, new Integer(version));
            }
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        }finally{
            try{
                dis.close();
            }catch(Exception e){
            }
        }
        
        Vector clientDBFiles = clientFileDb.getFileNameList();
        
        for(int i = 0; i < clientDBFiles.size(); i++){
            String name = (String)clientDBFiles.elementAt(i);
            int version = clientFileDb.getFileVersion(name);
            localTable.put(name, new Integer(version | 0x80000000));
        }
    }
    
    private boolean isLocalResource(String name){
        Integer version = (Integer)localTable.get(name);
        
        if(version != null){
            return true;
        }else{
            return false;
        }
    }

    private void notifyGame(String name, int version, byte[] data){
        int type = resourceType(name);

        switch(type){
            case TYPE_PKG: {
                GameWorld.pkgData = data;
            }
                break;
            case TYPE_CTN: {
                AnimateCache.recvAnimate(getRecvName(name), data);
            }
                break;
            case TYPE_PIP: {
                GameMain.resourceAsynLoader.addLoad(ResourceAsynLoader.AYSN_LOAD_TYPE_IMAGE_BYTES, getRecvName(name), data);
            }
                break;
            case TYPE_ETF: {
                VMGame.recvEtfData(getRecvName(name), data);
            }
            default:
          		VMGame gameWorld = VMGame.getVMGame("game_world");
	      		if(gameWorld != null && gameWorld.getVM() != null) {
	      			VM gwvm = gameWorld.getVM();
	      			synchronized(gwvm){
	      				gwvm.callback("RevcFile", new int[]{gwvm.makeTempObject(name), version, gwvm.makeTempObject(data)});
		            }
	      		}
	      	
                break;
        }
    }

    private static byte resourceType(String name){
        byte result = -1;

        if(name.endsWith(POSTFIX_PKG)){
            result = TYPE_PKG;
        }else if(name.endsWith(POSTFIX_CTN)){
            result = TYPE_CTN;
        }else if(name.endsWith(POSTFIX_PIP)){
            result = TYPE_PIP;
        }else if(name.endsWith(POSTFIX_ETF)){
            result = TYPE_ETF;
        }else if(name.endsWith(POSTFIX_PNG)){
            result = TYPE_PNG;
        }

        return result;
    }

    private static String getLocalName(String name){
        if(resourceType(name) == TYPE_ETF){
            return name.substring(0, name.length() - POSTFIX_ETF.length()) + "_" + GameMain.getUIModel() + ".etf.gz";
        }

        return name;
    }

    private static String getRecvName(String name){
        if(resourceType(name) == TYPE_ETF){
            return name.substring(0, name.length() - POSTFIX_ETF.length());
        }

        return name;
    }
}