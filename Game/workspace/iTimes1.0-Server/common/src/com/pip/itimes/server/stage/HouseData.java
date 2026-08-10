package com.pip.itimes.server.stage;

import com.pip.itimes.server.bean.House;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class HouseData {

    private static final long VISIT_TIME = 12*3600*1000L;

    private House house;

    private List<Grid> basicItems = new ArrayList<Grid>();
    private List<Grid> extendedItems = new ArrayList<Grid>();
    private List<Grid> equipments = new ArrayList<Grid>();
    private Map<Integer,Long> visiteds = new HashMap<Integer,Long>();

    //mengjie add 雇佣管家处理中。。。
    private static final Map WaiterProcessing = new HashMap();
    public HouseData(House house) throws Exception{
        this.house = house;
        init();
    }

    public void init() throws Exception{
        byte[] bytes = house.getItems();
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte version = dis.readByte();
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                int id = dis.readInt();
                byte count = dis.readByte();
                Grid grid = new Grid();
                IItemTemplate template = Items.getTemplate(id);
                grid.item = template.newInstance();
                grid.count = count;
                basicItems.add(grid);
            }
            size = dis.readShort();
            for (int i = 0; i < size; i++) {
                int id = dis.readInt();
                byte count = dis.readByte();
                Grid grid = new Grid();
                IItemTemplate template = Items.getTemplate(id);
                grid.item = template.newInstance();
                grid.count = count;
                extendedItems.add(grid);
            }
            size = dis.readShort();
            for (int i = 0; i < size; i++) {
                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                Grid grid = new Grid();
                grid.item = equ;
                grid.count = 1;
                equipments.add(grid);
            }
        }
    }

    public void reset(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            //dos.write((byte)2);//items version
        	//dos.write((byte)3);//items version
        	
        	//dos.write((byte)4);				//items version 4  增加鉴定
        	//dos.write((byte)5);				//items version 5 装备刻字
//        	dos.write((byte)6);                 //items version 6 增加宝石系统
//          dos.write((byte)7);					//items version 7增加附魔系统
//            dos.write((byte)8);					//items version 8调整附魔数值
//        	dos.write((byte)9);					//items version 9增加属性攻
        	dos.write((byte)10);				//items version 10宝石养成
            dos.writeShort(basicItems.size());
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid =  basicItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            dos.writeShort(extendedItems.size());
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid =  extendedItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            dos.writeShort(equipments.size());
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid =  equipments.get(i);
                IEquipment equ = (IEquipment) grid.item;
                dos.write(equ.toDbBytes());
            }
            house.setItems(bos.toByteArray());
        } catch (Exception ex) {
        }
    }

    public int getId(){
        return house.getId();
    }

    public House getHouse(){
        return house;
    }

    public String getPlayerName(){
        return house.getPlayerName();
    }

    public int getPlayerId(){
        return house.getPlayerId();
    }

    public int getLevel(){
        return house.getLevel();
    }

    public void setLevel(int level){
        house.setLevel(level);
    }

    public int getStyle(){
        return house.getStyle();
    }

    public void setStyle(int style){
        house.setStyle(style);
    }

    public int getRule(){
        return house.getRule();
    }

    public void setRule(int rule){
        house.setRule(rule);
    }

    public void setAreaId(short areaId){
        house.setAreaId(areaId);
    }

    public short getAreaId(){
        return house.getAreaId();
    }

    public void setLastTime(Date date){
        house.setLastTime(date);
    }

    public Date getLastTime(){
        return house.getLastTime();
    }

    public void setTitle(String title){
        house.setTitle(title);
    }

    public String getTitle(){
        return house.getTitle();
    }

    public boolean isUsedWaiter(){
        Date time = house.getCanUseWaiterTime();
        if(time==null)
            return false;
        return System.currentTimeMillis()<=time.getTime();
    }

    //mengjie add 管家解除
    
    //管家到期日与当前时间相差几个月，小于1个月按1个月计算。
    public int getCanUseWaiterMonth(){
        Date time = house.getCanUseWaiterTime();
        if(time==null)
            return -1;
        Calendar currentTime = Calendar.getInstance();
        Calendar useTime = Calendar.getInstance();
        useTime.setTime(time);
        int month=0;
        if(currentTime.get(Calendar.YEAR) - useTime.get(Calendar.YEAR) > 0){
        	month = (currentTime.get(Calendar.YEAR) - useTime.get(Calendar.YEAR)) * 12;
        }else if(currentTime.get(Calendar.YEAR) - useTime.get(Calendar.YEAR) < 0){
        	return -1;
        }
        if (currentTime.getTimeInMillis() > useTime.getTimeInMillis()){
        	//日期超过了，月份加1
        	month = month + currentTime.get(Calendar.MONTH) - useTime.get(Calendar.MONTH) + 1;
        }else{
        	month = month + currentTime.get(Calendar.MONTH) - useTime.get(Calendar.MONTH);
        }
        return month;
//        Date time = house.getCanUseWaiterTime();
//        if(time==null)
//            return -1;
//        Calendar cal = Calendar.getInstance();
//        Date currentTime = cal.getTime();
//        int month=0;
//        if(currentTime.getYear()-time.getYear()>0){
//        	month = (currentTime.getYear()-time.getYear())*12;
//        }else if(currentTime.getYear()-time.getYear()<0){
//        	return -1;
//        }
//        if (currentTime.getDate()>time.getDate()){
//        	//日期超过了，月份加1
//        	month = month + currentTime.getMonth()-time.getMonth() + 1;
//        }else{
//        	month = month + currentTime.getMonth()-time.getMonth();
//        }
//        return month;
    }
    public static void modifyProcessing(int PlayerId, int value, int flag){
    	if (flag == 0){
    		//插入
    		WaiterProcessing.put(PlayerId, Integer.valueOf(value));
    	}else if (flag == 1){
    		//修改
    		int tmp_int = getWaiterProcessing(PlayerId);
    		WaiterProcessing.remove(PlayerId);
    		WaiterProcessing.put(PlayerId, Integer.valueOf(value+tmp_int));
    	}else if (flag == -1){
    		//删除
    		WaiterProcessing.remove(PlayerId);
    	}
    }
    
    public static int getWaiterProcessing(int PlayerId) {
    	if (WaiterProcessing.get(PlayerId) == null){
    		return -1;
    	}else{
    		return Integer.valueOf(WaiterProcessing.get(PlayerId).toString()).intValue();
    	}
	}

	public int getAutoBuyWaiter(){
    	return house.getAutoBuyWaiter();
    }
    public void setAutoBuyWaiter(int flag){
        house.setAutoBuyWaiter(flag);
    }
    public Date getCanUseWaiterTime(){
        return house.getCanUseWaiterTime();
    }
    //mengjie add end
    public void setCanUseWaiterTime(Date time){
        house.setCanUseWaiterTime(time);
    }


    public int getWaiterId(){
        return house.getWaiterId();
    }

    public void setWaiterId(int value){
        house.setWaiterId(value);
    }

    public void incUsediMoney(int value){
        house.setUsediMoney(house.getUsediMoney()+value);
    }

