package pip;


public class Monitor{
	public static void main(String[] args) {
        if(args == null || args.length == 0){
            System.out.println("Usage : monitor pip or cmcc");
        }else if(args[0].startsWith("cmcc")){
            new CmccMonitor().start();
        }else{
            new PipMonitor().start();
        }
	}
}