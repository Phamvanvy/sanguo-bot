package peony.game;

public class GameObjectRef {
	
	public byte type;
	public int id;
	public int instanceId;
	public int faction;
	public int mapInstanceId;
	public int battleType;
	
//	public GameObjectRef(byte type,int id){
//		this.type = type;
//		this.id = id;
//	}
	
	public GameObjectRef(byte type,int id,int instanceId, int mapInstanceId, int battleType){
		this.type = type;
		this.id = id;
		this.instanceId = instanceId;
		this.battleType = battleType;
		this.mapInstanceId = mapInstanceId;
	}
	
	public GameObjectRef(byte type,int id,int instanceId){
		this.type = type;
		this.id = id;
		this.instanceId = instanceId;
	}
	
	public GameObjectRef(byte type,int id,int instanceId, int faction){
		this.type = type;
		this.id = id;
		this.instanceId = instanceId;
		this.faction = faction;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)
			return false;
		if(obj instanceof GameObjectRef){
			GameObjectRef other = (GameObjectRef)obj;
			return type==other.type&&id==other.id&&instanceId==other.instanceId;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int t = type*31+id;
		t = t*31 + instanceId;
		return t;
	}
}
