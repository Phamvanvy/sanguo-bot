package com.pip.itimes.server.world.refresh;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.pip.itimes.server.stage.PlayerData;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class LockRefreshPool implements Runnable{

    protected Map objects = new HashMap();
    private Queue queue = new Queue();
    protected IRefreshCallback callback;

    public LockRefreshPool() {

    }

    public void setCallback(IRefreshCallback callback){
        this.callback = callback;
    }

    public void addRefreshObject(IRefreshObject obj){
        synchronized(this){
            if(objects.get(new Integer(obj.getId()))==null){
                objects.put(new Integer(obj.getId()), obj);
                if (!obj.isVisible()) {
                    schedule(obj);
                }
            }
        }
    }

    public IRefreshObject[] getVisibleObjects(){
        synchronized(this){
            Iterator ite = objects.values().iterator();
            List l = new ArrayList();
            while(ite.hasNext()){
                IRefreshObject obj = (IRefreshObject)ite.next();
                if(obj.isVisible()){
                    l.add(obj);
                }
            }
            IRefreshObject[] ret = new IRefreshObject[l.size()];
            l.toArray(ret);
            return ret;
        }
    }

    protected void schedule(IRefreshObject object) {
        synchronized (queue) {
            RefreshWrapper wrapper = new RefreshWrapper(object);
            wrapper.nextExecutionTime = System.currentTimeMillis() +
                                        object.getRefreshSecond() * 1000L;
            queue.add(wrapper);
            if (queue.getMin() == wrapper)
                queue.notify();
        }
    }

    private void createObject(RefreshWrapper object) {
        synchronized(this){
            IRefreshObject ro = object.object;
            ro.setVisible(true);
            objectCreated(ro);
        }
    }


    public abstract Lock lock(int id,PlayerData player) throws LockException;
//        synchronized(this){
//            IRefreshObject o = (IRefreshObject) objects.get(new Integer(id));
//            if(o==null)
//                throw new LockException(LockException.INEXISTENCE);
//            if(!o.isVisible()){
//                throw new LockException(LockException.INVISIBLE);
//            }
//            Lock ret = o.lock(player);
//            objectLocked(o);
//            return ret;
//        }


    public void objectLocked(IRefreshObject object){
        if(callback!=null)
            callback.objectLocked(object);
    }

    public void objectReleased(IRefreshObject object){
        if(callback!=null)
            callback.objectReleased(object);
    }

    public void objectCreated(IRefreshObject object){
        if(callback!=null){
            callback.objectCreated(object);
        }
    }
    public void objectDisappeared(IRefreshObject object){
        if(callback!=null){
            callback.objectDisappeared(object);
        }
    }

    public abstract void release(Lock lock,boolean complete) throws LockException;
//        synchronized(this){
//            IRefreshObject o = lock.getObject();
//            o.release(lock);
//            if (o.isEmpty()&&!o.isVisible()) {
//                schedule(o);
//            }
//        }

    public void start(){
        new Thread(this).start();
    }

    public void run() {
        while (true) {
            try {
                RefreshWrapper wrapper;
                boolean taskFired;
                synchronized (queue) {
                    while (queue.isEmpty())
                        queue.wait();
                    if (queue.isEmpty())
                        break;

                    long currentTime, executionTime;
                    wrapper = queue.getMin();

                    currentTime = System.currentTimeMillis();
                    executionTime = wrapper.nextExecutionTime;
                    if (taskFired = (executionTime <= currentTime)) {
                        queue.removeMin();
                        createObject(wrapper);
                    }

                    if (!taskFired)
                        queue.wait(executionTime - currentTime);
                }
            } catch (Throwable e) {

            }
        }
    }


   class Queue {
        private RefreshWrapper[] queue = new RefreshWrapper[128];

        private int size = 0;

        void add(RefreshWrapper task) {
            if (++size == queue.length) {
                RefreshWrapper[] newQueue = new RefreshWrapper[2 * queue.length];
                System.arraycopy(queue, 0, newQueue, 0, size);
                queue = newQueue;
            }

            queue[size] = task;
            fixUp(size);
        }


        RefreshWrapper getMin() {
            return queue[1];
        }


        void removeMin() {
            queue[1] = queue[size];
            queue[size--] = null;
            fixDown(1);
        }

        void rescheduleMin(long newTime) {
            queue[1].nextExecutionTime = newTime;
            fixDown(1);
        }

        boolean isEmpty() {
            return size == 0;
        }

        void clear() {
            for (int i = 1; i <= size; i++)
                queue[i] = null;

            size = 0;
        }


        private void fixUp(int k) {
            while (k > 1) {
                int j = k >> 1;
                if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
                    break;
                RefreshWrapper tmp = queue[j];
                queue[j] = queue[k];
                queue[k] = tmp;
                k = j;
            }
        }

        private void fixDown(int k) {
            int j;
            while ((j = k << 1) <= size) {
                if (j < size &&
                    queue[j].nextExecutionTime > queue[j + 1].nextExecutionTime)
                    j++;
                if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
                    break;
                RefreshWrapper tmp = queue[j];
                queue[j] = queue[k];
                queue[k] = tmp;
                k = j;
            }
        }
    }


    class RefreshWrapper {

        long nextExecutionTime;
        IRefreshObject object;

        public RefreshWrapper(IRefreshObject object) {
            this.object = object;
        }
    }

}


