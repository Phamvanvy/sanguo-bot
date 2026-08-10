package peony.game;

public class GameFile {
	public int version;
	public byte[] data;
	public String model;
	public GameFile(int version,byte[] data,String model){
		this.version = version;
		this.data = data;
		this.model = model;
	}
}
