package peony.game.attendant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import com.pip.sanguo.data.ProjectData;

import peony.game.Player;
import peony.game.Unit;
import peony.game.changed.AttendantBagChangedItem;

/**
 * 随从栏
 * @author dchen
 */
public class AttendantBag {

	private static final Logger log = Logger.getLogger(Attendant.class);
	public int maxSize;
	public List<Attendant> attendants;
	public Unit owner;
	public static int DEFAULTSIZE = 5; //随从栏默认个数
	public static int MAX_BAG = 20; //随从栏最大数量
	
	public AttendantBag(Unit owner, int maxSize){
		this.owner = owner;
		this.maxSize = maxSize;
		attendants = new ArrayList<Attendant>();
	}
	
	public AttendantBag(Unit owner){
		this(owner, DEFAULTSIZE);
	}
	
	public synchronized void addAttendant(Attendant attendant){
		attendants.add(attendant);
		if(owner.changed!=null){
			owner.changed.addChangedItem(new AttendantBagChangedItem(attendant, 1));
		}
	}
	
	public void addHpAndMp(){
		for(Attendant attendant : attendants){
			attendant.refreshProperties(false);
			attendant.setHp(attendant.maxhp, false);
			attendant.setMp(attendant.maxmp, false);
		}
	}
	
	public AttendantBag clone(){
		AttendantBag bag = new AttendantBag(owner,maxSize);
		for(Attendant attendant : attendants){
			bag.addAttendant(attendant.clone());
		}
		return bag;
	}
	
	public boolean isFull(){
		return attendants.size()>=maxSize;
	}
	
	public synchronized Attendant removeAttendant(int instanceId){
		Iterator<Attendant> it = attendants.iterator();
		while(it.hasNext()){
			Attendant attendant = it.next();
			if(attendant.getInstanceId()==instanceId){
				if(owner!=null)
				    attendant.removeBuff((Player)owner);
				it.remove();
				if(owner.changed!=null){
					owner.changed.addChangedItem(new AttendantBagChangedItem(attendant, 0));
				}
				return attendant;
			}
		}
		return null;
	}
	
	public Attendant getAttendant(int instanceId){
		for(Attendant attendant : attendants){
			if(attendant.getInstanceId()==instanceId){
				return attendant;
			}
		}
		return null;
	}
	
	public Attendant getAttendantById(int id){
		for(Attendant attendant : attendants){
			if(attendant.id==id){
				return attendant;
			}
		}
		return null;
	}
	
	public void throwAttendant(int instanceId, int serial){
		
	}
	
	public synchronized byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);//version
			dos.write(maxSize);
			dos.write(attendants.size());
			for (Attendant attendant : attendants) {
				dos.write(attendant.toDBBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
    }
    
    public static AttendantBag fromDBBytes(byte[] bytes,Player owner,ProjectData data){
    	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try{
			dis.read();//version
			int maxSize = dis.read();
			AttendantBag bag = new AttendantBag(owner,maxSize);
			int size = dis.read();
			for(int i=0;i<size;i++){
				Attendant attendant = Attendant.fromDBBytes(dis, owner, data);
				if(attendant!=null)
					bag.attendants.add(attendant);
			}
			return bag;
		}catch(Exception e){
			e.printStackTrace();
			return new AttendantBag(owner);
		}
    }
    
    public byte[] toClientBytes(Unit owner){
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(maxSize);
			dos.write(attendants.size());
			for (Attendant attendant : attendants) {
				if(attendant==null)
					continue;
				dos.write(attendant.toClientBytes(owner));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
    }
	
}
