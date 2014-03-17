package test.edu.upenn.cis455.hw1;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Blocking queue for incoming socket tasks ref:
 * http://tutorials.jenkov.com/java-concurrency/blocking-queues.html
 * 
 * @author Hangfei
 * 
 */
public class BlockingQueue {
    List<Socket> queue = new LinkedList<Socket>();
    int queueSize = 15;
    private static ServerLog serverLog = ServerLog.getInstance("serverlog");

    public BlockingQueue(int queueSize) throws IllegalArgumentException {
        try {
            this.queueSize = queueSize;
        } catch (IllegalArgumentException e) {
            serverLog.writeFile(e.toString());
            throw new IllegalArgumentException("zero or negative limit");
        }
    }

    public synchronized void enqueue(Socket object) throws InterruptedException {
        while (isFull()) {
            wait();
        }
        if (isEmpty()) {
            notifyAll();
        }
        this.queue.add(object);
    }

    public synchronized Object dequeue() throws InterruptedException {
        while (isEmpty()) {
            wait();
        }
        if (isFull()) {
            notifyAll();
        }
        return this.queue.remove(0);
    }

    private boolean isFull() {
        return this.queue.size() == queueSize;
    }

    private boolean isEmpty() {
        return this.queue.size() == 0;
    }
}
