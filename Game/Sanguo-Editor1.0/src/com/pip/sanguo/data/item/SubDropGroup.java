package com.pip.sanguo.data.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.util.Constants;

/**
 * 子掉落组数据结构，子掉落组中可以包含一个物品，也可以包含一个掉落组
 * @author Administrator
 *
 */
public class SubDropGroup extends DataObject{
    /**
     * 子掉落组的等级上限
     */
    public int levelMax;
    
    /**
     * 子掉落组的等级下限
     */
    public int levelMin;
    
    /**
     * 限制职业，-1表示不限制
     */
    public int job;
    
    /**
     * 掉落物品，掉落物品中可以是物品、装备或者一个掉落组
     */
    public List<DropItem> dropGroup = new ArrayList<DropItem>();
    
    public boolean depends(DataObject obj) {
        // TODO Auto-generated method stub
        return false;
    }

    public DataObject duplicate() {
        SubDropGroup subDropGroup = new SubDropGroup();
        subDropGroup.update(this);
        return subDropGroup;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }

    public void load(Element elem) {
        levelMax = Integer.parseInt(elem.getAttributeValue("levelMax"));
        levelMin = Integer.parseInt(elem.getAttributeValue("levelMin"));
        job = Integer.parseInt(elem.getAttributeValue("job"));
        
        List<Element> dropList = elem.getChildren("DropItem");
        dropGroup.clear();
        int totalWeight = 0;
        for (Element dropElem : dropList){
            DropItem dropItem = new DropItem();
            dropItem.load(dropElem);
            dropGroup.add(dropItem);
            totalWeight += dropItem.dropWeight;
        }
        
        // 计算掉落率
        if (totalWeight > 0) {
            for (DropItem item : dropGroup) {
                item.dropRate = (double)item.dropWeight / totalWeight;
            }
        }
    }

    public Element save() {
        Element ret = new Element("SubGroup");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("levelMax", String.valueOf(levelMax));
        ret.addAttribute("levelMin", String.valueOf(levelMin));
        ret.addAttribute("job", String.valueOf(job));
        
        for (DropItem item: dropGroup) {
            item.dropWeight = (int)(item.dropRate * 1000000);
            ret.addContent(item.save());
        }
        return ret;
    }

    public void update(DataObject obj) {
        SubDropGroup subDropGroup = (SubDropGroup)obj;

        levelMax = subDropGroup.levelMax;
        levelMin = subDropGroup.levelMin;
        job = subDropGroup.job;

        dropGroup.clear();
        for (DropItem drop : subDropGroup.dropGroup) {
            dropGroup.add((DropItem) drop.duplicate());
        }
    }
    
    public String toString(){
        String str = "[" + levelMin + "-" + levelMax + "]级";
        if (job >= 0) {
            str += "(" + Constants.COMBO_JOB[job] + ")";
        }
        return str;
    }
    
    /**
     * 在该掉落组内创建一个新的掉落物品，保证id唯一
     * @return
     */
    public DropItem getNewDropItem(){
        DropItem drop = new DropItem();
        
        drop.id = 0;
        while(findDropItem(drop.id) != null){
            drop.id++;
        }
        return drop;
    }
    
    /**
     * 根据DropItem的id查找数据
     * @param id
     * @return
     */
    private DropItem findDropItem(int id){
        for (DropItem item: dropGroup) {
            if(item.id == id){
                return item;
            }
        }
        return null;
    }

    /**
     * 调整配置表，使指定掉落项目的掉落率符合指定要求。
     * @param index
     * @param percent
     */
    public void setWeightByPercent(int index, double percent, Set<Integer> lockIDs) {
        // 计算锁定的列所占的百分比
        double lockPer = 0.0;
        for (DropItem item : dropGroup) {
            if (lockIDs.contains(item.id)) {
                lockPer += item.dropRate;
            }
        }
        
        // 100%特殊处理，其他非锁定列全部设置为0
        if (percent + lockPer > 0.99999f) {
            for (int i = 0; i < dropGroup.size(); i++) {
                DropItem item = dropGroup.get(i);
                if (lockIDs.contains(item.id)) {
                    continue;
                } else if (i == index) {
                    item.dropRate = 1.0 - lockPer;
                } else {
                    item.dropRate = 0.0;
                }
            }
            return;
        }
        
        // 重排新的掉落率
        double unlockTotal = 1.0 - lockPer - dropGroup.get(index).dropRate;
        if (unlockTotal < 0.00001f) {
            // 加入除了拖动的剩下的都为0，则不允许拖动
            return;
        }
        for (int i = 0; i < dropGroup.size(); i++) {
            DropItem item = dropGroup.get(i);
            if (lockIDs.contains(item.id)) {
                continue;
            } else if (i == index) {
                item.dropRate = percent;
            } else {
                item.dropRate = item.dropRate * (1.0 - lockPer - percent) / unlockTotal;
            }
        }
    }
    
    /**
     * 调整配置表，使所有未锁定的掉落项目均分掉落率。
     */
    public void averageWeight(Set<Integer> lockIDs) {
        // 计算锁定的列所占的百分比
        double lockPer = 0.0;
        int lockCount = 0;
        for (DropItem item : dropGroup) {
            if (lockIDs.contains(item.id)) {
                lockPer += item.dropRate;
                lockCount++;
            }
        }
       
        // 计算剩余平均
        if (lockCount == dropGroup.size()) {
            return;
        }
        double newPer = (1.0 - lockPer) / (dropGroup.size() - lockCount);
        for (DropItem item : dropGroup) {
            if (!lockIDs.contains(item.id)) {
                item.dropRate = newPer;
            }
        }
    }
    
    /**
     * 把掉落组摊平以提高效率。可以摊平的掉落组有以下条件：
     * 1. 包含的子掉落组只掉落1个物品。
     * 2. 包含的子掉落组有和本掉落组完全相同的玩家级别设置。
     * @param parent 所属掉落组
     */
    public void makeFlat(DropGroup parent) {
        List<DropItem> newList = new ArrayList<DropItem>();
        for (DropItem di : dropGroup) {
            if (di.dropType == DropItem.DROP_TYPE_DROPGROUP && di.quantityMin == 1 && di.quantityMax == 1) {
                // 如果是包含子掉落组，并且子掉落组只掉落一个，则可能合并
                DropGroup subGroup = (DropGroup)parent.owner.findObject(DropGroup.class, di.dropID);
                if (subGroup == null) {
                    throw new IllegalArgumentException("数据错误，掉路组不存在：" + di.dropID);
                }
                
                // 合并前子掉落组先做摊平处理
                subGroup.makeFlat();
                
                // 查找包含的掉落组中是否有包含本组级别设置的子组
                SubDropGroup mergeGroup = null;
                for (SubDropGroup sdg : subGroup.subGroup) {
                    if (sdg.levelMin <= levelMin && sdg.levelMax >= levelMax && 
                            (sdg.job == job || sdg.job == -1)) {
                        mergeGroup = sdg;
                        break;
                    }
                }
                
                // 如果找到，则计算掉落率合并
                if (mergeGroup == null) {
                    newList.add(di);
                } else {
                    for (DropItem dii : mergeGroup.dropGroup) {
                        DropItem newdi = (DropItem)dii.duplicate();
                        newdi.dropRate *= di.dropRate;
                        newList.add(newdi);
                    }
                }
            } else {
                newList.add(di);
            }
        }
        
        // 合并操作完成后，根据掉落率重新设定掉落权重
        dropGroup = newList;
        for (DropItem di : dropGroup) {
            di.dropWeight = (int)(di.dropRate * 1000000);
        }
    }
}
