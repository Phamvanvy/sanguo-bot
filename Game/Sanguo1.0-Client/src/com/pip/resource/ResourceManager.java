package com.pip.resource;

//#if NewUI2
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
//#endif

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;

import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimateCache;
import com.pip.image.ImageSet;
import com.pip.io.UASegment;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameWorld;
import com.pip.sanguo.SanguoMIDlet;
import com.pip.ui.VM;
import com.pip.ui.VMGame;
import com.pip.util.SortHashtable;


/**
 * 资源管理器
 * @author leo
 *
 */
public class ResourceManager{
	//#if NewUI2
	public SortHashtable imageCache = new SortHashtable();
	//#else
	//# private SortHashtable imageCache = new SortHashtable();
	//#endif
    
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

    private Hashtable fileDatas = new Hashtable();		//文件已下载的数据
    private Hashtable fileDataNums = new Hashtable();	//文件已下载的数据大小
    
    private int curUpdate;	//当前更新的数据大小
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
    
//#if NewUI2    
    
    /**
     * 资源包内文件版本号文件(CRC),GlobalVar
     */
    public static final String VAR_PACK_DATA = "varPackData";
    /**
     * 资源包描述文件本身版本号文件(CRC),GlobalVar
     */
    public static final String VAR_NEED_PACK = "varNeedPackData";
    
    /**
     * 是否使用资源包
     */
//#=    public static final boolean USE_RESOURCE_PACK = ${UseResPack};
    /**
     * 是否已经载入资源包版本文件pack.data
     */
    public boolean hadLoadPackData = false;
    /**
     * 资源包文件名与版本号映射
     */
    public Map<String,Integer> localPackTable = new HashMap<String,Integer>();
    
    /**
     * npc资源
     */
    public static final int PACK_DATA_NPC = 1;
    /**
     * 关卡资源
     */
    public static final int PACK_DATA_MAP = (1 << 1);
    /**
     * 音乐资源
     */
    public static final int PACK_DATA_MUSIC = (1 << 2);
    /**
     * 脚本资源
     */
    public static final int PACK_DATA_SCRIPTS = (1 << 3);
    /**
     * 资源包版本描述文件名pack_${UIModel}${config}.data
     * config:0x1111,右数第一位表示是否包含NPC,第二位表示是否包含关卡，第三位表示是否包含MP3
     * 第四位表示是否包含脚本
     */
//#=    public static final String PACK_TABLE_FILENAME = "${ResPackInfoName}";
    /**
     * pack.data的版本号
     */
    private int packTableVersion = 0;
    /**
     * RMS中pack.data的版本号
     */
    private int packTableRmsVersion = 0;
    
    public int mapSize = 0; //关卡包大小(Byte)
    
//# public static final SortHashtable findImageSets = new SortHashtable();

//#else    
    public static final boolean USE_RESOURCE_PACK = false;
    public static final String PACK_TABLE_FILENAME = "";
//#endif

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
    	//#if NewUI2
    	//# if(GameMain.resetType == 0 || mode != 0){
    	//# synchronized (findImageSets) {
    	//# 	Object[] keys0 = findImageSets.keys();
    	//#     for(int i=0; i<keys0.length; i++) {
    	//#     	ImageSet img = (ImageSet)keys0[i];
    	//#     	if(img != null){
    	//#     		img.unbind();
    	//#     	}
            	
    	//#     }
    	//#     findImageSets.clear();
    	//# }
    	//# }
        //#endif
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

        //#if NewUI2
        if(USE_RESOURCE_PACK){
        	if(hadLoadPackData == false){
        		loadPackTable();
        		Tool.setGlobalValue(VAR_PACK_DATA, PACK_TABLE_FILENAME);
        		Tool.setGlobalValue(VAR_NEED_PACK, VM.TRUE);
        	}
        }
        //#endif
        
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
        	/*result = Tool.loadLocalResource(getLocalName(name));
            if(result != null){
            	return result;
            }*/
        
        if(isLocalResource(name)){
            result = clientFileDb.loadFile(name);

            if(result == null){
                result = Tool.loadLocalResource(getLocalName(name));
            }
            
          //#ifdef buildtest
            if(debugMode && name.endsWith(POSTFIX_ETF)){
                result = Tool.loadLocalResource(getLocalName(name));
            }
//# 
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
//#             
            System.out.println("load Resource from data database : " + name);
          //#endif
        }

        //#if NewUI2
        //从资源包里找
        if(result == null && USE_RESOURCE_PACK){
			synchronized (localPackTable) {
				if(localPackTable.containsKey(name)){
	        		result = Tool.loadLocalResource(getLocalName(name));
	        		System.out.println("load resource in pool."+name);
	        	}
			}
        }
        //#endif
        
