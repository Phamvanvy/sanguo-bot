package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Set;
import java.util.TreeSet;

public class FormulaList {
	
	public Set<Integer> ids = new TreeSet<Integer>();
	
	public FormulaList(){
		
	}
	
	public FormulaList(int[] ids){
		for(int i=0;i<ids.length;i++){
			this.ids.add(ids[i]);
		}
	}
	
	
	public void addFormula(int id){
		ids.add(id);
	}
	
	public boolean contains(int id){
		return ids.contains(id);
	}
	
	public int getFormulaCount(){
		return ids.size();
	}
	
	@Override
	public FormulaList clone(){
		FormulaList ret = new FormulaList();
		ret.ids = new TreeSet<Integer>(ids);
		return ret;
	}
	
	public static FormulaList fromDBBytes(byte[] bytes,Player p){
		FormulaList ret = new FormulaList();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bis);
		try{
			int size = dis.readShort();
			for(int i=0;i<size;i++)
				ret.ids.add(dis.readInt());
		}catch(Exception ex){
			
		}
		return ret;
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeShort(ids.size());
			for(int id:ids){
				dos.writeInt(id);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}