//    public int getUsediMoney(){
//        return house.getUsediMoney();
//    }
//
//    public void setUsediMoney(int value){
//        house.setUsediMoney(value);
//    }

    public void incLeaveMessageTimes(){
        house.setLeaveMessageTimes(house.getLeaveMessageTimes()+1);
    }


    public void addVisited(int playerId){
        long current = System.currentTimeMillis();
        if(visiteds.containsKey(playerId)){
            long t = visiteds.get(playerId);
            if((t-current)>=VISIT_TIME){
                house.setVisitedTimes(house.getVisitedTimes()+1);
                visiteds.put(playerId,current);
            }
        }else{
            house.setVisitedTimes(house.getVisitedTimes()+1);
            visiteds.put(playerId,current);
        }
    }

    public int getUsediMoney(){
        return house.getUsediMoney();
    }

    public void setUsediMoney(int value){
        house.setUsediMoney(value);
    }

    public Grid[] getItems(){
        Grid[] ret = new Grid[basicItems.size()+extendedItems.size()+equipments.size()];
        List l = new ArrayList(ret.length);
        for(int i=0;i<basicItems.size();i++){
            l.add(basicItems.get(i));
        }
        for(int i=0;i<extendedItems.size();i++){
            l.add(extendedItems.get(i));
        }
        for(int i=0;i<equipments.size();i++){
            l.add(equipments.get(i));
        }
        l.toArray(ret);
        return ret;
    }

    public void empty(){
        basicItems.clear();
        extendedItems.clear();
        equipments.clear();
        reset();
    }

    public IItem completeRemoveItem(int itemId,int count){
        IItemTemplate template = Items.getTemplate(itemId);
        byte type = template.getType();
        if(type==IItem.TYPE_BASIC){
            for(int i=0;i<basicItems.size();i++){//从前
                Grid grid = basicItems.get(i);
                if(grid.item.getItemId()==itemId){//找到
                	if(grid.count>=count){//有足够数量
                		grid.count -= count;//减去靠前的
                		if (grid.count <= 0) {
                			basicItems.remove(i);
                		}else{
                			update(basicItems, itemId, 0);
                		}
                		return grid.item;
                	}
                }
            }
        }
        else if(type==IItem.TYPE_EXTENDED){
            for(int i=0;i<extendedItems.size();i++){//从前
                Grid grid = extendedItems.get(i);
                if(grid.item.getItemId()==itemId){//找到
                	if(grid.count>=count){//有足够数量
                		grid.count -= count;//减去靠前的
                		if (grid.count <= 0) {
                			extendedItems.remove(i);
                		}else{
                			update(extendedItems, itemId, 0);
                		}
                		return grid.item;
                	}
                }
            }
        }
        else if(type==IItem.TYPE_EQU){
            for(int i=0;i<equipments.size();i++){
                Grid grid = equipments.get(i);
                if(grid.item.getItemId()==itemId&&grid.item.getId()==count){
                    equipments.remove(i);
                    return grid.item;
                }
            }
        }
        return null;
    }
    
    public void update(List items, int itemId, int start){
    	Grid gridSoure = null;
    	for(int i=start; i<items.size(); i++){
    		Grid grid2 = (Grid)items.get(i);
			if(grid2.item.getItemId() == itemId){
				if(gridSoure == null && grid2.count < 99){
					gridSoure = grid2;
				}else{
					int count = 99 - gridSoure.count;
					if(count > grid2.count){
						count = grid2.count;
					}
					gridSoure.count += count;
					grid2.count -= count;
					if(grid2.count <= 0){
						items.remove(i);
					}
					update(items, itemId, i);
					break;
				}
			}
    	}
    }

    public Grid getItem(int itemId, int instanceId) {
        IItemTemplate template = Items.getTemplate(itemId);
        return getItem(template, instanceId);
    }

    public Grid getItem(IItemTemplate template, int instanceId) {
        byte type = template.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid =  basicItems.get(i);
                if (grid.item.getItemId() == template.getItemId())
                    return grid;
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid =  extendedItems.get(i);
                if (grid.item.getItemId() == template.getItemId())
                    return grid;
            }
        }
