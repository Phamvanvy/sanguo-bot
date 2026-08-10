package com.pip.itimes.server.stage;

import com.pip.itimes.server.bean.Shop;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ShopData {

    private Shop shop;
    private List basicItems = new ArrayList();
    private List extendedItems = new ArrayList();
    private List equipments = new ArrayList();

    public ShopData(Shop shop) throws Exception{
        this.shop = shop;
        init();
    }

    private void init() throws Exception {
        byte[] bytes = shop.getItems();
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte version = dis.readByte();

            if(version == 9){
	            try{
	            	byte tmp = dis.readByte();
	            	
	            	if(tmp >= 10){
	            		version = tmp;
	            	}else{
	            		dis.reset();
	            		dis.skip(1);
	            	}
	            }catch(Exception e){
	            }
            }
            
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


    public int getAreaId() {
        return shop.getAreaId();
    }

    public Date getCreateTime() {
        return shop.getCreateTime();
    }

    public int getId() {
        return shop.getId();
    }


    public int getLevel() {
        return shop.getLevel();
    }

    public int getMoney() {
        return shop.getMoney();
    }

    public String getName() {
        return shop.getName();
    }

    public int getPlayerId() {
        return shop.getPlayerId();
    }

    public void setAreaId(int areaId) {
        shop.setAreaId(areaId);
    }

    public void setCreateTime(Date createTime) {
        shop.setCreateTime(createTime);
    }


    public void setId(int id) {
        shop.setId(id);
    }


    public void setLevel(int level) {
        shop.setLevel(level);
    }

    public void setMoney(int money) {
        shop.setMoney(money);
    }

    public void setName(String name) {
        shop.setName(name);
    }

    public void setPlayerId(int playerId) {
        shop.setPlayerId(playerId);
    }

    public void setGridSize(short gridSize){
        shop.setGridSize(gridSize);
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
            for(int i=0;i<basicItems.size();i++){
                Grid grid = (Grid)basicItems.get(i);
                if(grid.item.getItemId()==itemId){
                    if(grid.count>=count){
                        grid.count -= count;
                        if (grid.count <= 0) {
                            basicItems.remove(i);
                        }
                        return grid.item;
                    }
                }
            }
        }
        else if(type==IItem.TYPE_EXTENDED){
            for(int i=0;i<extendedItems.size();i++){
                Grid grid = (Grid)extendedItems.get(i);
                if(grid.item.getItemId()==itemId){
                    if(grid.count>=count){
                        grid.count -= count;
                        if (grid.count <= 0) {
                            extendedItems.remove(i);
                        }
                        return grid.item;
                    }
                }
            }
        }
        else if(type==IItem.TYPE_EQU){
            for(int i=0;i<equipments.size();i++){
                Grid grid = (Grid)equipments.get(i);
                if(grid.item.getItemId()==itemId&&(grid.item.getId()==count||count==-1)){
                    equipments.remove(i);
                    return grid.item;
                }
            }
        }
        return null;
    }

    public Grid getItem(int itemId, int instanceId) {
        IItemTemplate template = Items.getTemplate(itemId);
        return getItem(template, instanceId);
    }

    public Grid getItem(IItemTemplate template, int instanceId) {
        byte type = template.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == template.getItemId())
                    return grid;
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
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
                Grid grid = (Grid) equipments.get(i);
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
                    break;
                }
            }
        }
        else if(type==IItem.TYPE_EXTENDED){
            for(int i=0;i<extendedItems.size();i++){
                Grid grid = (Grid)extendedItems.get(i);
                if(grid.item.getItemId()==item.getItemId()&&grid.count<=99){
                    n -= (99-grid.count);
                    break;
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
        for(int i=0;i<items.size();i++){
            Grid grid = (Grid)items.get(i);
            if(grid.item.getItemId()==item.getItemId()&&grid.count<99){
                int l = 99-grid.count;
                if(count>l) count=l;
                grid.count += count;
                return count;
            }
        }
        return 0;
    }

    /**
     * 加入物品到新的格子中
     * @param items List
     * @param item IItem
     * @param count int
     */

    private int addToNewGrid(List items,IItem item,int count){
        int ret = 0;
        while(count>0&&(getGridSize()-getCurrentGridSize())>0){
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
        if((getGridSize()-getCurrentGridSize())<getNeedGrid(item,count))
            return false;
        byte type = item.getType();
        if(type==IItem.TYPE_BASIC){
            int added = addToGrid(basicItems,item,count);
            count -= added;
            if(count>0)
                addToNewGrid(basicItems,item,count);
        }
        else if(type==IItem.TYPE_EXTENDED){
            int added = addToGrid(extendedItems,item,count);
            count -= added;
            if(count>0)
                addToNewGrid(extendedItems,item,count);
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
            if((getGridSize()-getCurrentGridSize())>0){
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
               if(template.getType()==IItem.TYPE_EQU){
                  completeRemoveItem(template.getItemId(),-1);
               }else{
                   completeRemoveItem(template.getItemId(),grids[i].count);
               }

           }
           return true;
        }
        return false;
    }

    public boolean hasItem(byte type,int itemId,int count){
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        }
         else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
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
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        }
         else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                if (grid.item.getItemId() == item.getItemId() &&
                    grid.item.getId() == count) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFull(){
        return getCurrentGridSize()>=getGridSize();
    }

    public short getGridSize(){
        return shop.getGridSize();
    }

    public void setGirdSize(short gridSize){
        shop.setGridSize(gridSize);
    }

    public short getCurrentGridSize(){
        return (short)(basicItems.size()+extendedItems.size()+equipments.size());
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
            //dos.write((byte)9);					//items version 9增加属性攻
            dos.write((byte)10);					//items version 10宝石养成
            dos.writeShort(basicItems.size());
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            dos.writeShort(extendedItems.size());
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            dos.writeShort(equipments.size());
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                IEquipment equ = (IEquipment) grid.item;
                dos.write(equ.toDbBytes());
            }
            shop.setItems(bos.toByteArray());
        } catch (Exception ex) {
        }
    }

    public Shop getShop(){
        return shop;
    }

    public byte getState(){
        return shop.getState();
    }

    public void setState(byte state){
        shop.setState(state);
    }

    public void setBuyPlayerId(int player){
        shop.setBuyPlayerId(player);
    }

    public int getBuyPlayerId(){
        return shop.getBuyPlayerId();
    }

    public void setPrice(int price){
        shop.setPrice(price);
    }

    public int getPrice(){
        return shop.getPrice();
    }

    public Date getSellTime(){
        return shop.getSellTime();
    }

    public void setSellTime(Date time){
        shop.setSellTime(time);
    }

    public void setLevelupTime(Date time){
        shop.setLevelupTime(time);
    }

    public Date getLevelupTime(){
        return shop.getLevelupTime();
    }

    public String toString(){
        StringBuffer buff = new StringBuffer(200);
        buff.append("店名:");
        buff.append(getName());
        buff.append("\n");
        buff.append("规模:");
        buff.append(shop.LEVEL[getLevel()-1]);
        buff.append("\n");
        buff.append("仓库位:");
        buff.append(getCurrentGridSize());
        buff.append("/");
        buff.append(getGridSize());
        buff.append("\n");
        buff.append("资金:");
        buff.append(getMoney());
        buff.append("\n");
        buff.append("状态:");
        buff.append(getStateString());
        return buff.toString();
    }

    public String getStateString(){
      byte state = getState();
      if(state==Shop.STATE_NORMAL)
            return "正常";
        else if(state==Shop.STATE_CLOSED)
            return "关闭";
        else if(state==Shop.STATE_SELL)
            return "转让";
        return "";
    }
}
