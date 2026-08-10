package com.pip.itimes.server.world.game;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class RefreshService implements Runnable{

    private Queue queue = new Queue();

    public RefreshService() {

    }

    public void start(){
        new Thread(this).start();
    }

    public void queue(IRefresh object,int second){
        synchronized (queue) {
            RefreshWrapper wrapper = new RefreshWrapper(object);
            wrapper.nextExecutionTime = System.currentTimeMillis() +
                                       second * 1000L;
            queue.add(wrapper);
            if (queue.getMin() == wrapper)
                queue.notify();
        }
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
                        wrapper.object.refresh();
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
         IRefresh object;

         public RefreshWrapper(IRefresh object) {
             this.object = object;
         }
    }
}
