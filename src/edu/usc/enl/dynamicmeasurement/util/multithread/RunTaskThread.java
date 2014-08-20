package edu.usc.enl.dynamicmeasurement.util.multithread;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread.FinishThreadMethod;

import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/21/13
 * Time: 7:41 AM <br/>
 * This is a thread that looks into its queue, picks up a task and run it.
 * If the queue is empty, it will just wait on the queue
 */
public class RunTaskThread extends Thread {
    private final BlockingQueue<Runnable> queue;
    private boolean seenEmptyQueue = false;

    public RunTaskThread(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    public synchronized boolean isSeenEmptyQueue() {
        return seenEmptyQueue;
    }

    @Override
    public void run() {
        while (true) {
            Runnable take = null;

            synchronized (queue) {
                try {
                    if (queue.isEmpty()) {
                        synchronized (this) {
                            seenEmptyQueue = true;
                            this.notify();
                        }
                        queue.wait();
                        continue; //don't run take
                    } else {
                        synchronized (this) {
                            seenEmptyQueue = false;
                        }
                        take = queue.poll();
                        if (take instanceof FinishThreadMethod) {
                            synchronized (this) {
                                seenEmptyQueue = true;
                                this.notify();
                            }
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            take.run();


//            try {
//                synchronized (queue) {
//                    if (queue.isEmpty()) {
//                        finished = true;
//                        this.notify();
//                        queue.wait();
//                    } else {
//                        take = queue.poll();
//                    }
//                }
//                synchronized (this) {
//                    finished = false;
//                }
//
//            } catch (InterruptedException e) {
//                break;
//            }
//            if (take instanceof FinishThreadMethod) {
//                break;
//            }
//                System.out.println("start " + take);
//            take.run();
//                System.out.println("finish " + take);*/
        }
//        synchronized (this) {
//            finished = true;
//            this.notify();
//        }
    }

    public void filledQueue() {
        seenEmptyQueue = false;
    }
}
