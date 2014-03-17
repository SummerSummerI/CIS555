package edu.upenn.cis455.webserver;

import java.util.Hashtable;
import java.util.Set;

/**
 * provides control panel data
 * 
 * @version 2.0 add get status
 * @author cis455
 * 
 */
public class ControlPanel {
    // get state, won't invoke the thread
    public static Hashtable<Long, Thread.State> controlPanelState() {
        Hashtable<Long, Thread.State> threadPoolTable = ThreadPool.getState();
        return threadPoolTable;

    }

    // get status, will invoke the thread
    public static Hashtable<Long, String> controlPanelStatus() {
        Hashtable<Long, String> threadPoolTable = ThreadPool.getStatus();
        return threadPoolTable;
    }

    public static Set<Long> threadIdSet() {
        Hashtable<Long, Thread.State> threadPoolTable = ThreadPool.getState();
        return threadPoolTable.keySet();
    }
}
