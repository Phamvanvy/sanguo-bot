package peony.game.file;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import peony.game.Server;
import peony.service.Service;

public class FileService implements Service {
	
	protected Map<String,FileData> files = new HashMap<String,FileData>();

	public void shutdown() {

	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("client_files/files.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
		String s = null;
		while((s=reader.readLine()) != null){
			byte[] data = Server.server.getServiceRegistry().getDataService().data.findFile("client_files/"+s);
			FileData fd = new FileData(s,crc(data),data);
			files.put(fd.name, fd);
		}
	}
	
	public FileData getFileData(String name){
		return files.get(name);
	}
	
	protected int crc(byte[] data){
		byte _crc = 0;
		int _datLength = data.length;
		for(int i=0; i<_datLength; i++){
			_crc ^= data[i];
		}
		int _crcDat = _crc;
		return _crcDat << 24 | _datLength;
	}

}


