package peony.service.feast;

public class Npc {
    public int npcId;
    public int x;
    public int y;
    public Npc(int npcId,int x,int y){
    	this.npcId = npcId;
    	this.x = x;
    	this.y = y;
    }
    public int getId(){
    	return npcId;
    }
    
    public int getX(){
    	return x;
    }
    
    public int getY(){
    	return y;
    }
}

