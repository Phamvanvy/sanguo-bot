package com.pip.sanguo.editor.card;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.BuffPropertyDescriptor;
import com.pip.sanguo.editor.property.CardDescDescriptor;
import com.pip.sanguo.editor.property.CardHoleDescriptor;
import com.pip.sanguo.editor.property.CardItemPropertyDescriptor;

/**
 * 卡片属性页。
 * @author zlguo
 */
public class CardPropertySource implements IPropertySource {
    private CardEditor owner;
    private Card card;
    
    public CardPropertySource(CardEditor owner, Card card) {
        this.owner = owner;
        this.card = card;
    }
    
    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = new IPropertyDescriptor[19];
        ret[0] = new TextPropertyDescriptor("title", "名称");
        ret[1] = new PropertyDescriptor("id", "ID");
        ret[2] = new PropertyDescriptor("suiteId", "系列ID");
        ret[3] = new CardHoleDescriptor("holeId", "卡位ID");
//        ret[4] = new ComboBoxPropertyDescriptor("quality","品质",Card.QUALITY_NAMES);
        ret[4] = new ComboBoxPropertyDescriptor("type","类型",Card.TYPE_NAMES);
        ret[5] = new ComboBoxPropertyDescriptor("star","星级",Card.STAR_NAMES);
//        ret[6] = new ComboBoxPropertyDescriptor("canUse","可以使用",Card.CANUSE_NAMES);
        ret[6] = new TextPropertyDescriptor("rate", "闪卡几率(1~10000)");
        ret[7] = new CardItemPropertyDescriptor("itemId", "对应物品");
        ret[8] = new CardDescDescriptor("description", "描述");
        ret[9] = new TextPropertyDescriptor("energy_min", "卡片充能值下限");
        ret[10] = new TextPropertyDescriptor("energy_max", "卡片充能值上限");
        ret[11] = new BuffPropertyDescriptor("buffId", "卡片效果BUFF");
        ret[12] = new TextPropertyDescriptor("bufflevel1", "普通卡片BUFF级别");
        ret[13] = new TextPropertyDescriptor("bufflevel2", "闪卡BUFF级别");
        ret[14] = new TextPropertyDescriptor("buffduration", "buff持续时间(小时)");
        ret[15] = new ComboBoxPropertyDescriptor("property_type","属性类型",Card.PROPERTY_TYPE_NAMES);
        ret[16] = new TextPropertyDescriptor("property_basevalue", "基础属性价值");
        ret[17] = new TextPropertyDescriptor("property_uplevelvalue", "成长属性价值");
        ret[18] = new BuffPropertyDescriptor("buff2Id", "卡片技能BUFF");
        return ret;
    }

    public Object getPropertyValue(Object id) {
        if("id".equals(id)){
            return card.id;
        } else if("title".equals(id)){
            return card.title;
        } else if("holeId".equals(id)){
            return card.holeId;
        } else if("quality".equals(id)){
            return card.quality;
        } else if("type".equals(id)){
            return card.type;
        } else if("star".equals(id)){
            return card.star - 1;
        } else if("canUse".equals(id)){
            return card.canUse?1:0;
        } else if("description".equals(id)){
            return card.description;
        } else if("rate".equals(id)){
            return String.valueOf(card.rate);
        } else if("suiteId".equals(id)){
            return card.suiteId;
        } else if("itemId".equals(id)){
            Item item = EditorApplication.getProj().findItem(card.itemId);
            if(item != null){
                return item.id;
            } else {
                return -1;
            }
        }else if("energy_min".equals(id)){
            return String.valueOf(card.energy_min);
        }else if("energy_max".equals(id)){
            return String.valueOf(card.energy_max);
        }else if("buffId".equals(id)){
            return new Integer(card.buffId);
        }else if("buffduration".equals(id)){
            return String.valueOf(card.buffDuration);
        }else if("bufflevel1".equals(id)){
            return String.valueOf(card.buffLevel1);
        }else if("bufflevel2".equals(id)){
            return String.valueOf(card.buffLevel2);
        }else if("property_type".equals(id)){
            return card.prorertyType;
        }else if("property_basevalue".equals(id)){
            return String.valueOf(card.propertyBaseValue);
        }else if("property_uplevelvalue".equals(id)){
            return String.valueOf(card.propertyUpLevelValue);
        }else if("buff2Id".equals(id)){
            return new Integer(card.buff2Id);
        }
        return null;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}

    public void setPropertyValue(Object id, Object value) {
        boolean dirty = true;
        if("id".equals(id)){
            card.id = ((Integer)value).intValue();
        } else if("title".equals(id)){
            String oldTitle = card.title;
            card.title = (String)value;
            if(!oldTitle.equals(card.title)){
                DataObjectCategory type = card.owner.findCategory(Card.class, card.categoryName);
                if(type != null){
                    for (int i = 0; i < type.objects.size(); i++) {
                        Card c = (Card)type.objects.get(i);
                        if(c.title.equals(card.title)){
                            card.holeId = c.holeId;
                            break;
                        }
                    }
                }
            }
        } else if("holeId".equals(id)){
            card.holeId = ((Integer)value).intValue();
        } else if("quality".equals(id)){
            card.quality = ((Integer)value).intValue();
        } else if("type".equals(id)){
            card.type = ((Integer)value).intValue();
        } else if("star".equals(id)){
            card.star = ((Integer)value).intValue() + 1;
        } else if("canUse".equals(id)){
            card.canUse = ((Integer)value).intValue() == 1?true:false;
        } else if("description".equals(id)){
            card.description = (String)value;
        } else if("rate".equals(id)){
            card.rate = Integer.parseInt((String)value);
        } else if("suiteId".equals(id)){
            card.suiteId = Integer.parseInt((String)value);
        } else if("itemId".equals(id)){
            card.itemId = ((Integer)value).intValue();
            card.propertyBaseValue = card.getDefaultPropertyBaseValue();
            card.propertyUpLevelValue = card.getDefaultPropertyUpLevelValue();
        } else if("energy_min".equals(id)){
            card.energy_min = Integer.parseInt((String)value);
        } else if("energy_max".equals(id)){
            card.energy_max = Integer.parseInt((String)value);
        } else if("buffId".equals(id)){
            card.buffId = ((Integer)value).intValue();
            card.prorertyType = card.getBufftypeById(card.buffId);
        } else if("buffduration".equals(id)){
            card.buffDuration = Integer.parseInt((String)value);
        } else if("bufflevel1".equals(id)){
            card.buffLevel1 = Integer.parseInt((String)value);
        } else if("bufflevel2".equals(id)){
            card.buffLevel2 = Integer.parseInt((String)value);
        }else if("property_type".equals(id)){
            card.prorertyType = ((Integer)value).intValue();
        }else if("property_basevalue".equals(id)){
            card.propertyBaseValue = Integer.parseInt((String)value);
        }else if("property_uplevelvalue".equals(id)){
            card.propertyUpLevelValue = Integer.parseInt((String)value);
        }else if("buff2Id".equals(id)){
            card.buff2Id=((Integer)value).intValue();
        }else {
            dirty = false;
        } 
        if(dirty){
            owner.setDirty(true);
        }
    }
}
