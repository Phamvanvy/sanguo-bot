package com.pip.resource;

import java.util.Hashtable;
import java.util.Vector;

import com.pip.common.Tool;
import com.pip.engine.AnimateCache;
import com.pip.engine.GamePackage;
import com.pip.image.ImageSet;
import com.pip.io.UASegment;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameRole;
import com.pip.sanguo.GameView;
import com.pip.sanguo.GameWorld;
import com.pip.sanguo.SanguoMIDlet;
import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class ResourceAsynLoader implements Runnable{
    private Tool keyMaker = new Tool();

    private Vector waitingList = new Vector();
    private Vector queue = new Vector();
    private Hashtable table = new Hashtable();
    private Vector resourceQueue = new Vector();
    private Hashtable resourceTable = new Hashtable();

    public static final byte AYSN_LOAD_TYPE_BYTES = 0;
    public static final byte AYSN_LOAD_TYPE_IMAGE = 1;
    public static final byte AYSN_LOAD_TYPE_IMAGE_CACHE = 2;
    public static final byte AYSN_LOAD_TYPE_VMGAME = 3;
    public static final byte AYSN_LOAD_TYPE_WORLD = 4;
    public static final byte AYSN_LOAD_TYPE_ROLE = 5;
    public static final byte AYSN_LOAD_TYPE_PKG = 6;
    public static final byte AYSN_LOAD_TYPE_MAP = 7;
    public static final byte AYSN_LOAD_TYPE_IMAGE_BYTES = 100;
    
    public static final byte AYSN_LOAD_TYPE_SAVE_DATABASE_FILE = 110;
    public static final byte AYSN_LOAD_TYPE_SAVE_DATABASE_INFOMATION = 111;

    public ResourceAsynLoader(){
        new Thread(this).start();
    }

    public void clear(){
        synchronized(queue){
            saveAllResource();
            
            queue.removeAllElements();
            resourceQueue.removeAllElements();
            resourceTable.clear();
            table.clear();
        }
    }

    public byte[] getResource(String name){
        Integer rkey = (Integer)resourceTable.get(name);
        
        if(rkey != null){
            AyncLoadItem item = (AyncLoadItem)table.get(rkey);
            return item.data;
        }
        
        return null;
    }
    
    public void addDatabaseAction(byte type, ResourceDatabase database, String name, byte[] data, int version){
        synchronized(waitingList){
            AyncLoadItem item = new AyncLoadItem();
            item.key = keyMaker.nextKey();
            item.type = type;
            item.ready = false;
            item.name = name;
            item.data = data;
            item.objData = database;
            item.intData = version;
            
            waitingList.addElement(item);
            
            //baiyang: 下载背景音乐后可立即通过findResource方法获得数据
            if (item.type == AYSN_LOAD_TYPE_SAVE_DATABASE_FILE) {
                resourceTable.put(item.name, new Integer(item.key));
                table.put(new Integer(item.key), item);
            }
        }
    }
    
    public int addLoad(byte type, String name, byte[] data){
        synchronized(waitingList){
            AyncLoadItem item = new AyncLoadItem();
            item.key = keyMaker.nextKey();
            item.type = type;
            item.ready = false;
            item.name = name;
            item.data = null;
            item.imgData = null;
            item.objData = null;

            switch(type){
                case AYSN_LOAD_TYPE_IMAGE_BYTES:
                    item.data = data;

                    break;
            }

            waitingList.addElement(item);

            return item.key;
        }
    }

    //如果key等于-2，则是检测当前是否有未保存完毕的resourceAction
    public boolean checkLoad(int key){
        if(key >= 0){
            AyncLoadItem item = (AyncLoadItem) table.get(new Integer(key));
    
            if(item != null){
                return item.ready;
            }else{
                return false;
            }
        }else if(key == Tool.CHECK_RESOURCE_SAVE_KEY){
            if(resourceQueue.size() > 0){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public Object getLoad(int key){
        AyncLoadItem item = (AyncLoadItem) table.get(new Integer(key));

        if(item != null){
            table.remove(new Integer(key));
            
            switch(item.type){
                case AYSN_LOAD_TYPE_BYTES:
                    return item.data;
                case AYSN_LOAD_TYPE_IMAGE:
                case AYSN_LOAD_TYPE_IMAGE_CACHE:
                    return item.imgData;
            }
        }

        return null;
    }
    
    private void transWaitingToQueue(){
        synchronized(waitingList){
            for(int i = 0; i < waitingList.size(); i++){
                AyncLoadItem item = (AyncLoadItem)waitingList.elementAt(i);
                
                switch(item.type){
                    case AYSN_LOAD_TYPE_SAVE_DATABASE_FILE:
                    case AYSN_LOAD_TYPE_SAVE_DATABASE_INFOMATION:
                        resourceQueue.addElement(new Integer(item.key));
                        if (item.type == AYSN_LOAD_TYPE_SAVE_DATABASE_FILE) {
                            resourceTable.put(item.name, new Integer(item.key));
                        }
                        
                        break;
                    default:
                        queue.addElement(new Integer(item.key));
                }
                
                table.put(new Integer(item.key), item);
            }
            
            waitingList.removeAllElements();
        }
    }

    public void run(){
        while(SanguoMIDlet.isRun){
            try{
                synchronized(queue){
                    transWaitingToQueue();
                    
                    if(queue.size() > 0){
                        Integer itemKey = (Integer) queue.firstElement();
                        queue.removeElementAt(0);

                        AyncLoadItem item = (AyncLoadItem) table.get(itemKey);

                        switch(item.type){
                            case AYSN_LOAD_TYPE_BYTES:
                                item.data = GameMain.resourceManager.findResource(item.name);
                                item.ready = true;

                                break;
                            case AYSN_LOAD_TYPE_IMAGE:
                                item.imgData = GameMain.resourceManager.findImageSet(item.name, false);
                                item.ready = true;

                                break;
                            case AYSN_LOAD_TYPE_IMAGE_CACHE:
                                item.imgData = GameMain.resourceManager.findImageSet(item.name, true);
                                item.ready = true;

                                break;
                            case AYSN_LOAD_TYPE_VMGAME:
                            	int vmKey = VMGame.loadVMGame(item.name, VMGame.VM_TYPE_GAME, true);
                            	
                            	VMGame gameWorld = VMGame.getVMGame("game_world");
                            	if(gameWorld != null) {
                            		 VM vm = gameWorld.getVM();                                
                                     synchronized(vm){
                                         vm.callback(VMGame.CALLBACK_LOAD_ETF_END1, new int[]{vm.makeTempObject(item.name), vmKey});
                                     }
                            	}
                               

                                item.ready = true;

                                break;
                            case AYSN_LOAD_TYPE_WORLD:
                                GameMain.world = new GameWorld();
                                item.ready = true;

                                break;
                            case AYSN_LOAD_TYPE_ROLE: {
                                UASegment segment = (UASegment) GameWorld.instance.readGameData("game_role_create");
                                GameWorld.instance.removeGameData("game_role_create");

                                segment.reset();
                                int id = segment.readInt();
                                String name = segment.readString();

                                GameRole role = GameRole.createRole(id, name);
                                role.faction = segment.readByte();
                                role.sprite.setPosition(segment.readInt(), segment.readInt());
                                role.sprite.setDir(segment.readInt());
                                role.sprite.setAnimateDir(role.sprite.getDir());
                                role.state = segment.readInt();
                                
                                GameWorld.player = role;
                                GameWorld.addSprite(GameWorld.player);
                                item.ready = true;
                            }
                                break;
                            case AYSN_LOAD_TYPE_PKG:
                                try{
                                    while(GameWorld.pkgData == null){
                                        Thread.sleep(50);
                                    }

                                    GameWorld.gamePackage = new GamePackage(GameWorld.pkgData);
                                    GameWorld.currentAreaId = GameWorld.gamePackage.areaID;
                                    GameView.mapNpcAnimateNeedLoad = true;
                                    GameWorld.pkgData = null;
                                    item.ready = true;
                                }catch(Exception e){
                                	//#ifdef buildtest
                                    e.printStackTrace();
                                  //#endif
                                }

                                break;
                            case AYSN_LOAD_TYPE_MAP:
                                GameWorld.loadMap();
                                item.ready = true;

                                break;
                            case AYSN_LOAD_TYPE_IMAGE_BYTES:
                                item.imgData = new ImageSet(item.data);
                                AnimateCache.recvImage(item.name, item.imgData);
                                table.remove(itemKey);
                                item.ready = true;

                                break;

                        }
                    }
                    
                    if(resourceQueue.size() > 0 && GameWorld.inLoading){
                        saveAllResource();
                    }
                }

                Thread.sleep(GameMain.MILLIS_PRE_UPDATE);
            }catch(Exception e){
            	//#ifdef buildtest
                e.printStackTrace();
              //#endif
            }
        }
    }
    
    private void saveAllResource(){
        int rsize = resourceQueue.size();

        for(int i = 0; i < rsize; i++){
            Integer itemKey = (Integer) resourceQueue.elementAt(i);
            AyncLoadItem item = (AyncLoadItem) table.get(itemKey);
            
            switch(item.type){
                case AYSN_LOAD_TYPE_SAVE_DATABASE_FILE:{
                    ResourceDatabase database = (ResourceDatabase)item.objData;
                    database.saveFile(item.name, item.data, item.intData);
                    table.remove(itemKey);
                    item.ready = true;
                }
                    break;
                case AYSN_LOAD_TYPE_SAVE_DATABASE_INFOMATION:{
                    ResourceDatabase database = (ResourceDatabase)item.objData;
                    database.saveInformation(item.intData == VM.TRUE);
                    table.remove(itemKey);
                    item.ready = true;
                }
                    break;
            }
        }
        
        GameMain.resourceManager.saveResourceInfo(true);
        resourceQueue.removeAllElements();
        resourceTable.clear();
    }

    private class AyncLoadItem{
        public byte type;
        public boolean ready;
        public int key;
        public String name;
        public byte[] data;
        public ImageSet imgData;
        public Object objData;
        public int intData;
    }
}
