package edu.usc.enl.dynamicmeasurement.util.multithread;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread.FinishThreadMethod;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/18/13
 * Time: 3:19 PM <br/>
 * Runs a queue of task on multiple threads. Must be finished at the end by calling finishThreads method.
 * I got better performance comparing to the Java ThreadPoolExecutor (it may be my bad parameter configuration)
 */
public class MultiThread {
    private final RunTaskThread[] threads;
    private final BlockingQueue<Runnable> queue;

    public MultiThread(int threadsNum) {
        threads = new RunTaskThread[threadsNum];
        queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < threadsNum; i++) {
            threads[i] = new RunTaskThread(queue);
        }
        for (RunTaskThread thread : threads) {
            thread.start();
        }
    }

    public void offer(Runnable task) {
        queue.offer(task);
    }

//    public void runTask(ThreadMethod task) {
//        queue.offer(task);
//        synchronized (queue){
//            queue.notify();
//        }
//    }

    public void finishThreads() {
        for (RunTaskThread thread : threads) {
            offer(new FinishThreadMethod());
        }

        runJoin();
    }

    public void run() {
        synchronized (queue) {
            for (RunTaskThread thread : threads) {
                thread.filledQueue();
            }
            queue.notifyAll();
        }
    }

    public void runJoin() {
        run();
        join();
    }

    public void join() {
        try {
//            synchronized (queue) {
//                if (!queue.isEmpty()) {
//                    queue.wait();
//                }
//            }
            for (RunTaskThread thread : threads) {
                synchronized (thread) {
                    if (!thread.isSeenEmptyQueue()) {
                        thread.wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
