package peony.service.feast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu{
	int menuId;
	String menuName;
	int majorMaterial;
	Map<Integer,Material> materials = new HashMap<Integer,Material>();
	public Menu(int menuId,String menuName,int majorMaterial){
		this.menuId = menuId;
		this.menuName = menuName;
		this.majorMaterial = majorMaterial;
	}
	
	public void addMaterial(Material material){
		materials.put(material.getId(), material);
	}
	
	public Material getMaterial(int materialId){
		return materials.get(materialId);
	}
	
	public List<Material> getMaterials(){
		List<Material> ms = new ArrayList<Material>();
		for(Material m : materials.values()){
			ms.add(m);
		}
		return ms;
	}
}