        return result;
    }
    
    //#if NewUI2
    /**
     * 载入内置资源包内容pack.data
     */
    public void loadPackTable(){
    	try {
    		//载入包里的pack.data数据
			InputStream is = SanguoMIDlet.instance.getClass().getResourceAsStream("/AndroidLarge/"+PACK_TABLE_FILENAME);
    		DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
    		int count = dis.readInt();
    		for (int i = 0; i < count; i++) {
				String name = dis.readUTF();
				int version = dis.readInt();
				localPackTable.put(name, new Integer(version));
				//#ifdef buildtest
				System.out.println("local pack:"+name+" version:"+version);
				//#endif
			}
    		
    		dis.close();
    		
    		is = SanguoMIDlet.instance.getClass().getResourceAsStream("/AndroidLarge/"+PACK_TABLE_FILENAME+".ver");
    		dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
    		packTableVersion = dis.readInt();
    		dis.close();
    		
    		//载入RMS里的pack.data数据
    		if(dataDb.hasFile(PACK_TABLE_FILENAME)){
    			byte[] result = dataDb.loadFile(PACK_TABLE_FILENAME);
    			dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(result))));
    			syncLocalPackTable(dis);
    			dis.close();
    			
    			//更新packTableVersion
    			result = dataDb.loadFile(PACK_TABLE_FILENAME+".ver");
    			dis = new DataInputStream(new ByteArrayInputStream(result));
    			packTableRmsVersion = dis.readInt();
    			packTableVersion = packTableRmsVersion;
    			dis.close();
    		}
    		
    		
    		hadLoadPackData = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * 同步Pack.data
     * @param dis
     */
    public void syncLocalPackTable(DataInputStream dis){
    	try {
    		int count = dis.readInt();
    		for (int i = 0; i < count; i++) {
    			String fileName = dis.readUTF();
    			int fileVersion = dis.readInt();
    			if(localPackTable.containsKey(fileName)){
    				Integer obj = localPackTable.get(fileName);
    				if(obj.intValue() != fileVersion){
    					localPackTable.remove(fileName);
    					System.out.println("pack file out of date:"+fileName);
    				}
    			}
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    //#endif

    public ImageSet findImageSet(String name, boolean cache){
        ImageSet result = (ImageSet)imageCache.get(name);

        if(result == null){
            byte[] data = findResource(name);
            try {
                result = new ImageSet(data);
                result.fileName = name;
//#if opengl == true
                //# if(Canvas.openglMode){
                	//# result.bindTexture(Canvas.GL_POOL_MISC, name);
              //# findImageSets.put(result, "");
                //# }
//#endif
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
    	// 所有requestResource都改成异步，交给ResourceAsynLoader处理，不管是否本地存在
    	GameMain.resourceAsynLoader.addLoad(ResourceAsynLoader.AYSN_LOAD_TYPE_FIND_RESOURCE, name, null);
    }
    
    // 这个函数由ResourceAsynLoader调用，实际载入本地资源或开始下载
    public void requestResourceImpl(String name) {
    	byte[] data = findResource(name);

        if(data == null){
        	//9.16
        	//#if NewUI2
        	if(name.endsWith(".pkg")){
        		curUpdate = 0;
        		VMGame gameWorld = VMGame.getVMGame("game_world");
	      		if(gameWorld != null && gameWorld.getVM() != null) {
	      			VM gwvm = gameWorld.getVM();
	      			synchronized(gwvm){
	      				gwvm.callback("isNeedDownloadPkg", new int[]{VM.TRUE});
		            }
	      		}
        	}
        	//#endif
            Tool.sendGetFile(name);

          //#ifdef buildtest
            System.out.println("download new Resource : " + name);
          //#endif
        }

        if(data != null){
        	//9.16
        	//#if NewUI2
        	if(name.endsWith(".pkg")){
        		curUpdate = 0;
        		VMGame gameWorld = VMGame.getVMGame("game_world");
	      		if(gameWorld != null && gameWorld.getVM() != null) {
	      			VM gwvm = gameWorld.getVM();
	      			synchronized(gwvm){
	      				gwvm.callback("isNeedDownloadPkg", new int[]{VM.FALSE});
		            }
	      		}
        	}
        	//#endif
            notifyGame(name, -1, data);
        }
    }
    
    public void recvFileData(String name, int version, byte[] data, int fileLength, int startIndex){
    	curUpdate += data.length;
    	
    	byte[] updateDatas = (byte[])fileDatas.get(name);
    	if(updateDatas == null){
    		updateDatas = new byte[fileLength];
    	}
    	System.arraycopy(data, 0, updateDatas, startIndex, data.length);
    	
    	Integer date = (Integer)fileDataNums.get(name);
    	int dataLength = 0;
    	if(date != null){
    		dataLength = date.intValue();
    	}
    	int curFileLen = data.length + dataLength;
    	if(curFileLen == fileLength){
    		fileDataNums.remove(name);
    		fileDatas.remove(name);
    		recvResource(name, version, updateDatas);
    	}else{
    		fileDataNums.put(name, new Integer(curFileLen));
    		fileDatas.put(name, updateDatas);
    	}
    }
    
    public int getCurUpdate(){
    	return this.curUpdate;
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
        
      //#if NewUI2
        //同步pack.data
        if(name.equals(PACK_TABLE_FILENAME)){
        	synchronized (localPackTable) {
        		try {
        			DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data))));
            		syncLocalPackTable(dis);
        			dis.close();
        			packTableVersion = version;
        			ByteArrayOutputStream baos = new ByteArrayOutputStream();
        			DataOutputStream dos = new DataOutputStream(baos);
        			dos.writeInt(packTableVersion);
        			byte[] packVersionData = baos.toByteArray();
        			dos.close();
        			dataDb.saveFile(PACK_TABLE_FILENAME+".ver", packVersionData, packTableVersion);
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }
        //#endif
        
        VMGame vmg = VMGame.getVMGame("game_icon");
        if(vmg != null){
        	VM vm = vmg.gtvm;
            synchronized(vm){
                vm.callback("ReceiveFile", new int[]{vm.makeTempObject(name)});
            }
        }
        
        vmg = VMGame.getVMGame("game_init_once");
        if(vmg != null){
        	VM vm = vmg.gtvm;
            synchronized(vm){
                vm.callback("ReceiveFile", new int[]{vm.makeTempObject(name)});
            }
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
                //#if NewUI2
                if(USE_RESOURCE_PACK){
                	realcount += 1;
                }
                //#endif
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
                
                //#if NewUI2
                if(USE_RESOURCE_PACK){
                	segment.writeString(PACK_TABLE_FILENAME);
                	segment.writeInt(packTableVersion);
                }
                //#endif
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
            }else if (name.equals("menu1280.pip")) {
            	if(GameMain.viewWidth <= 960){//不是Android Pad版
            		continue;
            	}
            }
            
            if((version & 0x80000000) != 0){
            	Integer localVersion = null;
            	if(localTable.containsKey(name)){
            		localVersion = (Integer)localTable.get(name);
            	}
            	
        		if(localVersion == null || localVersion.intValue() != version){
        			localTable.put(name, new Integer(version));
        			clientFileDb.updateFile(name);
                    updateList.addElement(name);
                    currentUpdateTable.put(name, name);

                  //#ifdef buildtest
                    System.out.println("new resource Version : " + name);
                  //#endif
        		}
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
        //#if NewUI2
        DataInputStream dis = new DataInputStream(new BufferedInputStream(bis));
        //#else
        //# DataInputStream dis = new DataInputStream(bis);
        //#endif
       
        
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
        //#if NewUI2
        dis = new DataInputStream(new BufferedInputStream(bis));
        //#else
        //# dis = new DataInputStream(bis);
        //#endif
        
        
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
    private Hashtable downloads = new Hashtable();
    public void addDownloadFile(String name){
    	downloads.put(name, "");
    }
    private void notifyGame(String name, int version, byte[] data){
        int type = resourceType(name);

        switch(type){
            case TYPE_PKG: {
                GameWorld.pkgData = data;
            }
                break;
            case TYPE_CTN: {
            	//#if NewUI2
            	//# Object obj = downloads.get(name);
            	//# if(obj != null){
            	//# 	downloads.remove(name);
            	//# }
            	//# AnimateCache.recvAnimate(getRecvName(name), data);
            	//#else
                AnimateCache.recvAnimate(getRecvName(name), data);
                //#endif
            }
                break;
            case TYPE_PIP: {
            	//#if NewUI2
            	//# Object obj = downloads.get(name);
            	//# if(obj != null){
            	//# 	downloads.remove(name);
            	//# }
            	//# GameMain.resourceAsynLoader.addLoad(ResourceAsynLoader.AYSN_LOAD_TYPE_IMAGE_BYTES, getRecvName(name), data);
            	//#else
            	GameMain.resourceAsynLoader.addLoad(ResourceAsynLoader.AYSN_LOAD_TYPE_IMAGE_BYTES, getRecvName(name), data);
                //#endif
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

    public static String getLocalName(String name){
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
    
    /**
     * 判断本地是否有这个文件
     * @param name
     * @return
     */
    public boolean hasFile(String name){
    	if(isLocalResource(name)){
    		return true;
    	}
    	return dataDb.hasFile(name);
    }
}
