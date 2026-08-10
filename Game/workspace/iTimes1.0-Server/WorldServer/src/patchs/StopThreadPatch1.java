package patchs;

public class StopThreadPatch1 implements Runnable {

    public void run() {
        Thread[] ts = new Thread[256];
        int count = Thread.enumerate(ts);
        for(int i=0;i<count;i++){
            if(ts[i].getName().equals("Thread-25")){
                ts[i].stop();
                System.out.println("Thread-25 stop");
            }
        }
    }

}
