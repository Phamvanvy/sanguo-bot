package peony.game.changed;

import org.apache.mina.common.ByteBuffer;

import peony.game.Player;
import peony.net.Packet;

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
}
