package peony.game.changed;

import org.apache.mina.common.ByteBuffer;
import peony.game.Equipments;
import peony.game.GameItem;
import peony.game.Player;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.net.Packet;
import peony.service.cards.CardInfo;
import peony.service.cards.CardService;
import peony.service.cards.EquipCardChangedItem;

public class PacketChangedItemVisitor implements ChangedItemVisitor {

	public Packet pt;
	public Player owner;
	
	
	public void init(Packet pt, Player owner){
		this.pt = pt;
		this.owner = owner;
	}
	
	
	public void visit(IntPropertyChangedItem changedItem) {
		pt.put(ChangedItem.TYPE_INT);
		pt.put(changedItem.id);
		pt.putInt(changedItem.value);
	}

	public void visit(StringPropertyChangedItem changedItem) {
		pt.put(ChangedItem.TYPE_STRING);
		pt.put(changedItem.id);
		pt.putString(changedItem.value);
	}

	public void visit(BagChangedItem changedItem) {
		if(!changedItem.notify){
			pt.put(ChangedItem.TYPE_COMPLEX);
			pt.put(changedItem.id);
			pt.put(changedItem.totle);
			pt.put(changedItem.grid.toClientByte());
		}else{
			pt.put(ChangedItem.TYPE_COMPLEX);
			pt.put(changedItem.id);
			ByteBuffer buffer = ByteBuffer.allocate(12, false);
			buffer.putInt(changedItem.item.template.id);
			buffer.putInt(changedItem.item.instanceId);
			buffer.putShort((short)changedItem.count);
			pt.put(buffer.array());
		}
	}
	
	public void visit(SkillChangedItem changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		pt.put(changedItem.skill.toClientBytes(owner));
	}

	public static final byte[] ZERO = {(byte)0};
	
	public void visit(EquipChangedItem changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		if(changedItem.item==null){
			byte[] bytes = new byte[2];
			bytes[0] = (byte)changedItem.index;
			bytes[1] = 0;
			pt.put(bytes);
		}else{
			byte[] b = changedItem.item.toClientBytes();
			byte[] bytes = new byte[b.length+2];
			bytes[0] = (byte)changedItem.index;
			bytes[1] = 1;
			System.arraycopy(b, 0, bytes, 2, b.length);
			pt.put(bytes);
		}
	}
	
	public void visit(DurationChangedItem changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		ByteBuffer buffer = ByteBuffer.allocate(6, false);
		buffer.putInt(changedItem.item.instanceId);
		buffer.putShort((short)changedItem.item.duration);
		pt.put(buffer.array());
	}
	
	public void visit(BindChangedItem changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		ByteBuffer buffer = ByteBuffer.allocate(8, false);
		buffer.putInt(changedItem.item.instanceId);
		buffer.putInt(changedItem.item.bindInstance);
//		buffer.put(changedItem.item.bind?(byte)1:(byte)0);
		pt.put(buffer.array());
	}
	
	public void visit(AddTitleChangedItem changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		pt.put(changedItem.title.toClientBytes());
	}
	
	public void visit(HorseEquipChangedItem changedItem){
		pt.put(ChangedItem.TYPE_HORSE_COMPLEX);
		pt.put(changedItem.id);
		pt.putInt(changedItem.horseId);
		if(changedItem.item==null){
			byte[] bytes = new byte[2];
			bytes[0] = (byte)changedItem.index;
			bytes[1] = 0;
			pt.put(bytes);
		}else{
			byte[] b = changedItem.item.toClientBytes();
			byte[] bytes = new byte[b.length+2];
			bytes[0] = (byte)changedItem.index;
			bytes[1] = 1;
			System.arraycopy(b, 0, bytes, 2, b.length);
			pt.put(bytes);
		}
	}
	
	public void visit(HorseIntPropertyChangedItem changedItem){
		pt.put(ChangedItem.TYPE_HORSE_INT);
		pt.put(changedItem.id);
		pt.putInt(changedItem.horseId);
		pt.putInt(changedItem.value);
	}
	
	public void visit(HorseSkillChangedItem changedItem) {
		pt.put(ChangedItem.TYPE_HORSE_COMPLEX);
		pt.put(changedItem.id);
		pt.putInt(changedItem.horseId);
		if (!changedItem.add) {
			ByteBuffer buffer = ByteBuffer.allocate(5);
			buffer.put((byte) 0);
			buffer.putInt(changedItem.skill.getId());
			pt.put(buffer.array());
		} else {
			byte[] data = changedItem.skill.toClientBytes(owner);
			byte[] bytes = new byte[data.length+1];
			bytes[0] = 1;
			System.arraycopy(data, 0, bytes, 1, data.length);
			pt.put(bytes);
		}
	}
	
	public void visit(HorseStringPropertyChangedItem changedItem){
		pt.put(ChangedItem.TYPE_HORSE_STRING);
		pt.put(changedItem.id);
		pt.putInt(changedItem.horseId);
		pt.putString(changedItem.value);
	}
	
	public void visit(AttendantStringPropertyChangedItem changedItem){
		pt.put(ChangedItem.TYPE_ATTENDANT_STRING);
		pt.put(changedItem.id);
		pt.putInt(changedItem.attendantInstanceId);
		pt.putString(changedItem.value);
	}
	
