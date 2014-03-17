package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Thread Pool for multi-thread server
 * reference:  http://tutorials.jenkov.com/java-concurrency/thread-pools.html
 * @author cis455
 *
 */
public class ThreadPool {
    private BlockingQueue taskQueue = null;
    private static List<ThreadWorker> threads = new ArrayList<ThreadWorker>();
    protected static ServerLog serverLog = ServerLog.getInstance("serverlog");;
    
    public ThreadPool(int threadLimit, int taskLimit) {
        taskQueue = new BlockingQueue(taskLimit);

        // add threads to the threads list
        for (int i = 0; i < threadLimit; i++) {
            threads.add(new ThreadWorker(taskQueue));
        }
        // let the threadworker in the threads list start work
        for (ThreadWorker thread : threads) {
            thread.start();
        }
    }

    // add tasks to the task queue
    public void handleSocket(Socket s) {
        try {
            taskQueue.enqueue(s);
        } catch (InterruptedException e) {
            serverLog.writeFile(e.toString());
        }
    }

    // get status
    public static Hashtable<Long, Thread.State> getState() {
        // threads name
        Hashtable<Long, Thread.State> threadPoolStatus = new Hashtable<Long, Thread.State>();
        for (ThreadWorker element : threads) {
            threadPoolStatus.put(element.getId(), element.getState());
        }
        return threadPoolStatus;
    }

    // get status
    public static Hashtable<Long, String> getStatus() {
        // threads name
        Hashtable<Long, String> threadPoolStatus = new Hashtable<Long, String>();
        for (ThreadWorker element : threads) {
            if (element.getState() != Thread.State.WAITING) {
                threadPoolStatus.put(element.getId(),
                        element.getWorkingStatus());
            } else {
                threadPoolStatus.put(element.getId(), element.getState()
                        .toString());
            }

        }
        return threadPoolStatus;
    }

    /**
     * Close the threads
     */
    public static void closeThreads() {
        for (ThreadWorker element : threads) {
            element.setRunFlag(false);
            element.interrupt();
        }
    }
}