//        else if (type == IItem.TYPE_TASK) {
//            for (int i = 0; i < taskItems.size(); i++) {
//                Grid grid = (Grid) taskItems.get(i);
//                if (grid.item.getItemId() == template.getItemId())
//                    return grid;
//            }
//        }
        else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid =  equipments.get(i);
                if (grid.item.getItemId() == template.getItemId() &&
                    grid.item.getId() == instanceId)
                    return grid;
            }
        }
        return null;
    }

    /**
     * 测试需要多少格子才能装下指定数量的物品,如果物品能够完全加入到现有的格子中,那么返回值为0
     * @param item IItem
     * @param count int
     * @return short
     */
    public short getNeedGrid(IItem item,int count){
        byte type = item.getType();
        int n = count;
        if(type==IItem.TYPE_BASIC){
            for(int i=0;i<basicItems.size();i++){
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == item.getItemId()&&grid.count<=99) {
                    n -= (99-grid.count);
                }
            }
        }
        else if(type==IItem.TYPE_EXTENDED){
            for(int i=0;i<extendedItems.size();i++){
                Grid grid = (Grid)extendedItems.get(i);
                if(grid.item.getItemId()==item.getItemId()&&grid.count<=99){
                    n -= (99-grid.count);
                }
            }
        }
        else if(type==IItem.TYPE_EQU)
            return (short)1;
        if(n<=0)
            return 0;
        else{
            return (short)(n/99+((n%99)!=0?1:0));
        }
    }

    /**
     * 加入物品到已经有的格子中，返回加入数量
     * @param items List
     * @param item IItem
     * @param count int
     * @return int
     */
    private int addToGrid(List items,IItem item,int count){
    	int addCount = 0;
        for(int i=0;i<items.size();i++){
            Grid grid = (Grid)items.get(i);
            if(grid.item.getItemId()==item.getItemId()&&grid.count<99){
                int l = 99-grid.count;
                if(l < count - addCount){
                	grid.count += l;
                	addCount += l;
                }else{
                	int add = count - addCount;
                	grid.count += add;
                	addCount += add;
                }
                if(addCount == count){
                	break;
                }
            }
        }
        return addCount;
    }

    /**
     * 加入物品到新的格子中
     * @param items List
     * @param item IItem
     * @param count int
     */

    private int addToNewGrid(List items,IItem item,int count){
        int ret = 0;
        while(count>0&&(getGridSize()-getCurrentGridSize()+ getAddGridSize())>0){
            if(count>=99){
                Grid grid = new Grid();
                grid.item = item;
                grid.count = 99;
                items.add(grid);
                count -= 99;
                ret += 99;
            }else{
                Grid grid = new Grid();
                grid.item = item;
                grid.count = (short)count;
                items.add(grid);
                count -= count;
                ret += count;
            }
        }
        return ret;
    }
    
    public boolean completeAddItem(IItem item,int count){
    	if((getGridSize()-getCurrentGridSize()+ getAddGridSize())<getNeedGrid(item,count))
    		return false;
        byte type = item.getType();
        if(type==IItem.TYPE_BASIC){
            int added = addToGrid(basicItems,item,count);
            count -= added;
            if(count>0){
            	addToNewGrid(basicItems,item,count);
            }
        }
        else if(type==IItem.TYPE_EXTENDED){
            int added = addToGrid(extendedItems,item,count);
            count -= added;
            if(count>0){
            	addToNewGrid(extendedItems,item,count);
            }
        }
        else if(type==IItem.TYPE_EQU){
            Grid grid = new Grid();
            grid.item = item;
            grid.count = 1;
            equipments.add(grid);
        }
        
        return true;
    }

    public int addItem(IItem item,int count){
        byte type = item.getType();
        if(type==IItem.TYPE_BASIC){
            int added = addToGrid(basicItems,item,count);
            int l = count - added;
            l = addToNewGrid(basicItems,item,count);
            return added+l;
        }
        else if(type==IItem.TYPE_EXTENDED){
            int added = addToGrid(basicItems,item,count);
            int l = count - added;
            l = addToNewGrid(basicItems,item,count);
            return added+l;
        }
        else if(type==IItem.TYPE_EQU){
            if((getGridSize()-getCurrentGridSize()+getAddGridSize())>0){
                Grid grid = new Grid();
                grid.item = item;
                grid.count = 1;
                equipments.add(grid);
                return 1;
            }
            return 0;
        }
        return 0;
    }

    public boolean completeRemoveItem(TemplateGrid[] grids){
        boolean hasAll = true;
        for(int i=0;i<grids.length;i++){
            IItemTemplate template = grids[i].template;
            if(!hasItem(template.getType(),template.getItemId(),grids[i].count))
                hasAll = false;
        }
        if(hasAll){
           for(int i=0;i<grids.length;i++){
               IItemTemplate template = grids[i].template;
               completeRemoveItem(template.getItemId(),grids[i].count);
           }
           return true;
        }
        return false;
    }

    public String[] getTitles(){
        List<String> l = new ArrayList<String>(50);
        for(int i=0;i<extendedItems.size();i++){
            Grid grid = extendedItems.get(i);
            ExtendedItem item = (ExtendedItem)grid.item;
            Effect[] effects = item.getEffects();
            if(effects.length>0&&effects[0].getType()==7){ //title item
                TitleEffect e = (TitleEffect)effects[0];
                l.add(e.getTitle());
            }
        }
        String[] ret = new String[l.size()];
        l.toArray(ret);
        return ret;
    }

    public boolean hasItem(byte type,int itemId,int count){
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid =  basicItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid =  extendedItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        }
         else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid =  equipments.get(i);
                if (grid.item.getItemId() == itemId &&
                    grid.item.getId() == count) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasItem(IItem item,int count){
        byte type = item.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid =  basicItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid =  extendedItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        }
         else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid =  equipments.get(i);
                if (grid.item.getItemId() == item.getItemId() &&
                    grid.item.getId() == count) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getGridSize(){
        return house.getGridSize();
    }

    public void setGirdSize(int gridSize){
        house.setGridSize(gridSize);
    }
    
    public int getAddGridSize(){
        return house.getAddGridSize();
    }

    public void setAAGirdSize(int addGridSize){
        house.setAddGridSize(addGridSize);
    }

    public short getCurrentGridSize(){
        return (short)(basicItems.size()+extendedItems.size()+equipments.size());
    }

    public byte[] getVisibleParts(){
        return house.getParts();
    }

    public void setVisibleParts(byte[] parts){
        house.setParts(parts);
    }

    public void addVisiblePart(byte part){
        byte[] old = getVisibleParts();
        byte[] parts = new byte[old.length+1];
        System.arraycopy(old,0,parts,0,old.length);
        parts[old.length] = part;
        setVisibleParts(parts);
    }

    public boolean hasVisiblePart(byte part){
        byte[] parts = getVisibleParts();
        for(int i=0;i<parts.length;i++){
            if(parts[i] == part)
                return true;
        }
        return false;
    }

    public void clearVisibleParts(){
        setVisibleParts(new byte[0]);
    }

}
