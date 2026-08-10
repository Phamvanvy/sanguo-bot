package peony.game;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

public class Resource {
	
	protected File f;
	protected byte[] data;
	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	public Resource(File f) throws IOException{
		this.f = f;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(new BufferedInputStream(new FileInputStream(f)), output);
        data = output.toByteArray();
	}
	
	public String getResourcePath(){
		return null;
	}
	
	public String getFilePath(){
		return f.getAbsolutePath();
	}
	
	public byte[] getData(){
		return data;
	}
	
	public Reader getDataAsReader(){
		return new InputStreamReader(new ByteArrayInputStream(data));
	}
	
    public static int copy(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
