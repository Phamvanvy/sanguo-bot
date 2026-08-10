package peony.game.file;

public class FileData{
	public byte[] data;
	public int version;
	public String name;
	
	public FileData(String name,int version,byte[] data){
		this.name = name;
		this.version = version;
		this.data = data;
	}
	
}
