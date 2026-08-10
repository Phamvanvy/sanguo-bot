package peony.game;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResourceRepository {
	
	private String rootPath;
	private String[] exts;
	private ExtFileFilter filter;
	
	private Map<String,Resource> resources = new HashMap<String,Resource>();
	
	private static ResourceRepository instance;
	
	ResourceRepository(String rootPath,String[] exts) throws IOException{
		if(rootPath==null||exts==null)
			throw new IllegalArgumentException();
		this.rootPath = new File(rootPath).getCanonicalPath();
		this.exts = exts;
		filter = new ExtFileFilter(this.exts);
	}
	
	public static final void init(String rootPath,String exts) throws IOException{
		instance = new ResourceRepository(rootPath,exts.split("/"));
		instance.load();
	}
	
	public static final Resource get(String path){
		return instance.getResource(path);
	}
	
	protected static final void p(){
		instance.print();
	}
	
	public void load() throws IOException{
		scanFileAndLoad(new File(rootPath));
	}
	
	protected void scanFileAndLoad(File f) throws IOException{
		if(!f.isDirectory()){
			loadFile(f);
		}else{
			File[] fs = f.listFiles(filter);
			for(int i=0;i<fs.length;i++){
				scanFileAndLoad(fs[i]);
			}
		}
	}
	
	protected void loadFile(File f) throws IOException{
		String s = normalizer(f);
		Resource r = new Resource(f);
		resources.put(s, r);
	}
	
	protected String normalizer(File f) throws IOException{
		return f.getCanonicalPath().substring(rootPath.length()).replace('\\','/' );
	}
	
	public Resource getResource(String path){
		return resources.get(path);
	}
	
	public void print(){
		Iterator<String> ite = resources.keySet().iterator();
		while(ite.hasNext()){
			System.out.println(ite.next());
		}
	}
	
	public static void main(String[] args) throws IOException{
		ResourceRepository.init("D:/cvshome/projects/SanGo/Sanguo-Editor1.0/data/", "xml/map");
		ResourceRepository.p();
	}
}

class ExtFileFilter implements FileFilter{

	protected String[] exts;
	
	ExtFileFilter(String[] exts){
		String[] ss = new String[exts.length];
		for(int i=0;i<ss.length;i++){
			ss[i] = "." + exts[i];
		}
		this.exts = ss;
	} 

	public boolean accept(File file) {
		if(file.isDirectory())
			return true;
		String filename = file.getName();
		for(int i=0;i<exts.length;i++){
			if(filename.endsWith(exts[i]))
				return true;
		}
		return false;
	}

	
}