	public void visit(HorseBagChangedItem changedItem) {
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		if (changedItem.count == 0) {
			ByteBuffer buffer = ByteBuffer.allocate(5, false);
			buffer.put((byte)0);
			buffer.putInt(changedItem.h.instanceId);
			pt.put(buffer.array());
		} else {
			byte[] bytes = changedItem.h.toClientBytes(owner);
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length+1, false);
			buffer.put((byte)1);
			buffer.put(bytes);
			pt.put(buffer.array());
		}
	}
	
	public void visit(AttendantIntProoertyChangedItem changedItem){
		pt.put(ChangedItem.TYPE_ATTENDANT_INT);
		pt.put(changedItem.id);
		pt.putInt(changedItem.attendantInstanceId);
		pt.putInt(changedItem.value);
	}
	
	public void visit(AttendantBagChangedItem changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		if (changedItem.count == 0) {
			ByteBuffer buffer = ByteBuffer.allocate(5, false);
			buffer.put((byte)0);
			buffer.putInt(changedItem.attendant.getInstanceId());
			pt.put(buffer.array());
		} else {
			byte[] bytes = changedItem.attendant.toClientBytes(owner);
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length+1, false);
			buffer.put((byte)1);
			buffer.put(bytes);
			pt.put(buffer.array());
		}
	}
	
	public void visit(AttendantSkillChangedItem changedItem){
		pt.put(ChangedItem.TYPE_ATTENDANT_COMPLEX);
		pt.put(changedItem.id);
		pt.putInt(changedItem.attendantInstanceId);
		if (!changedItem.add) {
			ByteBuffer buffer = ByteBuffer.allocate(5);
			buffer.put((byte) 0);
			buffer.putInt(changedItem.skill.getId());
			pt.put(buffer.array());
		} else {
			byte[] data = changedItem.skill.toClientBytes(owner);
			byte[] bytes = new byte[data.length+2];
			bytes[0] = 1;
			bytes[1] = (byte)changedItem.index;
			System.arraycopy(data, 0, bytes, 2, data.length);
			pt.put(bytes);
		}
	}
	
	public void visit(AttendantEquipChangedItem changedItem){
		pt.put(ChangedItem.TYPE_ATTENDANT_COMPLEX);
		pt.put(changedItem.id);
		pt.putInt(changedItem.attendantInstanceId);
		if(changedItem.item==null){
			byte[] bytes = new byte[2];
			bytes[0] = (byte)changedItem.index;
			bytes[1] = 0;
			pt.put(bytes);
		}else{
			byte[] b = changedItem.item.toClientBytes();
			byte[] bytes = new byte[b.length+2];
			bytes[0] = (byte)changedItem.index;
			bytes[1] = 1;
			System.arraycopy(b, 0, bytes, 2, b.length);
			pt.put(bytes);
		}
	}
	
	public void visit(HorseFoodChange changedItem){
		pt.put(ChangedItem.TYPE_HORSE_COMPLEX);
		pt.put(changedItem.id);
		pt.putInt(changedItem.h.instanceId);
		pt.putInt(changedItem.foodId);
	}
	
	public void visit(PlayerEnemyChangeToken changedItem){
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changedItem.id);
		pt.putInt(changedItem.enemyId);
		pt.put(changedItem.together);
	}
	
	public void visit(EquipCardChangedItem changed){
		CardService service = Server.server.getServiceRegistry().getCardService();
		int index = changed.index;
		int upExp = changed.upExp;
		int type = changed.type;
		CardInfo cardInfo = changed.cardInfo;
//		level					byte			卡片级别
//		exp						int				升级所需经验
//		attDesc					String			属性增强描述
		pt.put(index);
		pt.put(type);
		pt.put(cardInfo.level);
		pt.putInt(upExp);
		pt.putUTF(service.getEnhanceDesc(cardInfo.cardId, cardInfo.level));
	}
	
	public void visit(RemoveSkillChangeItem changed){
		int size = changed.size;
		int[] skillIds = changed.skillIds;
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changed.id);
		pt.put(size);
		for(int i=0;i<size;i++){
			pt.putShort(skillIds[i]);
		}
	}


	public void visit(InvalidItem changed) {
		pt.put(ChangedItem.TYPE_COMPLEX);
		pt.put(changed.id);
		if(changed.item!=null){
			int size=changed.item.length;
			pt.put(size);
			for(GameItem item:changed.item){
//				pt.putInt(item.template.id);
				pt.put(Equipments.getIndex(item.template.equipment.minorType));
				int valTime = item.validTime;
				if(valTime>0){
					valTime = (int)(item.validTime-System.currentTimeMillis()/60000);
					if(valTime<=0){
						valTime = -1;
					}
				}
				pt.putInt(valTime);
			}
		}else{//如果身上没有装备,直接跳过不读取
			pt.put(0);
		}
		if(changed.gridId!=null){
			int size=changed.gridId.length;
			TransactionBagGrid[] grids = new TransactionBagGrid[size];
			System.arraycopy(changed.gridId, 0, grids, 0, size);
			for(int i=0;i<changed.gridId.length;i++){
				TransactionBagGrid grid = changed.gridId[i];
				pt.putInt(grid.id);
				if(grid.getItem()==null){
					size--;
					grids[i] = null;
				}
			}
			pt.put(size);
			for(TransactionBagGrid grid : grids){
				if(grid!=null){
					pt.putInt(grid.id);
					try {
						int valTime = grid.getItem().validTime;
						if(valTime>0){
							valTime = (int)(grid.getItem().validTime-System.currentTimeMillis()/60000);
							if(valTime<=0){
								valTime = -1;
							}
						}
						pt.putInt(valTime);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			pt.put(0);
		}
	}
}
